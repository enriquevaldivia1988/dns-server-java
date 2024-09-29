import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Main {
    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");

        // Uncomment this block to pass the first stage
        try (DatagramSocket serverSocket = new DatagramSocket(2053)) {
            while (true) {
                final byte[] buf = new byte[512];
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);
                System.out.println("Datos recibidos");

                // Construir encabezado DNS
                short ID = 1234;
                boolean qr = true;
                byte opcode = 0;
                boolean aa = false;
                boolean tc = false;
                boolean rd = false;
                boolean ra = false;
                byte z = 0;
                byte rcode = 0;
                short qdcount = 0;
                short ancount = 0;
                short nscount = 0;
                short arcount = 0;

                ByteBuffer buffer = ByteBuffer.allocate(512).order(ByteOrder.BIG_ENDIAN);
                buffer.putShort(ID);
                buffer.put((byte)((qr ? 1 : 0) << 7 | (opcode & 0x0F) << 3 | (aa ? 1 : 0) << 2 | (tc ? 1 : 0) << 1 | (rd ? 1 : 0)));
                buffer.put((byte)((ra ? 1 : 0) << 7 | (z & 0x07) << 4 | (rcode & 0x0F)));
                buffer.putShort(qdcount);
                buffer.putShort(ancount);
                buffer.putShort(nscount);
                buffer.putShort(arcount);

                final byte[] bufResponse = new byte[buffer.position()];
                System.arraycopy(buffer.array(), 0, bufResponse, 0, bufResponse.length);

                final DatagramPacket packetResponse = new DatagramPacket(bufResponse, bufResponse.length, packet.getSocketAddress());
                serverSocket.send(packetResponse);
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
