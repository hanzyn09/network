/*opening*/
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

public class Layout extends JFrame {
	// JTextField textField = new JTextField(80);
	// JTextArea messageArea = new JTextArea(16, 80);
	Container frm;
	RoomGUI gui = new RoomGUI();
	JFrame frame = new JFrame();

	public Layout() {
		setTitle("who is the MAFIA");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ImageIcon background = new ImageIcon("opening.png");

		frm = new JFrame("who is the MAFIA");

		frm.setBounds(0, 0, 1200, 900);
		ImageIcon start_btt = new ImageIcon("start_button.png");
		ImageIcon start_btt_p = new ImageIcon("start_button_pressed.png");

		JLabel imageLabel = new JLabel(background);
		imageLabel.setBounds(0, 0, 1200, 900);
		frm.add(imageLabel);

		JLabel btt_L = new JLabel();
		JButton strt_btt = new JButton(start_btt);
		strt_btt.setRolloverIcon(start_btt_p);
		strt_btt.setPressedIcon(start_btt_p);// �־ �ǰ� �� �־ ��
		strt_btt.setSize(150, 90);
		strt_btt.setBorderPainted(false);
		strt_btt.setBounds(535, 455, 150, 90);
		btt_L.add(strt_btt);
		strt_btt.addActionListener(new press_start());
		frm.add(btt_L);

		frm.setSize(1200, 900);
		frm.setVisible(true);
	}

	private class press_start implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			frm.setVisible(false);
			Thread t1 = new Thread(new RoomGUI());
			Thread t2 = new Thread(new Send_socket());
			t1.start();
			if(t2.getState() == Thread.State.NEW)
				t2.start();
			else{
				Thread t = new Thread(new Send_socket());
				t.start();
			}
		}
	}

	public static void main(String[] args) {
		Layout client = new Layout();
		client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}