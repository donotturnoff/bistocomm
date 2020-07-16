import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ServerInfoWindow extends JFrame implements ActionListener {
	
	private ChatWindow main;
	private JPanel networkInfoPanel, serverGreetingInfoPanel;
	private JLabel hostLabel, hostValueLabel, portLabel, portValueLabel;
	private JTextField pingField;
	private JTextArea allPropertiesArea, splashArea;
	private JButton refreshButton;
	
	public ServerInfoWindow(ChatWindow main) {
		super("Server information");
		setMinimumSize(new Dimension(400, 400));
		setSize(600, 800);
		this.main = main;
	}
	
	public void createGUI() {
		networkInfoPanel = new JPanel(new GridLayout(3, 2));
		refreshButton = new JButton("Refresh");
		hostLabel = new JLabel("Host: ");
		hostValueLabel = new JLabel(main.getHost());
		portLabel = new JLabel("Port: ");
		portValueLabel = new JLabel(Integer.toString(main.getPort()));
		serverGreetingInfoPanel = new JPanel(new BorderLayout());
		pingField = new JTextField();
		splashArea = new JTextArea();
        allPropertiesArea = new JTextArea();
		splashArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		allPropertiesArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		
		refreshButton.addActionListener(this);
		
		pingField.setEditable(false);
		splashArea.setEditable(false);
		allPropertiesArea.setEditable(false);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				pingField.setText(main.getPing());
				splashArea.setText(main.getSplash());
                allPropertiesArea.setText(main.getAllProperties());
			}
		});
		
		networkInfoPanel.add(new JPanel());
		networkInfoPanel.add(refreshButton);
		networkInfoPanel.add(hostLabel);
		networkInfoPanel.add(hostValueLabel);
		networkInfoPanel.add(portLabel);
		networkInfoPanel.add(portValueLabel);
		
		serverGreetingInfoPanel.add(pingField, BorderLayout.PAGE_START);
		serverGreetingInfoPanel.add(splashArea, BorderLayout.CENTER);
		serverGreetingInfoPanel.add(allPropertiesArea, BorderLayout.PAGE_END);
		
		add(networkInfoPanel, BorderLayout.PAGE_START);
		add(serverGreetingInfoPanel, BorderLayout.CENTER);
	}
	
	public void actionPerformed(ActionEvent e) {
		new ConnectWorker(main, null, main.getHost(), main.getPort()).execute();
	}
}
