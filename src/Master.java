import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.util.ArrayList;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

public class Master extends JFrame
{
	private static final int SCR_W=120;
	private static final int SCR_H=80;
	private ArrayList<Client> clients;
	private DatagramSocket server;
	private final int SVR_PORT = 4242;
	private final int CLIENT_PORT=4440;
	private boolean server_is_running = true;
	private final int BUFFER_SIZE = 512;

	public Master()
	{
		//Window setup
		super("Interos IO v0.1");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBounds(0,0,SCR_W,SCR_H);
		this.setLayout(new GridBagLayout());
		this.setBackground(Color.BLACK);

		clients = new ArrayList<Client>();
		//setup socket
		try
		{
			server = new DatagramSocket(SVR_PORT);
		}catch(SocketException e)
		{
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		//draw the UI
		initUI();
		//initialise the handlers
		initHandlers();

		//listen for incoming messages
		Thread tListener = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					listen();
				}catch(IOException e)
				{
					System.err.println(e.getMessage());
				}
			}
		});
		tListener.start();
	}

	public void sendDatagram(String message, String ip) throws IOException
	{
		InetAddress dest_ip = InetAddress.getByName(ip);
		DatagramPacket outbound = new DatagramPacket(message.getBytes(),message.getBytes().length,dest_ip,CLIENT_PORT);
		server.send(outbound);
	}

	public void listen() throws IOException
	{
		byte[] buffer = new byte[BUFFER_SIZE];
		DatagramPacket inbound_packet = new DatagramPacket(buffer,buffer.length);
		System.out.println("Server is running....");
		while(server_is_running)
		{
			server.receive(inbound_packet);

			handleInboundMessage(inbound_packet);
		}
	}

	private boolean handleInboundMessage(DatagramPacket inbound_packet)
	{
		String msg = new String(inbound_packet.getData(),0,inbound_packet.getLength());
		if(msg==null)return false;
		if(!msg.isEmpty())
		{
			//System.out.println(inbound_packet.getAddress() + ":" + inbound_packet.getPort() + ">>" + msg);
			String[] command_params = msg.split(" ");
			String cmd = command_params[0];
			switch(cmd.toUpperCase())
			{
				case "HELLO":
					String client_name = command_params[1];//HELLO <client name>
					System.out.println(String.format("%s @%s:%i says HELLO.",client_name,
																						inbound_packet.getAddress().toString(),inbound_packet.getPort()));
					clients.add(new Client(client_name, inbound_packet.getAddress().toString(), inbound_packet.getPort()));
					sendDatagram("ACK HELLO",inbound_packet.getAddress().toString());
					break;
				default:
					System.err.println("Unknown command: " + cmd);
			}
			return true;
		}else return false;
	}

	private void initUI()
	{
		this.add(new JLabel("Switch to this window to send \ninput to the selected client."));

		this.pack();
		this.setLocationRelativeTo(null);
	}

	private void initHandlers()
	{
		this.addKeyListener(new KeyListener()
		{

			@Override
			public void keyPressed(KeyEvent e)
			{
				handleKeyPress(e);
			}

			@Override
			public void keyReleased(KeyEvent e)
			{
				handleKeyRelease(e);
			}

			@Override
			public void keyTyped(KeyEvent e)
			{
				handleKeyTyped(e);
			}

		});
	}

	public void handleKeyPress(KeyEvent e)
	{
		System.out.println(String.format("Key [%s] pressed.",e.getKeyCode()));
	}

	public void handleKeyRelease(KeyEvent e)
	{
		System.out.println(String.format("Key [%s] released.",e.getKeyCode()));
	}

	public void handleKeyTyped(KeyEvent e)
	{
		System.out.println(String.format("Key [%s] typed.",e.getKeyCode()));
	}

	public static void main(String[] args)
	{
		new Master().setVisible(true);
	}

}
