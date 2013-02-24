package session;

import java.sql.Timestamp;
import java.util.Date;

public class SessionState {
	private String sessionID;
	private int versionNumber;
	private String message;
	private Timestamp expirationTime;
	
	public SessionState(String s, int v, String m) {
		setSessionID(s);
		setVersionNumber(v);
		setMessage(m);
		Date date = new Date();
		Timestamp curTime = new Timestamp(date.getTime());
		expirationTime.setTime(curTime.getTime() + 60*60*100); // Set the expiration time to one hour in the future		
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
	
	@SuppressWarnings("unused")
	private Timestamp getExpirationTime() {
		return expirationTime;
	}
}
