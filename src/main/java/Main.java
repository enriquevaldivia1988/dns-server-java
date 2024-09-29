import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Los registros de tu programa aparecerán aquí.");

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

                // Extraer la sección de preguntas
                List<byte[]> questionSections = new ArrayList<>();
                List<String> domainNames = new ArrayList<>();
                for (int i = 0; i < qdcount; i++) {
                    int position = receivedBuffer.position();
                    StringBuilder domainNameBuilder = new StringBuilder();
                    while (true) {
                        byte length = receivedBuffer.get();
                        if (length == 0) break;
                        if ((length & 0xC0) == 0xC0) { // Comprensión
                            int offset = ((length & 0x3F) << 8) | (receivedBuffer.get() & 0xFF);
                            ByteBuffer offsetBuffer = ByteBuffer.wrap(packet.getData(), offset, packet.getData().length - offset).order(ByteOrder.BIG_ENDIAN);
                            while (true) {
                                byte labelLength = offsetBuffer.get();
                                if (labelLength == 0) break;
                                byte[] label = new byte[labelLength];
                                offsetBuffer.get(label);
                                if (domainNameBuilder.length() > 0) {
                                    domainNameBuilder.append('.');
                                }
                                domainNameBuilder.append(new String(label));
                            }
                            break;
                        } else {
                            byte[] label = new byte[length];
                            receivedBuffer.get(label);
                            if (domainNameBuilder.length() > 0) {
                                domainNameBuilder.append('.');
                            }
                            domainNameBuilder.append(new String(label));
                        }
                    }
                    domainNames.add(domainNameBuilder.toString());

                    receivedBuffer.getShort(); // Tipo (debe ser 1)
                    receivedBuffer.getShort(); // Clase (debe ser 1)

                    int endPosition = receivedBuffer.position();
                    byte[] questionSection = new byte[endPosition - position];
                    System.arraycopy(packet.getData(), position, questionSection, 0, questionSection.length);
                    questionSections.add(questionSection);
                }

                // Construir encabezado DNS de respuesta
                ByteBuffer buffer = ByteBuffer.allocate(512).order(ByteOrder.BIG_ENDIAN);
                buffer.putShort(ID);
                buffer.put(responseFlags1);
                buffer.put(responseFlags2);
                buffer.putShort(qdcount);
                buffer.putShort(qdcount);  // Responder con una respuesta por cada pregunta
                buffer.putShort((short) 0);  // NSCOUNT
                buffer.putShort((short) 0);  // ARCOUNT

                // Añadir la sección de preguntas
                for (byte[] questionSection : questionSections) {
                    buffer.put(questionSection);
                }

                // Añadir la sección de respuestas
                for (String domainName : domainNames) {
                    String[] labels = domainName.split("\\.");
                    for (String label : labels) {
                        buffer.put((byte) label.length());
                        buffer.put(label.getBytes());
                    }
                    buffer.put((byte) 0);  // Byte nulo para terminar el nombre de dominio
                    buffer.putShort((short) 1);  // Tipo de registro (1 para A)
                    buffer.putShort((short) 1);  // Clase de registro (1 para IN)
                    buffer.putInt(60);  // TTL de 60 segundos
                    buffer.putShort((short) 4);  // Longitud del campo RDATA es 4 bytes
                    buffer.put(new byte[]{(byte) 8, (byte) 8, (byte) 8, (byte) 8});  // Dirección IP 8.8.8.8
                }

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
