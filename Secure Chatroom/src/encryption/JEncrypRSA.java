package encryption;

import java.security.*;
import java.util.Scanner;

import javax.crypto.Cipher;

/* source : https://gist.github.com/dmydlarz/32c58f537bb7e0ab9ebf */
public class JEncrypRSA {
	
    public static final int keySize = 2048;
	
	public static void main(String[] args) {
		//1. Ask the user to enter the message "No body can see me".
		String msg = "No body can see me";
		Scanner reader = new Scanner(System.in);  // Reading from System.in
		System.out.println("Enter the following sentence: "+msg);
		String userInput = reader.nextLine(); 
		reader.close(); 
		//2. Generate the public and private keys
		try {
			KeyPair keyPair = buildKeyPair();
			PublicKey pubKey = keyPair.getPublic();
		    PrivateKey privateKey = keyPair.getPrivate();
		    
			//3. Encode the message using the public key.
			byte [] encrypted = encrypt(privateKey, userInput);     

	        //4. Print the encoded text.
	        System.out.println(new String(encrypted));  
	        
			//5. Decode the message using the private key.
	        byte[] secret = decrypt(pubKey, encrypted);  
	        System.out.println(new String(secret));
	        
		} catch (Exception e) {
			e.printStackTrace();
		}

		

	}
	
	 	public static KeyPair buildKeyPair() throws NoSuchAlgorithmException {
	        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
	        keyPairGenerator.initialize(keySize);
	        return keyPairGenerator.genKeyPair();
	    }

	    public static byte[] encrypt(Key privateKey, String message) throws Exception {
	        Cipher cipher = Cipher.getInstance("RSA");  
	        cipher.init(Cipher.ENCRYPT_MODE, privateKey);  

	        return cipher.doFinal(message.getBytes());  
	    }
	    
	    public static byte[] encrypt(Key privateKey, byte[] message) throws Exception {
	        Cipher cipher = Cipher.getInstance("RSA");  
	        cipher.init(Cipher.ENCRYPT_MODE, privateKey);  
	        return cipher.doFinal(message);  
	    }
	    
	    public static byte[] decrypt(Key publicKey, byte [] encrypted) throws Exception {
	        Cipher cipher = Cipher.getInstance("RSA");  
	        cipher.init(Cipher.DECRYPT_MODE, publicKey);
	        
	        return cipher.doFinal(encrypted);
	    }
}
