package dataextraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableInfo {
	public List<List<String>> dictionary;
	public List<Map<String, Integer>> reverseDictionary;
	public Set<List<Integer>> contents;
	public List<Map<String, String>> names;
	
	TableInfo (int numColumns) {
		dictionary = new ArrayList<List<String>>(numColumns);
		reverseDictionary = new ArrayList<Map<String, Integer>>(numColumns);
		for (int i = 0; i < numColumns; i++) {
			dictionary.add(new ArrayList<String>());
			reverseDictionary.add(new HashMap<String, Integer>());
		}
		contents = new HashSet<List<Integer>>();
	}
	
	TableInfo () {
		dictionary = new ArrayList<List<String>>();
		reverseDictionary = new ArrayList<Map<String, Integer>>();
		contents = new HashSet<List<Integer>>();
	}
	
	TableInfo (List<List<String>> dictionary, List<Map<String, Integer>> reverseDictionary, Set<List<Integer>> contents) {
		this.dictionary = dictionary;
		this.reverseDictionary = reverseDictionary;
		this.contents = contents;
	}
	
	public TableInfo getSubTable (List<Integer> columns) {
		List<List<String>> newDictionary = new ArrayList<List<String>>();
		List<Map<String, Integer>> newReverseDictionary = new ArrayList<Map<String, Integer>>();
		Set<List<Integer>> newContents = new HashSet<List<Integer>>();
		List<Map<String, String>> newNames = new ArrayList<Map<String, String>>();
		for (List<Integer> tuple : contents) {
			List<Integer> newTuple = new ArrayList<Integer>();
			for (int col : columns) {
				newTuple.add(tuple.get(col));
			}
			newContents.add(newTuple);
		}
		for (int col : columns) {
			newDictionary.add(dictionary.get(col));
			newReverseDictionary.add(reverseDictionary.get(col));
			newNames.add(names.get(col));
			
		}
		TableInfo newTable = new TableInfo(newDictionary, newReverseDictionary, newContents);
		newTable.names = newNames;
		return newTable;
	}
}
