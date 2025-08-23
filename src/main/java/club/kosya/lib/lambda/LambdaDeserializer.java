package club.kosya.lib.lambda;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.util.Arrays;
import java.util.List;

public class LambdaDeserializer {
    public static <T> T deserialize(byte[] bytes, Object... capturedArgs) {
        return deserialize(bytes, Arrays.asList(capturedArgs));
    }

    public static <T> T deserialize(byte[] bytes, List<Object> capturedArgs) {
        return fromSerializedLambda(bytesToSerializedLambda(bytes), capturedArgs.toArray());
    }

    public static SerializedLambda bytesToSerializedLambda(byte[] data) {
        try (var bais = new ByteArrayInputStream(data);
             var ois = new ObjectInputStream(bais)) {
            var lambdaData = (LambdaSerializer.SerializedLambdaData) ois.readObject();
            return new SerializedLambda(
                    Class.forName(lambdaData.capturingClass().replace('/', '.')),
                    lambdaData.functionalInterfaceClass(),
                    lambdaData.functionalInterfaceMethodName(),
                    lambdaData.functionalInterfaceMethodSignature(),
                    lambdaData.implMethodKind(),
                    lambdaData.implClass(),
                    lambdaData.implMethodName(),
                    lambdaData.implMethodSignature(),
                    lambdaData.instantiatedMethodType(),
                    new Object[lambdaData.capturedArgsCount()]
            );
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to deserialize lambda", e);
        }
    }

    public static <T> T fromSerializedLambda(SerializedLambda serializedLambda, Object[] capturedArgs) {
        try {
            var capturingClass =
                    Class.forName(serializedLambda.getCapturingClass().replace('/', '.'));

            var lookup =
                    MethodHandles.privateLookupIn(capturingClass, MethodHandles.lookup());
            var functionalInterface = Class.forName(serializedLambda.getFunctionalInterfaceClass().replace("/", "."));

            var capturedArgTypes = new Class[capturedArgs.length];
            for (var i = 0; i < capturedArgs.length; i++) {
                capturedArgTypes[i] = capturedArgs[i].getClass();
            }

            var factoryType = MethodType.methodType(functionalInterface, capturedArgTypes);

            var target = lookup.findStatic(capturingClass,
                    serializedLambda.getImplMethodName(),
                    MethodType.fromMethodDescriptorString(serializedLambda.getImplMethodSignature(), capturingClass.getClassLoader()));

            var interfaceMethodType = MethodType.fromMethodDescriptorString(
                    serializedLambda.getFunctionalInterfaceMethodSignature(),
                    capturingClass.getClassLoader());
            var instantiatedMethodType = MethodType.fromMethodDescriptorString(
                    serializedLambda.getInstantiatedMethodType(),
                    capturingClass.getClassLoader());

            var callSite = LambdaMetafactory.metafactory(
                    lookup,
                    serializedLambda.getFunctionalInterfaceMethodName(),
                    factoryType,
                    interfaceMethodType,
                    target,
                    instantiatedMethodType
            );

            return (T) callSite.getTarget().invokeWithArguments(capturedArgs);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to deserialize lambda", e);
        }
    }
}
