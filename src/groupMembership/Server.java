package groupMembership;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Server {
	public InetAddress ip;
	public Integer port;
	
	public Server(InetAddress sIP, Integer sPort) {
		ip = sIP;
		port = sPort;
	}
	
	public Server(String sIP, String sPort) {
		try {
			ip = InetAddress.getByName(sIP);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		port = new Integer(sPort);
	}
	
	public String toString() {
		return ip.getHostAddress() + ":" + port;
	}
	
	public boolean equals(Server s2) {
		return ((ip.equals(s2.ip)) && (port == s2.port));
	}

}
