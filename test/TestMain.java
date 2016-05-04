
import cl.tide.fm.sensonet.SensonetClient;
import java.io.IOException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author edisondelgado
 */
public class TestMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
       SensonetClient client = new SensonetClient();
       System.out.println("Attemp Login");
       client.login("edisonsk@gmail.com", "123456");
       System.out.println("logged");
       client.setToken("3108cdab606186a35d337a44ed754042ddcbb400c19f09109f9ed709b34c0b37");
       System.out.println("created ");
       client.findOrCreateDataSource("senso");
       
    }
    
}
