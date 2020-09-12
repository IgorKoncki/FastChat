package zad1;

public class ChatText {

	String text;
	
	public ChatText(String text) {
		this.text = text;
	}
	
	public String addMessage(String sender,String message) {
		String post = sender + ": " + message + System.lineSeparator();
		text += post;
		return post;
	}
	
	public String getCurText() {
		String toReturn = "" + text;
		return toReturn;
	}
	
	public String getText() {
		return text;
	}
	
}
