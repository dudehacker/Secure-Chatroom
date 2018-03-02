package client;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import encryption.JEncrypDES;
import encryption.JEncrypRSA;
import session.Session;


public class Client {
	// Security
	private static final String ID = "INITIATOR A";
	public static final int PortNumber = 1333;
	private PrivateKey privateKey;
	public PublicKey publicKey;
	private PublicKey serverPublicKey;
	private SecretKey sessionKey;
	private JEncrypDES encrypter;

	
	// Connection
	private Socket socket;
	private DataInputStream input;
	private DataOutputStream output;
	private Session session;
	
	public Client() {
		try {
			KeyPair keyPair = JEncrypRSA.buildKeyPair();
			publicKey = keyPair.getPublic();
		    privateKey = keyPair.getPrivate();
		    sessionKey = KeyGenerator.getInstance("DES").generateKey();
		    encrypter = new JEncrypDES(sessionKey);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			if (output != null)
				output.close();
			if (input != null)
				input.close();
			if (socket != null)
				socket.close();
			System.exit(0);
	    } 
	    catch (IOException e) {
	       System.out.println(e);
	    }
	}
	
	public void connect() {
		try {

			Random rand = new Random(System.currentTimeMillis());
			int nonce = rand.nextInt(Integer.MAX_VALUE);
			int nonce2;
			socket = new Socket("localhost", PortNumber);
			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());
			session = new Session(socket,input,output,encrypter);
			// public key distribution
			session.sendBytes(publicKey.getEncoded());
			byte[] serverKey1 = JEncrypRSA.decrypt(privateKey,session.readByte());
			byte[] serverKey2 = JEncrypRSA.decrypt(privateKey,session.readByte());
			byte[] serverKey = new byte[serverKey1.length + serverKey2.length];
			System.arraycopy(serverKey1, 0, serverKey, 0, serverKey1.length);
			System.arraycopy(serverKey2, 0, serverKey, serverKey1.length, serverKey2.length);
			serverPublicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(serverKey)); 
			
			
			// RSA
			byte [] msg1 = JEncrypRSA.encrypt(serverPublicKey, "" +nonce + Session.separator + ID); 
			session.sendBytes(msg1);
			byte[] msg2 = session.readByte();
			String[] msg2Decrypted = new String(JEncrypRSA.decrypt(privateKey, msg2)).split(Session.separator);
			int n1 = Integer.parseInt(msg2Decrypted[0]);
			if (n1!=nonce) {
				System.out.println("Nonce 1 is incorrect");
				System.exit(-1);
			}
			nonce2 = Integer.parseInt(msg2Decrypted[1]);
			byte[] msg3 = JEncrypRSA.encrypt(serverPublicKey, ""+nonce2);
			session.sendBytes(msg3);
			
			byte[] temp = JEncrypRSA.encrypt(privateKey, sessionKey.getEncoded());
			if (temp.length > 245) {
				byte[] part1 = new byte[245];
				System.arraycopy(temp, 0, part1, 0, part1.length); 
				byte[] encryptedServerKey = JEncrypRSA.encrypt(serverPublicKey, part1);
				session.sendBytes(encryptedServerKey);
				byte[] part2 = new byte[temp.length - part1.length];
				System.arraycopy(temp, part1.length, part2, 0, part2.length);
				encryptedServerKey = JEncrypRSA.encrypt(serverPublicKey, part2);
				session.sendBytes(encryptedServerKey);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public String[] read() {
		return session.read();
	}

	public void send(String text) {
		session.sendMessage(text);
	}
	

    
}
