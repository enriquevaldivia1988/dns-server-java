import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.List;

public class DNSResolver {
    private final SocketAddress resolverAddress;

    public DNSResolver(SocketAddress resolverAddress) {
        this.resolverAddress = resolverAddress;
    }

    public DNSMessage resolve(DNSMessage question) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            for (String qd : question.getQuestions()) {
                DNSMessage forward = question.clone();
                forward.setQuestions(List.of(qd));
                byte[] buffer = forward.toByteArray();
                DatagramPacket forwardPacket = new DatagramPacket(buffer, buffer.length, resolverAddress);
                socket.send(forwardPacket);

                buffer = new byte[512];
                forwardPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(forwardPacket);
                DNSMessage forwardResponse = new DNSMessage(buffer);

                question.addAnswers(forwardResponse.getAnswers());
            }
            question.setResponseFlags();
            return question;
        }
    }
}
