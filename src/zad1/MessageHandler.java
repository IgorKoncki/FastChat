package zad1;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class MessageHandler {
	
	Server parent;
	
	public MessageHandler(Server parent) {
		this.parent = parent;
	}

	public void logIn(String message, SelectionKey key) throws IOException {
		String[] splitted = message.split(":"); //login:[name]:[pass]
		boolean sent = false;
		if(splitted.length>=3) {
			for(User user : parent.Users) {
				if(user.name.equals(splitted[1]) && user.password.equals(splitted[2]) && !user.isLoggedIn()) {
					System.out.println("User exists.");
					user.logIn();
					key.attach(user);
					ByteBuffer bb = parent.charset.encode(CharBuffer.wrap("ok:" + user.currentId));
					((SocketChannel)key.channel()).write(bb);
					sent = true;
				}
			}
			if(!sent) {
				System.out.println("User does not exist.");
				ByteBuffer bb = parent.charset.encode(CharBuffer.wrap("err:wrong log in info"));
				System.out.println(bb.remaining());
				((SocketChannel)key.channel()).write(bb);
			}
		}else {
			System.out.println("Incorrect log in data.");
			ByteBuffer bb = parent.charset.encode(CharBuffer.wrap("err:wrong log in info"));
			((SocketChannel)key.channel()).write(bb);
		}
	}
	
	public void logOut(String message, SelectionKey key) throws IOException {
		String[] splitted = message.split(":");//logout:[ID]
		if(splitted.length>=2 && key.attachment()!=null && ((User)key.attachment()).currentId.equals(splitted[1])) {
			((User)key.attachment()).logOut();
			key.channel().close();
			key.cancel();
		}else {
			ByteBuffer bb = parent.charset.encode(CharBuffer.wrap("err:wrong id info"));
			((SocketChannel)key.channel()).write(bb);
		}
	}
	
	public void getMessage(String message, SelectionKey key) throws IOException {
		String[] splitted = message.split(":");//message:[ID]:[text]
		if(splitted.length>=3 && key.attachment()!=null && ((User)key.attachment()).currentId.equals(splitted[1])) {
			String post = parent.chatText.addMessage(((User)key.attachment()).name, splitted[2]);
			sendtoall(post);
		}else {
			ByteBuffer bb = parent.charset.encode(CharBuffer.wrap("err:wrong id info"));
			((SocketChannel)key.channel()).write(bb);
		}
	}
	
	private void sendtoall(String post) {
		for(SelectionKey key : parent.selector.keys()) {
			if(key.attachment()!=null) {
				try {
					ByteBuffer bb = parent.charset.encode(CharBuffer.wrap(post)); //[name]:[message]
					((SocketChannel)key.channel()).write(bb);
				} catch (IOException e) {
					System.err.println("Sent to all: " + e);
					((User)key.attachment()).logOut();
					key.cancel();
				}
			}
		}
		
	}

	public void getArchive(String message, SelectionKey key) throws IOException {
		String[] splitted = message.split(":");//getarchive:[ID]
		if(splitted.length>=2 && key.attachment()!=null && ((User)key.attachment()).currentId.equals(splitted[1])) {
			ByteBuffer bb = parent.charset.encode(CharBuffer.wrap(parent.chatText.text));
			((SocketChannel)key.channel()).write(parent.charset.encode(CharBuffer.wrap(""+bb.remaining()))); //how many bytes will be sent
			while(bb.remaining()>0) {
				((SocketChannel)key.channel()).write(bb);
			}
		}else {
			ByteBuffer bb = parent.charset.encode(CharBuffer.wrap("err:wrong id info"));
			((SocketChannel)key.channel()).write(bb);
		}
	}
}
