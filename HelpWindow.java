import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Dimension;

public class HelpWindow extends JFrame {
	
	public HelpWindow() {
		super("Help");
		setMinimumSize(new Dimension(400, 400));
		setSize(800, 800);
		add(new JLabel("Help coming soon."));
	}
}
