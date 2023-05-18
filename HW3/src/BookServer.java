// kn9558
// ai6358

public class BookServer {
    public static void main(String[] args) {
        int tcpPort;
        int udpPort;
        if (args.length != 1) {
            System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
            System.exit(-1);
        }
        String fileName = args[0];
        tcpPort = 7000;
        udpPort = 8000;

        // parse the inventory file
        Library library = new Library(fileName);

        // create listeners for UDP and TCP communication
        UDPConnector udpc = new UDPConnector(udpPort, library);
        TCPConnector tcpc = new TCPConnector(tcpPort, library);
        
        // start the threads
        udpc.start();
        tcpc.start();
    }
    
    // perform user commands against library - used by child threads for UDP/TCP
    public static String doLibraryCommand(Library library, String cmd) {
    	String[] tokens = cmd.split(" ");
    	switch (tokens[0].trim()) {
          	case "begin-loan":
          		return library.begin_loan(tokens[1].trim(), cmd.substring(cmd.indexOf("\"")));
          	case "end-loan":
          		return library.end_loan(Integer.parseInt(tokens[1].trim()));
          	case "get-loans":
          		return library.get_loans(tokens[1].trim());
          	case "get-inventory":
          		return library.get_inventory();
          	case "set-mode":
          		return tokens[1].trim().equals("t") ? "The communication mode is set to TCP" : "The communication mode is set to UDP";
          	case "exit":
          		return "exit";
          	default:
          		return null;
    	}
    }
}