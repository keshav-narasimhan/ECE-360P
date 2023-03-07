// kn9558
// ai6358

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPConnector extends Thread {
	private ServerSocket socketTCP;
	private Library library;

	TCPConnector(int port, Library library) {
		this.library = library;

		try { this.socketTCP = new ServerSocket(port); } 
		catch (IOException e) { e.printStackTrace(); }
	}

	@Override
	public void run() {
		while (true) {
			try {
				Socket clientSocket = socketTCP.accept();
				new TCPChild(library, clientSocket).start();
			} catch (IOException e) { e.printStackTrace(); }
		}
	}
}