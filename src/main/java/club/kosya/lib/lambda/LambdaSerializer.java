package club.kosya.lib.lambda;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;

import static org.springframework.util.ReflectionUtils.makeAccessible;

public class LambdaSerializer {
    public static byte[] serialize(WorkflowLambda lambda) {
        return serializedLambdaToBytes(toSerializedLambda(lambda));
    }

    public static <T> SerializedLambda toSerializedLambda(T value) {
        if (!value.getClass().isSynthetic()) {
            throw new IllegalArgumentException("Please provide a lambda expression (e.g. BackgroundJob.enqueue(() -> myService.doWork()) instead of an actual implementation.");
        }

        try {
            var writeReplaceMethod = value.getClass().getDeclaredMethod("writeReplace");
            makeAccessible(writeReplaceMethod);
            return (SerializedLambda) writeReplaceMethod.invoke(value);
        } catch (Exception shouldNotHappen) {
            throw new RuntimeException(shouldNotHappen);
        }
    }

    public static byte[] serializedLambdaToBytes(SerializedLambda serializedLambda) {
        try (var baos = new ByteArrayOutputStream();
             var oos = new ObjectOutputStream(baos)) {
            var data = new SerializedLambdaData(
                    serializedLambda.getCapturingClass(),
                    serializedLambda.getFunctionalInterfaceClass(),
                    serializedLambda.getFunctionalInterfaceMethodName(),
                    serializedLambda.getFunctionalInterfaceMethodSignature(),
                    serializedLambda.getImplClass(),
                    serializedLambda.getImplMethodName(),
                    serializedLambda.getImplMethodSignature(),
                    serializedLambda.getImplMethodKind(),
                    serializedLambda.getInstantiatedMethodType()
            );

            oos.writeObject(data);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize lambda", e);
        }
    }

    static class SerializedLambdaData implements Serializable {
        final String capturingClass;
        final String functionalInterfaceClass;
        final String functionalInterfaceMethodName;
        final String functionalInterfaceMethodSignature;
        final String implClass;
        final String implMethodName;
        final String implMethodSignature;
        final int implMethodKind;
        final String instantiatedMethodType;

        public SerializedLambdaData(String capturingClass, String functionalInterfaceClass,
                                    String functionalInterfaceMethodName, String functionalInterfaceMethodSignature,
                                    String implClass, String implMethodName, String implMethodSignature,
                                    int implMethodKind, String instantiatedMethodType) {
            this.capturingClass = capturingClass;
            this.functionalInterfaceClass = functionalInterfaceClass;
            this.functionalInterfaceMethodName = functionalInterfaceMethodName;
            this.functionalInterfaceMethodSignature = functionalInterfaceMethodSignature;
            this.implClass = implClass;
            this.implMethodName = implMethodName;
            this.implMethodSignature = implMethodSignature;
            this.implMethodKind = implMethodKind;
            this.instantiatedMethodType = instantiatedMethodType;
        }
    }
}
