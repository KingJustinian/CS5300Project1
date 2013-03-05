package session;

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
	
	public void incrementVersion() {
		versionNumber++;
	}
	
	public String toString() {
		return "ID: " + getSessionID() + " Version: " + getVersionNumber() + 
				" Message: " + getMessage() + " Expires: " + getExpirationTime();
	}
}
