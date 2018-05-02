import java.security.*;
import java.util.Base64;  
import java.io.IOException;  

public class CreateKeys {

	public static String[] generateKeyPair() {
		
		String[] keyPair = new String[2]; 
		String pubKey;
		String pvtKey;  			

		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			Base64.Encoder encoder = Base64.getEncoder();  
	
			kpg.initialize(2048);
			KeyPair kp = kpg.generateKeyPair();
			
			Key pub = kp.getPublic();
			Key pvt = kp.getPrivate();	
		
			pvtKey = encoder.encodeToString(pvt.getEncoded());
			pubKey = encoder.encodeToString(pub.getEncoded());

			keyPair[0] = pvtKey;
			keyPair[1] = pubKey;  

			return keyPair;  
		} catch (NoSuchAlgorithmException e){
		
			System.out.println("could not create key pair");   		
			keyPair[0] = null; 
			keyPair[1] = null;  
			return keyPair;	
		}
	}
}









