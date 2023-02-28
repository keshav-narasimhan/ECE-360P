import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class Library {
	// class variables
	private HashMap<String, Integer> inventory;
	private HashMap<String, ArrayList<Integer>> records;
	private HashMap<Integer, String> loans;
	private int loan_id;
	
	public Library(String file) {
		this.inventory = new HashMap<>();
		this.records = new HashMap<>();
		this.loans = new HashMap<>();
		this.loan_id = 1;
		initialize_library(file);
	}
	
	public synchronized String begin_loan(String username, String bookname) {
		if (!this.inventory.containsKey(bookname)) {
			return "Request Failed - We do not have this book";
		}
		
		int quantity = this.inventory.get(bookname);
		if (quantity == 0) {
			return "Request Failed - Book not available";
		}
		
		this.inventory.replace(bookname, quantity - 1);
		this.loans.put(loan_id, bookname);
		if (this.records.containsKey(username)) {
			ArrayList<Integer> list_of_loans = this.records.get(username);
			list_of_loans.add(loan_id);
		} else {
			ArrayList<Integer> list_of_loans = new ArrayList<>();
			list_of_loans.add(loan_id);
			this.records.put(username, list_of_loans);
		}
		loan_id++;
		return "Your request has been approved, " + (loan_id - 1) + " " + username + " " + bookname;
	}
	
	public synchronized String end_loan(int id) {
		if (!this.loans.containsKey(id)) {
			return id + " not found, no such borrow record";
		}
		
		String bookname = this.loans.get(id);
		int quantity = this.inventory.get(bookname);
		this.inventory.replace(bookname, quantity + 1);
		return id + " is returned";
	}
	
	public synchronized String get_loans(String username) {
		ArrayList<Integer> list_of_loans = this.records.get(username);
		if (list_of_loans == null) {
			return "No record found for " + username;
		}
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list_of_loans.size(); i++) {
			int l = list_of_loans.get(i);
			String bookname = this.loans.get(l);
			sb.append(l + " " + bookname);
			if (i < list_of_loans.size() - 1) {
				sb.append("\n");
			}
		}
		
		return sb.toString();
	}
	
	public synchronized String get_inventory() {
		StringBuilder sb = new StringBuilder();
		for (String bookname : this.inventory.keySet()) {
			int quantity = this.inventory.get(bookname);
			sb.append(bookname + " " + quantity + "\n");
		}
		
		if (this.inventory.keySet() != null) {
			sb.deleteCharAt(sb.length() - 1);
		}
		
		return sb.toString();
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
