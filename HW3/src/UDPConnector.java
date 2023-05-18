// kn9558
// ai6358

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPConnector extends Thread {
	private DatagramSocket socketUDP;
	private Library library;
	private byte[] buffer;

	UDPConnector(int port, Library library) {
		this.library = library;
		this.buffer = new byte[4096];

		try { this.socketUDP = new DatagramSocket(port); } 
		catch (SocketException e) { e.printStackTrace(); }
	}

	@Override
	public void run() {
		while (true) {
			DatagramPacket rpacket;
			try {
				rpacket = new DatagramPacket(buffer, buffer.length);
				socketUDP.receive(rpacket);
				new UDPChild(library, socketUDP, rpacket).start();
			} catch (IOException e) {
				e.printStackTrace();
				socketUDP.close();
				return;
			}
		}
	}
}