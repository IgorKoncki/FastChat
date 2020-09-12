/**
 *
 *  @author Koncki Igor S16692
 *
 */

package zad1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;


public class Client {
	static final int sPort = 9001;
	static final String sAddress = "localhost";
	Charset charset = Charset.forName("UTF-8");
	JTextArea messagesView;
	String iD;
	SocketChannel s;
	JFrame mainWindow;
	JLabel nameLabel;

	public static void main(String[] args) {
			  new Client();
			  /*
			SocketChannel s = SocketChannel.open();
			s.connect(new InetSocketAddress("localhost",9001));
			String str = "ala ma kota";
			Charset chset = Charset.forName("UTF-8");
			ByteBuffer bb = chset.encode(str);
			new java.util.Scanner(System.in).nextLine();
			s.write(bb);
			s.read(bb);
			System.out.println(bb.capacity());
			bb.flip();
			System.out.println(chset.decode(bb));
			s.close();
			*/
	  
  	}
	
	public Client() {
		mainWindow = new JFrame("FastChat");
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.getContentPane().setLayout(new BorderLayout());
		mainWindow.setPreferredSize(new Dimension(1200,900));
		mainWindow.setBackground(new Color(255,245,220));
		
		JScrollPane messagesPane = new JScrollPane();
		messagesView = new JTextArea();
		messagesView.setEditable(false);
		messagesPane.setViewportView(messagesView);
		
		
		JPanel inputTextPanel = new JPanel();
		JTextPane inputTextPane = new JTextPane();
		inputTextPane.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					String message = inputTextPane.getText().replaceAll(System.lineSeparator(),"");
					if(message.length()<=1000) {
						try {
							sendMessage(message);
						} catch (IOException e1) {
							JOptionPane.showMessageDialog(mainWindow, "Could not sent message");
						}
					}else {
						JOptionPane.showMessageDialog(mainWindow, "1 Message can only have 1000 characters.");
					}
					inputTextPane.setText("");
				}
			}
		});
		inputTextPanel.add(inputTextPane);
		
		JPanel topPanel = new JPanel();
		topPanel.setBackground(new Color(255,245,220));
		topPanel.setLayout(new BorderLayout());
		nameLabel = new JLabel("Unknown User");
		nameLabel.setText("Unknown User");
		topPanel.add(nameLabel,BorderLayout.WEST);
		JButton logInButton = new JButton();
		logInButton.setText("Log in");
		logInButton.setEnabled(false);
		JButton logOutButton = new JButton("Log out");
		logOutButton.setEnabled(false);
		logInButton.addActionListener((e)->{
			String name = JOptionPane.showInputDialog("Type in your username");
			String pass = JOptionPane.showInputDialog("Type in your password");
			try {
				logIn(name, pass);
				if(iD!=null) {
					logInButton.setEnabled(false);
					logOutButton.setEnabled(true);
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		});
		logOutButton.addActionListener((e)->{
			try {
				logOut();
			} catch (IOException e1) {
				System.err.println(e);
			}
			JOptionPane.showMessageDialog(mainWindow, "You logged out. Goodbye");
			System.exit(0);
		});
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new FlowLayout());
		buttonsPanel.add(logInButton);
		buttonsPanel.add(logOutButton);
		topPanel.add(buttonsPanel,BorderLayout.EAST);
		
		JPanel westPanel = new JPanel();
		westPanel.setBackground(new Color(255,245,220));
		JPanel eastPanel = new JPanel();
		eastPanel.setBackground(new Color(255,245,220));
		
		mainWindow.getContentPane().add(messagesPane, BorderLayout.CENTER);
		mainWindow.getContentPane().add(inputTextPane, BorderLayout.SOUTH);
		mainWindow.getContentPane().add(topPanel, BorderLayout.NORTH);
		mainWindow.getContentPane().add(westPanel, BorderLayout.WEST);
		mainWindow.getContentPane().add(eastPanel, BorderLayout.EAST);
		
		mainWindow.pack();
		mainWindow.setVisible(true);
		
		try {
			connect();
			logInButton.setEnabled(true);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(mainWindow, "Could not contact with the server");
			System.exit(1);
		}
	}
	
	public void connect() throws IOException {
		s = SocketChannel.open();
		s.connect(new InetSocketAddress("localhost",9001));
		
	}
	
	
	public void logIn(String name, String pass) throws IOException {
		ByteBuffer bb = charset.encode(CharBuffer.wrap("login:" + name + ":" + pass));
		s.write(bb);
		bb.clear();
		bb = ByteBuffer.allocate(9001);
		s.read(bb);
		bb.flip();
		String[] message = charset.decode(bb).toString().split(":");
		if(message[0].equals("ok")) {
			iD = message[1];
			JOptionPane.showMessageDialog(mainWindow, "Successfully logged in.");
			nameLabel.setText(name);
			//Pobieranie danych o dotychczasowym czacie
			String archive = "";
			bb = charset.encode(CharBuffer.wrap("getarchive:" + iD));
			s.write(bb);
			bb = ByteBuffer.allocate(9001);
			s.read(bb);
			bb.flip();
			String firstMessage = charset.decode(bb).toString();
			bb.clear();
			if(!firstMessage.split(":")[0].equals("err")) {
				int all = Integer.parseInt(firstMessage);
				int gotten = 0;
				while(gotten<all) {
					gotten += s.read(bb);
					bb.flip();
					archive += charset.decode(bb).toString();
					bb.clear();
				}
				messagesView.setText(archive);
				//Start of listening for messages from other people
				setUpListening();
			}else {
				JOptionPane.showMessageDialog(mainWindow, "Could not get the archived chat.");
			}
		}else {
			JOptionPane.showMessageDialog(mainWindow, "Could not log in. " + message[1]);
		}
	}

	private void setUpListening() throws IOException {
		s.configureBlocking(false);
		Selector selector = Selector.open();
		s.register(selector, SelectionKey.OP_READ);
		new Thread(()-> {
			while(true) {
				try {
					selector.select();
					for(SelectionKey key : selector.selectedKeys()) {
						ByteBuffer bb = ByteBuffer.allocate(9001);
						s.read(bb);
						bb.flip();
						String message = charset.decode(bb).toString();
						synchronized(messagesView) {
							messagesView.append(message);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	private void sendMessage(String message) throws IOException {
		s.write(charset.encode(CharBuffer.wrap("message:" + iD + ": " + message)));
		
	}
	
	public void logOut() throws IOException {
		ByteBuffer bb = charset.encode(CharBuffer.wrap("logout:" + iD));
		s.write(bb);
		bb.clear();
	}
}
