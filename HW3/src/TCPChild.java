import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPChild extends Thread {
	private Library library;
	private Socket socketTCP;

	TCPChild(Library library, Socket socketTCP) {
		this.library = library;
		this.socketTCP = socketTCP;
	}

	@Override
	public void run() {
		BufferedReader reader;
		PrintWriter writer;
		boolean close = false;

		try {
			reader = new BufferedReader(new InputStreamReader(socketTCP.getInputStream()));
			writer = new PrintWriter(socketTCP.getOutputStream());

			while (true) {
				String message = reader.readLine();
				if (message == null)
					break;

				String commandResult = BookServer.doLibraryCommand(library, message);
				if (commandResult == null) {
					System.out.println("INCORRECT USER COMMAND!");
					System.exit(-1);
				}

				if (commandResult.equals("exit")) {
					String content = this.library.get_inventory();
					this.library.write_inventory(content);
					close = true;
					commandResult = "";
				}

				commandResult += "\nFINISHED";
				writer.println(commandResult);
				writer.flush();

				if(close) { this.socketTCP.close(); }
			}
		} catch (IOException e) { e.printStackTrace(); }
	}
}