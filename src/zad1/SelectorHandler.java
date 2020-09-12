package zad1;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class SelectorHandler {

	public static void accept(Selector selector, SelectionKey serverSKey) {
		try {
			ServerSocketChannel serverSChannel = (ServerSocketChannel)serverSKey.channel(); //Accept the connection and register the client in the selector
			SocketChannel socketChannel = serverSChannel.accept();
			socketChannel.configureBlocking(false);
			socketChannel.register(selector,SelectionKey.OP_READ);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void readAndHandle(Selector selector, SelectionKey key, Server parent) {
		
		ByteBuffer buffer = ByteBuffer.allocate(9001);
		SocketChannel client = (SocketChannel) key.channel();
		Charset charset = parent.charset;
		try {
			System.out.println("Reading message.");
			int noBytes = client.read(buffer); //read client message
			System.out.println("Decoding message. " + noBytes);
			buffer.flip();
			CharBuffer cb = charset.decode(buffer);
			String message = cb.toString();
			System.out.println("Message = " + message);
			if(noBytes < 0) {//Cancel if client closed connection
				System.out.println("Client closed connection.");
				key.cancel();
			}else { //Handle the message from the client
				MessageHandler handler = new MessageHandler(parent);
				switch(message.split(":")[0]) {
					case "login":
						System.out.println("Handle log in.");
						handler.logIn(message, key);
					break;
					case "getarchive":
						System.out.println("Handle get archive.");
						handler.getArchive(message, key);
					break;
					case "message":
						System.out.println("Handle get message.");
						handler.getMessage(message, key);
					break;
					case "logout":
						System.out.println("Handle log out.");
						handler.logOut(message, key);
					break;
				}
			}
	     } catch (IOException e) {
			System.err.println(e);
			if(key.attachment()!=null)
				((User)key.attachment()).logOut();
			key.cancel(); //In case of connection problems abort the client
	     }
	}
	
}
