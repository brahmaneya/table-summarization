package dataextraction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import solvers.Rule;
import solvers.Scorer;

/**
 * This class represents a sample of a table. It consists of a uniform random sample of size given by size(), from all the table
 * rules that are covered by the filterRule. Used for estimating counts, etc of subRules of the filterRule.
 */
public class TableSample extends TableInfo {
	public TableInfo table;
	// Inherited member dictionary :  Modified dictionary obtained by taking table.dictionary entries from columnMapping indexes.
	public Rule filterRule;
	public Integer totalCount; // Total number of tuples in the original table (actual or estimated) covered by the filterRule.
	// Inherited member contents : contains the truncated tuples in the sample (truncated to remove columns fixed by the filterRule
	public List<Integer> columnMapping; // The i^th entry here is j if the i^th row in the sample corresponds to the j^th column of the table.
	public List<Integer> reverseColumnMapping; // The i^th entry here is -1 if the i^th entry in the filterRule != -1. Else it is j such that columnMapping.get(j) = i
	
	private void setReverseColumnMapping () {
		reverseColumnMapping = new ArrayList<Integer>();
		for (int i = 0; i < table.dictionary.size(); i++) {
			reverseColumnMapping.add(-1);
		}
		for (int i = 0; i < columnMapping.size(); i++) {
			reverseColumnMapping.set(columnMapping.get(i), i);
		}
	}
	
	private void setDictionary () {
		dictionary = new ArrayList<List<String>>();
		for (int i = 0; i < columnMapping.size(); i++) {
			dictionary.add(table.dictionary.get(columnMapping.get(i)));
		}
	}
	
	public TableSample (TableInfo table, Rule filterRule, Integer totalCount, Set<List<Integer>> contents, List<Integer> columnMapping) {
		this.table = table;
		this.filterRule = filterRule;
		this.totalCount = totalCount;
		this.contents = contents;
		this.columnMapping = columnMapping;
		setReverseColumnMapping();
		setDictionary();
	}
	
	public Integer size() {
		return contents.size();
	}
	
	/**
	 * The probability with which any tuple satisfying filterRule was loaded into this sample.
	 */
	public double sampleFraction () {
		return (1.0  * size())/totalCount;
	}

	/**
	 * Create a sample of max-size equal to 'size', with filter as the filterRule.
	 * Thus in order to get all rules covered by filter rule, set size = Integer.MAX_VALUE.
	 * To get a random sample of the table, set filter to be the empty rule. 
	 */
	public static TableSample createSample(TableInfo table, Rule filter, Integer size) {
		final int tableLength = table.dictionary.size();
		final int sampleLength = tableLength - filter.size();
		int totalCount = 0; // Total number of tuples in table that satisfies the filter.
		List<List<Integer>> contentsList = new ArrayList<List<Integer>>();
		List<Integer> columnMapping = new ArrayList<Integer>();
		for (int i = 0; i < tableLength; i++) {
			if (filter.get(i) == -1) {
				columnMapping.add(i);
			}
		}
		for (List<Integer> tuple : table.contents) {
			if (Rule.isSubRule(filter, tuple)) {
				totalCount++;
				if (contentsList.size() < size) {
					List<Integer> sampleTuple = new ArrayList<Integer>();
					for (int i = 0; i < sampleLength; i++) {
						sampleTuple.add(tuple.get(columnMapping.get(i)));
					}
					contentsList.add(sampleTuple);
				} else {
					int toReplace = (int) (Math.random() * totalCount);
					if (toReplace < size) {
						List<Integer> sampleTuple = new ArrayList<Integer>();
						for (int i = 0; i < sampleLength; i++) {
							sampleTuple.add(tuple.get(columnMapping.get(i)));
						}
						contentsList.set(toReplace, sampleTuple);
					}
				}
			}
		}
		Set<List<Integer>> contents = new HashSet<List<Integer>>(contentsList);
		TableSample result = new TableSample(table, filter, totalCount, contents, columnMapping);
		return result;
	}
	
	/**
	 * Create a sample by filtering tableSample according to the filter rule. Note that the filter rule must be a superRule of
	 * tableSample.filterRule for this to be a meaningful sample. Note that totalCount now is only an estimate. 
	 */
	public static TableSample createFilteredSample(TableSample tableSample, Rule filter) {
		if (!Rule.isSubRule(tableSample.filterRule, filter)) {
			throw new IllegalArgumentException("filter Rule must be a super-rule of tableSample.filterRule");
		}
		List<Integer> columnMapping = new ArrayList<Integer>();
		final List<Integer> oldColumnMapping = tableSample.columnMapping;
		for (int i = 0; i < oldColumnMapping.size(); i++) {
			if (filter.get(oldColumnMapping.get(i)) == -1) {
				columnMapping.add(oldColumnMapping.get(i));
			}
		}
		int totalCount = 0;
		Set<List<Integer>> contents = new HashSet<List<Integer>>();
		for (List<Integer> tuple : tableSample.contents) {
			if (tableSample.isSubRule(filter, tuple)) {
				totalCount++;
				List<Integer> newTuple = new ArrayList<Integer>();
				for (int i = 0; i < oldColumnMapping.size(); i++) {
					if (filter.get(oldColumnMapping.get(i)) == -1) {
						newTuple.add(tuple.get(i));
					}
				}
				contents.add(newTuple);
			}
		}
		totalCount /= tableSample.sampleFraction();
		TableSample result = new TableSample(tableSample.table, filter, totalCount, contents, columnMapping);
		return result;
	}
	
	/**
	 * Get the value at given 'index' of the table tuple that corresponds to the given tuple in the sample.
	 */
	public Integer get (List<Integer> tuple, Integer index) {
		final int reverseIndex = reverseColumnMapping.get(index);
		if (reverseIndex == -1) {
			return filterRule.get(index);
		} else {
			return tuple.get(reverseIndex);
		}
	}
	
	/**
	 * Finds if tuple is covered by rule. The rule is assumed to be a super-rule of the filter rule. Note that this condition is not
	 * being checked, and hence an lead to bugs if not satisfied by the arguments.
	 */
	public boolean isSubRule (Rule rule, List<Integer> tuple) {
		for (int i = 0; i < columnMapping.size(); i++) {
			final int j = columnMapping.get(i);
			if (rule.get(j) != -1 && rule.get(j) != tuple.get(i)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Take a rule in format of the original table and convert it into a corresponding rule for the reduced table. Also adjusts 
	 * count according to sampleFraction. The new rule is scored in reduced form as well. 
	 */
	public Rule truncateRule (Rule rule, Scorer scorer) {
		List<Integer> values = new ArrayList<Integer>();
		for (int i = 0; i < columnMapping.size(); i++) {
			values.add(rule.get(columnMapping.get(i)));
		}
		Rule result = new Rule (this, values, (int)(rule.count * sampleFraction()), rule.counted, scorer);
		return result;
	}
	
	/**
	 * Reverse of truncateRule
	 */
	public Rule expandRule (Rule rule, Scorer scorer) {
		List<Integer> values = new ArrayList<Integer>();
		for (int i = 0; i < table.dictionary.size(); i++) {
			if (reverseColumnMapping.get(i) == -1) {
				values.add(filterRule.get(i));
			} else {
				values.add(rule.get(reverseColumnMapping.get(i)));				
			}
		}
		Rule result = new Rule (table, values, (int)(rule.count / sampleFraction()), rule.counted, scorer);
		return result;
	}
	
	/**
	 * Expand rule values/valueMap only
	 */
	public Rule expandRule (Rule rule) {
		List<Integer> values = new ArrayList<Integer>();
		for (int i = 0; i < table.dictionary.size(); i++) {
			if (reverseColumnMapping.get(i) == -1) {
				values.add(filterRule.get(i));
			} else {
				values.add(rule.get(reverseColumnMapping.get(i)));				
			}
		}
		Rule result = new Rule (values);
		return result;
	}
	
	/**
	 * Updates counts (estimates) of rules (that must be subRules of filterRules), according to sample.
	 */
	public static void updateCounts (Map<Rule, Rule> ruleMap) {
		
	}
}
