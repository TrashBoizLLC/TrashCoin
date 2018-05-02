import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;




public class TrashServer {

	  // The server socket.
	  private static ServerSocket serverSocket = null;
	  // The client socket.
	  private static Socket clientSocket = null;
	
	  // This chat server can accept up to maxClientsCount clients' connections.
	  private static final int maxClientsCount = 10;
	  private static final clientThread[] threads = new clientThread[maxClientsCount];
	
	  //initialize the Block chain and set difficulty
	  public static ArrayList<Block> blockchain = new ArrayList<Block>();  
	  public static ArrayList<String> publicKeys = new ArrayList<String>();  
	  public static int difficulty = 5;    
	  public static String currentHash = null;  
	  public static int blockNumber = 0; 
	  public static String dataBuffer = null; 
	  //cancle cases
	  private static boolean blockSolved = false;  
	  public static File[] directoryListing = null; 
	  
	  public static void main(String args[]) {
	
	  	//Assigned port number 
	   	int portNumber = 25023;
	    	System.out.println("Using default port: " + portNumber);  
    
	  	//Attempting to open  server socket on port
		try {
      			serverSocket = new ServerSocket(portNumber);
    		} catch (IOException e) {
     			System.out.println(e);
   		}

		//creating genisis block
		if(blockchain.isEmpty()) {
			blockchain.add(new Block("first ", "0"));    
			blockNumber++;  
		}

		//Load public keys
		File dir = new File("ClientPubKey");  
		
		//System.out.println(dir.length());  
		directoryListing = dir.listFiles();  

		
		/*
		for(int i = 0; i < directoryListing.length; i++) {
			System.out.println(directoryListing[i]); 
		}
		*/

		// Create a client socket for each connection and pass it to a new client thread
		while (true) {
      			try {
        			clientSocket = serverSocket.accept();
        			int i;
				for (i = 0; i < maxClientsCount; i++) {
          				if (threads[i] == null) {
            					(threads[i] = new clientThread(clientSocket, threads)).start();
            					break;
          				}
        			}
        			if (i == maxClientsCount) {
          				PrintStream os = new PrintStream(clientSocket.getOutputStream());
          				os.println("Server too busy. Try later.");
          				os.close();
          				clientSocket.close();
        			}
      			} catch (IOException e) {
        			System.out.println(e);
      			}
    		}
	}
}

class clientThread extends Thread {

	private String clientName = null;
	private DataInputStream is = null;
	private PrintStream os = null;
	private Socket clientSocket = null;
	private final clientThread[] threads;
	private int maxClientsCount;
	private int difficulty = TrashServer.difficulty;  
	private String LogString = null;  
	private int calculatedNonce = 0;
 
	 public clientThread(Socket clientSocket, clientThread[] threads) {
	  	this.clientSocket = clientSocket;
	  	this.threads = threads;
		maxClientsCount = threads.length;
	  }

	  public void run() {
	  	int maxClientsCount = this.maxClientsCount;
	  	clientThread[] threads = this.threads;
		Block threadBlock = null;

    		try {
      
			//input stream
      			is = new DataInputStream(clientSocket.getInputStream());
     			os = new PrintStream(clientSocket.getOutputStream());
     			String name;
     			while (true) {
				os.println("Enter your name.");
				name = is.readLine().trim();
          			break;
      			}
			
			//log user login
			LogString = (LocalDateTime.now())+ " " + name + " logged into the server.";   
			LoggingFunc.loginLogoutLogger("LoginLogoutLog.txt", LogString);   		

			//request public key from user. 
			os.println("/pubKeyReq " + name);
	
      			/* Welcome the new the client. */
      			os.println("Welcome " + name + " to the Trash Coin server.\nFor list of commands enter /help in a new line.");
      			
			threadBlock = TrashServer.blockchain.get(TrashServer.blockchain.size()-1);
			os.println("/currentBlock " + threadBlock.previousHash + " " 
						+ threadBlock.timeStamp + " " 
						+ threadBlock.hash + " " 
						+ difficulty + " "
						+ threadBlock.data);    
				
			//setting client name to thread location at [i]
			synchronized (this) {
        			for (int i = 0; i < maxClientsCount; i++) {
          				if (threads[i] != null && threads[i] == this){ 
            					clientName = name;
            					break;
          				}
        			}
      			}
		
	
		      	// Clients communication with server 
      			while (true) {
				String line = is.readLine();
       				

				//client functions
				if(line.startsWith("/solveBlockAttempt")){
					String[] streamData;  
					String tempHash = null;  
					int reward = TrashServer.difficulty * 10;

					streamData = line.split("\\s"); 
					tempHash = streamData[1]; 
 					

					//TrashServer.blockchain.get(TrashServer.blockchain.size()-1).hash = streamData[1];						
					calculatedNonce = Integer.parseInt(streamData[2]);   

					if(isBlockValid(TrashServer.blockchain, calculatedNonce, tempHash)) {
						if(!doesBlockExist(TrashServer.blockchain, tempHash)) {			
							TrashServer.blockchain.get(TrashServer.blockchain.size()-1).hash = tempHash;  
							System.out.println(clientName + " has mined block, awarded: " + reward + " TrashCoins");   
							if(TrashServer.dataBuffer != null && !TrashServer.dataBuffer.isEmpty()){
								TrashServer.dataBuffer = TrashServer.dataBuffer.concat(clientName + " " + reward + " ");  
							}else {
								TrashServer.dataBuffer = clientName + " " + reward + " ";  	
							}
							//System.out.println(TrashServer.dataBuffer);  
		
					        	TrashServer.blockchain.add(new Block( TrashServer.dataBuffer, TrashServer.blockchain.get(TrashServer.blockchain.size()-1).hash));
                	       				TrashServer.dataBuffer = null;
			
							os.println("You have solved the block awarded: " + reward + " TrashCoins.");  

							threadBlock = TrashServer.blockchain.get(TrashServer.blockchain.size()-1);
							
                                			synchronized (this) {
								for (int i = 0; i < maxClientsCount; i++) {
									if (threads[i] != null && threads[i].clientName != null) {                                                                     
												
											os.println("/killThread");			
												
											os.println("/currentBlock " + threadBlock.previousHash + " "
        	        	                			                	+ threadBlock.timeStamp + " "
                	        	                        				+ threadBlock.hash + " "
                        	        	                				+ difficulty + " "
												+ threadBlock.data);
									}
								}
							}
  						}
					} else {
							threadBlock = TrashServer.blockchain.get(TrashServer.blockchain.size()-1);
                                                        os.println("/currentBlock " + threadBlock.previousHash + " "                                                                                                        
                                                                + threadBlock.timeStamp + " "                                                                                                                               
                                                                + threadBlock.hash + " "                                                                                                                                    
                                                                + difficulty + " "                                                                                                                                          
                                                                + threadBlock.data);   

					}
				
				}
				else if(line.startsWith("/pay")) {
				
					String recipient = null;
					String[] stringData = null;
					int paymentAmount = 0;  

					stringData = line.split("\\s");					
	
					recipient = stringData[1]; 
					paymentAmount = Integer.parseInt(stringData[2]);

					if(payUser(TrashServer.blockchain, clientName, recipient, paymentAmount)){
						os.println("You have sent " + paymentAmount + " to " + recipient);
					}else {
						os.println("Insufficient funds");   
					}

				}
				else if(line.startsWith("/wallet")) {
					os.println(clientName + " Your wallet balance is: " + getWalletValue(TrashServer.blockchain, clientName));  
				}
				else if(line.startsWith("/help")) {
					os.println("commands \n /pay [client name] [Ammount] \n /wallet");  
				}
				else if(line.startsWith("/pubKey")){
					String[] streamData = line.split("\\s");
				
					File tmpFile = new File("ClientPubKey", name + "PubKey.txt");
					
					if(!tmpFile.exists()){
					
						tmpFile.createNewFile();
						
						FileWriter fw = new FileWriter(tmpFile);  
						BufferedWriter bw = new BufferedWriter(fw);
							
						bw.write(name);  
						bw.newLine();
						bw.write(streamData[1]);   
						bw.close();  
						fw.close();  
					}
				}
				else if(line.startsWith("/quit")) {
       					System.out.println(name + " has disconnected");  
					break;
        			}
          		
				/* The message is public, broadcast it to all other clients. */
				/*
				synchronized (this) {
            				for (int i = 0; i < maxClientsCount; i++) {
              					if (threads[i] != null && threads[i].clientName != null) {
                					threads[i].os.println("<" + name + "> " + line);
              					}
            				}
          			}
				*/

			}
      	
                        //log user logout 
                        LogString = (LocalDateTime.now())+ " " + name + " logged out of the server.";
                        LoggingFunc.loginLogoutLogger("LoginLogoutLog.txt", LogString);
	
			//message to inform users that a user is quiting 
			synchronized (this) {
      				for (int i = 0; i < maxClientsCount; i++) {
          				if (threads[i] != null && threads[i] != this && threads[i].clientName != null) {
            					threads[i].os.println("*** The user " + name + " is leaving the chat room !!! ***");
          				}
        			}
      			}
      			os.println("*** Bye " + name + " ***");

      			/*
      			 * Clean up. Set the current thread variable to null so that a new client
      			 * could be accepted by the server. Close all streams.
      			 */
      			synchronized (this) {
        			for (int i = 0; i < maxClientsCount; i++) {
          				if (threads[i] == this) {
            					threads[i] = null;
          				}
        			}
      			}
      			is.close();
      			os.close();
      			clientSocket.close();
    		} catch (IOException e) {
    			System.out.println("Server Crashed");  
		}
  	}

	public static Boolean isBlockValid(ArrayList<Block> blockchain, int calculatedNonce, String tempHash) {
                String hashTarget = new String(new char[TrashServer.difficulty]).replace('\0', '0');

		Block currentBlock = blockchain.get(blockchain.size()-1);  

		if(tempHash.equals(currentBlock.calculateHash(calculatedNonce))){
			//System.out.println(calculatedNonce);  
			return true;
		} else {
			//System.out.println("Current data is:" + currentBlock.data);  
			//System.out.println("invalid Block");   
			return false;
		}        
	}

	public static int getWalletValue(ArrayList<Block> blockchain, String clientName) {
		
		String[] dataString = null;
		int walletBalance = 0;

		for(int i = 0; i < blockchain.size(); i++) { 
			dataString = blockchain.get(i).data.split("\\s");      
			for(int k = 0; k < dataString.length - 1; k++){
				if(dataString[k].equals(clientName)){
					walletBalance = walletBalance + Integer.parseInt(dataString[k+1]);
				}
			}	
		}
		return walletBalance; 
	}

	public static Boolean payUser(ArrayList<Block> blockchain, String clientName, String recipient, int paymentAmount) {

		int walletBalance = 0;
		
		walletBalance = getWalletValue(blockchain, clientName);

		if(paymentAmount > walletBalance){
			return false;
		} else {

                	if(TrashServer.dataBuffer != null && !TrashServer.dataBuffer.isEmpty()){
                        	TrashServer.dataBuffer = TrashServer.dataBuffer.concat(clientName + " " + (-1)*paymentAmount + " ");
                        	TrashServer.dataBuffer = TrashServer.dataBuffer.concat(recipient + " " + paymentAmount + " ");
			}else {
                        	TrashServer.dataBuffer = clientName + " " + (-1)*paymentAmount + " ";
                        	TrashServer.dataBuffer = TrashServer.dataBuffer.concat(recipient + " " + paymentAmount + " ");		
			}


			return true;
		}
	}	

	public static Boolean doesBlockExist(ArrayList<Block> blockchain, String createdHash){
		
		for(int i = 0; i < blockchain.size(); i++) {                                                                                                                                                                
                	if(createdHash.equals(blockchain.get(i).hash)){
				return true;
			}
		} 
		return false;
	}


}
