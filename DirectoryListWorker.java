import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import java.util.List;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class DirectoryListWorker extends SwingWorker<String, String[]> {
	
	private ChatWindow main;
	
	public DirectoryListWorker(ChatWindow main) {
		this.main = main;
	}
	
	@Override
	protected String doInBackground() throws UnknownHostException, IOException, SecurityException, IllegalArgumentException {
		String host = main.getHost();
		int port = main.getPort();
		TransferAgent t = new TransferAgent(host, port);
		publish(new String[]{"", "", "Retrieving directory"});
		String response = t.transfer("DIR");
		int status = t.extractStatus(response);
		String directoryList = t.extractData(response);
		if (status == 200) {
			return directoryList;
		} else {
			throw new IOException(directoryList);
		}
	}
	
	@Override
	protected void process(List<String[]> statusUpdates) {
		main.setStatus(statusUpdates.get(statusUpdates.size() - 1));
	}
	
	@Override
	protected void done() {
		try {
			String[] directoryListData = get().split("\n");
			main.setActionStatus("Retrieved directory");
			main.setDirectoryListData(directoryListData);
		} catch (ExecutionException e) {
			main.setActionStatus("Directory retrieval failed: " + e.getMessage());
			JOptionPane.showMessageDialog(main, "Directory retrieval failed: " + e.getMessage(), "Directory retrieval failed", JOptionPane.ERROR_MESSAGE);
		} catch (InterruptedException e) {
			main.setActionStatus("Directory retrieval interrupted");
			JOptionPane.showMessageDialog(main, "Directory retrieval interrupted", "Directory retrieval interrupted", JOptionPane.ERROR_MESSAGE);
		}
	}
}
