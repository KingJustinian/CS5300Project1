package session;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Date;
import java.util.UUID;
import java.util.Timer;
import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;

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
	
	private String serverIP;
	private int serverPort;
	
	public static String IPP_NULL = "0.0.0.0";
	
	private int last_sess_num = 0;
	
	private static String cookieName = "CS5300PROJ1SESSION";
	protected static final Integer SESSION_TIMEOUT_SECS = 60;
	protected static final Integer cleanerInterval = 60;
	public static ConcurrentHashMap<String, SessionState> sessionData = new ConcurrentHashMap<String, SessionState>();
    
	protected static final SessionCleaner sessionCleaner = new SessionCleaner();
	protected static final Timer cleanTimer = new Timer();
	
	public RPCServer rServer;
	
    // Determine if a session exists already based on if the session ID stored is valid
	private boolean invalidState(SessionState st) {
		return (st == null || st.getExpirationTime().before((new Timestamp(new Date().getTime()))));
	}
	
	// Create a new session in the HashMap and return a cookie with the sessionID and version number
	private Cookie createSession(String message) {
		int sID = last_sess_num++;
		int version = 0;
		SessionState st = new SessionState(sID, serverIP, serverPort, version, message);
		sessionData.put(st.getSessionID(), st);
		return (new Cookie(cookieName, st.getSessionID() + "^" + version + "^" + serverIP + "_" + serverPort));
	}
	

	// Start the garbage collection and the RPCServer which begins the program essentially
    public MainServlet() {
        super();
        cleanTimer.schedule(sessionCleaner, cleanerInterval*1000, cleanerInterval*1000);
        try {
        	rServer = new RPCServer(this);
        	serverIP = InetAddress.getLocalHost().getHostAddress();
        	serverPort = rServer.getServerPort();
            new Thread(rServer).start();    
            
        } catch (Exception e) {
        	e.printStackTrace();
        }


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
		} else { // Else get the session from the cookie, if it exists
			String temp[] = userCookie.getValue().split("\\^");
			sessionID = temp[0];
			curState = sessionData.get(sessionID);
			previousUser = true;
			// The case when a cookie exists for a session but when the session identified doesn't
			// exist in the HashMap (i.e. restarting the program when and the client still has
			// a cookie from before. Or if the session has expired
			if (invalidState(curState)) { 
				userCookie = createSession(message);
				userCookie.setMaxAge(SESSION_TIMEOUT_SECS); 
				String temp2[] = userCookie.getValue().split("\\^");
				sessionID = temp2[0];
				curState = sessionData.get(sessionID);
				previousUser = false;
			}
		} 
		
		// Update the session/cookie appropriately depending on the command (or lack of command if a returning user)
		if (command != null) {
			if (command.equals("LogOut")) {
				if (userCookie != null) {
					sessionData.remove(sessionID);
					userCookie.setMaxAge(0);
				}
			} else if (command.equals("Replace")) {
				message = request.getParameter("replaceText");
				curState.setMessage(message);
				curState.incrementVersion();
				curState.setNewExpirationTime();
				sessionData.put(sessionID, curState);
				userCookie = new Cookie(cookieName, sessionID + "^" + curState.getVersionNumber() 
						+ "^" + serverIP + "_" + serverPort);
				userCookie.setMaxAge(SESSION_TIMEOUT_SECS);
			} else { // Refresh command
				// Update the session data and cookie
				curState.incrementVersion();
				curState.setNewExpirationTime();
				sessionData.put(sessionID, curState);
				userCookie = new Cookie(cookieName, sessionID + "^" + curState.getVersionNumber()
						+ "^" + serverIP + "_" + serverPort);
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
				sessionData.put(sessionID, curState);
				userCookie = new Cookie(cookieName, sessionID + "^" + curState.getVersionNumber()
						+ "^" + serverIP + "_" + serverPort);
				userCookie.setMaxAge(SESSION_TIMEOUT_SECS);
			}
		}
	
		message = curState.getMessage();
		
		System.out.println(curState.toString());
		System.out.println(userCookie.getValue());
		
		// Code for testing sessionRead in RPC Client NOTE: ERROR IF LOGOUT IS USED WHEN THIS CODE IS UNCOMMENTED
		SessionState testReadState = RPCClient.sessionRead(curState.getSessionID(), 
				curState.getVersionNumber(), serverIP, serverPort);
		System.out.println("Test read state: " + testReadState.toString());
		
		// Code for testing sessionWrite and sessionDelete in RPC Client
		/*String writeID = Integer.toString(last_sess_num) + "_" + serverIP + "_" + Integer.toString(serverPort);
		SessionState testWriteState = new SessionState(last_sess_num++, serverIP, serverPort, 0, "Write Works!");
		RPCClient.sessionWrite(testWriteState, serverIP, serverPort);
		System.out.println(sessionData.get(writeID).toString());
		RPCClient.sessionDelete(testWriteState.getSessionID(), testWriteState.getVersionNumber(), serverIP, serverPort);
		if (sessionData.get(writeID) != null) System.out.println(sessionData.get(writeID).toString());*/
		
		
		request.setAttribute("message", message);
		if (curState != null) request.setAttribute("Discard_Time", curState.getExpirationTime());
		request.setAttribute("Expires", userCookie.getMaxAge());
		//request.setAttribute("serverAddr", request.getLocalAddr());
		//request.setAttribute("serverPort", request.getLocalPort());
		request.setAttribute("serverAddr", serverIP);
	    request.setAttribute("serverPort", serverPort);
	    
	    String[] tempP = userCookie.getValue().split("\\^")[2].split("_");
	    String IPPPrimary = tempP[0] + ":" + tempP[1];
	    request.setAttribute("IPPPrimary", IPPPrimary);
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
