import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class LoginWindow extends JFrame implements ActionListener {
	
	private ChatWindow main;
	private JPanel loginPanel;
	private JLabel usernameLabel, passwordLabel;
	private JTextField usernameInput;
	private JPasswordField passwordInput;
	private JButton loginButton;
	
	public LoginWindow(ChatWindow main) {
		super("Login");
		setSize(400, 120);
		setResizable(false);
		this.main = main;
		createGUI();
	}
	
	private void createGUI() {
		loginPanel = new JPanel(new GridLayout(3, 2));
		usernameLabel = new JLabel("Username");
		passwordLabel = new JLabel("Password");
		usernameInput = new JTextField();
		passwordInput = new JPasswordField();
		loginButton = new JButton("Login");
        
        loginPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		usernameInput.addActionListener(this);
		passwordInput.addActionListener(this);
		loginButton.addActionListener(this);
		
		loginPanel.add(usernameLabel);
		loginPanel.add(usernameInput);
		loginPanel.add(passwordLabel);
		loginPanel.add(passwordInput);
		loginPanel.add(new JPanel());
		loginPanel.add(loginButton);
		
		add(loginPanel);
	}
	
	public void actionPerformed(ActionEvent e) {
		String username = usernameInput.getText();
		String password = new String(passwordInput.getPassword());
		
		if (main.isConnected()) {
			if (main.isAuthenticated()) {
				String oldUsername = main.getUsername();
				int choice = JOptionPane.showConfirmDialog(this, "Do you wish to logout as " + oldUsername + " and login as " + username + "?", "Already logged in", JOptionPane.YES_NO_OPTION);
				if (choice == 0) {
					login(username, password);
				}
			} else {
				login(username, password);
			}
		} else {
			JOptionPane.showMessageDialog(this, "You are not connected yet. Connect to a server before logging in.", "Not connected", JOptionPane.ERROR_MESSAGE);
			main.getConnectWindow().setVisible(true);
		}
	}
	
	private void login(String username, String password) {
		new LoginWorker(main, this, username, password).execute();
	}
}
