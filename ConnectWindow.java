import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JSpinner;
import javax.swing.JButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

public class ConnectWindow extends JFrame implements ActionListener, KeyListener {
	
	private ChatWindow main;
	private JPanel connectPanel;
	private JLabel hostLabel, portLabel;
	private JTextField hostInput;
	private SpinnerNumberModel portModel;
	private JSpinner portInput;
	private JButton connectButton;
	
	public ConnectWindow(ChatWindow main) {
		super("Connect");
		setSize(400, 120);
		setResizable(false);
		this.main = main;
        createGUI();
	}
	
	private void createGUI() {
		connectPanel = new JPanel(new GridLayout(3, 2));
		hostLabel = new JLabel("Host");
		portLabel = new JLabel("Port");
		hostInput = new JTextField();
		portModel = new SpinnerNumberModel(5001, 1, 65535, 1);
		portInput = new JSpinner(portModel);
		connectButton = new JButton("Connect");
        
		connectPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
		hostInput.addActionListener(this);
		((JSpinner.DefaultEditor) portInput.getEditor()).getTextField().addKeyListener(this);
		connectButton.addActionListener(this);
		
		connectPanel.add(hostLabel);
		connectPanel.add(hostInput);
		connectPanel.add(portLabel);
		connectPanel.add(portInput);
		connectPanel.add(new JPanel());
		connectPanel.add(connectButton);
		
		add(connectPanel);
	}
	
	public void actionPerformed(ActionEvent e) {
		connect();
	}
	
	public void keyPressed(KeyEvent e) {}
	
	public void keyTyped(KeyEvent e) {}
	
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			connect();
		}
	}
	
	private void connect() {
		String host = hostInput.getText();
		int port = ((Integer) portInput.getValue()).intValue();
		
		if (main.isConnected()) {
			String oldHost = main.getHost();
			int choice = JOptionPane.showConfirmDialog(this, "Do you wish to disconnect from " + oldHost + " and connect to " + host + "?", "Already connected", JOptionPane.YES_NO_OPTION);
			if (choice == 0) {
				new ConnectWorker(main, this, host, port).execute();
			}
		} else {
			new ConnectWorker(main, this, host, port).execute();
		}
	}
}
