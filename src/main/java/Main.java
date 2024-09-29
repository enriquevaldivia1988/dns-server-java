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

                // Analizar el encabezado DNS
                ByteBuffer receivedBuffer = ByteBuffer.wrap(packet.getData()).order(ByteOrder.BIG_ENDIAN);
                short ID = receivedBuffer.getShort();
                byte flags1 = receivedBuffer.get();
                byte flags2 = receivedBuffer.get();
                short qdcount = receivedBuffer.getShort();
                short ancount = receivedBuffer.getShort();
                short nscount = receivedBuffer.getShort();
                short arcount = receivedBuffer.getShort();

                byte qr = 1;
                byte opcode = (byte) ((flags1 >> 3) & 0x0F);
                byte rd = (byte) ((flags1 >> 0) & 0x01);
                byte rcode = (opcode == 0) ? (byte) 0 : (byte) 4;

                byte responseFlags1 = (byte) ((qr << 7) | (opcode << 3) | (rd << 0));
                byte responseFlags2 = (byte) ((0 << 7) | (0 << 4) | (rcode & 0x0F));

                // Construir encabezado DNS de respuesta
                ByteBuffer buffer = ByteBuffer.allocate(512).order(ByteOrder.BIG_ENDIAN);
                buffer.putShort(ID);
                buffer.put(responseFlags1);
                buffer.put(responseFlags2);
                buffer.putShort(qdcount);
                buffer.putShort((short) 1);  // Una respuesta en ANCOUNT
                buffer.putShort((short) 0);  // NSCOUNT
                buffer.putShort((short) 0);  // ARCOUNT

                // Añadir la sección de pregunta
                String domainName = "\u000ccodecrafters\u0002io";
                buffer.put(domainName.getBytes());
                buffer.put((byte) 0);  // Byte nulo para terminar el nombre de dominio
                buffer.putShort((short) 1);  // Tipo A de registro
                buffer.putShort((short) 1);  // Clase IN

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
