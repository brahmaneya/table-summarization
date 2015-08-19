package dataextraction;

import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Donations {
	
	final static String CONTRIBSBIGFILELOCATION = "TestDatasets/contribs_big/contributions.fec.2014.csv";
	final static String CONTRIBSDENVERFILELOCATION = "TestDatasets/contribs_denver/contributions.csv";
	final static String ALLWEBKFILELOCATION = "TestDatasets/donations_data/all_webk_2016.csv";
	final static String CANDIDATESFILELOCATION = "TestDatasets/donations_data/candidates_2016.csv";
	final static String SUPERPACFILELOCATION = "TestDatasets/donations_data/superpac_contribs_2016.csv";
	final static String DATAFILELOCATION = CONTRIBSBIGFILELOCATION;
	
	public static void addNames (TableInfo table) {
	}
	
	public static TableInfo parseData() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(DATAFILELOCATION));
		List<List<String>> dictionary = new ArrayList<List<String>>();
		List<Map<String, Integer>> reverseDictionary = new ArrayList<Map<String, Integer>>();
		List<List<Integer>> contents = new ArrayList<List<Integer>>();
		String line = br.readLine();
		out.println(line);
		{
			String[] vals = line.split(",");
			for (int i = 0; i < vals.length; i++) {
				dictionary.add(new ArrayList<String>());
				reverseDictionary.add(new HashMap<String, Integer>());
			}
			out.println(vals.length);
		}
	
		line = br.readLine(); // ignore first line since its column names.
		do {
			//out.println(line);
			if (Math.random() > 0.1) {
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
	
	public static void main(String[] args) throws IOException {
		TableInfo ti = parseData();
		out.println(ti.contents.size());
		int numColumns = ti.dictionary.size();
		for (int col = 0; col < numColumns; col++) {
			out.println(col + "\t" + ti.dictionary.get(col).size());
		}
	}
}
