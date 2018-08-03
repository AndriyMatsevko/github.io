package client;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class GuiClient extends JFrame {

	private JTextArea incoming;
	private JTextArea list;
	private JTextField outgoing;
	private JTextField login;
	private JPasswordField password;
	private JButton loginButton;
	private JButton sendButton;
	private JLabel systemMsg;
	private JLabel clientList;
	private JLabel messages;
	private JLabel message;
	private JLabel messgLogin;
	private JLabel welcom;
	private JScrollPane qScroller;
	private JScrollPane qScrollerList;
	private BufferedReader reader;
	private PrintWriter writer;
	private Socket sock;
	private boolean log;
	private InputStreamReader streamReader;
	private Msg local;
	private GridBagConstraints constraints;
	private JComboBox comboBox;
	private String client;

	public void go() {
		setLocal();
		constraints = new GridBagConstraints();
		incoming = new JTextArea(15, 40);
		incoming.setLineWrap(true);
		incoming.setWrapStyleWord(true);
		incoming.setEditable(false);
		list = new JTextArea(15, 20);
		list.setLineWrap(true);
		list.setWrapStyleWord(true);
		list.setEditable(false);
		messages = new JLabel();
		message = new JLabel();
		clientList = new JLabel();
		messgLogin = new JLabel();
		sendButton = new JButton();
		loginButton = new JButton();
		setView();
		qScroller = new JScrollPane(incoming);
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		qScrollerList = new JScrollPane(list);
		qScrollerList.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScrollerList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		outgoing = new JTextField(20);
		login = new JTextField(11);
		password = new JPasswordField(11);
		systemMsg = new JLabel();
		welcom = new JLabel();
		systemMsg.setForeground(Color.RED);
		sendButton.addActionListener(new SendButtonListener());
		loginButton.addActionListener(new LoginButtonListener());

		setResizable(false);
		setTitle("Chat");

		viewOne();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

	}

	private void setView() {
		messages.setText(local.getMessages());
		message.setText(local.getMessage());
		clientList.setText(local.getClientList());
		messgLogin.setText(local.getMessgLogin());
		sendButton.setText(local.getSendButton());
		loginButton.setText(local.getLoginButton());
	}

	private void viewOne() {
		setSize(250, 170);
		setLayout(new GridBagLayout());
		int x, y;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridheight = 1;
		setInGrid(messgLogin, x = 0, y = 0);
		setInGrid(comboBox, 1, 0);
		setInGrid(login, 0, 1);
		setInGrid(password, 0, 2);
		setInGrid(loginButton, 0, 3);
		setInGrid(systemMsg, 0, 4);
	}

	private void viewTwo() {
		setSize(740, 400);
		remove(messgLogin);
		remove(login);
		remove(password);
		remove(loginButton);
		remove(systemMsg);
		remove(comboBox);

		setLayout(new GridBagLayout());
		int x, y;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.gridheight = 1;
		setInGrid(messages, x = 0, y = 0);
		setInGrid(clientList, 1, 0);
		setInGrid(qScroller, 0, 1);
		setInGrid(qScrollerList, 1, 1);
		setInGrid(message, 0, 2);
		setInGrid(outgoing, 0, 3);
		setInGrid(welcom, 1, 3);
		setInGrid(sendButton, 0, 4);
		setInGrid(systemMsg, 0, 5);
	}

	private void setInGrid(Component component, int x, int y) {
		constraints.gridx = x;
		constraints.gridy = y;
		add(component, constraints);
	}

	private void setLocal() {
		local = Msg.UA;

		String[] items = { "UA", "RU" };

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String item = (String) comboBox.getSelectedItem();
				local = Msg.valueOf(item);
				setView();
			}
		};
		comboBox = new JComboBox(items);
		comboBox.addActionListener(actionListener);
	}

	private void threadStart() {
		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();
	}

	private void setUpNetworking() throws UnknownHostException, IOException {
		sock = new Socket("127.0.0.1", 5000);
		InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
		reader = new BufferedReader(streamReader);
		writer = new PrintWriter(sock.getOutputStream());
		writer.println(login.getText());
		writer.flush();
		writer.println(password.getText());
		writer.flush();
		log = Boolean.parseBoolean(reader.readLine());
		System.out.println(log);
	}

	private class SendButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (outgoing.getText().equals("")) {
				systemMsg.setText(local.getEnterMsg());
			} else {
				systemMsg.setText("");
				writer.println(outgoing.getText());
				writer.flush();
				outgoing.setText("");
				outgoing.requestFocus();
			}
		}

	}

	private class LoginButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (login.getText().equals("") | password.getText().equals("")) {
				systemMsg.setText(local.getLoginfail());
			} else {
				try {
					setUpNetworking();
					systemMsg.setText("");
					client = login.getText();
					login.setText("");
					password.setText("");
					init(log);
					threadStart();
					welcom.setText(local.getClient() + client);
					setTitle("Chat:  " + client);
					incoming.append(local.getConnectToChat());
				} catch (IOException e1) {
					systemMsg.setText(local.getConnectfail());
				} catch (MyException e1) {
					systemMsg.setText(local.getLoginfail());
					try {
						sock.close();
					} catch (IOException e2) {
						e2.printStackTrace();
					}
				}
			}
		}
	}

	private class IncomingReader implements Runnable {

		@Override
		public void run() {

			String message;
			char messCh;
			try {
				while ((message = reader.readLine()) != null) {
					messCh = message.charAt(0);
					switch (messCh) {
					case '!':
						incoming.append(message.substring(1, message.length()) + "\n");
						incoming.setCaretPosition(incoming.getDocument().getLength());
						break;
					case '$':
						list.setText("");
						list.append(replaceList(message));
						break;
					}
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, local.getConnectDrop());
				System.exit(0);
			}
		}
	}

	private void init(boolean b) throws MyException {

		if (!b) {
			throw new MyException("");
		}
		viewTwo();
	}

	private String replaceList(String message) {

		String str = message.replaceAll("[\\$\\[\\]]", "");
		return str.replace(", ", "\n");
	}
}
