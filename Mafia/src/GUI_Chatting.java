import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class GUI_Chatting {

    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 40);

    public GUI_Chatting() {
        // Layout GUI
        textField.setEditable(false);
        messageArea.setEditable(false);
       
        frame.getContentPane().add(textField, "North"); //ä��â�� �� ���ʿ� ��ġ
        frame.getContentPane().add(new JScrollPane(messageArea), "Center"); //��ũ���� �߾ӿ� ��ġ
        frame.getContentPane().add(new JScrollPane(messageArea), "Center"); 
        frame.pack(); //��ü ä��â�� ������

        // Add Listeners
        textField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText()); //�ؽ�Ʈ���� �Է¹��� �� ����Ʈ �ϱ�
                textField.setText("");
            }
        });
    }

/*� ������ ������ ������ �Է¹���*/
    private String getServerAddress() {
        return JOptionPane.showInputDialog(
            frame,
            "Enter IP Address of the Server:",
            "Welcome to the Chatter",
            JOptionPane.QUESTION_MESSAGE);
    }

/*���ӿ��� ����� �̸��� �Է¹���*/
    private String getName() {
        return JOptionPane.showInputDialog(
            frame,
            "Choose a screen name:",
            "Screen name selection",
            JOptionPane.PLAIN_MESSAGE);
    }

/*�������Ӱ� �̸��Է�â�� ����*/
    private void run() throws IOException {

        // Make connection and initialize streams
        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 9001);
        in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Process all messages from server, according to the protocol.
        while (true) {
            String line = in.readLine();
            if (line.startsWith("SUBMITNAME")) {
                out.println(getName());
            } else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);
            } else if (line.startsWith("MESSAGE")) {
                messageArea.append(line.substring(8) + "\n");
            }
            //if(5���� �ƴٰ� �������� �˸��� ������)
            String candidate=null;
            String[] selections={"user1","user2","user3","user4","user5","user6","user7"};//��ǥ�� ���� �����̸��� ��Ƴ���. �������� �޾ƿ;� ��.
            candidate=(String) JOptionPane.showInputDialog(null, "5���� �������ϴ�. ������ ������Ű�ڽ��ϱ�?", "vote", JOptionPane.QUESTION_MESSAGE,null,selections,"user1");
            //null���� �� �˾��� ��� pane�� �̸��� ���´�.
            //return candidate(); ->�������� candidate�� ������.
        }
       
     
    }

	public static void main(String[] args) throws Exception {
		GUI_Chatting client = new GUI_Chatting();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //������ ��ư�� ������ ����
        client.frame.setVisible(true); //ä��â�� ������
        client.run(); //��������, �̸��Է� â�� ���

	}

}
