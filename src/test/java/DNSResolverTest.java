import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DNSResolverTest {

    @Test
    void resolveWithEmptyQuestion() throws IOException {
        SocketAddress socketAddress = new InetSocketAddress("localhost", 8080);
        DNSResolver dnsResolver = new DNSResolver(socketAddress);
        DNSMessage question = new DNSMessage() {
            @Override
            public List<String> getQuestions() {
                return new ArrayList<>();
            }

            @Override
            public byte[] toByteArray() {
                return new byte[0];
            }
        };

        DNSMessage result = dnsResolver.resolve(question);
        assertEquals(new ArrayList<>(), result.getQuestions());
    }

}
