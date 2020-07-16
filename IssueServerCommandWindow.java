import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class IssueServerCommandWindow extends JFrame implements ActionListener {
	
	private ChatWindow main;
	private JPanel commandInputPanel;
	private JTextField commandInput;
	private JButton commandIssueButton;
	private JTextArea commandResultArea;
	
	public IssueServerCommandWindow(ChatWindow main) {
		super("Issue server command");
		setMinimumSize(new Dimension(400, 400));
		setSize(600, 800);
		this.main = main;
		createGUI();
	}
	
	private void createGUI() {
		commandInputPanel = new JPanel(new BorderLayout());
		commandInput = new JTextField();
		commandIssueButton = new JButton("Execute");
		commandResultArea = new JTextArea();
		
		commandInputPanel.setBorder(new EmptyBorder(10, 10, 5, 10));
		commandInput.addActionListener(this);
		commandIssueButton.addActionListener(this);
		commandResultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		commandResultArea.setBorder(new EmptyBorder(10, 10, 10, 10));
		commandResultArea.setEditable(false);
        commandResultArea.setLineWrap(true);
		
		commandInputPanel.add(commandInput, BorderLayout.CENTER);
		commandInputPanel.add(commandIssueButton, BorderLayout.LINE_END);
		add(commandInputPanel, BorderLayout.PAGE_START);
		add(commandResultArea, BorderLayout.CENTER);
	}
	
	public void actionPerformed(ActionEvent e) {
		new IssueServerCommandWorker(main, this).execute();
	}
	
	public void setOutput(String output) {
		commandResultArea.setText(output);
	}
	
	public String getInput() {
		return commandInput.getText();
	}
}
