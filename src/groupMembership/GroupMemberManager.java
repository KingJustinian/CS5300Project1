package groupMembership;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GroupMemberManager {
	
	private Set<Server> MemberSet = new HashSet<Server>(); // Stores the group members
	Server server; // Stores the server that this is operating in
	
	public GroupMemberManager(Server s) {
		server = s;
	}

	public void addMember(Server s) {
		System.out.println(s);
		if(serverCheck(s)){
			MemberSet.add(s);
		}
	}
	
	public boolean serverCheck(Server s){
		boolean result = true;
		int size = numServers();
		if(size !=0){
			for(Server i: MemberSet) {
				System.out.println(s.equals(i));
				if(s.equals(i)){
					result = false;
				}
			}
		}
		return result;
	}
	
	public void removeMember(Server s) {
		int size = numServers();
		if(size != 0){
			for(Server i : MemberSet) {
				System.out.println(s.equals(i));
				if(s.equals(i)){
					MemberSet.remove(i);
				}
			}
		}
	}
	
	// Returns a single server from the member set randomly
	public Server getRandServer() {
		int size = numServers();
		if (size != 0) {
			int item = new Random().nextInt(size);
			int i = 0;
			for (Server s : MemberSet) {
				if (i == item)
					return s;
				i = i + 1;
			}
		}
		return new Server("0.0.0.0", "0");
	}
	
	public Set<Server> getMemberSet() {
		return MemberSet;
	}
	
	public int numServers() {
		int size = MemberSet.size();
		return size;
	}
}
