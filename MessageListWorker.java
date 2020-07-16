import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import java.util.List;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MessageListWorker extends SwingWorker<String, String[]> {
	
	private ChatWindow main;
	private String recipient;
	
	public MessageListWorker(ChatWindow main, String recipient) {
		this.main = main;
		this.recipient = recipient;
	}
	
	@Override
	protected String doInBackground() throws UnknownHostException, IOException, SecurityException, IllegalArgumentException {
		String host = main.getHost();
		int port = main.getPort();
		String username = main.getUsername();
		String password = main.getPassword();
		if (recipient.length() > 0) {
			TransferAgent t = new TransferAgent(host, port);
			publish(new String[]{"", "", "Retrieving message list"});
			String response = t.transfer("GET " + username + " " + password + " " + recipient);
			int status = t.extractStatus(response);
			String messageList = t.extractData(response);
			if (status == 200) {
				return messageList;
			} else {
				throw new IOException(messageList);
			}
		} else if (main.isAuthenticated()) {
			return "Select a user from the directory to view messages";
		} else {
			return "Login to view messages";
		}
	}
	
	@Override
	protected void process(List<String[]> statusUpdates) {
		main.setStatus(statusUpdates.get(statusUpdates.size() - 1));
	}
	
	@Override
	protected void done() {
		try {
			String messageListData = get();
			main.setActionStatus("Retrieved message list");
			main.setMessageListData(messageListData);
			main.viewConversation(recipient);
		} catch (ExecutionException e) {
			main.setActionStatus("Message retrieval failed: " + e.getMessage());
			JOptionPane.showMessageDialog(main, "Message retrieval failed: " + e.getMessage(), "Message retrieval failed", JOptionPane.ERROR_MESSAGE);
			main.viewConversation("");
		} catch (InterruptedException e) {
			main.setActionStatus("Message retrieval interrupted");
			JOptionPane.showMessageDialog(main, "Message retrieval interrupted", "Message retrieval interrupted", JOptionPane.ERROR_MESSAGE);
			main.viewConversation("");
		}
	}
}
