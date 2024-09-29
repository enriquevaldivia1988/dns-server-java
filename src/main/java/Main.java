import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Main {
    public static void main(String[] args) {
        String resolverIP = args[1].split(":")[0];
        int resolverPort = Integer.parseInt(args[1].split(":")[1]);
        SocketAddress resolver = new InetSocketAddress(resolverIP, resolverPort);
        DNSResolver dnsResolver = new DNSResolver(resolver);
        DNSServer dnsServer = new DNSServer(2053, dnsResolver);
        dnsServer.start();
    }
}
