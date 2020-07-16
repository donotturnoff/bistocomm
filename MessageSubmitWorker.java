import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import java.util.List;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MessageSubmitWorker extends SwingWorker<Void, String[]> {
	
	private ChatWindow main;
	private String messageToSend;
	
	public MessageSubmitWorker(ChatWindow main, String messageToSend) {
		this.main = main;
		this.messageToSend = messageToSend;
	}
	
	@Override
	protected Void doInBackground() throws UnknownHostException, IOException, SecurityException, IllegalArgumentException {
		String host = main.getHost();
		int port = main.getPort();
		String username = main.getUsername();
		String password = main.getPassword();
		String recipient = main.getRecipient();
		TransferAgent t = new TransferAgent(host, port);
		publish(new String[]{"", "", "Sending message"});
		String response = t.transfer("POST " + username + " " + password + " " + recipient + " " + messageToSend);
		int status = t.extractStatus(response);
		String message = t.extractData(response);
		if (status == 201) {
			return null;
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
			get();
			main.setActionStatus("Message sent");
			main.setMessage("");
			new MessageListWorker(main, main.getRecipient()).execute();
		} catch (ExecutionException e) {
			main.setActionStatus("Sending failed: " + e.getMessage());
			JOptionPane.showMessageDialog(main, "Sending failed: " + e.getMessage(), "Sending failed", JOptionPane.ERROR_MESSAGE);
		} catch (InterruptedException e) {
			main.setActionStatus("Sending interrupted");
			JOptionPane.showMessageDialog(main, "Sending interrupted", "Sending interrupted", JOptionPane.ERROR_MESSAGE);
		}
	}
}
