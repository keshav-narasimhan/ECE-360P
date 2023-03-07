import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPChild extends Thread {
	private Library library;
	private DatagramSocket socketUDP;
	private DatagramPacket rpacket;
	private InetAddress inetAddress;
	private int port;

	UDPChild(Library library, DatagramSocket socketUDP, DatagramPacket rpacket) {
		this.library = library;
		this.socketUDP = socketUDP;
		this.rpacket = rpacket;
		this.inetAddress = rpacket.getAddress();
		this.port = rpacket.getPort();
	}

	@Override
	public void run() {
		String cmd;
		boolean close = false;

		try {
			cmd = new String(rpacket.getData(), 0, rpacket.getLength());
			String commandResult = BookServer.doLibraryCommand(library, cmd);
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
			
			byte[] sbuf = commandResult.getBytes();
			DatagramPacket spacket = new DatagramPacket(sbuf, sbuf.length, inetAddress, port);
			socketUDP.send(spacket);

			if (close) { this.socketUDP.close(); }
		} catch (IOException e) { e.printStackTrace(); }
	}
}