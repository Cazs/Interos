import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BoxLayout;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import java.util.ArrayList;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

public class Master extends JFrame implements KeyListener
{
	private static final int SCR_W=120;
	private static final int SCR_H=80;
	private ArrayList<Client> clients;
	private DatagramSocket server;
	private final int SVR_PORT = 4242;
	private final int CLIENT_PORT=4440;
	private boolean server_is_running = true;
	private final int BUFFER_SIZE = 512;
	private String master_name="Superuser";
	private boolean verbose = true;
	private Client selected_client;
	private JComboBox<Client> cbx_clients;

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

	private boolean handleInboundMessage(DatagramPacket inbound_packet) throws IOException
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
					String client_ip = inbound_packet.getAddress().toString();
					//get rid of the first slash if it exists
					if(client_ip.charAt(0)=='/' || client_ip.charAt(0)=='\\')
						client_ip = client_ip.substring(1);
					System.out.println(String.format("%s @%s:%s says HELLO.",client_name,
																						client_ip,inbound_packet.getPort()));
					Client new_client = new Client(client_name, client_ip, inbound_packet.getPort());
					clients.add(new_client);

					cbx_clients.addItem(new_client);

					System.out.println("Added new client: " + client_name);
					sendDatagram("ACK HELLO " + master_name,client_ip);
					break;
				default:
					System.err.println("Unknown command: " + cmd);
			}
			return true;
		}else return false;
	}

	private void initUI()
	{
		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container,BoxLayout.Y_AXIS));

		container.add(new JLabel("Switch to this window to send \ninput to the selected client."));

		cbx_clients = new JComboBox<Client>();
		cbx_clients.setPreferredSize(new Dimension(120,45));

		container.add(cbx_clients);

		this.add(container);
		this.pack();
		this.setLocationRelativeTo(null);

		//this.requestFocusInWindow();
	}

	private void initHandlers()
	{
		this.addKeyListener(this);
		cbx_clients.addKeyListener(this);
		cbx_clients.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent ev)
			{
				if(ev.getStateChange()==ItemEvent.SELECTED)
				{
					selected_client = (Client)ev.getItem();
					if(selected_client!=null)
						System.out.println("Selected client: " + selected_client.getName());
					else System.err.println("Client selection is null.");
				}
			}
		});
	}

	@Override
	public void keyPressed(KeyEvent event)
	{
		try
		{
			handleKeyPress(event);
		}catch(IOException e)
		{
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}

	@Override
	public void keyReleased(KeyEvent event)
	{
		try
		{
			handleKeyRelease(event);
		}catch(IOException e)
		{
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}

	@Override
	public void keyTyped(KeyEvent event)
	{
		try
		{
			handleKeyTyped(event);
		}catch(IOException e)
		{
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}

	public void handleKeyPress(KeyEvent e) throws IOException
	{
		if(verbose)System.out.println(String.format("Key [%s] pressed.",e.getKeyCode()));
		if(selected_client==null)
		{
			JOptionPane.showMessageDialog(null,"You haven't selected a client to control yet.","No client selected",JOptionPane.ERROR_MESSAGE);
			return;
		}

		sendDatagram("KEYPRESS " + e.getKeyCode(),selected_client.getIP());
	}

	public void handleKeyRelease(KeyEvent e) throws IOException
	{
		if(verbose)System.out.println(String.format("Key [%s] released.",e.getKeyCode()));
		if(selected_client==null)
		{
			JOptionPane.showMessageDialog(null,"You haven't selected a client to control yet.","No client selected",JOptionPane.ERROR_MESSAGE);
			return;
		}

		sendDatagram("KEYRELEASE " + e.getKeyCode(),selected_client.getIP());
	}

	public void handleKeyTyped(KeyEvent e) throws IOException
	{
		if(verbose)System.out.println(String.format("Key [%s] typed.",e.getKeyCode()));
		if(selected_client==null)
		{
			JOptionPane.showMessageDialog(null,"You haven't selected a client to control yet.","No client selected",JOptionPane.ERROR_MESSAGE);
			return;
		}

		sendDatagram("KEYTYPE " + e.getKeyCode(),selected_client.getIP());
	}

	public static void main(String[] args)
	{
		new Master().setVisible(true);
	}

}
