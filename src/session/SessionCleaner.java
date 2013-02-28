package session;

import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.TimerTask;
import java.sql.Timestamp;

public class SessionCleaner extends TimerTask {

	@Override
	public void run() {
		Timestamp now = new Timestamp(new Date().getTime());
		
		ConcurrentHashMap<String, SessionState> timerHash = MainServlet.sessionData;
		
		for(Enumeration<String> i = timerHash.keys(); i.hasMoreElements();) {
			String key = i.nextElement();
			SessionState val = timerHash.get(key);
			if((now.after(val.getExpirationTime()))) {
				timerHash.remove(key);
				System.out.println("Remove" + key);
			}
		}
		System.out.println("Cleaner is running.");
	}

}
