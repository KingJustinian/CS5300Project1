package rpc;

import java.net.DatagramSocket;
import java.net.SocketException;

import session.MainServlet;

public class RPCServer implements Runnable {
	private DatagramSocket RPCSocket;
	private int serverPort;
	private MainServlet SessionMain;
	
	public RPCServer(MainServlet sMain) {
		try {
			RPCSocket = new DatagramSocket();
			serverPort = RPCSocket.getLocalPort();
			SessionMain = sMain;
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	
	
	public int getServerPort() {
		return serverPort;
	}

}
