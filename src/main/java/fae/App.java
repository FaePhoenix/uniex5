package fae;

import fae.Helper.Levenshtein;
import fae.Server.Server;



public class App 
{
    public static void main( String[] args )
    {   
        aufgabe5();
        //testing();

    }

    public static void aufgabe5() {
        try  {
            Server server = new Server(8080, "serverLocation/");
            server.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testing(){
        String seq1 = "AGGGTCA";
        String seq2 = "GGTCGGATA";
        Levenshtein calc = new Levenshtein();

        int disance = calc.calcdistance(seq1, seq2);
        System.out.println(disance);
    }
}
