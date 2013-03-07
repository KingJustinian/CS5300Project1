package rpc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URLEncoder;

import session.MainServlet;
import session.SessionState;

public class RPCServer implements Runnable {
	private DatagramSocket RPCSocket;
	private int serverPort;
	
	private boolean running = true;
	
	public RPCServer(MainServlet sMain) {
		try {
			RPCSocket = new DatagramSocket();
			serverPort = RPCSocket.getLocalPort();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		
		while(running) {
			byte[] inBuf = new byte[4096];
			
			// Packet to receive information
			DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
			
			try {
				RPCSocket.receive(recvPkt);
				InetAddress returnAddr = recvPkt.getAddress();
				int returnPort = recvPkt.getPort();
				
				// Prepare the data before computing the response
				byte[] recData = recvPkt.getData();
				String[] parseData = RPCClient.unmarshal(recData).split("\\^");
				String callID = parseData[0];
				int operationCode = Integer.parseInt(parseData[1]);
				String sessionID = parseData[2];
				String versionNum = parseData[3];
				String response = null;
				SessionState retrievedSession;
				
				// Compute the response depending on the requested operation
				switch (operationCode) {
					case RPCClient.PROBE_CODE:
						response = callID;
						break;
						
					case RPCClient.SESSIONREAD_CODE:
						retrievedSession = MainServlet.sessionData.get(sessionID);
						// If the retrieved session is not null and is the correct version number, formulate the response
						if (!(retrievedSession == null || retrievedSession.getVersionNumber() != Integer.parseInt(versionNum))) {
							response = callID;
							response += "^" + URLEncoder.encode(Integer.toString(retrievedSession.getVersionNumber()), "UTF-8");
							response += "^" + URLEncoder.encode(retrievedSession.getMessage(), "UTF-8");
							response += "^" + URLEncoder.encode(retrievedSession.getExpirationTime().toString(), "UTF-8");
						}
						break;
				}
				
				
				// Send the response
				byte[] outBuffer = new byte[4096];
				outBuffer = RPCClient.marshal(response);
				DatagramPacket sendPacket = new DatagramPacket(outBuffer, outBuffer.length, returnAddr, returnPort);
				RPCSocket.send(sendPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	
	
	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public int getServerPort() {
		return serverPort;
	}

}
