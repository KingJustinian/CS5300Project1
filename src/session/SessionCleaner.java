package session;

import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.TimerTask;
import java.sql.Timestamp;

public class SessionCleaner extends TimerTask {

	private MainServlet sessionManager;
	
	public SessionCleaner(MainServlet sMain) {
		sessionManager = sMain;
	}
	
	@Override
	public void run() {
		Timestamp now = new Timestamp(new Date().getTime());
		
		ConcurrentHashMap<String, SessionState> timerHash = sessionManager.sessionData;
		
		sessionManager.writeLock.lock();		
		for(Enumeration<String> i = timerHash.keys(); i.hasMoreElements();) {
			String key = i.nextElement();
			SessionState val = timerHash.get(key);
			if((now.after(val.getExpirationTime()))) {
				timerHash.remove(key);
				System.out.println("Remove" + key);
			}
		}
		sessionManager.writeLock.unlock();
		System.out.println("Cleaner is running.");
	}

}
