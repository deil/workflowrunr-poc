package club.kosya.lib.lambda;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static club.kosya.lib.lambda.LambdaDeserializer.deserialize;
import static club.kosya.lib.lambda.LambdaSerializer.serialize;
import static org.junit.jupiter.api.Assertions.*;

class LambdaSerializationIntegrationTest {

    @Test
    void testSimpleLambdaSerializationAndDeserialization() {
        // Arrange
        AtomicBoolean executed = new AtomicBoolean(false);
        WorkflowLambda originalLambda = () -> executed.set(true);

        // Act
        var result = serialize(originalLambda);
        WorkflowLambda deserializedLambda = deserialize(result.definition(), executed);

        // Assert
        assertNotNull(result.definition());
        assertTrue(result.definition().length > 0);
        assertNotNull(deserializedLambda);

        assertFalse(executed.get());
        deserializedLambda.run();
        assertTrue(executed.get());
    }

    @Test
    void testLambdaWithCapturedVariableSerializationAndDeserialization() {
        // Arrange
        AtomicReference<String> result = new AtomicReference<>();
        String capturedValue = "test-value-123";
        WorkflowLambda originalLambda = () -> result.set(capturedValue);

        // Act
        var serializationResult = serialize(originalLambda);
        WorkflowLambda deserializedLambda = deserialize(serializationResult.definition(), result, capturedValue);

        // Assert
        assertNotNull(serializationResult.definition());
        assertNotNull(deserializedLambda);

        assertNull(result.get());
        deserializedLambda.run();
        assertEquals(capturedValue, result.get());
    }

    @Test
    void testLambdaWithMultipleCapturedVariables() {
        // Arrange
        AtomicReference<String> result = new AtomicReference<>();
        String value1 = "hello";
        String value2 = "world";
        WorkflowLambda originalLambda = () -> result.set(value1 + " " + value2);

        // Act
        var serializationResult = serialize(originalLambda);
        WorkflowLambda deserializedLambda = deserialize(serializationResult.definition(), result, value1, value2);
        deserializedLambda.run();

        // Assert
        assertEquals("hello world", result.get());
    }

    @Test
    void testSerializationIsRepeatable() {
        // Arrange
        String capturedValue = "repeatable-test";
        WorkflowLambda lambda = () -> System.out.println(capturedValue);

        // Act
        var result1 = serialize(lambda);
        var result2 = serialize(lambda);

        // Assert
        assertArrayEquals(result1.definition(), result2.definition());
    }

    @Test
    void testDeserializationWithWrongCapturedArgs() {
        // Arrange
        String originalValue = "original";
        WorkflowLambda lambda = () -> System.out.println(originalValue);
        var serializationResult = serialize(lambda);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            deserialize(serializationResult.definition(), "wrong-number-of-args", "extra-arg");
        });
    }

    @Test
    void testLambdaWithoutCapturedVariables() {
        // Arrange
        WorkflowLambda lambda = () -> System.out.println("No captured variables");

        // Act
        var serializationResult = serialize(lambda);
        WorkflowLambda deserializedLambda = deserialize(serializationResult.definition());

        // Assert
        assertNotNull(deserializedLambda);
        assertDoesNotThrow(() -> deserializedLambda.run());
    }
}