package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import encryption.JEncrypDES;
import encryption.JEncrypRSA;
import session.Session;

public class Server extends Thread{
	private ServerSocket MyService;
	private Socket clientSocket = null;
	private DataInputStream input;
	private DataOutputStream output;
	public static final int PortNumber = 1333;
	
	private PrivateKey privateKey;
	public PublicKey publicKey;
	public PublicKey clientPublicKey;
	private SecretKey sessionKey;
	private JEncrypDES encrypter;
	private Session session;
	
	public Server() {
		try {
			KeyPair keyPair = JEncrypRSA.buildKeyPair();
			publicKey = keyPair.getPublic();
		    privateKey = keyPair.getPrivate();
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
				if (clientSocket != null)
					clientSocket.close();
				if (MyService != null)
					MyService.close();
				System.exit(0);
		    } 
		    catch (IOException e) {
		       System.out.println(e);
		    }
	}
	
	public void connect() {
		try {
			MyService = new ServerSocket(PortNumber);
			clientSocket = MyService.accept();
			input = new DataInputStream(clientSocket.getInputStream());
			output = new DataOutputStream(clientSocket.getOutputStream());
			session = new Session(clientSocket,input,output);

			
			// Public key distribution
			byte[] clientKey = session.readByte();
			clientPublicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(clientKey)); 
			byte[] serverPublicKey = publicKey.getEncoded();
			if (serverPublicKey.length > 245) {
				byte[] part1 = new byte[245];
				System.arraycopy(serverPublicKey, 0, part1, 0, part1.length); 
				byte[] encryptedServerKey = JEncrypRSA.encrypt(clientPublicKey, part1);
				session.sendBytes(encryptedServerKey);
				byte[] part2 = new byte[serverPublicKey.length - part1.length];
				System.arraycopy(serverPublicKey, part1.length, part2, 0, part2.length);
				encryptedServerKey = JEncrypRSA.encrypt(clientPublicKey, part2);
				session.sendBytes(encryptedServerKey);
			}
			

			
			// RSA
			byte[] msg1 =  session.readByte();
			String[] msg1Decrypted = new String(JEncrypRSA.decrypt(privateKey, msg1)).split(Session.separator);
			int n1 = Integer.parseInt(msg1Decrypted[0]);
			
			Random rand = new Random(System.currentTimeMillis());
			int n2 = rand.nextInt(Integer.MAX_VALUE);
			byte [] msg2 = JEncrypRSA.encrypt(clientPublicKey, "" +n1 + Session.separator + n2); 
			session.sendBytes(msg2);
			
			byte[] msg3 =  session.readByte();
			int nonce = Integer.parseInt(new String(JEncrypRSA.decrypt(privateKey, msg3)));
			if (nonce!= n2) {
				System.out.println("Nonce 2 is incorrect");
				System.exit(-1);
			}
			
			// get session key from client
			byte[] clientKey1 = JEncrypRSA.decrypt(privateKey,session.readByte());
			byte[] clientKey2 = JEncrypRSA.decrypt(privateKey,session.readByte());
			byte[] clientKeyCombined = new byte[clientKey1.length + clientKey2.length];
			System.arraycopy(clientKey1, 0, clientKeyCombined, 0, clientKey1.length);
			System.arraycopy(clientKey2, 0, clientKeyCombined, clientKey1.length, clientKey2.length);
			byte[] key = JEncrypRSA.decrypt(clientPublicKey, clientKeyCombined);
			sessionKey = new SecretKeySpec(key, 0, key.length, "DES");  
			encrypter = new JEncrypDES(sessionKey);
			session.setEncrypter(encrypter);
		}catch (Exception e) {
		   e.printStackTrace();
		   System.exit(-1);
		}
	}
	
	public String[] read() {
		return session.read();
	}

	public void send(String text) {
		session.sendMessage(text);
	}
	
	
}
