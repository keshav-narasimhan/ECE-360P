// kn9558
// ai6358

import java.util.Scanner;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class BookClient {
    public static void main(String[] args) {
        String hostAddress;
        int tcpPort;
        int udpPort;
        int clientId;

        if (args.length != 2) {
            System.out.println("ERROR: Provide 2 arguments: command-file, clientId");
            System.out.println("\t(1) command-file: file with commands to the server");
            System.out.println("\t(2) clientId: an integer between 1..9");
            System.exit(-1);
        }

        String commandFile = args[0] + ".txt";
        clientId = Integer.parseInt(args[1]);
        String outputFileName = "out_" + clientId + ".txt";
        hostAddress = "localhost";
        tcpPort = 7000;// hardcoded -- must match the server's tcp port
        udpPort = 8000;// hardcoded -- must match the server's udp port
        
        boolean isUDP = true;
        Scanner sc;
        InetAddress inetAddress;
        DatagramSocket socketUDP;
        Socket socketTCP;
        PrintWriter writer;
        BufferedReader reader;
        File outputFile;
        FileOutputStream fos;

        try {
            sc = new Scanner(new FileReader(commandFile));
            inetAddress = InetAddress.getByName(hostAddress);
            socketUDP = new DatagramSocket();
            socketTCP = new Socket(inetAddress, tcpPort);
            writer = new PrintWriter(socketTCP.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socketTCP.getInputStream()));
            outputFile = new File(outputFileName);
    		fos = new FileOutputStream(outputFile);

            while (sc.hasNextLine()) {
                String cmd = sc.nextLine();
                String[] tokens = cmd.split(" ");
                String userCommand = tokens[0];
                
//                System.out.println("isUDP: " + isUDP);
//                System.out.println("Command: " + cmd);

                if (userCommand.equals("set-mode")) {
                    // TODO: set the mode of communication for sending commands to the server
                	if (tokens[1].equals("t")) { isUDP = false; } 
                	else { isUDP = true; }
                } 

                if (userCommand.equals("set-mode") || userCommand.equals("begin-loan") || userCommand.equals("end-loan") || userCommand.equals("get-loans") || userCommand.equals("get-inventory") || userCommand.equals("exit")) {
                	if (isUDP) { communicateUDP(socketUDP, cmd, udpPort, inetAddress, fos); }
                	else { communicateTCP(socketTCP, cmd, writer, reader, fos); }
                } else {
                    System.out.println("ERROR: No such command");
                }
            }
            
            fos.close();
            socketUDP.close();
            socketTCP.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void communicateUDP(DatagramSocket socketUDP, String msg, int udpPort, InetAddress inetAddress, FileOutputStream fos) {
//    	System.out.println("Reached UDP");
    	DatagramPacket spacket, rpacket;
    	byte[] sbuf, rbuf;
 
    	try {
    		sbuf = msg.getBytes();
        	spacket = new DatagramPacket(sbuf, sbuf.length, inetAddress, udpPort);
			socketUDP.send(spacket);
			
			rbuf = new byte[4096];
			rpacket = new DatagramPacket(rbuf, rbuf.length);
			socketUDP.receive(rpacket);
			
			String output = new String(rpacket.getData(), 0, rpacket.getLength());
			output += "\n";
			fos.write(output.getBytes());
			System.out.print(output);
		} catch (IOException e) { e.printStackTrace(); }
    	
    }
    
    private static void communicateTCP(Socket socketTCP, String msg, PrintWriter writer,  BufferedReader reader, FileOutputStream fos) {
//    	System.out.println("Reached TCP");
    	writer.println(msg);
    	writer.flush();
    	
    	try {
    		String line;
			while ((line = reader.readLine()) != null) {
				if (line.equals("FINISHED")) { break; }
				line += "\n";
				fos.write(line.getBytes());
				System.out.print(line);
			}
		} catch (IOException e) { e.printStackTrace(); }
    }
}