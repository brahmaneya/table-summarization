package dataextraction;

import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import solvers.Rule;

public class USCensus1990 {
	final static String DATAFILELOCATION = "TestDatasets/USCensus1990/USCensus1990.data.txt";

	public static void addNames (TableInfo table) {
		List<Map<String, String>> names = new ArrayList<Map<String, String>>();
		
		for (List<String> col : table.dictionary) {
			Map<String, String> colNames = new HashMap<String, String>();
			colNames.put("column", "C");
			for (String name : col) {
				colNames.put(name, name);
			}
			names.add(colNames);
		}
		table.names = names;
	}
	
	public static TableInfo parseData(Double sampleProb) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(DATAFILELOCATION));
		List<List<String>> dictionary = new ArrayList<List<String>>();
		List<Map<String, Integer>> reverseDictionary = new ArrayList<Map<String, Integer>>();
		List<List<Integer>> contents = new ArrayList<List<Integer>>();
		
		String line = br.readLine();
		{
			String[] vals = line.split(",");
			for (int i = 0; i < vals.length; i++) {
				dictionary.add(new ArrayList<String>());
				reverseDictionary.add(new HashMap<String, Integer>());
			}
		}
		
		while ((line = br.readLine()) != null) {
			if (Math.random() > sampleProb) {
				continue;
			}
			String[] vals = line.split(",");
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
		}
		
		br.close();
		
		TableInfo table = new TableInfo(dictionary, reverseDictionary, contents);
		addNames(table);
		return table;
	}
	
	/*
	 * Updates counts of rules over entire table. Returns total number of tuples.
	 */
	public static Long updateRuleCounts (TableInfo table, List<Integer> columns, Set<Rule> rules) throws IOException {
		Long numTuples = (long)0;
		for (Rule rule : rules) {
			rule.count = 0;
		}
		BufferedReader br = new BufferedReader(new FileReader(DATAFILELOCATION));
		List<Map<String, Integer>> reverseDictionary = table.reverseDictionary;
		String line = br.readLine();
		
		while ((line = br.readLine()) != null) {
			numTuples++;
			String[] vals = line.split(",");
			List<Integer> tuple = new ArrayList<Integer>(vals.length);
			Integer dictColumnNumber = 0;
			for (int i : columns) {
				final String value = vals[i];
				Map<String, Integer> columnDictionary = reverseDictionary.get(dictColumnNumber);
				if (columnDictionary.containsKey(value)) {
					tuple.add(columnDictionary.get(value));
				} else {
					tuple = null;
					break;
				}
				dictColumnNumber++;
			}
			if (tuple == null) {
				continue;
			}
			for (Rule rule : rules) {
				if (Rule.isSubRule(rule, tuple)) {
					rule.count++;
				}
			}
		}
		br.close();
		return numTuples;
	}
	
	public static void main(String[] args) throws IOException {
		TableInfo ti = parseData(0.004);
		out.println(ti.contents.size());
	}

}
