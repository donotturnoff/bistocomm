import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.SwingConstants;
import javax.swing.ListSelectionModel;
import javax.swing.BoxLayout;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Desktop;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

public class ChatWindow extends JFrame implements ActionListener, ListSelectionListener, DocumentListener {
	
	private JMenuBar menuBar;
	private JMenu fileMenu, serverMenu, accountMenu, conversationMenu, helpMenu;
	private JMenuItem quitMenuItem, connectMenuItem, disconnectMenuItem, viewServerInformationMenuItem, issueServerCommandMenuItem, loginMenuItem, logoutMenuItem, registerMenuItem, refreshDirectoryListMenuItem, saveConversationMenuItem, refreshConversationMenuItem, deleteConversationMenuItem, helpMenuItem, aboutMenuItem, websiteMenuItem;
	private JPanel leftPanel, buttonsPanel, directoryPanel, messagesPanel, messageInputPanel, statusPanel;
	private JButton connectButton, disconnectButton, loginButton, logoutButton, registerButton, helpButton, sendButton;
	private JScrollPane directoryListScrollPane, messageListScrollPane;
	private JList<String> directoryList;
	private JTextArea messageList;
	private JTextField messageInput;
	private JLabel serverStatusLabel, accountStatusLabel, actionStatusLabel;
	private JFileChooser conversationSaveDialog;
	
	private ConnectWindow connectWindow;
	private LoginWindow loginWindow;
	private RegisterWindow registerWindow;
	private HelpWindow helpWindow;
	private IssueServerCommandWindow issueServerCommandWindow;
	private ServerInfoWindow serverInfoWindow;
	
	private String host, username, password, recipient, splash, ping, allProperties;
	private int port;
	private String[] dir;
	
	public ChatWindow() {
		super("BistoComm Client");
		setMinimumSize(new Dimension(600, 400));
		setSize(1000, 800);
		setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		host = "";
		username = "";
		password = "";
		recipient = "";
		splash = "";
		ping = "";
        allProperties = "";
		port = 0;
		dir = new String[0];
		
		conversationSaveDialog = new JFileChooser();
		
		connectWindow = new ConnectWindow(this);
		loginWindow = new LoginWindow(this);
		registerWindow = new RegisterWindow(this);
		helpWindow = new HelpWindow();
		issueServerCommandWindow = new IssueServerCommandWindow(this);
		serverInfoWindow = new ServerInfoWindow(this);
		
		createAndShowGUI();
	}
	
	private void createAndShowGUI() {
		createAndShowMenuBar();
		createAndShowLeftPanel();
		createAndShowMessagesPanel();
		createAndShowStatusPanel();
		setVisible(true);
	}
	
	private void createAndShowMenuBar() {
		menuBar = new JMenuBar();
		
		fileMenu = new JMenu("File");
		quitMenuItem = new JMenuItem("Quit");
		quitMenuItem.addActionListener(this);
		fileMenu.add(quitMenuItem);
		menuBar.add(fileMenu);
		
		serverMenu = new JMenu("Server");
		connectMenuItem = new JMenuItem("Connect");
		disconnectMenuItem = new JMenuItem("Disconnect");
		viewServerInformationMenuItem = new JMenuItem("View server information");
		issueServerCommandMenuItem = new JMenuItem("Issue server command");
		connectMenuItem.addActionListener(this);
		disconnectMenuItem.addActionListener(this);
		viewServerInformationMenuItem.addActionListener(this);
		issueServerCommandMenuItem.addActionListener(this);
		disconnectMenuItem.setEnabled(false);
		viewServerInformationMenuItem.setEnabled(false);
		issueServerCommandMenuItem.setEnabled(false);
		serverMenu.add(connectMenuItem);
		serverMenu.add(disconnectMenuItem);
		serverMenu.addSeparator();
		serverMenu.add(viewServerInformationMenuItem);
		serverMenu.add(issueServerCommandMenuItem);
		menuBar.add(serverMenu);
		
		accountMenu = new JMenu("Account");
		loginMenuItem = new JMenuItem("Login");
		logoutMenuItem = new JMenuItem("Logout");
		registerMenuItem = new JMenuItem("Register");
		refreshDirectoryListMenuItem = new JMenuItem("Refresh directory");
		loginMenuItem.addActionListener(this);
		logoutMenuItem.addActionListener(this);
		registerMenuItem.addActionListener(this);
		refreshDirectoryListMenuItem.addActionListener(this);
		loginMenuItem.setEnabled(false);
		logoutMenuItem.setEnabled(false);
		registerMenuItem.setEnabled(false);
		refreshDirectoryListMenuItem.setEnabled(false);
		accountMenu.add(loginMenuItem);
		accountMenu.add(logoutMenuItem);
		accountMenu.add(registerMenuItem);
		accountMenu.addSeparator();
		accountMenu.add(refreshDirectoryListMenuItem);
		menuBar.add(accountMenu);
		
		conversationMenu = new JMenu("Conversation");
		saveConversationMenuItem = new JMenuItem("Save conversation");
		refreshConversationMenuItem = new JMenuItem("Refresh conversation");
		deleteConversationMenuItem = new JMenuItem("Delete conversation");
		saveConversationMenuItem.addActionListener(this);
		refreshConversationMenuItem.addActionListener(this);
		deleteConversationMenuItem.addActionListener(this);
		saveConversationMenuItem.setEnabled(false);
		refreshConversationMenuItem.setEnabled(false);
		deleteConversationMenuItem.setEnabled(false);
		conversationMenu.add(saveConversationMenuItem);
		conversationMenu.add(refreshConversationMenuItem);
		conversationMenu.addSeparator();
		conversationMenu.add(deleteConversationMenuItem);
		menuBar.add(conversationMenu);
		
		helpMenu = new JMenu("Help");
		helpMenuItem = new JMenuItem("Help");
		aboutMenuItem = new JMenuItem("About");
		websiteMenuItem = new JMenuItem("Website");
		helpMenuItem.addActionListener(this);
		aboutMenuItem.addActionListener(this);
		websiteMenuItem.addActionListener(this);
		helpMenu.add(helpMenuItem);
		helpMenu.addSeparator();
		helpMenu.add(aboutMenuItem);
		helpMenu.add(websiteMenuItem);
		menuBar.add(helpMenu);
		
		setJMenuBar(menuBar);
	}
	
	private void createAndShowLeftPanel() {
		leftPanel = new JPanel(new BorderLayout());
		leftPanel.setBorder(new EmptyBorder(10, 10, 10, 5));
		createAndShowButtonsPanel();
		createAndShowDirectoryPanel();
		add(leftPanel, BorderLayout.LINE_START);
	}
	
	private void createAndShowButtonsPanel() {
		buttonsPanel = new JPanel(new GridLayout(2, 3));
		buttonsPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
		
		JPanel connectButtonPanel = new JPanel(new BorderLayout());
		JPanel disconnectButtonPanel = new JPanel(new BorderLayout());
		JPanel loginButtonPanel = new JPanel(new BorderLayout());
		JPanel logoutButtonPanel = new JPanel(new BorderLayout());
		JPanel registerButtonPanel = new JPanel(new BorderLayout());
		JPanel helpButtonPanel = new JPanel(new BorderLayout());
		
		connectButton = new JButton("Connect");
		disconnectButton = new JButton("Disconnect");
		loginButton = new JButton("Login");
		logoutButton = new JButton("Logout");
		registerButton = new JButton("Register");
		helpButton = new JButton("Help");
		
		connectButtonPanel.setBorder(new EmptyBorder(0, 0, 5, 5));
		disconnectButtonPanel.setBorder(new EmptyBorder(5, 0, 0, 5));
		loginButtonPanel.setBorder(new EmptyBorder(0, 5, 5, 5));
		logoutButtonPanel.setBorder(new EmptyBorder(5, 5, 0, 5));
		registerButtonPanel.setBorder(new EmptyBorder(0, 5, 5, 0));
		helpButtonPanel.setBorder(new EmptyBorder(5, 5, 0, 0));
		
		connectButtonPanel.add(connectButton, BorderLayout.CENTER);
		disconnectButtonPanel.add(disconnectButton, BorderLayout.CENTER);
		loginButtonPanel.add(loginButton, BorderLayout.CENTER);
		logoutButtonPanel.add(logoutButton, BorderLayout.CENTER);
		registerButtonPanel.add(registerButton, BorderLayout.CENTER);
		helpButtonPanel.add(helpButton, BorderLayout.CENTER);
		
		connectButton.addActionListener(this);
		disconnectButton.addActionListener(this);
		loginButton.addActionListener(this);
		logoutButton.addActionListener(this);
		registerButton.addActionListener(this);
		helpButton.addActionListener(this);
		
		disconnectButton.setEnabled(false);
		loginButton.setEnabled(false);
		logoutButton.setEnabled(false);
		registerButton.setEnabled(false);
		
		buttonsPanel.add(connectButtonPanel);
		buttonsPanel.add(loginButtonPanel);
		buttonsPanel.add(registerButtonPanel);
		buttonsPanel.add(disconnectButtonPanel);
		buttonsPanel.add(logoutButtonPanel);
		buttonsPanel.add(helpButtonPanel);
		leftPanel.add(buttonsPanel, BorderLayout.PAGE_START);
	}
	
	private void createAndShowDirectoryPanel() {
		directoryPanel = new JPanel(new BorderLayout());
		directoryPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		
		directoryList = new JList<String>(new String[0]);
		directoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		directoryList.setLayoutOrientation(JList.VERTICAL);
		directoryList.setVisibleRowCount(20);
		directoryList.addListSelectionListener(this);
		directoryList.setEnabled(false);
        directoryList.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		directoryListScrollPane = new JScrollPane(directoryList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		directoryPanel.add(directoryListScrollPane, BorderLayout.CENTER);
		leftPanel.add(directoryPanel, BorderLayout.CENTER);
	}
	
	private void createAndShowMessagesPanel() {
		messagesPanel = new JPanel(new BorderLayout());
		messagesPanel.setBorder(new EmptyBorder(10, 5, 10, 10));
		
		messageList = new JTextArea();
        messageList.setBorder(new EmptyBorder(10, 10, 10, 10));
		messageList.setEditable(false);
		messageList.setLineWrap(true);
		Font monospaceFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
		messageList.setFont(monospaceFont);
		
		messageListScrollPane = new JScrollPane(messageList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		messageInputPanel = new JPanel();
		messageInputPanel.setLayout(new BoxLayout(messageInputPanel, BoxLayout.LINE_AXIS));
		
		messageInput = new JTextField();
		messageInput.addActionListener(this);
		messageInput.getDocument().addDocumentListener(this);
		
		sendButton = new JButton("Send");
		sendButton.addActionListener(this);
		sendButton.setEnabled(false);
		
		messageInputPanel.add(messageInput);
		messageInputPanel.add(sendButton);
		
		messagesPanel.add(messageListScrollPane, BorderLayout.CENTER);
		messagesPanel.add(messageInputPanel, BorderLayout.PAGE_END);
		
		setDirectoryListData(new String[]{"Connect to a server to view the directory"});
		setMessageListData("Connect to a server and login to view messages");
		
		add(messagesPanel, BorderLayout.CENTER);
	}
	
	private void createAndShowStatusPanel() {
		statusPanel = new JPanel();
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusPanel.setPreferredSize(new Dimension(getWidth(), 24));
		statusPanel.setLayout(new GridLayout(1, 3));
		
		serverStatusLabel = new JLabel("Not connected");
		accountStatusLabel = new JLabel("Not authenticated");
		actionStatusLabel = new JLabel("Idle");
		serverStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		accountStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		actionStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		statusPanel.add(serverStatusLabel);
		statusPanel.add(accountStatusLabel);
		statusPanel.add(actionStatusLabel);
		add(statusPanel, BorderLayout.PAGE_END);
	}
	
	public void actionPerformed(ActionEvent e) {
		JComponent source = (JComponent) e.getSource();
		if (source == connectButton || source == connectMenuItem) {
            connectWindow.setLocationRelativeTo(this);
			connectWindow.setVisible(true);
		} else if (source == loginButton || source == loginMenuItem) {
            loginWindow.setLocationRelativeTo(this);
			loginWindow.setVisible(true);
		} else if (source == registerButton || source == registerMenuItem) {
            registerWindow.setLocationRelativeTo(this);
			registerWindow.setVisible(true);
		} else if (source == helpButton || source == helpMenuItem) {
            helpWindow.setLocationRelativeTo(this);
			helpWindow.setVisible(true);
		} else if (source == disconnectButton || source == disconnectMenuItem) {
			disconnect();
		} else if (source == logoutButton || source == logoutMenuItem) {
			logout();
		} else if (source == sendButton || source == messageInput) {
			String message = messageInput.getText();
			if (message.length() > 0 && isConnected() && isAuthenticated() && recipient.length() > 0) {
				new MessageSubmitWorker(this, message).execute();
			}
		} else if (source == quitMenuItem) {
			System.exit(0);
		} else if (source == deleteConversationMenuItem) {
			if (isConnected()) {
				if (isAuthenticated()) {
					if (recipient.length() > 0) {
						int confirmation = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete your conversation with " + recipient + "? This will only delete the conversation from your account; " + recipient + " will still have their copy. Deletion cannot be undone.", "Confirm deletion", JOptionPane.YES_NO_OPTION);
						if (confirmation == 0) {
							new ConversationDeletionWorker(this).execute();
						} else {
							setActionStatus("Conversation deletion aborted");
						}
					} else {
						JOptionPane.showMessageDialog(this, "You must select a conversation from the directory in order to delete it", "Select a conversation first", JOptionPane.INFORMATION_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(this, "You must login and select a conversation from the directory in order to delete it", "Select a conversation first", JOptionPane.INFORMATION_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(this, "You must connect to a server, login and select a conversation from the directory in order to delete it", "Select a conversation first", JOptionPane.INFORMATION_MESSAGE);
			}
		} else if (source == issueServerCommandMenuItem) {
			if (isConnected()) {
                issueServerCommandWindow.setLocationRelativeTo(this);
				issueServerCommandWindow.setVisible(true);
			} else {
				JOptionPane.showMessageDialog(this, "You must connect to a server in order to communicate with it", "Connect first", JOptionPane.INFORMATION_MESSAGE);
			}
		} else if (source == aboutMenuItem) {
			JOptionPane.showMessageDialog(this, "BistoComm is a simple client-server chat system.\nhttp://donotturnoff.net/projects/bistocomm", "About", JOptionPane.INFORMATION_MESSAGE);
		} else if (source == refreshConversationMenuItem) {
			new MessageListWorker(this, recipient).execute();
		} else if (source == refreshDirectoryListMenuItem) {
			new DirectoryListWorker(this).execute();
		} else if (source == saveConversationMenuItem) {
			if (isConnected()) {
				if (isAuthenticated()) {
					if (recipient.length() > 0) {
                        conversationSaveDialog.setSelectedFile(new File(recipient + ".log"));
						int confirmation = conversationSaveDialog.showOpenDialog(this);
						if (confirmation == JFileChooser.APPROVE_OPTION) {
							File outputFile = conversationSaveDialog.getSelectedFile();
							try {
								FileWriter outputFileWriter = new FileWriter(outputFile);
								outputFileWriter.write(messageList.getText());
								outputFileWriter.close();
							} catch (IOException ioe) {
								JOptionPane.showMessageDialog(this, "Failed to save conversation: " + ioe.getMessage(), "IO error", JOptionPane.ERROR_MESSAGE);
								setActionStatus("IO error when saving conversation");
							}
						}
					} else {
						JOptionPane.showMessageDialog(this, "You must select a conversation from the directory in order to save it", "Select a conversation first", JOptionPane.INFORMATION_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(this, "You must login and select a conversation from the directory in order to save it", "Select a conversation first", JOptionPane.INFORMATION_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(this, "You must connect to a server, login and select a conversation from the directory in order to save it", "Select a conversation first", JOptionPane.INFORMATION_MESSAGE);
			}
		} else if (source == viewServerInformationMenuItem) {
			if (isConnected()) {
                closeConversation();
                showServerInformation();
			} else {
				JOptionPane.showMessageDialog(this, "You must connect to a server before you can view server information", "Connect first", JOptionPane.INFORMATION_MESSAGE);
			}
		} else if (source == websiteMenuItem) {
			try {
				Desktop.getDesktop().browse(new URI("http://donotturnoff.net/projects/bistocomm"));
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(this, "Failed to open default browser. The URL of BistoComm's webpage is http://donotturnoff.net/projects/bistocomm", "Failed to open browser", JOptionPane.ERROR_MESSAGE);
				setActionStatus("Failed to open browser");
			} catch (URISyntaxException urie) {
				JOptionPane.showMessageDialog(this, "Failed to open webpage. The URL of BistoComm's webpage is http://donotturnoff.net/projects/bistocomm", "Failed to open browser", JOptionPane.ERROR_MESSAGE);
				setActionStatus("Failed to open webpage");
			}
		}
	}
	
	public void valueChanged(ListSelectionEvent e) {
		JList source = (JList) e.getSource();
		
		int index = source.getSelectedIndex();
		if (index == -1) {
			viewConversation("");
		} else {
			new MessageListWorker(this, dir[index]).execute();
		}
	}
	
	public void changedUpdate(DocumentEvent e) {}
	
	public void removeUpdate(DocumentEvent e) {
		if (messageInput.getText().length() == 0 || recipient.length() == 0) {
			sendButton.setEnabled(false);
		}
	}
	
	public void insertUpdate(DocumentEvent e) {
		if (recipient.length() > 0) {
			sendButton.setEnabled(true);
		}
	}
	
	public void connect(String host, int port, String splash, String ping, String allProperties) {
		this.host = host;
		this.port = port;
		this.splash = splash;
		this.ping = ping;
		this.allProperties = allProperties;
		serverInfoWindow.createGUI();
		disconnectButton.setEnabled(true);
		disconnectMenuItem.setEnabled(true);
		viewServerInformationMenuItem.setEnabled(true);
		issueServerCommandMenuItem.setEnabled(true);
		loginButton.setEnabled(true);
		loginMenuItem.setEnabled(true);
		registerButton.setEnabled(true);
		registerMenuItem.setEnabled(true);
		refreshDirectoryListMenuItem.setEnabled(true);
		setStatus(new String[]{ping, "", "Connection succeeded"});
		logout();
	}
	
	public void login(String username, String password, String status) {
		this.username = username;
		this.password = password;
		logoutButton.setEnabled(true);
		logoutMenuItem.setEnabled(true);
		directoryList.setEnabled(true);
		setStatus(new String[]{"", status, "Authentication succeeded"});
		viewConversation("");
	}
	
	public void viewConversation(String recipient) {
		this.recipient = recipient;
		if (recipient.length() > 0) {
			if (messageInput.getText().length() > 0) {
				sendButton.setEnabled(true);
			}
			refreshConversationMenuItem.setEnabled(true);
			saveConversationMenuItem.setEnabled(true);
			deleteConversationMenuItem.setEnabled(true);
		} else {
            closeConversation();
		}
	}
    
    public void closeConversation() {
        recipient = "";
        directoryList.clearSelection();
        sendButton.setEnabled(false);
        refreshConversationMenuItem.setEnabled(false);
        saveConversationMenuItem.setEnabled(false);
        deleteConversationMenuItem.setEnabled(false);
        showServerInformation();
        appendMessageListData("\n\nSelect a user from the directory to view messages");
    }
	
	public void disconnect() {
		logout();
		host = "";
		port = 0;
		splash = "";
		ping = "";
		disconnectButton.setEnabled(false);
		disconnectMenuItem.setEnabled(false);
		viewServerInformationMenuItem.setEnabled(false);
		issueServerCommandMenuItem.setEnabled(false);
		loginButton.setEnabled(false);
		loginMenuItem.setEnabled(false);
		registerButton.setEnabled(false);
		registerMenuItem.setEnabled(false);
		refreshDirectoryListMenuItem.setEnabled(false);
		setDirectoryListData(new String[]{"Connect to a server to view the directory"});
		setMessageListData("Connect to a server and login to view messages");
		setServerStatus("Not connected");
	}
	
	public void logout() {
		viewConversation("");
		username = "";
		password = "";
        recipient = "";
		logoutButton.setEnabled(false);
		logoutMenuItem.setEnabled(false);
		directoryList.setEnabled(false);
		showServerInformation();
        appendMessageListData("\n\nLogin to view messages");
		setAccountStatus("Not authenticated");
	}
    
    private void showServerInformation() {
        setMessageListData("Connected to " + host + ":" + port + "\n\n" + splash + "\n\n" + ping + "\n\nServer properties:\n" + allProperties);
    }
	
	public void setStatus(String[] status) {
		if (status[0].length() > 0) {
			setServerStatus(status[0]);
		}
		if (status[1].length() > 0) {
			setAccountStatus(status[1]);
		}
		if (status[2].length() > 0) {
			setActionStatus(status[2]);
		}
	}
	
	public void setServerStatus(String status) {
		serverStatusLabel.setText(status);
	}
	
	public void setAccountStatus(String status) {
		accountStatusLabel.setText(status);
	}
	
	public void setActionStatus(String status) {
		actionStatusLabel.setText(status);
	}
	
	public void setDirectoryListData(String[] directoryListData) {
		dir = Arrays.copyOf(directoryListData, directoryListData.length);
        for (int i = 0; i < directoryListData.length; i++) {
            if (directoryListData[i].equals(username)) {
                directoryListData[i] = username + " (You)";
            }
        }
		directoryList.setListData(directoryListData);
	}
	
	public void setMessageListData(String messageListData) {
		messageList.setText(messageListData);
	}
    
    public void appendMessageListData(String messageListData) {
		messageList.append(messageListData);
	}
	
	public void setMessage(String message) {
		messageInput.setText(message);
	}
	
	public ConnectWindow getConnectWindow() {
		return connectWindow;
	}
	
	public String getMessage() {
		return messageInput.getText();
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getRecipient() {
		return recipient;
	}
	
	public String getSplash() {
		return splash;
	}
	
	public String getPing() {
		return ping;
	}
    
    public String getAllProperties() {
		return allProperties;
	}
	
	public boolean isConnected() {
		return host.length() > 0 && port > 0;
	}
	
	public boolean isAuthenticated() {
		return username.length() > 0 && password.length() > 0;
	}
}
