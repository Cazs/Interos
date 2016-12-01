public class Client
{
  private String name;
  private String ip;
  private int port;

  public Client(String name, String ip, int port)
  {
    this.name = name;
    this.ip=ip;
    this.port=port;
  }

  public String getName(){return this.name;}

  public String getIP(){return this.ip;}

  public int getPort(){return this.port;}

  public void setName(String name){this.name=name;}

  public void setIP(String ip){this.ip=ip;}

  public void setPort(int port){this.port=port;}
}
