package accessParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;

public class Main {
	
	static HashMap<String, ArrayList<String>> map, simpleMap;
	
	public static void main(String args[]) {
		map = new HashMap<String, ArrayList<String>>();
		simpleMap = new HashMap<String, ArrayList<String>>();
		
		System.out.println("*******************************");
		System.out.println("    Access Log Parser V1.2");
		System.out.println("    ----------------------");
		System.out.println("Created By Jimmy Glasscock 2019");
		System.out.println("*******************************");
		
		parseFile(args);
	}
	
	//opens file
	//parses through each line, tallys number of occurences of each page & ip
	//outputs to a nice lil file
	public static void parseFile(String [] args) {
		String filepath = "";
		
		filepath = pathPrompt();
		Date date = datePrompt();
		
		Scanner scanner;
		try {
			scanner = new Scanner(new File(filepath));
			
			//actually parses the file
			pagesAccessedCount(scanner, date);
			
		} catch (FileNotFoundException e) {
			System.out.println("If this is triggering the programmer didn't do their job :(");
		}
		
		//export all that good stuff to a new file
		exportData();
	}
	
	public static void pagesAccessedCount(Scanner scanner, Date date) {
		String currentLine = "", ip, pageAccessed, pageAccessedSimple = null;
		
		boolean started = (date == null);
		
		ArrayList<String> ipList;
		
		String dateString = date.toString();
		
		while(scanner.hasNextLine()) {
			
			currentLine = scanner.nextLine();
			
			while(!started) {
				int dateIndex = (currentLine.indexOf('['))+1;
				
				if(currentLine.substring(dateIndex, dateIndex+2).equals(dateString.substring(8,10)) && currentLine.substring(dateIndex+3, dateIndex+6).equals(dateString.substring(4,7)) && currentLine.substring(dateIndex+7,dateIndex+11).equals(dateString.substring(dateString.length()-4))) {
					started = true;
				}else {
					currentLine = scanner.nextLine();
				}
			}
			
			//gets 6th index of space
			//int startOfURL = currentLine.indexOf(" ", currentLine.indexOf(" ", currentLine.indexOf(" ",currentLine.indexOf(" ",currentLine.indexOf(" ",currentLine.indexOf(" ")+1)+1)+1)+1)+1);
			int index = currentLine.indexOf("/", currentLine.indexOf("/", currentLine.indexOf("/")+1)+1);
			int startOfURL = -1;
			
			//last slash to help find specific folders
			int lastSlash = currentLine.substring(0, currentLine.lastIndexOf('/')).lastIndexOf('/');
			int secondLastSlash = currentLine.substring(0,lastSlash).lastIndexOf('/');
			
			if(index == -1) {
				currentLine = scanner.nextLine();
			}else {
				startOfURL = index;
			}
			
			
			//capture ip address
			ip = currentLine.substring(0, currentLine.indexOf(" "));
			
			//capture website accessed and add it to the count
			pageAccessed = currentLine.substring(startOfURL+1, currentLine.indexOf(" ", startOfURL+1));
			
			//sets simple url 
			if( startOfURL == -1 || (currentLine.substring(startOfURL+1)).contains(".php") || (currentLine.substring(startOfURL+1)).contains(".html") || currentLine.substring(startOfURL).indexOf('/') == -1 || (currentLine.substring(startOfURL+1)).contains("404") || (currentLine.substring(startOfURL+1)).contains(".asp")) {
				pageAccessedSimple = null;
			}else {
				if(onlyNumbers(currentLine.substring(startOfURL+1))){
					pageAccessedSimple = null;
				}else{
					pageAccessedSimple = currentLine.substring(secondLastSlash+1, lastSlash);
					
					//prevents parsing in weird data we don't want
					if(pageAccessedSimple.length() > 0 && pageAccessedSimple.charAt(0) == '2') {
						pageAccessedSimple = null;
					}
				}
			}
			
			if(map.get(pageAccessed) == null) {
				ipList = new ArrayList<String>();
				ipList.add(ip);
				map.put(pageAccessed, ipList);
			}else {
				ipList = map.get(pageAccessed);
				ipList.add(ip);
			}
			
			if(pageAccessedSimple != null) {
				if(simpleMap.get(pageAccessedSimple) == null) {
					ipList = new ArrayList<String>();
					ipList.add(ip);
					simpleMap.put(pageAccessedSimple, ipList);
				}else {
					ipList = simpleMap.get(pageAccessedSimple);
					ipList.add(ip);
				}
			}
		}
		
	}
	
	@SuppressWarnings("resource")
	public static String pathPrompt() {
		String result = "";
		
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("\nType file path of the access log, then press enter:");
		
		result = scanner.nextLine();
		
		//check to see if file exists
		File file = new File(result);
		
		//if not, prompt the user again
		if(file.exists()) {
			return result;
		}else {
			System.out.println("\nInvalid File Path!");
			
			return pathPrompt();
		}
	}
	
	public static Date datePrompt() {
		
		String result = null;
		
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Enter a starting date(MM/DD/YYYY), then press enter:");
		
		result = scanner.nextLine();
		
		scanner.close();
		
		if(result == "" || result == null) {
			return null;
		}
		
		DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
		Date date;
		
		try {
			date = format.parse(result);
		} catch (ParseException e) {
			System.out.println("INVALID DATE!");
			
			date = datePrompt();
		}
		
		return date;
	}

	public static void exportData() {
		PrintWriter out = null, out2 = null, out3 = null;
		
		try {
			out = new PrintWriter("parsedAccess.csv");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ArrayList<String> keys = new ArrayList<String>();
		ArrayList<String> webstrings = new ArrayList<String>();
		
		for(String key : map.keySet()) {
			keys.add(key);
		}
		
		Collections.sort(keys);
		
		for(String key : keys) {
			ArrayList<String> list = map.get(key);
			
			out.println(list.size() + "," + key);
		}
		
		out.close();
		
		System.out.println("\nDONE!");
		System.out.println("\nOutput file named parsedAccess.csv\n");
		
		
		//finds IP addresses of attackers
		try {
			out2 = new PrintWriter("listOfAttacks-IP.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ArrayList<String> attacks = map.get("1.php");
		
		if(attacks != null) {
			for(int i = 0; i < attacks.size(); i++) {
				out2.println(attacks.get(i));
			}
		}
		
		out2.close();
		
		System.out.println("\nDONE PARSING ATTACKS!");
		System.out.println("\nOutput file named listOfAttacks-IP.txt\n");
		
		try {
			out3 = new PrintWriter("parsedAccessSimple.csv");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		keys = new ArrayList<String>();
		webstrings = new ArrayList<String>();
		
		for(String key : simpleMap.keySet()) {
			keys.add(key);
		}

		
		for(String key : keys) {
			ArrayList<String> list = simpleMap.get(key);
			
			webstrings.add(list.size() + "," + key);
			//out3.println(list.size() + " " + key);
		}
		
		//Sorts webpage names by times accessed
		Collections.sort(webstrings, String.CASE_INSENSITIVE_ORDER);
		
		//populates file with data
		for(int i = 0; i < webstrings.size(); i++) {
			out3.println(webstrings.get(i));
		}
		
		out3.close();
		
		System.out.println("\nDONE!");
		System.out.println("\nOutput file named parsedAccessSimple.csv\n");
	}

	public static boolean onlyNumbers(String str) {
		char [] nums = {'0','1','2','3','4','5','6','7','8','9', '<', '>'};
		
		for(int i = 0; i < nums.length; i++) {
			if(str.charAt(0) == nums[i]) {
				return true;
			}
		}
		
		return false;
	}

}
