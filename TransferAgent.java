import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.regex.Pattern;

public class TransferAgent {
	
	private String host;
	private int port;
	private Socket s;
	private PrintWriter out;
	private Scanner in;
	
	public TransferAgent(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	private void open() throws UnknownHostException, IOException, SecurityException, IllegalArgumentException {
		s = new Socket(host, port);
		in = new Scanner(s.getInputStream());
		out = new PrintWriter(s.getOutputStream(), true);
		in.useDelimiter(Pattern.compile("\r\n"));
	}
	
	public String transfer(String request) throws UnknownHostException, IOException, SecurityException, IllegalArgumentException {
		open();
		out.println(request + "\r\n");
		StringBuilder responseBuilder = new StringBuilder();
		while (in.hasNext()) {
			responseBuilder.append(in.next());
		}
		close();
		return responseBuilder.toString();
	}
	
	private String read(BufferedReader in, int length) throws IOException {
		StringBuilder builder = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			builder.append(in.read());
		}
		return builder.toString();
	}
	
	private void close() throws IOException {
		out.flush();
		out.close();
		in.close();
		s.close();
	}
	
	public int extractStatus(String response) {
		if (response == null) {
			return 0;
		} else {
			return Integer.parseInt(response.split(" ")[0]);
		}
	}
	
	public String extractData(String response) {
		if (response == null) {
			return "";
		} else {
			return response.substring(response.indexOf(" ") + 1);
		}
	}
}
