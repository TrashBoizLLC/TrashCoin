import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.File;  
import java.io.FileReader;  
import java.io.BufferedWriter;  
import java.io.FileWriter;  

public class TrashClient implements Runnable {

  // The client socket
  private static Socket clientSocket = null;
  // The output stream
  public static PrintStream os = null;
  // The input stream
  public static DataInputStream is = null;

  private static BufferedReader inputLine = null;
  private static boolean closed = false;
 
  //variables 
  private static String currentHash = null;     
  private static String previousHash = null;  
  private static String currentTimeStamp = null;
  private static String currentData = null;   
  public static String blockSolved = null;
  public static int currentDifficulty = 0; 

  public static void main(String[] args) {

    // Port.
    int portNumber = 25023;
    // Host.
    String host = "localhost";

    /*
     * Open a socket on a given host and port. Open input and output streams.
     */
    try {
      clientSocket = new Socket(host, portNumber);
      inputLine = new BufferedReader(new InputStreamReader(System.in));
      os = new PrintStream(clientSocket.getOutputStream());
      is = new DataInputStream(clientSocket.getInputStream());
    } catch (UnknownHostException e) {
      System.err.println("Don't know about host " + host);
    } catch (IOException e) {
      System.err.println("Couldn't get I/O for the connection to the host "
          + host);
    }

    //writing to the socket  
    if (clientSocket != null && os != null && is != null) {
      try {

        /* Create a thread to read from the server. */
        new Thread(new TrashClient()).start();
        while (!closed) {
          
		//if(inputLine.startsWith("/pay")) {


		//}

		//sending message to server
        	os.println(inputLine.readLine().trim());
        }
        /*
         * Close the output stream, close the input stream, close the socket.
         */
        os.close();
        is.close();
        clientSocket.close();
      } catch (IOException e) {
        System.err.println("IOException:  " + e);
      }
    }
  }

  public void run() {
    String responseLine;
    String[] blockData;
    Thread solverThread = null;   
    String[]keyPair = null;
    String pvtKey = null;
    String pubKey = null;


    //Creating key pait
    //String[] keyPair = CreateKeys.generateKeyPair(); 
    
    try {
      while ((responseLine = is.readLine()) != null) {

        if(responseLine.startsWith("/currentBlock")) {
          blockData = responseLine.split("\\s");   

	  previousHash = blockData[1];
	  currentTimeStamp =  blockData[2];  
	  currentHash = blockData[3]; 
	  currentDifficulty = Integer.parseInt(blockData[4]);   
	  currentData = blockData[5] + " "; 
 
	  for(int i = 6; i < blockData.length; i++){ 
	  	currentData = currentData.concat(blockData[i] + " ");    
	  }

	  //System.out.println("current data is:" + currentData);   
	  solverThread = new Thread(new MineBlock(previousHash, currentTimeStamp, currentData, currentDifficulty, currentHash)); 	
	  solverThread.start();
		
	} else if(responseLine.startsWith("/pubKeyReq")) {

		String[] keyData = responseLine.split("\\s");
		String clientName = keyData[1];  

		File tmpFile = new File("ClientKeys", clientName + "KeyPair.txt");
    	
		if(tmpFile.exists()){
		
			//System.out.println("File exists");  
	
			FileReader fileReader = new FileReader(tmpFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader); 
			pvtKey = bufferedReader.readLine();
			pubKey = bufferedReader.readLine();
		
			bufferedReader.close();

    		} else {
			
			tmpFile.createNewFile();

			keyPair = CreateKeys.generateKeyPair();
			pvtKey = keyPair[0];  
			pubKey = keyPair[1]; 
				
			FileWriter fw = new FileWriter(tmpFile);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(pvtKey);
		        bw.newLine();  	
			bw.write(pubKey);
			bw.close();
			fw.close();
		}

		os.println("/pubKey " + pubKey);


	} else if(responseLine.startsWith("/killThread")) {

		if(solverThread.isAlive()){
	
			solverThread.stop();

		}

	} else {        
		System.out.println(responseLine);
	}
	
	if (responseLine.indexOf("*** Bye") != -1)
          break;
      	}
      closed = true;
    } catch (IOException e) {
      System.err.println("IOException:  " + e);
    }
  }
}
