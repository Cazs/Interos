public class Client
{
  private String name;
  private String ip;
  private int port;
  private long last_seen;

  public Client(String name, String ip, int port)
  {
    this.name = name;
    this.ip=ip;
    this.port=port;
  }

  public String getName(){return this.name;}

  public String getIP(){return this.ip;}

  public int getPort(){return this.port;}

  public long getLastSeen(){return last_seen;}

  public void setName(String name){this.name=name;}

  public void setIP(String ip){this.ip=ip;}

  public void setPort(int port){this.port=port;}

  public void setLastSeen(long last_seen){this.last_seen=last_seen;}

  @Override
  public String toString()
  {
    return this.name + " @ " + this.ip + ":" + this.port;
  }
}
