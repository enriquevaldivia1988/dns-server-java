import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

public class DNSServer {
    private final int port;
    private final DNSResolver resolver;

    public DNSServer(int port, DNSResolver resolver) {
        this.port = port;
        this.resolver = resolver;
    }

    public void start() {
        try (DatagramSocket serverSocket = new DatagramSocket(port)) {
            while (true) {
                byte[] buf = new byte[512];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);
                System.out.println("Received data");

                DNSMessage question = new DNSMessage(buf);
                DNSMessage response = resolver.resolve(question);
                byte[] responseData = response.toByteArray();

                DatagramPacket sendPacket = new DatagramPacket(
                        responseData, responseData.length, packet.getSocketAddress());
                serverSocket.send(sendPacket);
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
