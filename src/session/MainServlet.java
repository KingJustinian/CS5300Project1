package session;

import groupMembership.Server;
import groupMembership.GroupMemberManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.Set;
import java.util.HashSet;
import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



import rpc.RPCClient;
import rpc.RPCServer;


/**
 * Servlet implementation class MainServlet
 */
@WebServlet("/MainServlet")
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Server thisServer;
	
	public GroupMemberManager gm;
	
	public static String IP_NULL ="0.0.0.0";
	public static String IPP_NULL = "0.0.0.0:0";
	
	private int last_sess_num = 0;
	
	private static String cookieName = "CS5300PROJ1SESSION";
	protected static final Integer SESSION_TIMEOUT_SECS = 60;
	protected static final Integer cleanerInterval = 60;
	public ConcurrentHashMap<String, SessionState> sessionData = new ConcurrentHashMap<String, SessionState>();
    
	protected final SessionCleaner sessionCleaner = new SessionCleaner(this);
	protected static final Timer cleanTimer = new Timer();
	
	public RPCServer rServer;
	
	// Locks for the HashMap
	public final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	public final Lock readLock = rwl.readLock();
	public final Lock writeLock = rwl.writeLock();
	
    // Determine if a session exists already based on if the session ID stored is valid
	private boolean invalidState(SessionState st) {
		return (st == null || st.getExpirationTime().before((new Timestamp(new Date().getTime()))));
	}
	
	// Create a new session in the HashMap and return a cookie with the sessionID and version number
	private Cookie createSession(String message) throws NumberFormatException, UnknownHostException {
		int sID = last_sess_num++;
		int version = 0;
		SessionState st = new SessionState(sID, thisServer, version, message);
		writeLock.lock();
		sessionData.put(st.getSessionID(), st);
		writeLock.unlock();
		
		String ippB = sessionWrite(st, null);
			
		return (new Cookie(cookieName, st.getSessionID() + "^" + version + "^" + thisServer.ip.getHostAddress() 
				+ ":" + thisServer.port +"^"+ ippB));
	}
	

	// Start the garbage collection and the RPCServer which begins the program essentially
    public MainServlet() {
        super();
        cleanTimer.schedule(sessionCleaner, cleanerInterval*1000, cleanerInterval*1000);
        try {
        	rServer = new RPCServer(this);
        	thisServer = new Server(InetAddress.getLocalHost(), rServer.getServerPort());
            new Thread(rServer).start();    
            gm = new GroupMemberManager(thisServer);
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
    
    private SessionState sessionRead(String cookie_vals, HttpServletRequest req){
    	SessionState state = null;
    	
    	String[] sTemp = cookie_vals.split("\\^");
    	String sID = sTemp[0];
    	int ver = Integer.parseInt(sTemp[1]);
    	String ipP = sTemp[2].split(":")[0];
    	int portP = Integer.parseInt(sTemp[2].split(":")[1]);
    	String ipB = sTemp[3].split(":")[0];
    	int portB = Integer.parseInt(sTemp[3].split(":")[1]);
    	
    	if(!ipP.equals(IP_NULL)){
    		try {
				state = RPCClient.sessionRead(sID, ver, new Server(InetAddress.getByName(ipP), portP));
			} catch (UnknownHostException e) {
				System.out.println("Primary IP could not be resolved");
				e.printStackTrace();
			}
    		if(state == null && !ipB.equals(IP_NULL)){
    			try {
					state = RPCClient.sessionRead(sID,  ver, new Server(InetAddress.getByName(ipB), portB));
					if(state != null){
						req.setAttribute("found", "IPPBackup");
					}
				} catch (UnknownHostException e) {
					System.out.println("Secondary IP could not be resolved");
					e.printStackTrace();
				}
    		} else {
    			req.setAttribute("found", "IPPPrimary");
    		}
    	} if(state == null){
    		System.out.println("State not retrieved");
    	}
    	return state;
    }
    
    private void sessionDelete(String cookie_vals){
    	String[] sTemp = cookie_vals.split("\\^");
    	String sID = sTemp[0];
    	int ver = Integer.parseInt(sTemp[1]);
    	String ipP = sTemp[2].split(":")[0];
    	int portP = Integer.parseInt(sTemp[2].split(":")[1]);
    	String ipB = sTemp[3].split(":")[0];
    	int portB = Integer.parseInt(sTemp[3].split(":")[1]);
    	
    	if(!ipP.equals(IP_NULL)){
    		try {
				RPCClient.sessionDelete(sID, ver, new Server(InetAddress.getByName(ipP), portP));
			} catch (UnknownHostException e) {
				System.out.println("Primary Server delete failed");
				e.printStackTrace();
			}
    	};
    	
    	if(!ipP.equals(IP_NULL)){
    		try {
				RPCClient.sessionDelete(sID, ver, new Server(InetAddress.getByName(ipP), portP));
			} catch (UnknownHostException e) {
				System.out.println("Backup Server delete failed");
				e.printStackTrace();
			}
    	};
    }
    
    private String sessionWrite(SessionState session, String cookie_vals) throws NumberFormatException, UnknownHostException{
    	String ippB = IPP_NULL;
    	String thisIPP = thisServer.ip.getHostAddress() + ":" + thisServer.port;
    	
    	boolean result = false;
    	
    	if(cookie_vals != null && !cookie_vals.split("\\^")[2].equals(thisIPP)){
    		//System.out.println(cookie_vals.split("\\^")[2]);
    		//System.out.println(thisIPP);
    		Server tempP = new Server(InetAddress.getByName(cookie_vals.split("\\^")[2].split(":")[0]),
    				Integer.parseInt(cookie_vals.split("\\^")[2].split(":")[1]));
    		result = RPCClient.sessionWrite(session, tempP);
    		if(result){
    			if(gm.serverCheck(tempP)){
    				//System.out.println("HERE1");
	    			gm.addMember(tempP);
	    			ippB = cookie_vals.split("\\^")[2];
    			}
    		} else {
    			gm.removeMember(tempP);
    		}
    	}
    	
    	if(!result && cookie_vals != null && !cookie_vals.split("\\^")[3].equals(thisIPP) && !cookie_vals.split("\\^")[3].equals(IPP_NULL)){
    		Server tempB = new Server(InetAddress.getByName(cookie_vals.split("\\^")[3].split(":")[0]),
    				Integer.parseInt(cookie_vals.split("\\^")[3].split(":")[1]));
    		//System.out.println(tempB.toString());
    		result = RPCClient.sessionWrite(session, tempB);
    		if(result){
    			if(gm.serverCheck(tempB)){
	    			gm.addMember(tempB);
	    			//System.out.println("HERE2");
	    			ippB = cookie_vals.split("\\^")[3];
    			}
    		} else { 
    			gm.removeMember(tempB);
    		}
    	}
    	
    	if(!result){
    		Set<Server> mbrSet = gm.getMemberSet();
    		mbrSet.remove(thisServer);
    		if(cookie_vals != null){
    			Server tempP = new Server(InetAddress.getByName(cookie_vals.split("\\^")[2].split(":")[0]),
        				Integer.parseInt(cookie_vals.split("\\^")[2].split(":")[1]));
    			Server tempB = new Server(InetAddress.getByName(cookie_vals.split("\\^")[3].split(":")[0]),
        				Integer.parseInt(cookie_vals.split("\\^")[3].split(":")[1]));
    			mbrSet.remove(tempP);
    			mbrSet.remove(tempB);
    		}
    		
    		int i = 0;
    		Server[] tempSet = mbrSet.toArray(new Server[0]);
    		
    		while(i < tempSet.length && !result){
    			result = RPCClient.sessionWrite(session, tempSet[i]);
    			if(result){
    				if(gm.serverCheck(tempSet[i])){
	    				ippB = tempSet[i].toString();
	    				gm.addMember(tempSet[i]);
    				}
    				break;
    			} else {
    				gm.removeMember(tempSet[i]);
    			}
    			i++;
    		}
    	}
    	
    	if(cookie_vals != null && !ippB.equals(cookie_vals.split("\\^")[2]) && !ippB.equals(IPP_NULL)){
			Server tempP = new Server(InetAddress.getByName(cookie_vals.split("\\^")[2].split(":")[0]),
    				Integer.parseInt(cookie_vals.split("\\^")[2].split(":")[1]));
    		RPCClient.sessionDelete(session.getSessionID(), session.getVersionNumber()-1, tempP);
    	}
    	if(cookie_vals != null && !ippB.equals(cookie_vals.split("\\^")[3]) && !ippB.equals(IPP_NULL)){
			Server tempB = new Server(InetAddress.getByName(cookie_vals.split("\\^")[3].split(":")[0]),
    				Integer.parseInt(cookie_vals.split("\\^")[3].split(":")[1]));
    		RPCClient.sessionDelete(session.getSessionID(), session.getVersionNumber()-1, tempB);
    	}
    	
    	return ippB;
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//PrintWriter out = response.getWriter();
		
		Cookie userCookie = null;
		String message = "Welcome!";
		String sessionID = null;
		SessionState curState = null;
		boolean previousUser = false; // Determines if the user is returning with a valid cookie/session
		
		// If the cookie already exists then retrieve it and store it into userCookie
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for(Cookie cookie: cookies) {
				if (cookieName.equals(cookie.getName())) {
					userCookie = cookie;
				}
			}
		}
		request.setAttribute("found", "CACHE");

		String command = request.getParameter("command");
		
		// Simulate a crash by telling the RPCServer to not respond to requests anymore
		if (command != null && command.equals("Crash")) {
			rServer.setRunning(false);
			System.exit(0);
		}
		
		// Create a new session if there wasn't a cookie
		if (userCookie == null) {
			userCookie = createSession(message);
			userCookie.setMaxAge(SESSION_TIMEOUT_SECS); 
			String temp[] = userCookie.getValue().split("\\^");
			sessionID = temp[0];
			curState = sessionData.get(sessionID);
			request.setAttribute("found", "CACHE");
		} else { // Else get the session from the cookie, if it exists
			String temp[] = userCookie.getValue().split("\\^");
			sessionID = temp[0];
			curState = sessionData.get(sessionID);
			//System.out.println("HALLO!");
			//System.out.println(curState.getSessionID());
			//System.out.println(curState.getVersionNumber());
			//System.out.println(curState.getMessage());
			//System.out.println("HALLO!");
			previousUser = true;
			// The case when a cookie exists for a session but when the session identified doesn't
			// exist in the HashMap (i.e. restarting the program when and the client still has
			// a cookie from before. Or if the session has expired
			if (invalidState(curState)) { 
				curState = sessionRead(userCookie.getValue(), request);
				if(invalidState(curState)){
					userCookie = createSession(message);
					userCookie.setMaxAge(SESSION_TIMEOUT_SECS); 
					String temp2[] = userCookie.getValue().split("\\^");
					sessionID = temp2[0];
					curState = sessionData.get(sessionID);
					previousUser = false;
				}
			}
		} 
		
		// Update the session/cookie appropriately depending on the command (or lack of command if a returning user)
		if (command != null) {
			if (command.equals("LogOut")) {
				if (userCookie != null) {
					writeLock.lock();
					sessionData.remove(sessionID);
					writeLock.unlock();
					sessionDelete(userCookie.getValue());
					userCookie.setMaxAge(0);
				}
			} else if (command.equals("Replace")) {
				message = request.getParameter("replaceText");
				curState.setMessage(message);
				curState.incrementVersion();
				curState.setNewExpirationTime();
				writeLock.lock();
				sessionData.put(sessionID, curState);
				writeLock.unlock();
				//System.out.println(curState.getSessionID());
				//System.out.println(curState.getVersionNumber());
				//System.out.println(curState.getMessage());
				String ippB = sessionWrite(curState, userCookie.getValue());
				userCookie = new Cookie(cookieName, sessionID + "^" + curState.getVersionNumber() 
						+ "^" + thisServer.ip.getHostAddress() + ":" + thisServer.port + "^"+ ippB);
				userCookie.setMaxAge(SESSION_TIMEOUT_SECS);
			} else { // Refresh command
				// Update the session data and cookie
				curState.incrementVersion();
				curState.setNewExpirationTime();
				writeLock.lock();
				sessionData.put(sessionID, curState);
				writeLock.unlock();
				String ippB = sessionWrite(curState, userCookie.getValue());
				//System.out.println(curState.getSessionID());
				//System.out.println(curState.getVersionNumber());
				//System.out.println(curState.getMessage());
				userCookie = new Cookie(cookieName, sessionID + "^" + curState.getVersionNumber()
						+ "^" + thisServer.ip.getHostAddress() + ":" + thisServer.port + "^"+ ippB);
				userCookie.setMaxAge(SESSION_TIMEOUT_SECS);
			}
		} else {
			// When there was no command and the user is returning with a valid session, the 
			// version number/expiration time still need to be incremented accordingly since EVERY
			// request must update the version number (essentially a refresh command) 
			if (previousUser) {
				// Update the session data and cookie
				curState.incrementVersion();
				curState.setNewExpirationTime();
				writeLock.lock();				
				sessionData.put(sessionID, curState);
				writeLock.unlock();
				String ippB = sessionWrite(curState, userCookie.getValue());
				//System.out.println(curState.getSessionID());
				//System.out.println(curState.getVersionNumber());
				userCookie = new Cookie(cookieName, sessionID + "^" + curState.getVersionNumber()
						+ "^" + thisServer.ip.getHostAddress() + ":" + thisServer.port + "^" + ippB);
				userCookie.setMaxAge(SESSION_TIMEOUT_SECS);
			}
		}
	
		message = curState.getMessage();
		
		// Code for testing sessionRead in RPC Client NOTE: ERROR IF LOGOUT IS USED WHEN THIS CODE IS UNCOMMENTED
		/*SessionState testReadState = RPCClient.sessionRead(curState.getSessionID(), 
				curState.getVersionNumber(), thisServer);
		System.out.println("Test read state: " + testReadState.toString()); */
		
		// Code for testing sessionWrite and sessionDelete in RPC Client
		/*String writeID = Integer.toString(last_sess_num) + "_" + thisServer.ip.getHostAddress() + "_" + Integer.toString(thisServer.port);
		SessionState testWriteState = new SessionState(last_sess_num++, thisServer, 0, "Write Works!");
		RPCClient.sessionWrite(testWriteState, thisServer);
		System.out.println(sessionData.get(writeID).toString());
		RPCClient.sessionDelete(testWriteState.getSessionID(), testWriteState.getVersionNumber(), thisServer);
		if (sessionData.get(writeID) != null) System.out.println(sessionData.get(writeID).toString());*/
		
		
		request.setAttribute("message", message);
		if (curState != null) request.setAttribute("Discard_Time", curState.getExpirationTime());
		request.setAttribute("Expires", userCookie.getMaxAge());
		request.setAttribute("serverAddr", thisServer.ip.getHostAddress());
	    request.setAttribute("serverPort", thisServer.port);
	    
	    Set<Server> set = gm.getMemberSet();
	    String mbrList = "";
	    Iterator<Server> it = set.iterator();
	    while(it.hasNext()){
	    	Server temp = it.next();
	    	mbrList = mbrList + " " + temp.toString();
	    }
	    request.setAttribute("mbrList", mbrList);
	    
	    String[] tempP = userCookie.getValue().split("\\^")[2].split(":");
	    String IPPPrimary = tempP[0] + ":" + tempP[1];
	    String[] tempB = userCookie.getValue().split("\\^")[3].split(":");
	    String IPPBackup = tempB[0] + ":" + tempB[1];
	    request.setAttribute("IPPPrimary", IPPPrimary);
	    request.setAttribute("IPPBackup", IPPBackup);
		request.setAttribute("vNum", curState.getVersionNumber());
		
		if (userCookie != null) response.addCookie(userCookie);
		
		if (command != null && command.equals("LogOut")) {
			request.getRequestDispatcher("/logout.jsp").forward(request,response);
		} else {
		request.getRequestDispatcher("/index.jsp").forward(request, response);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
