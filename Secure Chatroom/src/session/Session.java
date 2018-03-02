package session;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import javax.swing.JOptionPane;

import encryption.JEncrypDES;

public class Session {
	
	private Socket socket;
	private DataInputStream input;
	private DataOutputStream output;
	private JEncrypDES encrypter;
	private int rcounter;
	private int scounter;
	public final static String separator = "õ";

	
	public Session(Socket socket, DataInputStream input, DataOutputStream output) {
		this.socket = socket;
		this.input = input;
		this.output = output;
		rcounter = 0;
		scounter = 0;
	}
	
	public Session(Socket socket, DataInputStream input, DataOutputStream output, JEncrypDES encrypter ) {
		this.socket = socket;
		this.input = input;
		this.output = output;
		this.encrypter = encrypter;
		rcounter = 0;
		scounter = 0;
	}
	
	public void setEncrypter(JEncrypDES cypher) {
		encrypter = cypher;
	}
	
	public byte[] readByte() {
		try {
			int length = input.readInt(); 
			if(length>0) {
			    byte[] message = new byte[length];
			    input.readFully(message, 0, message.length); 
			    return message;
			}
		} catch(Exception e) {
			error(e.getMessage());
		}
		return null;
	}
	
	public void sendBytes(byte [] bytes) {
		try {
			if (socket != null && output != null) {
				output.writeInt(bytes.length);
				output.write(bytes);
			}
		}catch(Exception e) {
			error(e.getMessage());
		}
	}
	
	
	public void sendMessage(String msg) {
		try {
			if (socket != null && output != null) {
				scounter++;
				String encrypted = encrypter.encrypt(""+scounter +separator+msg);
				sendBytes(encrypted.getBytes());
			}
		}catch(Exception e) {
			error(e.getMessage());
		}
	}
	
	public String[] read() {
		String[] outputs = new String[2];
		try {
		    String encrypted = new String(readByte());
		    outputs[0] = encrypted;
			String decrypted = encrypter.decrypt(encrypted);
			String[] parts = decrypted.split(separator);
			if (Integer.parseInt(parts[0]) == rcounter+1) {
				rcounter++;
				outputs[1] = parts[1];
			} else {
				error("replay attack");
			}
		} catch (Exception e) {
			error(e.getMessage());
		}
		return outputs;
	}
	
	public void error(String msg) {
		JOptionPane.showMessageDialog(null,
			    msg,
			    "Error",
			    JOptionPane.ERROR_MESSAGE);
	}
}
