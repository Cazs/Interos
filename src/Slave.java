import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.io.IOException;
import java.net.SocketException;
import java.lang.NumberFormatException;
import java.awt.Robot;
import java.awt.AWTException;


public class Slave
{
	private boolean is_connected = false;
	private static String master_ip="localhost";
	private String master_name="Superuser";
	private static String client_name="default_client";

	private DatagramSocket socket;
	private final int MASTER_SEARCH_INTERVAL = 1000;//ms
	private static int client_port=4440;
	private static int svr_port = 4242;
	private boolean server_is_running = true;
	private final int BUFFER_SIZE = 512;
	private Timer tMasterFinder;

	private Robot io_controller;

	public Slave()
	{
		try
		{
			//init Robot
			io_controller = new Robot();
			//setup socket
			socket = new DatagramSocket(client_port);
		}catch(SocketException e)
		{
			System.err.println(e.getMessage());
			System.exit(-1);
		}catch(AWTException e)
		{
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		//Look for masters
		tMasterFinder = new Timer();
		tMasterFinder.scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				if(!is_connected)
				{
					//send request for masters
					try
					{
						sendDatagram("HELLO " + client_name,master_ip);
					}catch(IOException e)
					{
						System.err.println(e.getMessage());
					}
				}else
				{
					//ping server so it (and I) know we are still connected.
				}
			}
		},0,MASTER_SEARCH_INTERVAL);
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
		DatagramPacket outbound = new DatagramPacket(message.getBytes(),message.getBytes().length,dest_ip,svr_port);
		socket.send(outbound);
	}

	public void listen() throws IOException
	{
		byte[] buffer = new byte[BUFFER_SIZE];
		DatagramPacket inbound_packet = new DatagramPacket(buffer,buffer.length);
		System.out.println("Slave server is running....");
		while(server_is_running)
		{
			socket.receive(inbound_packet);
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
				case "ACK":
					handleACK(command_params, inbound_packet);
					break;
				case "KEYPRESS":
					handleKeyPress(command_params);
					break;
				case "KEYRELEASE":
					handleKeyRelease(command_params);
					break;
				case "KEYTYPE":
					handleKeyType(command_params);
					break;
				default:
					System.err.println("Unknown command: " + cmd);
			}
			return true;
		}else return false;
	}

	public void handleACK(String[] command_params, DatagramPacket inbound_packet)
	{
		String response_to_cmd = command_params[1];//ACK <cmd> <master name>
		if(response_to_cmd.toUpperCase().equals("HELLO"))
		{
			master_name = command_params[2];
			master_ip = inbound_packet.getAddress().toString();
			//get rid of the first slash if it exists
			if(master_ip.charAt(0)=='/' || master_ip.charAt(0)=='\\')
				master_ip = master_ip.substring(1);
			System.out.println(String.format("Connected to master %s @%s:%s.",master_name,master_ip,inbound_packet.getPort()));

			is_connected=true;
			//tMasterFinder.cancel();//stop the Timer that looks for masters
		}
	}

	public void handleKeyPress(String[] command_params) throws NumberFormatException
	{
		if(command_params.length>1)
		{
			int key_code = Integer.valueOf(command_params[1]);
			System.out.println("Pressing key: " + key_code);
			io_controller.keyPress(key_code);
		}
	}

	public void handleKeyRelease(String[] command_params) throws NumberFormatException
	{
		if(command_params.length>1)
		{
			int key_code = Integer.valueOf(command_params[1]);
			System.out.println("Releasing key: " + key_code);
			io_controller.keyRelease(key_code);
		}
	}

	public void handleKeyType(String[] command_params) throws NumberFormatException
	{
		if(command_params.length>1)
		{
			int key_code = Integer.valueOf(command_params[1]);
			//TODO: make a key type event
		}
	}

	public static void main(String[] args)
	{
		//<client name> <server address> <server port> <client port>
		if(args.length>=4)
		{
			client_name = args[0];
			try
			{
				master_ip = args[1];
				svr_port = Integer.valueOf(args[2]);
				client_port = Integer.valueOf(args[3]);
				new Slave();//.setVisible(true);
			}catch(NumberFormatException e)
			{
				System.err.println(e.getMessage());
			}
		}else System.err.println("Not enough actual arguments.");
	}

}
