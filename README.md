CS 5300 Project 1
Justin Burden jwb279
Mitchell Davis msd79


This project has the standard setup of a dynamic web application project and should run normally.

Our project is displayed via the file index.jsp. The primary servlet is MainServlet.java. Here is where
most of the computing work is done. It uses the class SessionState.java, which stores information for
an individual session. SessionCleaner.java is a class specialized to garbage collect the 
timed-out sessions, which is run with a timer in MainServlet.java. The package rpc contains a class for the 
RPC client and one for the RPC server. The package groupMembership contains a server class which stores
the ip/port information for a server, along with a GroupMemberManager.java which contains a set
of the servers (group membership) for each application server. 

The sessions are stored in a ConcurrentHashMap in MainServlet.java. The key is the session ID, and the
value is of type SessionState, which stores the session information in an instance of a SessionState.
The cookies contain the session ID, version number, IPPprimary, and IPPbackup, with a "^" character separating
the values so that it can be parsed easily. 

Overview of structure:
-Session Package
---MainServlet.java (contains the hashmap which stores session states and utilizes all of the other classes)
---SessionCleaner.java (performs garbage collection)
---SessionState.java (stores information about a session)
-groupMembership Package
---Server.java (stores IP/Port information for a server)
---GroupMemberManager.java (Stores a set of servers, the group membership)
-rpc Package
---RPC Client (SessionRead, SessionWrite, SessionDelete methods which are sent to a RPC server)
---RPC Server (Determine what method the RPC Client asked for and perform the necessary action/send data back)
In Webcontent, index.jsp displays information for the page.

For testing on elastic beanstalk I used the Eclipse plugin and simply had to right click on the AWS Elastic
Beanstalk server and run it.


