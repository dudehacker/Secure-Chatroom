package encryption;

import java.util.Scanner;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

/* source: http://www.java2s.com/Code/Java/Security/EncryptingaStringwithDES.htm */
public class JEncrypDES {
	
	Cipher ecipher;
	Cipher dcipher;
	
	public JEncrypDES(SecretKey key) throws Exception {
	    ecipher = Cipher.getInstance("DES");
	    dcipher = Cipher.getInstance("DES");
	    ecipher.init(Cipher.ENCRYPT_MODE, key);
	    dcipher.init(Cipher.DECRYPT_MODE, key);
	  }
	
	public static byte[] encryptKey(byte[] key, byte[] msg) throws Exception {
		Cipher ecipher = Cipher.getInstance("DES");
		SecretKey DESkey = new SecretKeySpec(key, 0, key.length, "DES"); 
		ecipher.init(Cipher.ENCRYPT_MODE, DESkey);
		return ecipher.doFinal(msg);
	}
	
	public static byte[] decryptKey(byte[] key, byte[] msg) throws Exception {
		Cipher ecipher = Cipher.getInstance("DES");
		SecretKey DESkey = new SecretKeySpec(key, 0, key.length, "DES"); 
		ecipher.init(Cipher.DECRYPT_MODE, DESkey);
		return ecipher.doFinal(msg);
	}

	public String encrypt(String str) throws Exception {
	    // Encode the string into bytes using utf-8
	    byte[] utf8 = str.getBytes("UTF8");
	    // Encrypt
	    byte[] enc = ecipher.doFinal(utf8);
	    // Encode bytes to base64 to get a string
	    return new sun.misc.BASE64Encoder().encode(enc);
	  }
	
	public String decrypt(String str) throws Exception {
	    // Decode base64 to get bytes
	    byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str);
	    byte[] utf8 = dcipher.doFinal(dec);
	    // Decode using utf-8
	    return new String(utf8, "UTF8");
	  }
	
	public static void main(String[] args) {
		// Ask the user to enter the message "No body can see me"
		String msg = "No body can see me";
		Scanner reader = new Scanner(System.in);  // Reading from System.in
		System.out.println("Enter the following sentence: "+msg);
		String userInput = reader.nextLine(); 
		reader.close(); 
		// Generate a DES key
		SecretKey key;
		try {
			key = KeyGenerator.getInstance("DES").generateKey();
			System.out.println(new String(key.getEncoded()));
			// Encode the message using DES.
			JEncrypDES encrypter = new JEncrypDES(key);
		    String encrypted = encrypter.encrypt(userInput);
			// Print the encoded text.
		    System.out.println(encrypted);
			// Decode the message using DES.
		    String decrypted = encrypter.decrypt(encrypted);
			// Print the result of decoding the encoded text (which should be the original text).
		    System.out.println(decrypted);
		} catch (Exception e) {
			e.printStackTrace();
		}
	    
	    
		
	}
}
