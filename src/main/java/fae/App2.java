package fae;

import fae.Client.Client;

public class App2 {
    public static void main( String[] args )
    {   
        try  {
            Client client = new Client();
            client.connect(8080);
            client.run();

        } catch (Exception e) {
            e.printStackTrace();
        }
        

    }
}
