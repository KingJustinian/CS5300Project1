package session;

import groupMembership.Server;

import java.sql.Timestamp;
import java.util.Date;

public class SessionState {
	private String sessionID;
	private int versionNumber;
	private String message;
	private Timestamp expirationTime;
	
	public SessionState(int sNum, String sIP, int sPort, int v, String m) {
		String tempID = Integer.toString(sNum) + "_" + sIP + "_" + Integer.toString(sPort);
		setSessionID(tempID);
		setVersionNumber(v);
		setMessage(m);
		Date date = new Date();
		Timestamp curTime = new Timestamp(date.getTime());
		expirationTime = new Timestamp(curTime.getTime() + MainServlet.SESSION_TIMEOUT_SECS*1000); 	
	}
	
	public SessionState(int sNum, Server server, int v, String m) {
		String tempID = Integer.toString(sNum) + "_" + server.ip.getHostAddress() + "_" + Integer.toString(server.port);
		setSessionID(tempID);
		setVersionNumber(v);
		setMessage(m);
		Date date = new Date();
		Timestamp curTime = new Timestamp(date.getTime());
		expirationTime = new Timestamp(curTime.getTime() + MainServlet.SESSION_TIMEOUT_SECS*1000); 	
	}
	
	public SessionState(String sID, int v, String m) {
		setSessionID(sID);
		setVersionNumber(v);
		setMessage(m);
		Date date = new Date();
		Timestamp curTime = new Timestamp(date.getTime());
		expirationTime = new Timestamp(curTime.getTime() + MainServlet.SESSION_TIMEOUT_SECS*1000); 	
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public int getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(int versionNumber) {
		this.versionNumber = versionNumber;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public Timestamp getExpirationTime() {
		return expirationTime;
	}
	
	public void setNewExpirationTime() {
		Date date = new Date();
		Timestamp curTime = new Timestamp(date.getTime());
		expirationTime = new Timestamp(curTime.getTime() + MainServlet.SESSION_TIMEOUT_SECS*1000);
	}
	
	public void setNewExpirationTime(String t) {
		expirationTime = Timestamp.valueOf(t);
	}
	
	public void incrementVersion() {
		versionNumber++;
	}
	
	public String toString() {
		return "ID: " + getSessionID() + " Version: " + getVersionNumber() + 
				" Message: " + getMessage() + " Expires: " + getExpirationTime();
	}
}
