import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import java.util.List;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class RegisterWorker extends SwingWorker<String, String[]> {
	
	private ChatWindow main;
	private RegisterWindow window;
	private String username;
	private String password;
	
	public RegisterWorker(ChatWindow main, RegisterWindow window, String username, String password) {
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
		publish(new String[]{"", "", "Registering"});
		String authResponse = t.transfer("REG " + username + " " + password);
		int status = t.extractStatus(authResponse);
		String registrationResult = t.extractData(authResponse);
		if (status == 201) {
			return registrationResult;
		} else if (status == 403) {
			throw new SecurityException(registrationResult); //To do: Use more appropriate exception - define custom?
		} else {
			throw new IOException(registrationResult);
		}
	}
	
	@Override
	protected void process(List<String[]> statusUpdates) {
		main.setStatus(statusUpdates.get(statusUpdates.size() - 1));
	}
	
	@Override
	protected void done() {
		try {
			String registrationResult = get();
			main.setStatus(new String[]{"", registrationResult, "Registration succeeded"});
			if (window != null) {
				window.setVisible(false);
			}
			new LoginWorker(main, null, username, password).execute();
		} catch (ExecutionException e) {
			main.setActionStatus("Registration failed: " + e.getMessage());
			JOptionPane.showMessageDialog(main, "Registration failed: " + e.getMessage(), "Registration failed", JOptionPane.ERROR_MESSAGE);
		} catch (InterruptedException e) {
			main.setActionStatus("Registration interrupted");
			JOptionPane.showMessageDialog(main, "Registration interrupted", "Registration interrupted", JOptionPane.ERROR_MESSAGE);
		}
	}
}
