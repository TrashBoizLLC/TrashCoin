import java.util.Random;  
import java.lang.Math;

public class MineBlock implements Runnable {

	public String currentHash = null;    
	public String previousHash = null;  
	public String currentTimeStamp = null;
	public String currentData = null; 	
	public String solvedHash = null;  
	public int currentDifficulty = 0;
	public int nonce = 0;  
	public int nonceRange = 0;

	public MineBlock(String previousHash, String currentTimeStamp, String currentData, int currentDifficulty, String currentHash) {
	
		this.previousHash = previousHash;   
		this.currentHash = currentHash;   
		this.currentTimeStamp = currentTimeStamp;   
		this.currentData = currentData;   
		this.currentDifficulty = currentDifficulty;   
	}

	public void run() {
		//solvedHash = mineABlock(currentDifficulty);   	
		String target = new String(new char[currentDifficulty]).replace('\0', '0');
		Random rand = new Random();
		nonceRange = 100 * ((int) java.lang.Math.pow(10,currentDifficulty));	

		while(!currentHash.substring( 0, currentDifficulty).equals(target)) {
                        nonce = rand.nextInt(nonceRange) + 1;
                        currentHash = calculateHash(nonce);
			//System.out.println(nonce); 

		}
		//System.out.println("Mini has solved hash:");  
		//System.out.println("Nonce: " + nonce);   
		TrashClient.os.println("/solveBlockAttempt " + currentHash + " " + nonce);     
	}    

        public String calculateHash(int nonce) {
                String calculatedhash = StringUtil.applySha256(
                                previousHash +
                                currentTimeStamp +
                                Integer.toString(nonce) +
                                currentData
                                );
                return calculatedhash;
        }
}
