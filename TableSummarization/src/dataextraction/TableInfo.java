package dataextraction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableInfo {
	public List<List<String>> dictionary;
	public List<Map<String, Integer>> reverseDictionary;
	public List<List<Integer>> contents;
	public List<Map<String, String>> names;
	public List<List<Double>> buckets; // Has numbers which denote the interval endpoints of buckets. The number of buckets is thus one less than number of endpoints.
	public List<Integer> numericalColumns; // List of indexes of numerical columns.
	public List<List<Double>> numericalValues; // For each tuple, has list of actual numerical values, since contents itself has only bucket index.
	
	TableInfo (int numColumns) {
		dictionary = new ArrayList<List<String>>(numColumns);
		reverseDictionary = new ArrayList<Map<String, Integer>>(numColumns);
		for (int i = 0; i < numColumns; i++) {
			dictionary.add(new ArrayList<String>());
			reverseDictionary.add(new HashMap<String, Integer>());
		}
		contents = new ArrayList<List<Integer>>();
		bucketizeNumericalColumns(new ArrayList<Integer>(), new ArrayList<Integer>(), new ArrayList<String>());
	}
	
	TableInfo () {
		dictionary = new ArrayList<List<String>>();
		reverseDictionary = new ArrayList<Map<String, Integer>>();
		contents = new ArrayList<List<Integer>>();
	}
	
	TableInfo (List<List<String>> dictionary, List<Map<String, Integer>> reverseDictionary, List<List<Integer>> contents) {
		this.dictionary = dictionary;
		this.reverseDictionary = reverseDictionary;
		this.contents = contents;
	}
	
	/**
	 * Takes a table and bucketizes the numerical columns. 
	 * @param numericalColumns : List of columns indexes that are numerical. 
	 * @param numBuckets : The i^th value in this list is the number of buckets to form for the i^th numerical column in the numericalColumns list.
	 * @param scheme : The i^th value in this list is the bucketing scheme (equal ranges, equal sizes, etc) to form for the i^th numerical column in the numericalColumns list.
	 * Schemes: 
	 * 	"range" : create buckets whose range lengths are equal.
	 * 	"size" : create buckets so as to have an approximately equal number of tuples in each bucket.	
	 */
	public void bucketizeNumericalColumns (List<Integer> numericalColumns, List<Integer> numBucketsList, List<String> schemes) {
		this.numericalColumns = numericalColumns;
		buckets = new ArrayList<List<Double>>();
		List<List<Double>> valueLists = new ArrayList<List<Double>>();
		for (Integer column : numericalColumns) {
			valueLists.add(new ArrayList<Double>());
		}
		numericalValues = new ArrayList<List<Double>>(); // values in numerical columns
		for (List<Integer> tuple : contents) {
			List<Double> valueList = new ArrayList<Double>();
			for (int column : numericalColumns) {
				final Double value = Double.parseDouble(dictionary.get(column).get(tuple.get(column)));
				valueList.add(value);
				valueLists.get(column).add(value);
			}
			numericalValues.add(valueList);
		}
		for (int column : numericalColumns) {
			Collections.sort(valueLists.get(column));			
		}
		for (int column : numericalColumns) {
			List<Double> bucketRanges = new ArrayList<Double>();
			buckets.add(bucketRanges);
			final List<Double> values = valueLists.get(column);
			final Integer numBuckets = numBucketsList.get(column);
			final String scheme = schemes.get(column);
			if (scheme.equals("range")) {
				final Double range = (values.get(values.size() - 1) - values.get(0)) / numBuckets;
				final Double startPoint = values.get(0);
				for (int i = 0; i <= numBuckets; i++) {
					bucketRanges.add(startPoint + i * range);
				}
			} else if (scheme.equals("size")) {
				for (int i = 0; i <= numBuckets; i++) {
					bucketRanges.add(values.get((i * values.size())/numBuckets));
				}
			} else {
				throw new IllegalArgumentException("Scheme value not recognized : " + scheme);
			}
			dictionary.set(column, new ArrayList<String>());
			for (int i = 0; i < numBuckets; i++) {
				dictionary.get(column).add(bucketRanges.get(i) + "-" + bucketRanges.get(i + 1));
			}
		}
		for (int tupleNo = 0; tupleNo < contents.size(); tupleNo++) {
			final List<Integer> tuple = contents.get(tupleNo);
			final List<Double> values = numericalValues.get(tupleNo);
			for (int columnNo = 0; columnNo < numericalColumns.size(); columnNo++) {
				final int columnIndex = numericalColumns.get(columnNo);
				final Double value = values.get(columnNo);
				int bucketNo = 0;
				final List<Double> bucketRanges = buckets.get(columnNo);
				while (value > bucketRanges.get(bucketNo + 1)) {
					bucketNo++;
				}
				tuple.set(columnIndex, bucketNo);
			}
		}
	}
	
	public TableInfo getSubTable (List<Integer> columns) {
		List<List<String>> newDictionary = new ArrayList<List<String>>();
		List<Map<String, Integer>> newReverseDictionary = new ArrayList<Map<String, Integer>>();
		List<List<Integer>> newContents = new ArrayList<List<Integer>>();
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
