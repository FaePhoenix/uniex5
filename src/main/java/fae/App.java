package fae;

import fae.Server.Server;

public class App 
{
    public static void main( String[] args )
    {   
        try  {
            Server server = new Server(8080, "serverLocation");
            server.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        

    }
}
