package rpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.UUID;

import session.SessionState;

public class RPCClient {
	
	public final static int PROBE_CODE = 0;
	public static final int SESSIONREAD_CODE = 1;

	// A request to simply check if a server is running
	public static boolean probe(String ip, int port) {
		try {
			DatagramSocket RPCSocket = new DatagramSocket();
			RPCSocket.setSoTimeout(1000);
			
			String callID = UUID.randomUUID().toString(); // A unique id for this call
			
			String outString = (callID + "^" + PROBE_CODE + "^0^0");
			byte[] outBuffer = RPCClient.marshal(outString);
			
			try {
				DatagramPacket sendPacket = new DatagramPacket(outBuffer, outBuffer.length, InetAddress.getByName(ip), port);
				RPCSocket.send(sendPacket);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// The response has been sent, now we look for a response
			
			byte[] inBuffer = new byte[4096];
			DatagramPacket receivePacket = new DatagramPacket(inBuffer, inBuffer.length);
			
			try {
				do {
					receivePacket.setLength(inBuffer.length);
					RPCSocket.receive(receivePacket);
				} while (!RPCClient.unmarshal(receivePacket.getData()).split("_")[0].equals(callID));
			} catch (IOException e) {
				receivePacket = null;
				return false;
			} 
						
		} catch (SocketException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public static SessionState sessionRead(String sessionID, int versionNum, String ip, int port) {
		SessionState session = null;
		try {
			DatagramSocket RPCSocket = new DatagramSocket();
			RPCSocket.setSoTimeout(1000);
			String callID = UUID.randomUUID().toString(); // A unique id for this call
			
			// Prep the information to be sent
			String tempSend = callID + "^" + SESSIONREAD_CODE + "^" + sessionID + "^" + versionNum;
			byte[] outBuffer = marshal(tempSend);
			DatagramPacket sendPacket = new DatagramPacket(outBuffer, outBuffer.length, InetAddress.getByName(ip), port);
			
			RPCSocket.send(sendPacket);
			
			// The packet has been sent, now wait for the server to reply
			
			byte[] inBuffer = new byte[4096];
			DatagramPacket receivedPacket = new DatagramPacket(inBuffer, inBuffer.length);
			String response = null;
			try {
				do {
					receivedPacket.setLength(inBuffer.length);
					RPCSocket.receive(receivedPacket);
					response = RPCClient.unmarshal(inBuffer);
					
					// Parse the response into a SessionState
					String[] responseSplit = response.split("\\^");
					session = new SessionState(sessionID, versionNum, null);
					session.setVersionNumber(Integer.parseInt(URLDecoder.decode(responseSplit[1], "UTF-8")));
					session.setMessage(URLDecoder.decode(responseSplit[2], "UTF-8"));
					session.setNewExpirationTime(URLDecoder.decode(responseSplit[3], "UTF-8"));
					
				} while (response == null || !(response.split("\\^")[0].equals(callID)));
			} catch (IOException e) {
				return null;
			} 
			
			return session;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	// Convert received bytes into a string
	public static String unmarshal(byte[] data) {
		try {
			ByteArrayInputStream inStream = new ByteArrayInputStream(data);
			ObjectInput input = new ObjectInputStream(inStream);
			String output = (String) input.readObject();
			return output;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// Convert string into bytes so they can be transmitted
	public static byte[] marshal(String data) {
		try {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(outStream);
			out.writeObject(data);
			byte[] output = outStream.toByteArray();
			return output;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
