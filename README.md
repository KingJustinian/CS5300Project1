CS 5300 Project 1a
Justin Burden jwb279
Mitchell Davis msd79


This project has the standard setup of a dynamic web application project and should run normally.

Our project is displayed via the file index.jsp. The primary servlet is MainServlet.java. Here is where
most of the computing work is done. It uses the class SessionState.java, which stores information for
an individual session. SessionCleaner.java is a class specialized to garbage collect the 
timed-out sessions, which is run with a timer in MainServlet.java.

The sessions are stored in a ConcurrentHashMap in MainServlet.java. The key is the session ID, and the
value is of type SessionState, which stores the session information in an instance of a SessionState.
The cookies currently contain the session ID and version number, with a "^" character separating
the values so that it can be parsed easily. When location is needed for the next assignment, it can
easily be truncated onto the end of this string. 

There wasn't a session expiration time specified, so it is currently one minute, which is convenient 
for testing.
