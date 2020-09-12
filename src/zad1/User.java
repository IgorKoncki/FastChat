package zad1;

public class User{

	public String name;
	public String password;
	public String currentId;
	
	public User(String name, String password) {
		this.name = name;
		this.password = password;
		this.currentId = "";
	}
	
	public String logIn() {
		currentId = name + (int)(Math.random()*10000);
		return currentId;
	}
	
	public void logOut() {
		currentId = "";
	}
	
	public boolean isLoggedIn() {
		return !(currentId.equals(""));
	}
	
}
