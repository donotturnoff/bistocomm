import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import java.util.List;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class IssueServerCommandWorker extends SwingWorker<String, String[]> {
	
	private ChatWindow main;
	private IssueServerCommandWindow window;
	
	public IssueServerCommandWorker(ChatWindow main, IssueServerCommandWindow window) {
		this.main = main;
		this.window = window;
	}
	
	@Override
	protected String doInBackground() throws UnknownHostException, IOException, SecurityException, IllegalArgumentException {
		String host = main.getHost();
		int port = main.getPort();
		String command = window.getInput();
		publish(new String[]{"", "", "Executing command"});
		TransferAgent t = new TransferAgent(host, port);
		publish(new String[]{"", "", command.split(" ")[0]});
		String response = t.transfer(command);
		return t.extractData(response);
	}
	
	@Override
	protected void process(List<String[]> statusUpdates) {
		main.setStatus(statusUpdates.get(statusUpdates.size() - 1));
	}
	
	@Override
	protected void done() {
		try {
			String output = get();
			window.setOutput(output);
		} catch (ExecutionException e) {
			main.setActionStatus("Execution failed: " + e.getMessage());
			JOptionPane.showMessageDialog(main, "Execution failed: " + e.getMessage(), "Execution failed", JOptionPane.ERROR_MESSAGE);
		} catch (InterruptedException e) {
			main.setActionStatus("Execution interrupted");
			JOptionPane.showMessageDialog(main, "Execution interrupted", "Execution interrupted", JOptionPane.ERROR_MESSAGE);
		}
	}
}
