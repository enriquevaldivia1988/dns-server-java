import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DNSMessageTest {

    @Test
    void checkToByteArray() {
        DNSMessage message = new DNSMessage();
        message.setQuestions(new ArrayList<>());
        message.getQuestions().add("example.com");
        Map<String, byte[]> answers = new HashMap<>();
        answers.put("example.com", new byte[]{127, 0, 0, 1});
        message.addAnswers(answers);

        byte[] byteArray = message.toByteArray();
        DNSMessage decodedMessage = new DNSMessage(byteArray);

        Assertions.assertEquals("example.com", decodedMessage.getQuestions().get(0));
        Assertions.assertArrayEquals(new byte[]{127, 0, 0, 1}, decodedMessage.getAnswers().get("example.com"));
    }

    @Test
    void toByteArrayWithoutAnswersShouldAlsoWork() {
        DNSMessage message = new DNSMessage();
        message.setQuestions(new ArrayList<>());
        message.getQuestions().add("example.com");

        byte[] byteArray = message.toByteArray();
        DNSMessage decodedMessage = new DNSMessage(byteArray);

        Assertions.assertEquals("example.com", decodedMessage.getQuestions().get(0));
        Assertions.assertNull(decodedMessage.getAnswers().get("example.com"));
    }

    @Test
    void toByteArrayWithoutQuestionsShouldAlsoWork() {
        DNSMessage message = new DNSMessage();
        Map<String, byte[]> answers = new HashMap<>();
        answers.put("example.com", new byte[]{127, 0, 0, 1});
        message.addAnswers(answers);

        byte[] byteArray = message.toByteArray();
        DNSMessage decodedMessage = new DNSMessage(byteArray);

        Assertions.assertTrue(decodedMessage.getQuestions().isEmpty());
        Assertions.assertArrayEquals(new byte[]{127, 0, 0, 1}, decodedMessage.getAnswers().get("example.com"));
    }

    @Test
    void testToByteArrayEmptyMessage() {
        DNSMessage dnsMessage = new DNSMessage();
        byte[] result = dnsMessage.toByteArray();
        assertTrue(result.length <= 512, "Result should fit into 512-byte buffer");
        assertArrayEquals(new byte[512], result, "Empty DNSMessage should be zeroed out");
    }

    @Test
    void testToByteArraySingleQuestion() {
        DNSMessage dnsMessage = new DNSMessage();
        dnsMessage.setQuestions(Collections.singletonList("test.com"));
        byte[] result = dnsMessage.toByteArray();
        assertTrue(result.length <= 512, "Result should fit into 512-byte buffer");
        assertArrayEquals(
                Arrays.copyOf(result, dnsMessage.getQuestions().get(0).length() + 12),
                Arrays.copyOfRange(result, 0, dnsMessage.getQuestions().get(0).length() + 12),
                "Trailing bytes should be zeroes"
        );
    }

    @Test
    void testToByteArrayWithAnswer() {
        DNSMessage dnsMessage = new DNSMessage();
        dnsMessage.setQuestions(Collections.singletonList("test.com"));
        Map<String, byte[]> answers = new HashMap<>();
        answers.put("test.com", new byte[]{127, 0, 0, 1});
        dnsMessage.addAnswers(answers);

        byte[] result = dnsMessage.toByteArray();
        assertTrue(result.length <= 512, "Result should fit into 512-byte buffer");
        assertArrayEquals(
                new byte[]{0, 0, 0, 0},
                Arrays.copyOfRange(result, result.length - 4, result.length),
                "Last 4 bytes should represent IP address of the answer"
        );
    }

    @Test
    void testClone_ShouldReturnCopyWithSameFieldsAndReferences() {
        DNSMessage msg = new DNSMessage();
        msg.setQuestions(Arrays.asList("google.com", "test.com"));
        Map<String, byte[]> answers = new HashMap<>();
        answers.put("google.com", new byte[]{127, 0, 0, 1});
        answers.put("test.com", new byte[]{127, 1, 1, 1});
        msg.addAnswers(answers);

        DNSMessage clonedMsg = msg.clone();
        Assertions.assertEquals(msg.getQuestions().get(0), clonedMsg.getQuestions().get(0));
        Assertions.assertEquals(msg.getQuestions().get(1), clonedMsg.getQuestions().get(1));
        Assertions.assertArrayEquals(msg.getAnswers().get("google.com"), clonedMsg.getAnswers().get("google.com"));
        Assertions.assertArrayEquals(msg.getAnswers().get("test.com"), clonedMsg.getAnswers().get("test.com"));
    }

    @Test
    void testClone_ShouldNotRetainOriginalReferences() {
        DNSMessage msg = new DNSMessage();
        msg.setQuestions(Arrays.asList("google.com", "test.com"));
        Map<String, byte[]> answers = new HashMap<>();
        answers.put("google.com", new byte[]{127, 0, 0, 1});
        answers.put("test.com", new byte[]{127, 1, 1, 1});
        msg.addAnswers(answers);

        DNSMessage clonedMsg = msg.clone();
        msg.getAnswers().get("google.com")[0] = 1;
        Assertions.assertNotEquals(msg.getAnswers().get("google.com")[0], clonedMsg.getAnswers().get("google.com")[1]);
        msg.getQuestions().set(0, "changed.com");
        Assertions.assertNotEquals(msg.getQuestions().get(0), clonedMsg.getQuestions().get(1));
    }
}
