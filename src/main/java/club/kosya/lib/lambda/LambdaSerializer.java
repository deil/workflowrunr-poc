package club.kosya.lib.lambda;

import lombok.RequiredArgsConstructor;

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
                    serializedLambda.getInstantiatedMethodType(),
                    serializedLambda.getCapturedArgCount()
            );

            oos.writeObject(data);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize lambda", e);
        }
    }

    record SerializedLambdaData(String capturingClass, String functionalInterfaceClass,
                                String functionalInterfaceMethodName, String functionalInterfaceMethodSignature,
                                String implClass, String implMethodName, String implMethodSignature, int implMethodKind,
                                String instantiatedMethodType, int capturedArgsCount) implements Serializable {
    }
}
