package club.kosya.lib.lambda2;

import aj.org.objectweb.asm.ClassReader;
import aj.org.objectweb.asm.ClassVisitor;
import aj.org.objectweb.asm.Handle;
import aj.org.objectweb.asm.MethodVisitor;
import aj.org.objectweb.asm.Opcodes;
import club.kosya.lib.lambda.WorkflowLambda;

import java.io.IOException;
import java.io.InputStream;

import static club.kosya.lib.lambda.LambdaSerializer.toSerializedLambda;

public class Experiments {
    private void enqueue(WorkflowLambda lambda) {
        var serializedLambda = toSerializedLambda(lambda);
        var isLambda = (serializedLambda.getImplMethodName().startsWith("lambda$") || serializedLambda.getImplMethodName().contains("$lambda-") || serializedLambda.getImplMethodName().contains("$lambda$"));
        if (isLambda) {
            try (var classContainingLambdaInputStream = getClassContainingLambdaAsInputStream(lambda)) {
                parse(classContainingLambdaInputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void parse(InputStream inputStream) throws IOException {
        var parser = new ClassReader(inputStream);
        parser.accept(new MyClassVisitor(Opcodes.ASM7), ClassReader.SKIP_FRAMES);
    }

    protected InputStream getClassContainingLambdaAsInputStream(Object lambda) {
        return getJavaClassContainingLambdaAsInputStream(lambda);
    }

    public static String getClassLocationOfLambda(Object lambda) {
        var name = lambda.getClass().getName();
        return "/" + toFQResource(name.substring(0, name.indexOf("$$"))) + ".class";
    }

    public static String toFQResource(String byteCodeName) {
        return byteCodeName.replace(".", "/");
    }

    public static InputStream getJavaClassContainingLambdaAsInputStream(Object lambda) {
        var location = getClassLocationOfLambda(lambda);
        return lambda.getClass().getResourceAsStream(location);
    }
}

class MyClassVisitor extends ClassVisitor {
    public MyClassVisitor(int api) {
        super(api);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return new MethodVisitor(Opcodes.ASM7) {
            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            }

            @Override
            public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            }

            @Override
            public void visitInsn(int opcode) {
            }

            @Override
            public void visitVarInsn(int opcode, int variable) {
            }

            @Override
            public void visitIntInsn(int opcode, int operand) {
            }

            @Override
            public void visitLdcInsn(Object value) {
            }

            @Override
            public void visitTypeInsn(int opcode, String type) {
                return;
            }
        };
    }
}
