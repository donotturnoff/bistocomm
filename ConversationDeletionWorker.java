import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import java.util.List;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ConversationDeletionWorker extends SwingWorker<String, String[]> {
	
	private ChatWindow main;
	
	public ConversationDeletionWorker(ChatWindow main) {
		this.main = main;
	}
	
	@Override
	protected String doInBackground() throws UnknownHostException, IOException, SecurityException, IllegalArgumentException {
		String host = main.getHost();
		int port = main.getPort();
		String username = main.getUsername();
		String password = main.getPassword();
		String recipient = main.getRecipient();
		TransferAgent t = new TransferAgent(host, port);
		publish(new String[]{"", "", "Deleting conversation"});
		String response = t.transfer("DELETE " + username + " " + password + " " + recipient);
		int status = t.extractStatus(response);
		String message = t.extractData(response);
		if (status == 200) {
			return message;
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
			String recipient = main.getRecipient();
			main.setActionStatus(status);
			main.setMessage("No messages from" + recipient);
			new MessageListWorker(main, recipient).execute();
		} catch (ExecutionException e) {
			main.setActionStatus("Conversation deletion failed: " + e.getMessage());
			JOptionPane.showMessageDialog(main, "Conversation deletion failed: " + e.getMessage(), "Conversation deletion failed", JOptionPane.ERROR_MESSAGE);
		} catch (InterruptedException e) {
			main.setActionStatus("Conversation deletion interrupted");
			JOptionPane.showMessageDialog(main, "Conversation deletion interrupted", "Conversation deletion interrupted", JOptionPane.ERROR_MESSAGE);
		}
	}
}
