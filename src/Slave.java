import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.io.IOException;
import java.net.SocketException;

public class Slave
{
	private boolean is_connected = false;
	private String master_ip;
	private String master_name;

	private DatagramSocket socket;
	private final int MASTER_SEARCH_INTERVAL = 1000;//ms
	private final int CLIENT_PORT=4440;
	private final int SVR_PORT = 4242;
	private boolean server_is_running = true;
	private final int BUFFER_SIZE = 512;
	private Timer tMasterFinder;

	public Slave()
	{
		//setup socket
		try
		{
			socket = new DatagramSocket(CLIENT_PORT);
		}catch(SocketException e)
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
				//send request for masters
				try
				{
					sendDatagram("HELLO Casper","localhost");
				}catch(IOException e)
				{
					System.err.println(e.getMessage());
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
		DatagramPacket outbound = new DatagramPacket(message.getBytes(),message.getBytes().length,dest_ip,SVR_PORT);
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
					String response_to_cmd = command_params[1];//ACK <cmd> <master name>
					if(response_to_cmd.toUpperCase().equals("HELLO"))
					{
						master_name = command_params[2];
						master_ip = inbound_packet.getAddress().toString();
						//get rid of the first slash if it exists
						if(master_ip.charAt(0)=='/' || master_ip.charAt(0)=='\\')
							master_ip = master_ip.substring(1);
						System.out.println(String.format("Connected to %s @%s:%s.",master_name,master_ip,inbound_packet.getPort()));

						is_connected=true;
						tMasterFinder.cancel();//stop the Timer that looks for masters
					}
					break;
				default:
					System.err.println("Unknown command: " + cmd);
			}
			return true;
		}else return false;
	}

	public static void main(String[] args)
	{
		new Slave();
	}

}
