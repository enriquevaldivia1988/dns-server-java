import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.*;

public class DNSMessage {
    private short id;
    private short flags;
    private List<String> questions = new ArrayList<>();
    private Map<String, byte[]> answers = new HashMap<>();

    public DNSMessage() {
        // Empty constructor
    }

    public DNSMessage(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        id = buffer.getShort();
        flags = buffer.getShort();
        int qdcount = buffer.getShort();
        int ancount = buffer.getShort();
        buffer.getShort(); // nscount
        buffer.getShort(); // arcount

        for (int i = 0; i < qdcount; i++) {
            questions.add(decodeDomainName(buffer));
            buffer.getShort(); // Type
            buffer.getShort(); // Class
        }

        for (int i = 0; i < ancount; i++) {
            String domain = decodeDomainName(buffer);
            buffer.getShort(); // Type = A
            buffer.getShort(); // Class = IN
            buffer.getInt();   // TTL
            buffer.getShort(); // Length
            byte[] ip = new byte[4];
            buffer.get(ip); // Data
            answers.put(domain, ip);
        }
    }

    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(512);
        buffer.putShort(id);
        buffer.putShort(flags);
        buffer.putShort((short) questions.size());
        buffer.putShort((short) answers.size());
        buffer.putShort((short) 0); // nscount
        buffer.putShort((short) 0); // arcount

        for (String domain : questions) {
            buffer.put(encodeDomainName(domain));
            buffer.putShort((short) 1); // Type = A
            buffer.putShort((short) 1); // Class = IN
        }

        for (String domain : answers.keySet()) {
            buffer.put(encodeDomainName(domain));
            buffer.putShort((short) 1);  // Type = A
            buffer.putShort((short) 1);  // Class = IN
            buffer.putInt(60);          // TTL
            buffer.putShort((short) 4);  // Length
            buffer.put(answers.get(domain)); // Data
        }
        return buffer.array();
    }

    private byte[] encodeDomainName(String domain) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (String label : domain.split("\\.")) {
            out.write(label.length());
            out.writeBytes(label.getBytes());
        }
        out.write(0); // Terminating null byte
        return out.toByteArray();
    }

    private String decodeDomainName(ByteBuffer buffer) {
        byte labelLength;
        StringJoiner labels = new StringJoiner(".");
        boolean compressed = false;
        int position = 0;
        while ((labelLength = buffer.get()) != 0) {
            if ((labelLength & 0xC0) == 0xC0) {
                compressed = true;
                int offset = ((labelLength & 0x3F) << 8) | (buffer.get() & 0xFF);
                position = buffer.position();
                buffer.position(offset);
            } else {
                byte[] label = new byte[labelLength];
                buffer.get(label);
                labels.add(new String(label));
            }
        }
        if (compressed) {
            buffer.position(position);
        }
        return labels.toString();
    }

    public DNSMessage clone() {
        DNSMessage clone = new DNSMessage();
        clone.id = id;
        clone.flags = flags;
        clone.questions = new ArrayList<>(questions);
        clone.answers = new HashMap<>(answers);
        return clone;
    }

    public List<String> getQuestions() {
        return questions;
    }

    public void setQuestions(List<String> questions) {
        this.questions = questions;
    }

    public Map<String, byte[]> getAnswers() {
        return answers;
    }

    public void addAnswers(Map<String, byte[]> answers) {
        this.answers.putAll(answers);
    }

    public void setResponseFlags() {
        char[] requestFlags = String.format("%16s", Integer.toBinaryString(flags))
                .replace(' ', '0')
                .toCharArray();
        requestFlags[0] = '1';  // QR
        requestFlags[13] = '1'; // RCODE
        flags = (short) Integer.parseInt(new String(requestFlags), 2);
    }
}
