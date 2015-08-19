package dataextraction;

import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Donations {
	
	final static String SERIALIZEDTABLEINFO = "TestDatasets/contribs_big/contributions.reduced.fec.2014.ser";
	
	final static String CONTRIBSSMALLFILELOCATION = "TestDatasets/contribs_big/contributions.reduced.fec.2014.csv";
	final static String CONTRIBSBIGFILELOCATION = "TestDatasets/contribs_big/contributions.fec.2014.csv";
	final static String CONTRIBSDENVERFILELOCATION = "TestDatasets/contribs_denver/contributions.csv";
	final static String ALLWEBKFILELOCATION = "TestDatasets/donations_data/all_webk_2016.csv";
	final static String CANDIDATESFILELOCATION = "TestDatasets/donations_data/candidates_2016.csv";
	final static String SUPERPACFILELOCATION = "TestDatasets/donations_data/superpac_contribs_2016.csv";
	
	// For CONTRIBSSMALLFILELOCATION only.
	public static void addNames (TableInfo table) {
List<Map<String, String>> names = new ArrayList<Map<String, String>>();
		
		Map<String, String> col1 = new HashMap<String, String>();
		col1.put("column", "Import Ref. Id");
		for (String val : table.dictionary.get(0)) {
			col1.put(val, val);
		}
		names.add(col1);
		
		Map<String, String> col2 = new HashMap<String, String>();
		col2.put("column", "Is Amendment");
		for (String val : table.dictionary.get(0)) {
			col2.put(val, val);
		}
		names.add(col2);
		
		Map<String, String> col3 = new HashMap<String, String>();
		col3.put("column", "Contributor Type");
		for (String val : table.dictionary.get(0)) {
			col3.put(val, val);
		}
		names.add(col3);
		
		Map<String, String> col4 = new HashMap<String, String>();
		col4.put("column", "Contributor Gender");
		for (String val : table.dictionary.get(0)) {
			col4.put(val, val);
		}
		names.add(col4);
		
		Map<String, String> col5 = new HashMap<String, String>();
		col5.put("column", "Recipient Party");
		for (String val : table.dictionary.get(0)) {
			col5.put(val, val);
		}
		names.add(col5);
		
		Map<String, String> col6 = new HashMap<String, String>();
		col6.put("column", "Recipient Type");
		for (String val : table.dictionary.get(0)) {
			col6.put(val, val);
		}
		names.add(col6);
		
		Map<String, String> col7 = new HashMap<String, String>();
		col7.put("column", "Committee Party");
		for (String val : table.dictionary.get(0)) {
			col7.put(val, val);
		}
		names.add(col7);
		
		Map<String, String> col8 = new HashMap<String, String>();
		col8.put("column", "Seat");
		for (String val : table.dictionary.get(0)) {
			col8.put(val, val);
		}
		names.add(col8);
		
		Map<String, String> col9 = new HashMap<String, String>();
		col9.put("column", "Seat Held");
		for (String val : table.dictionary.get(0)) {
			col9.put(val, val);
		}
		names.add(col9);
		
		Map<String, String> col10 = new HashMap<String, String>();
		col10.put("column", "Seat Status");
		for (String val : table.dictionary.get(0)) {
			col10.put(val, val);
		}
		names.add(col10);
		
		Map<String, String> col11 = new HashMap<String, String>();
		col11.put("column", "Seat Result");
		for (String val : table.dictionary.get(0)) {
			col11.put(val, val);
		}
		names.add(col11);
		
		table.names = names;
	}
	
	public static TableInfo parseData(String inputFile, Double sampleProb) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		List<List<String>> dictionary = new ArrayList<List<String>>();
		List<Map<String, Integer>> reverseDictionary = new ArrayList<Map<String, Integer>>();
		List<List<Integer>> contents = new ArrayList<List<Integer>>();
		String line = br.readLine();
		{
			String[] vals = line.split(",");
			for (int i = 0; i < vals.length; i++) {
				dictionary.add(new ArrayList<String>());
				reverseDictionary.add(new HashMap<String, Integer>());
				out.println(i + "  :  " + vals[i]);
			}
		}
	
		line = br.readLine(); // ignore first line since its column names.
		do {
			//out.println(line);
			if (Math.random() > sampleProb) {
				continue;
			}
			boolean inQuote = false;
			for (int i = 0; i < line.length(); i++) {
				if (line.charAt(i) == '\"') {
					inQuote = inQuote ^ true;
					continue;
				}
				if (inQuote && line.charAt(i) == ',') {
					line= line.substring(0,i) + ' ' + line.substring(i + 1);
				}
			}
			String[] vals = line.split(",");
			if (vals.length > dictionary.size()) {
				continue;
			}
			List<Integer> tuple = new ArrayList<Integer>(vals.length);
			for (int i = 0; i < vals.length; i++) {
				final String value = vals[i];
				/*if (value.equals("03")) {
					out.println(line);
					System.exit(1);
				}*/
				Map<String, Integer> columnDictionary = reverseDictionary.get(i);
				if (columnDictionary.containsKey(value)) {
					tuple.add(columnDictionary.get(value));
				} else {
					columnDictionary.put(value, columnDictionary.keySet().size());
					dictionary.get(i).add(value);
					tuple.add(columnDictionary.get(value));
				}
			}
			contents.add(tuple);
		} while ((line = br.readLine()) != null);
		
		br.close();
		
		TableInfo table = new TableInfo(dictionary, reverseDictionary, contents);
		addNames(table);
		return table;
	}
	
	public static void createSmallFile() throws IOException {
		final Double sampleFraction = 0.01;
		final Integer columnThreshold = 10;
		final String destinationFile = "TestDatasets/contribs_big/contributions.reduced.fec.2014.csv";
		TableInfo ti = parseData(CONTRIBSBIGFILELOCATION, sampleFraction);
		List<Integer> smallColumns = new ArrayList<Integer>();
		for (int col = 0; col < ti.dictionary.size(); col++) {
			final int numVals = ti.dictionary.get(col).size(); 
			if (numVals < columnThreshold && numVals > 1) {
				smallColumns.add(col);
			}
		}
		out.println(smallColumns.toString());
		PrintWriter pw = new PrintWriter(new FileWriter(destinationFile));
		for (List<Integer> row : ti.contents) {
			for (int i = 0; i < smallColumns.size(); i++) {
				int col = smallColumns.get(i);
				if (i != 0) {
					pw.print(",");
				}
				pw.print(ti.dictionary.get(col).get(row.get(col)));
			}
			pw.println();
		}
		pw.close();
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		boolean createTI = false;
		TableInfo ti;
		
		if(createTI) {
			ti = parseData(CONTRIBSSMALLFILELOCATION, 1.0);
			FileOutputStream fo = new FileOutputStream(SERIALIZEDTABLEINFO);
			ObjectOutputStream oo = new ObjectOutputStream(fo);
			oo.writeObject(ti);			
			oo.close();
		} else {
			FileInputStream fi = new FileInputStream(SERIALIZEDTABLEINFO);
			ObjectInputStream oi = new ObjectInputStream(fi);
			ti = (TableInfo) oi.readObject();
			oi.close();
		}
		
	}
}
