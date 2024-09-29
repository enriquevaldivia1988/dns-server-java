import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DNSHeader {
    private static final int HEADER_LENGTH = 12;

    private int id;
    private boolean qr;
    private int opcode;
    private boolean aa;
    private boolean tc;
    private boolean rd;
    private boolean ra;
    private int z;
    private int rcode;
    private int qdcount;
    private int ancount;
    private int nscount;
    private int arcount;

    public DNSHeader() {
        this.id = 1234;
        this.qr = true;
        this.opcode = 0;
        this.aa = false;
        this.tc = false;
        this.rd = false;
        this.ra = false;
        this.z = 0;
        this.rcode = 0;
        this.qdcount = 0;
        this.ancount = 0;
        this.nscount = 0;
        this.arcount = 0;
    }

    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_LENGTH);
        buffer.order(ByteOrder.BIG_ENDIAN);

        buffer.putShort((short) id);
        buffer.put((byte) ((qr ? 1 : 0) << 7 | opcode << 3 | (aa ? 1 : 0) << 2 | (tc ? 1 : 0) << 1 | (rd ? 1 : 0)));
        buffer.put((byte) ((ra ? 1 : 0) << 7 | z << 4 | rcode));
        buffer.putShort((short) qdcount);
        buffer.putShort((short) ancount);
        buffer.putShort((short) nscount);
        buffer.putShort((short) arcount);

        return buffer.array();
    }

    public static void main(String[] args) {
        DNSHeader header = new DNSHeader();
        byte[] headerBytes = header.toByteArray();

        System.out.println("DNS Header Bytes:");
        for (byte b : headerBytes) {
            System.out.printf("%02x ", b);
        }
    }
}
