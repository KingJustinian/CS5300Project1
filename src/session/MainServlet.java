package session;

import java.io.IOException;
import java.io.PrintWriter;
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

/**
 * Servlet implementation class MainServlet
 */
@WebServlet("/MainServlet")
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String cookieName = "CS5300PROJ1SESSION";
	protected static final Integer sessionTimeout = 300;
	protected static final Integer cleanerInterval = 60;
	protected static final ConcurrentHashMap<String, SessionState> sessionData = new ConcurrentHashMap<String, SessionState>();
    
	protected static final SessionCleaner sessionCleaner = new SessionCleaner();
	protected static final Timer cleanTimer = new Timer();
	
	
    // Determine if a session exists already based on if the session ID stored is valid
	private boolean invalidState(SessionState st) {
		return (st == null || st.getExpirationTime().before((new Timestamp(new Date().getTime()))));
	}
	
	// Create a new session in the HashMap and return a cookie with the sessionID and version number
	private Cookie createSession(String message) {
		String sessionID = UUID.randomUUID().toString();
		int version = 0;
		SessionState st = new SessionState(sessionID, version, message);
		sessionData.put(sessionID, st);
		return (new Cookie(cookieName, sessionID + "^" + version));
	}
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MainServlet() {
        super();
        cleanTimer.schedule(sessionCleaner, cleanerInterval*1000, cleanerInterval*1000);
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

		// Create a new session if there wasn't a cookie
		if (userCookie == null) {
			userCookie = createSession(message);
			userCookie.setMaxAge(60); // One hour from now (in seconds)
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
				userCookie.setMaxAge(60); // One hour from now (in seconds)
				String temp2[] = userCookie.getValue().split("\\^");
				sessionID = temp2[0];
				curState = sessionData.get(sessionID);
				previousUser = false;
			}
		} 
		
		String command = request.getParameter("command");
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
				userCookie = new Cookie(cookieName, sessionID + "^" + curState.getVersionNumber());
				userCookie.setMaxAge(60);
			} else { // Refresh command
				// Update the session data and cookie
				curState.incrementVersion();
				curState.setNewExpirationTime();
				sessionData.put(sessionID, curState);
				userCookie = new Cookie(cookieName, sessionID + "^" + curState.getVersionNumber());
				userCookie.setMaxAge(60);
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
				userCookie = new Cookie(cookieName, sessionID + "^" + curState.getVersionNumber());
				userCookie.setMaxAge(60);
			}
		}
	
		message = curState.getMessage();
		
		System.out.println(curState.toString());
		System.out.println(message);
		request.setAttribute("message", message);
		if (curState != null) request.setAttribute("expires", curState.getExpirationTime());
		request.setAttribute("serverAddr", request.getLocalAddr());
		request.setAttribute("serverPort", request.getLocalPort());
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
