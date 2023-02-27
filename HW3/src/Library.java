import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class Library {
	// class variables
	private HashMap<String, Integer> inventory;
	private HashMap<String, ArrayList<String>> records;
	
	public Library(String file) {
		this.inventory = new HashMap<>();
		this.records = new HashMap<>();
		initialize_library(file);
	}
	
	public String begin_loan(String username, String bookname) {
		// TODO
		return "";
	}
	
	public String end_loan(int loan_id) {
		// TODO
		return "";
	}
	
	public String get_loans(String username) {
		// TODO
		return "";
	}
	
	public String get_inventory() {
		// TODO
		return "";
	}
	
	private void initialize_library(String file) {
		// TODO
		Scanner scanner;
		file += ".txt";
		try {
			scanner = new Scanner(new FileReader(file));
		} catch (FileNotFoundException e) {
//			e.printStackTrace();
			System.out.println("Error in opening file");
			System.exit(-1);
			return;
		}
		
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			int index = line.lastIndexOf("\"");
			String book = line.substring(0, index + 1);
			String quantity = line.substring(index + 2);
			this.inventory.put(book, Integer.parseInt(quantity));
		}
		
//		System.out.println(this.inventory.toString());
	}
}
