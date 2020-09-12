/**
 *
 *  @author Koncki Igor S16692
 *
 */

package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.Charset;
import java.util.Iterator;

public class Server {

	public Selector selector;
	public User[] Users = {
		new User("Joe","12345"),
		new User("Mary","23456"),
		new User("George","34567")
	};
	public Charset charset = Charset.forName("UTF-8");
	public ChatText chatText;
	
public static void main(String[] args) {
	  try {
		new Server(9001);
	} catch (IOException e) {
		e.printStackTrace();
	}
	new java.util.Scanner(System.in).nextLine();
  }
  
  public Server(int port) throws IOException {
	  System.out.println("Start.");
	  chatText = new ChatText("Server: Welcome to new chat" + System.lineSeparator());
	  ServerSocketChannel serverSChannel = ServerSocketChannel.open();	  
	  serverSChannel.socket().bind(new InetSocketAddress("localhost",port));
	  serverSChannel.configureBlocking(false); 
	  selector = Selector.open();
	  serverSChannel.register(selector, SelectionKey.OP_ACCEPT); //Pomys³ na umieszczenie ServerSocketChannel w ramach selectora zamiast w innym threadzie z internetu. Zrozumiany.
	  System.out.println("Finished preparations.");
	  while(true) {
		  System.out.println("Wait for action.");
		  selector.select();
		  System.out.println("Action starts");
		  Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
		  while(iterator.hasNext()) {
			  SelectionKey key = iterator.next();
			  if(key.isAcceptable()) {
				  System.out.println("Accepting client.");
				  SelectorHandler.accept(selector,key);
				  System.out.println("Client accepted.");
			  }else
			  if(key.isReadable()){
				  System.out.println("Handling a message.");
				  SelectorHandler.readAndHandle(selector,key,this);
				  System.out.println("Message Handled.");
			  }
			  iterator.remove();
		  }
		  
	  }
	  
  }
}
