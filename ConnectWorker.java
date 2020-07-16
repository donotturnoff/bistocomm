import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import java.util.List;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ConnectWorker extends SwingWorker<String[], String[]> {
	
	private ChatWindow main;
	private ConnectWindow window;
	private String host;
	private int port;
	
	public ConnectWorker(ChatWindow main, ConnectWindow window, String host, int port) {
		this.main = main;
		this.window = window;
		this.host = (host.equals("")) ? "localhost" : host;
		this.port = port;
	}
	
	@Override
	protected String[] doInBackground() throws UnknownHostException, IOException, SecurityException, IllegalArgumentException {
		TransferAgent t = new TransferAgent(host, port);
		publish(new String[]{"", "", "Fetching splash"});
		String splashResponse = t.transfer("SPLASH");
		int splashStatus = t.extractStatus(splashResponse);
        String splashMessage = t.extractData(splashResponse);
		if (splashStatus == 200) {
			publish(new String[]{"", "", "Pinging server"});
			String pingResponse = t.transfer("PING");
			int pingStatus = t.extractStatus(pingResponse);
            String pingMessage = t.extractData(pingResponse);
			if (pingStatus == 200) {
                publish(new String[]{"", "", "Fetching server properties"});
                String infoResponse = t.transfer("INFO *");
                int infoStatus = t.extractStatus(infoResponse);
                String infoMessage = t.extractData(infoResponse);
                if (infoStatus == 200) {
                    return new String[]{splashMessage, pingMessage, infoMessage};
                } else {
                    throw new IOException(infoMessage);
                }
			} else {
				throw new IOException(pingMessage);
			}
		} else {
			throw new IOException(splashMessage);
		}
	}
	
	@Override
	protected void process(List<String[]> statusUpdates) {
		main.setStatus(statusUpdates.get(statusUpdates.size() - 1));
	}
	
	@Override
	protected void done() {
		try {
			String[] response = get();
			String splash = response[0];
			String ping = response[1];
			String allProperties = response[2];
			main.connect(host, port, splash, ping, allProperties);
			if (window != null) {
				window.setVisible(false);
			}
			new DirectoryListWorker(main).execute();
		} catch (ExecutionException e) {
			main.setActionStatus("Connection failed: " + e.getMessage());
			JOptionPane.showMessageDialog(main, "Connection failed: " + e.getMessage(), "Connection failed", JOptionPane.ERROR_MESSAGE);
		} catch (InterruptedException e) {
			main.setActionStatus("Connection interrupted");
			JOptionPane.showMessageDialog(main, "Connection interrupted", "Connection interrupted", JOptionPane.ERROR_MESSAGE);
		}
	}
}
