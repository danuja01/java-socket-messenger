package chatserver;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * A simple Swing-based client for the chat server. Graphically it is a frame with a text field for entering messages and a textarea to see the whole dialog.
 *
 * The client follows the Chat Protocol which is as follows. When the server sends "SUBMITNAME" the client replies with the desired screen name. The server will keep sending "SUBMITNAME" requests as long as the client submits screen names that are already in use. When the server sends a line beginning with "NAMEACCEPTED" the client is now allowed to start sending the server arbitrary strings to be broadcast to all chatters connected to the server. When the server sends a line beginning with "MESSAGE " then all characters following this string should be displayed in its message area.
 */
public class ChatClient {

	
	BufferedReader in;
	PrintWriter out;
	JFrame frame = new JFrame("Chatter");
	JTextField textField = new JTextField(40);
	JTextArea messageArea = new JTextArea(8, 40);
	
	// TODO: Add a list box
	
	//check box for boardcasting
	JCheckBox broadcastCheckBox = new JCheckBox("Broadcast");
	
	//Jlist for display client list
	JList<String> userList = new JList<String>();
	
	//Make it scrollabale
	JScrollPane userScrollPane = new JScrollPane(userList);
	
	private String clientName;


	  /**
     * Constructs the client by laying out the GUI and registering a
     * listener with the textfield so that pressing Return in the
     * listener sends the textfield contents to the server.  Note
     * however that the textfield is initially NOT editable, and
     * only becomes editable AFTER the client receives the NAMEACCEPTED
     * message from the server.
     */


    /**
     * Constructs the client by laying out the GUI and registering a listener with the textfield so that pressing Return in the listener sends the textfield contents to the server. Note however that the textfield is initially NOT editable, and only becomes editable AFTER the client receives the NAMEACCEPTED message from the server.
     */
    public ChatClient() {

        // Layout GUI
        textField.setEditable(false);
        messageArea.setEditable(false);
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BorderLayout());
        northPanel.add(textField, BorderLayout.CENTER);
        northPanel.add(broadcastCheckBox, BorderLayout.WEST);

        //making the layout for JList and checkbox
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(1, 2));
        centerPanel.add(broadcastCheckBox);
        centerPanel.add(userScrollPane);

        frame.getContentPane().add(northPanel, BorderLayout.NORTH);
        frame.getContentPane().add(centerPanel, BorderLayout.CENTER);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.SOUTH);
        
        frame.pack();


        // TODO: You may have to edit this event handler to handle point to point messaging,
        // where one client can send a message to a specific client. You can add some header to 
        // the message to identify the recipient. You can get the receipient name from the listbox.
        textField.addActionListener(new ActionListener() {
        	 /**
             * Responds to pressing the enter key in the textfield by sending
             * the contents of the text field to the server.    Then clear
             * the text area in preparation for the next message.
             */
            public void actionPerformed(ActionEvent e) {
            	//Here we must check whether user has selected a recipent from JList
                //If selected we must send the message to specific client only
                if (broadcastCheckBox.isSelected()) {
                    out.println(textField.getText());
                } else {
                	 String selectedClient = userList.getSelectedValue().toString();
                    out.println(selectedClient + ">>" + textField.getText());
                }
                textField.setText("");
            }
        });
    }

    /**
     * Prompt for and return the address of the server.
     */
    private String getServerAddress() {
        return JOptionPane.showInputDialog(
                frame,
                "Enter IP Address of the Server:",
                "Welcome to the Chatter",
                JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Prompt for and return the desired screen name.
     */
    private String getName() {
        String inputName = JOptionPane.showInputDialog(
                frame,
                "Choose a screen name:",
                "Screen name selection",
                JOptionPane.PLAIN_MESSAGE);
        this.setClientName(inputName);
        return inputName;
    }

    /**
     * Connects to the server then enters the processing loop.
     */
    private void run() throws IOException {

        // Make connection and initialize streams
        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 9001);
        in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Process all messages from server, according to the protocol.
        
        // TODO: You may have to extend this protocol to achieve task 9 in the lab sheet
        
        while (true) {
            String line = in.readLine();
            if (line.startsWith("SUBMITNAME")) {
                out.println(getName());
                frame.setTitle("USER : " + clientName);
            } else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);
            } else if (line.startsWith("MESSAGE")) {
                messageArea.append(line.substring(8) + "\n");
            } else if (line.startsWith("CLIENTLIST")) {
                String mixString = line.substring(10);
                String clientArray[] = mixString.split(",");
                DefaultListModel<String> model = new DefaultListModel<>();
                for (int i = 0; i < clientArray.length; i++) {
                    if (clientArray[i].equals(clientName)) {
                        continue;
                    }
                    model.addElement(clientArray[i]);
                }
                userList.setModel(model);
            }
        }

    }

    public void setClientName(String name) {
        this.clientName = name;
    }

  

    /**
     * Runs the client as an application with a closeable frame.
     */
    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}