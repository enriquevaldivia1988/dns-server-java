import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Logger;

public class DNSServer {
    Logger logger = Logger.getLogger(DNSServer.class.getName());

    private final int port;
    private final DNSResolver resolver;
    private boolean running;

    public DNSServer(int port, DNSResolver resolver) {
        this.port = port;
        this.resolver = resolver;
        this.running = true;

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void start() {
        try (DatagramSocket serverSocket = new DatagramSocket(port)) {
            while (running) {
                byte[] buf = new byte[512];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);
                logger.info("Received data");

                DNSMessage question = new DNSMessage(buf);
                DNSMessage response = resolver.resolve(question);
                byte[] responseData = response.toByteArray();

                DatagramPacket sendPacket = new DatagramPacket(
                        responseData, responseData.length, packet.getSocketAddress());
                serverSocket.send(sendPacket);
            }
        } catch (IOException e) {
            logger.info("IOException: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
    }
}
