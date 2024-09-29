import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Main {
    public static void main(String[] args) {
        // Puedes utilizar declaraciones print para depuración; se verán al ejecutar pruebas.
        System.out.println("Los registros de tu programa aparecerán aquí.");

        // Descomenta esta parte para pasar a la primera etapa
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
                short qdcount = 1;
                short ancount = 1;
                short nscount = 0;
                short arcount = 0;

                ByteBuffer buffer = ByteBuffer.allocate(512).order(ByteOrder.BIG_ENDIAN);
                buffer.putShort(ID);
                buffer.put((byte) ((qr ? 1 : 0) << 7 | (opcode & 0x0F) << 3 | (aa ? 1 : 0) << 2 | (tc ? 1 : 0) << 1 | (rd ? 1 : 0)));
                buffer.put((byte) ((ra ? 1 : 0) << 7 | (z & 0x07) << 4 | (rcode & 0x0F)));
                buffer.putShort(qdcount);
                buffer.putShort(ancount);
                buffer.putShort(nscount);
                buffer.putShort(arcount);

                // Añadir la sección de pregunta
                String domainName = "\u000ccodecrafters\u0002io";
                buffer.put(domainName.getBytes());
                buffer.put((byte) 0);  // Null byte to terminate the domain name
                buffer.putShort((short) 1);  // Type A record
                buffer.putShort((short) 1);  // Class IN

                // Añadir la sección de respuesta
                buffer.put(domainName.getBytes());
                buffer.put((byte) 0);  // Byte nulo para terminar el nombre de dominio
                buffer.putShort((short) 1);  // Tipo A de registro
                buffer.putShort((short) 1);  // Clase IN
                buffer.putInt(60);  // TTL de 60 segundos
                buffer.putShort((short) 4);  // Longitud del campo RDATA es 4 bytes
                buffer.put(new byte[]{(byte) 8, (byte) 8, (byte) 8, (byte) 8});  // IP address 8.8.8.8 codificada en formato de 4-byte

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
