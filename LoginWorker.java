import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import java.util.List;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class LoginWorker extends SwingWorker<String, String[]> {
	
	private ChatWindow main;
	private LoginWindow window;
	private String username;
	private String password;
	
	public LoginWorker(ChatWindow main, LoginWindow window, String username, String password) {
		this.main = main;
		this.window = window;
		this.username = username;
		this.password = password;
	}
	
	@Override
	protected String doInBackground() throws UnknownHostException, IOException, SecurityException, IllegalArgumentException {
		String host = main.getHost();
		int port = main.getPort();
		TransferAgent t = new TransferAgent(host, port);
		publish(new String[]{"", "", "Authenticating"});
		String authResponse = t.transfer("AUTH " + username + " " + password);
		int status = t.extractStatus(authResponse);
		String message = t.extractData(authResponse);
		if (status == 200) {
			return message;
		} else if (status == 401) {
			throw new SecurityException(message); //To do: Use more appropriate exception
		} else {
			throw new IOException(message);
		}
	}
	
	@Override
	protected void process(List<String[]> statusUpdates) {
		main.setStatus(statusUpdates.get(statusUpdates.size() - 1));
	}
	
	@Override
	protected void done() {
		try {
			String status = get();
			main.login(username, password, status);
			if (window != null) {
				window.setVisible(false);
			}
			new DirectoryListWorker(main).execute();
			new MessageListWorker(main, main.getRecipient()).execute();
		} catch (ExecutionException e) {
			main.setActionStatus("Authentication failed: " + e.getMessage());
			JOptionPane.showMessageDialog(main, "Authentication failed: " + e.getMessage(), "Authentication failed", JOptionPane.ERROR_MESSAGE);
		} catch (InterruptedException e) {
			main.setActionStatus("Authentication interrupted");
			JOptionPane.showMessageDialog(main, "Authentication interrupted", "Authentication interrupted", JOptionPane.ERROR_MESSAGE);
		}
	}
}
