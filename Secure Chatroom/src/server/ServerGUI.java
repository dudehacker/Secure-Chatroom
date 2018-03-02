package server;

import java.awt.EventQueue;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;


import java.awt.event.ActionEvent;
import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class ServerGUI {

	private JFrame frame;
	private JTextField textField;
	private JTextArea tf_Display;
	private Server server;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerGUI window = new ServerGUI();
					window.frame.setVisible(true);
					SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
						   @Override
						   protected Void doInBackground() throws Exception {
							   String[] msg;
							   window.server.connect();
							   window.log("Server started at " + Server.PortNumber);
							   window.log("Client is connected");
							   while(true) {
								   msg = window.server.read();
								   if (msg == null) break;
								   window.log("Encrypted: " + msg[0]);
								   window.log("Client: " + msg[1]);
							   }
						    return null;
						   }
						  };

					worker.execute();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ServerGUI() {
		server = new Server();
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings("serial")
	private void initialize() {
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				server.close();
			}
		});
		frame.setTitle("Chat Server");
		frame.setMinimumSize(new Dimension(800, 400));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout());
		JPanel bottom = new JPanel();
		bottom.setLayout(new BoxLayout(bottom,BoxLayout.X_AXIS));
		tf_Display = new JTextArea();
		tf_Display.setBackground(Color.WHITE);
		tf_Display.setEditable(false);
		tf_Display.setBounds(10, 23, 400, 229);
		JScrollPane scrollPane = new JScrollPane(tf_Display);
		scrollPane.setBounds(tf_Display.getBounds());
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		panel.add(scrollPane, BorderLayout.CENTER);
		tf_Display.setColumns(10);
		
		textField = new JTextField();
		textField.setToolTipText("Press Enter to send");


		textField.setPreferredSize(new Dimension(400,30));

		JButton btnSend = new JButton();
		String key = "Send";
		Action buttonAction = new AbstractAction(key) {
		    @Override
		    public void actionPerformed(ActionEvent evt) {
		        sendText();
		    }
		};
		btnSend.setAction(buttonAction);
		btnSend.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), key);
		btnSend.getActionMap().put(key, buttonAction);
		btnSend.setBounds(545, 273, 63, 23);
		bottom.add(Box.createRigidArea(new Dimension(20,5)));
		bottom.add(Box.createRigidArea(new Dimension(20,5)));
		bottom.add(textField, BorderLayout.CENTER);
		bottom.add(Box.createRigidArea(new Dimension(20,5)));
		bottom.add(btnSend, BorderLayout.EAST);
		bottom.add(Box.createRigidArea(new Dimension(20,5)));

		panel.add(bottom,BorderLayout.SOUTH);
	}
	
	public void sendText() {
		// add text to the display
		log("Server: " + textField.getText());
		
		// send msg to server
		if (server != null)
			server.send(textField.getText());
		
		// clear the input textfield
		textField.setText("");
	}
	
	private void log(String text) {
		tf_Display.setText(tf_Display.getText() + text + "\n");
	}
	
}


