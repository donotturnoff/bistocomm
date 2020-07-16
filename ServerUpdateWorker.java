import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import java.util.List;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ServerUpdateWorker extends SwingWorker<Void, String[]> {
	
	private ChatWindow main;
	private boolean connected, firstLoop;
	
	public ServerUpdateWorker(ChatWindow main) {
		this.main = main;
		connected = false;
		firstLoop = true;
	}
	
	@Override
	protected Void doInBackground() throws UnknownHostException, IOException, SecurityException, IllegalArgumentException, InterruptedException {
		while (true) {
			try {
				if (main.isConnected()) {
					String host = main.getHost();
					int port = main.getPort();
					TransferAgent t = new TransferAgent(host, port);
					publish(new String[]{"", "", "Pinging server"});
					String pingResponse = t.transfer("PING");
					int pingStatus = t.extractStatus(pingResponse);
					String pingMessage = t.extractData(pingResponse);
					publish(new String[]{pingMessage, "", ""});
					if (pingStatus == 200) {
						if (!connected && !firstLoop) {
							publish(new String[]{"", "", "Connection re-established"});
							JOptionPane.showMessageDialog(main, "Connection to server re-established", "Connection re-established", JOptionPane.INFORMATION_MESSAGE);
						}
						connected = true;
						String recipient = main.getRecipient();
						if (main.isAuthenticated() && recipient.length() > 0) {
							new MessageListWorker(main, recipient).execute();
						}
					}
					firstLoop = false;
				} else {
					firstLoop = true;
				}
			} catch (Exception e) {
				if (connected) {
					connected = false;
					publish(new String[]{"Connection lost", "", "Update loop error: " + e.getMessage()});
					int attemptReconnection = JOptionPane.showConfirmDialog(main, "Connection to server lost: " + e.getMessage() + ". Attempt reconnection?", "Connection lost", JOptionPane.YES_NO_OPTION);
					if (attemptReconnection == 1) {
						main.disconnect();
					}
				} else {
					publish(new String[]{"", "", "Update loop error: " + e.getMessage()});
				}
			} finally {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					if (connected) {
						connected = false;
						publish(new String[]{"Connection lost", "", "Update loop error: " + e.getMessage()});
						JOptionPane.showMessageDialog(main, "Connection to server interrupted: " + e.getMessage() + ". Reconnection will be attempted.", "Connection interrupted", JOptionPane.ERROR_MESSAGE);
					} else {
						publish(new String[]{"", "", "Update loop error: " + e.getMessage()});
					}
				} 
			}
		}
	}
	
	@Override
	protected void process(List<String[]> statusUpdates) {
		main.setStatus(statusUpdates.get(statusUpdates.size() - 1));
	}
}
