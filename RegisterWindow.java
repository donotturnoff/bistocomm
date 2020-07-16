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

public class RegisterWindow extends JFrame implements ActionListener {
	
	private ChatWindow main;
	private JPanel registerPanel;
	private JLabel usernameLabel, passwordLabel, passwordConfirmLabel;
	private JTextField usernameInput;
	private JPasswordField passwordInput, passwordConfirmInput;
	private JButton registerButton;
	
	public RegisterWindow(ChatWindow main) {
		super("Register");
		setSize(400, 145);
		setResizable(false);
		this.main = main;
		createGUI();
	}
	
	private void createGUI() {
		registerPanel = new JPanel(new GridLayout(4, 2));
		usernameLabel = new JLabel("Username");
		passwordLabel = new JLabel("Password");
		passwordConfirmLabel = new JLabel("Confirm password");
		usernameInput = new JTextField();
		passwordInput = new JPasswordField();
		passwordConfirmInput = new JPasswordField();
		registerButton = new JButton("Register");
        
        registerPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		usernameInput.addActionListener(this);
		passwordInput.addActionListener(this);
		passwordConfirmInput.addActionListener(this);
		registerButton.addActionListener(this);
		
		registerPanel.add(usernameLabel);
		registerPanel.add(usernameInput);
		registerPanel.add(passwordLabel);
		registerPanel.add(passwordInput);
		registerPanel.add(passwordConfirmLabel);
		registerPanel.add(passwordConfirmInput);
		registerPanel.add(new JPanel());
		registerPanel.add(registerButton);
		
		add(registerPanel);
	}
	
	public void actionPerformed(ActionEvent e) {
		String username = usernameInput.getText();
		String password = new String(passwordInput.getPassword());
		String passwordConfirm = new String(passwordConfirmInput.getPassword());
		
		if (password.equals(passwordConfirm)) {
			if (main.isConnected()) {
				if (main.isAuthenticated()) {
					String oldUsername = main.getUsername();
					int choice = JOptionPane.showConfirmDialog(this, "Do you wish to logout as " + oldUsername + " and register as " + username + "?", "Already logged in", JOptionPane.YES_NO_OPTION);
					if (choice == 0) {
						register(username, password);
					}
				} else {
					register(username, password);
				}
			} else {
				JOptionPane.showMessageDialog(this, "You are not connected yet. Connect to a server before logging in.", "Not connected", JOptionPane.ERROR_MESSAGE);
				main.getConnectWindow().setVisible(true);
			}
		} else {
			JOptionPane.showMessageDialog(this, "Please ensure your passwords match.", "Passwords do not match", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void register(String username, String password) {
		new RegisterWorker(main, this, username, password).execute();
	}
}
