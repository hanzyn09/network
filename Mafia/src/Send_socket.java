
package mafia_test;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.*;
import java.util.*;

public class Send_socket implements Runnable {
	private static int matrixSize = 7;
	static BufferedReader in;
	static PrintWriter out;
	JFrame frame = new JFrame();
	JPanel panel = new JPanel();
	JTextField textField = new JTextField(20);
	JTextArea messageArea = new JTextArea(4, 40);
	public String[] vote_name = new String[7];

	public Send_socket() {
		messageArea.setEditable(false);
		textField.setEditable(false);
		RoomGUI.frame.getContentPane().add(textField, "South");
		RoomGUI.frame.getContentPane().add(new JScrollPane(messageArea), "East");
		RoomGUI.frame.setVisible(true);
		textField.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				out.println(textField.getText());
				textField.setText("");
			}
		});
	}

	private String getServerAddress() {
		return JOptionPane.showInputDialog(frame, "Enter IP Address of the Server:", "Who is the mafia",
				JOptionPane.PLAIN_MESSAGE);
	}

	/* ���ӿ��� ����� �̸��� �Է¹��� */
	private String getsName() {
		return JOptionPane.showInputDialog(frame, "Choose a User's nikname:", "Who is the mafia",
				JOptionPane.PLAIN_MESSAGE);
	}

	/* �Ʒ� run �Լ��� int page�� ����ȭ�鿡�� ������ �� �� ������ �г����� �ް� �;� ���� �����Դϴ�. */
	void runChat(String[] players, int page) throws IOException {
		// Make connection and initialize streams

		String serverAddress = new String(getServerAddress());
		JFrame actionFrame = new JFrame();
		Socket socket = new Socket(serverAddress, 9001);
		int count = 0;

		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
		/* �Ʒ� while���� ���� �� �������ݿ��� KICKED���� ������ GUI�� ������ ���� �� �Ǵ� ������ �ֽ��ϴ�.. */

		while (true) {
			String line = in.readLine();
			if (line.startsWith("SUBMITNAME")) {
				out.println(getsName());
			} else if (line.startsWith("NAMEACCEPTED")) {
				textField.setEditable(true);
			} else if (line.startsWith("MESSAGE")) {
				messageArea.append(line.substring(8) + "\n");
			}
			// if(������ 5���� �Ǿ��ٰ� �˷��ָ�)

			// out.println("vote"+vote(players)); //-> players�� �����̸��� ��� ��Ʈ�� �迭
			else if (line.startsWith("JOB")) {
				line = line.substring(3);
				String selected = police(line);
				System.out.println("police" + selected);
				out.println("/is_he_mafia?" + selected);
			} else if (line.startsWith("IS_MAFIA?")) {
				messageArea.append(line.substring(9) + "\n");
				out.println("/kill");
			} else if (line.startsWith("NON")) {
				out.println("/kill");
			} else if (line.startsWith("VOTENAME ")) {// �׽�Ʈ�� ���� ���ư��� �κ�
				line = line.substring(9);
				String victim = vote(line);
				System.out.println(victim);
				out.println("/victim" + victim);
			} else if (line.startsWith("KILL")) {
				line = line.substring(4) + ",�ƹ��� �������� ����";
				String victim = mafia(line);
				System.out.println(victim);
				out.println("/dead" + victim);
			} else if (line.startsWith("DEAD")) {
				messageArea.append(line.substring(4) + "\n");
			} else if (line.startsWith("DOCTOR")) {
				line = line.substring(6);
				String protect = doctor(line);
				System.out.println(protect);
				out.println("/protect" + protect);
			} else if (line.startsWith("D_START")) {
				textField.setVisible(true);
				messageArea.append("\t\t[SYSTEM MESSAGE]" + "\n");
				messageArea.append("    \t\t���� �Ǿ����ϴ�" + "\n");
				messageArea.append(line.substring(7) + "\n");
			} else if (line.startsWith("T_START")) {
				if (line.indexOf("all object selected") != -1) {
					messageArea.append("\t\t[SYSTEM MESSAGE]" + "\n");
					messageArea.append("          \t����ڵ��� ��� ������Ʈ�� Ŭ�� �Ͽ����ϴ�" + "\n");
					messageArea.append("           \t5�е��� ����� �ؼ� ���ǾƸ� ã�Ƴ�����" + "\n");
				} else {
					messageArea.append("\t\t[SYSTEM MESSAGE]" + "\n");
					messageArea.append("          \t����ڰ� ��� ������Ʈ�� Ŭ�� �Ͽ����ϴ�" + "\n");
					messageArea.append("           \t5�е��� ����� �ؼ� ���ǾƸ� ã�Ƴ�����" + "\n");
				}
				Thread t3 = new Thread(new Timer_Start());
				t3.start();
				// JOptionPane.showMessageDialog(actionFrame, line.substring(8),
				// "Message", 2);
			} else if (line.startsWith("V_END")) {
				messageArea.append(line.substring(5) + "\n\n");
				messageArea.append("\t\t[SYSTEM MESSAGE]" + "\n");
				messageArea.append("    \t\t���� �Ǿ����ϴ�" + "\n");
				messageArea.append("          \t������ ������ �˰� ���� ����� �������ּ���" + "\n");
				messageArea.append("             \t���Ǿƴ� ���̰� ���� ����� �������ּ���" + "\n");
				messageArea.append("               \t�ǻ�� �츮�� ���� ����� �������ּ���" + "\n");
				textField.setVisible(false);
				out.println("/police");
			} else if (line.startsWith("object_description")) {
				line = line.substring(18);
				System.out.println(line);
				if (line.startsWith("room1,")) {
					String[] divide = line.split(",");
					for (int i = 0; i < divide.length; i++) {
						line += divide[i] + "\n";
					}
				} else if (line.startsWith("room2,")) {
					String[] divide = line.split(",");
					for (int i = 0; i < divide.length; i++) {
						line += divide[i] + "\n";
					}
				} else if (line.startsWith("foot size,")) {
					String[] divide = line.split(",");
					for (int i = 0; i < divide.length; i++) {
						line += divide[i] + "\n";
					}
				} else if (line.startsWith("mafia foot size,")) {
					String[] divide = line.split(",");
					for (int i = 0; i < divide.length; i++) {
						line += divide[i] + "\n";
					}
				} 
				JOptionPane.showMessageDialog(actionFrame, line, "CLUE", JOptionPane.PLAIN_MESSAGE);
			} else if (line.startsWith("FOUND")) {
				String first = line.substring(5, line.indexOf(","));
				line = line.substring(line.indexOf(",") + 1);
				String last = line;
				if (last.equals("everyone_select")) {
					messageArea.append("\t\t[SYSTEM MESSAGE]" + "\n");
					messageArea.append("\t" + first + "�� �޼����� �о����ϴ�" + "\n");
				} else {
					messageArea.append("\t\t[SYSTEM MESSAGE]" + "\n");
					messageArea.append("\t" + first + "�� �޼����� �о����ϴ�" + "\n");
					messageArea.append("\t" + last + "�� �ܼ��� ã�� �����Դϴ�" + "\n");
				}
			} else if (line.startsWith("CLUEFINDER")) {
				String first = line.substring(10, line.indexOf(","));
				line = line.substring(line.indexOf(",") + 1);
				String middle = line.substring(0, line.indexOf(","));
				line = line.substring(line.indexOf(",") + 1);
				String last = line;
				messageArea.append("\t\t[SYSTEM MESSAGE]" + "\n");
				messageArea.append("\t" + first + "�� " + middle + "�� " + last + "�� ���� �Ǿ����ϴ�.\n");
				messageArea.append("\t\t" + first + "�� �ܼ��� ã�� �����Դϴ�\n");
			}

			else if (line.startsWith("SHOW_JOB")) {
				line = line.substring(8);
				line = line.substring(0, line.indexOf(" ")) + "\n" + line.substring(line.indexOf(" ") + 1);
				JOptionPane.showMessageDialog(actionFrame, line, "Job", JOptionPane.PLAIN_MESSAGE);
			}

			else if (line.startsWith("SHOW_STORY")) {
				line = line.substring(10);
				String[] selections = line.split(",");
				String total = "";
				String[] divide = selections[0].split("/");
				selections[0] = divide[0] + "\n" + divide[1] + "\n" + divide[2];

				for (int i = 0; i < selections.length; i++) {
					if (i % 2 == 0)
						total += selections[i] + "\n\n";
					else
						total += selections[i] + "\n";
				}
				JOptionPane.showMessageDialog(actionFrame, total, "Story", JOptionPane.PLAIN_MESSAGE);
			}

			else if (line.startsWith("KICKED")) {
				textField.setVisible(false);
			}
			messageArea.setCaretPosition(messageArea.getDocument().getLength());
		}

	}

	public String vote(String line) { // �׽�Ʈ��
		String candidate = null;
		String[] selections = line.split(",");
		for (int i = 0; i < selections.length; i++)
			System.out.println(selections[i]);// ��ǥ�� ���� �����̸��� ��Ƴ���. �������� �޾ƿ;� ��.

		candidate = (String) JOptionPane.showInputDialog(null, "������ ó�� �Ͻðڽ��ϱ�?", "VOTE", JOptionPane.QUESTION_MESSAGE,
				null, selections, "user1");
		// null���� �� �˾��� ��� pane�� �̸��� ���´�.
		return candidate; // ->�������� candidate�� ������.
	}

	public String mafia(String line) { // �׽�Ʈ��
		String candidate = null;
		String[] selections = line.split(",");
		for (int i = 0; i < selections.length; i++)
			System.out.println(selections[i]);// ��ǥ�� ���� �����̸��� ��Ƴ���. �������� �޾ƿ;� ��.

		candidate = (String) JOptionPane.showInputDialog(null, "������ ���̽ðڽ��ϱ�?", "MAFIA", JOptionPane.QUESTION_MESSAGE,
				null, selections, "user1");
		// null���� �� �˾��� ��� pane�� �̸��� ���´�.
		return candidate; // ->�������� candidate�� ������.
	}

	public String police(String line) { // �׽�Ʈ��
		String selected = null;
		String[] selections = line.split(",");
		for (int i = 0; i < selections.length; i++)
			System.out.println(selections[i]);// ��ǥ�� ���� �����̸��� ��Ƴ���. �������� �޾ƿ;� ��.

		selected = (String) JOptionPane.showInputDialog(null, "������ ������ �ñ��ϽŰ���?", "POLICE", JOptionPane.QUESTION_MESSAGE,
				null, selections, "user1");
		// null���� �� �˾��� ��� pane�� �̸��� ���´�.
		return selected;
	}

	public String doctor(String line) {
		String protect = null;
		String[] selections = line.split(",");
		for (int i = 0; i < selections.length; i++)
			System.out.println(selections[i]);// ��ǥ�� ���� �����̸��� ��Ƴ���. �������� �޾ƿ;� ��.

		protect = (String) JOptionPane.showInputDialog(null, "������ ��Ű�� �ǰ���?", "DOCTOR", JOptionPane.QUESTION_MESSAGE,
				null, selections, "user1");
		// null���� �� �˾��� ��� pane�� �̸��� ���´�.
		return protect;
	}

	public void run() {
		try {
			this.runChat(null, 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}
}