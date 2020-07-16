/* To do
 * 
 * Reduce concurrent connections
 * Help
 * 
 */ 

import javax.swing.SwingUtilities;

public class BistoComm {
	
	public static void main(String[] args) {
		BistoComm bistoComm = new BistoComm();
		bistoComm.showGUI();
	}
	
	public void showGUI() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ChatWindow chatWindow = new ChatWindow();
				new ServerUpdateWorker(chatWindow).execute();
			}
		});
	}
}
