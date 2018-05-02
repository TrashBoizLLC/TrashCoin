import java.io.*; 
  
public class LoggingFunc {

	public static void loginLogoutLogger(String myfile, String mytext){
		
		try(FileWriter fw = new FileWriter(myfile, true);
    			BufferedWriter bw = new BufferedWriter(fw);
    			PrintWriter pw = new PrintWriter(bw))
		{
    			pw.println(mytext);
 	
		} catch (IOException e) {
    		
		}
	}

	public static void storingPublicKey(String myfile, String mytext, String clientName){

                try(FileWriter fw = new FileWriter(myfile, true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        PrintWriter pw = new PrintWriter(bw))
                {
                        pw.println(clientName + " Public Key");   
			pw.println(mytext);

                } catch (IOException e) {

                }
	}
}
