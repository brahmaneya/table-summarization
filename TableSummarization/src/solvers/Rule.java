package solvers;

import static java.lang.System.out;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataextraction.TableInfo;

public class Rule implements Comparable<Rule> {
	final List<Integer> values;
	public Integer count;
	Integer size; // Number of non-starred elements
	Integer score;
	Integer minMarginalValue; // Minimum marginal value of adding rule to solution. 
	Integer maxMarginalValue; // Maximum marginal value of adding rule to solution. 
	Integer latestCountedMarginalValue; // Latest marginal value is fully reliable, i.e. has been counted by making a pass over the data.
	public Boolean counted; // Have we made an actual count of the rule's coverage
	final Map<Integer, Integer> valueMap; // Useful for sparse rules. Map from index to non-star value at the index.
	
	public Integer get(Integer index) {
		return values.get(index);
	}

	public Integer size() {
		return valueMap.size();
	}
	
	public Integer length() {
		return values.size();
	}
	
	public static class nullScorer implements Scorer {
		@Override
		public void setScore(TableInfo table, Rule rule) {
			rule.score = 0;
		}
	}
	
	public static class sizeScorer implements Scorer {
		@Override
		public void setScore(TableInfo table, Rule rule) {
			rule.score = 0;
			for (int i = 0; i < rule.length(); i++) {
				if (rule.get(i) != -1 && table.dictionary.get(i).get(rule.get(i)) != "NA") {
					rule.score ++;
				}
			}
		}
	};
	
	public static class sizeSquareScorer implements Scorer {
		@Override
		public void setScore(TableInfo table, Rule rule) {
			rule.score = 0;
			for (int i = 0; i < rule.length(); i++) {
				if (rule.get(i) != -1 && table.dictionary.get(i).get(rule.get(i)) != "NA") {
					rule.score ++;
				}
			}
			rule.score = rule.score * rule.score;
		}
	};
	
	public static class sizeMinusKScorer implements Scorer {
		int k;
		public sizeMinusKScorer (int k) {
			this.k = k;
		}
		
		@Override
		public void setScore(TableInfo table, Rule rule) {
			rule.score = 0;
			for (int i = 0; i < rule.length(); i++) {
				if (rule.get(i) != -1 && table.dictionary.get(i).get(rule.get(i)) != "NA") {
					rule.score ++;
				}
			}
			rule.score = Math.max(0, rule.score - k);
		}
	};
	
	public static class columnsSizeScorer implements Scorer {
		final Set<Integer> columnsToScore;
		public columnsSizeScorer (Set<Integer> columnsToScore) {
			this.columnsToScore = columnsToScore;
		}
		
		@Override
		public void setScore(TableInfo table, Rule rule) {
			rule.score = 0;
			for (int i = 0; i < rule.length(); i++) {
				if (rule.get(i) != -1 && columnsToScore.contains(i)) {
					rule.score ++;
				}
			}
		}
	};
	
	public static class sizeBitsScorer implements Scorer {
		@Override
		public void setScore(TableInfo table, Rule rule) {
			rule.score = 0;
			for (int i = 0; i < rule.length(); i++) {
				final int numVals = table.dictionary.get(i).size();
				if (rule.get(i) != -1 && table.dictionary.get(i).get(rule.get(i)) != "NA") {
					rule.score += (int)Math.ceil((Math.log(numVals)/Math.log(2)));
				}
			}
		}
	};
	
	public void setScore (TableInfo table, Scorer scorer) {
		scorer.setScore(table, this);
	}
	
	public Rule deepValuesCopy () {
		final Map<Integer, Integer> newValueMap = new HashMap<Integer, Integer>();
		newValueMap.putAll(valueMap);
		return new Rule (newValueMap, length());
	}

	public void deleteVal (int index) {
		values.set(index, -1);
		valueMap.remove(index);
		size--;
	}

	public void addVal (int index, int value) {
		values.set(index, value);
		valueMap.put(index, value);
		size++;
	}
	
	public Rule(TableInfo table, List<Integer> values, Integer count, Boolean counted, Scorer scorer) {
		this.values = values;
		this.count = count;
		this.valueMap = new HashMap<Integer, Integer>();
		
		for (int i = 0; i < length(); i++) {
			if (values.get(i) != -1) {
				valueMap.put(i, values.get(i));
			}
		}
		
		this.size = valueMap.size();
		setScore(table, scorer);
		minMarginalValue = maxMarginalValue = latestCountedMarginalValue = score * count;
		this.counted = counted;
	}

	/**
	 * Constructor for temporary rule that only has the values, and no other associated information.
	 */
	public Rule(Map<Integer, Integer> valueMap, Integer length) {
		this.values = new ArrayList<Integer>();
		this.count = 0;
		this.valueMap = valueMap;
		
		for (int i = 0; i < length; i++) {
			if (valueMap.containsKey(i)) {
				values.add(valueMap.get(i));
			} else {
				values.add(-1);
			}
		}
		
		this.size = valueMap.size();
		this.counted = false;
	}

	/**
	 * Constructor for temporary rule that only has the values, and no other associated information.
	 */
	public Rule(TableInfo table, Map<Integer, Integer> valueMap, Integer length, Scorer scorer) {
		this.values = new ArrayList<Integer>();
		this.count = 0;
		this.valueMap = valueMap;
		
		for (int i = 0; i < length; i++) {
			if (valueMap.containsKey(i)) {
				values.add(valueMap.get(i));
			} else {
				values.add(-1);
			}
		}
		
		setScore(table, scorer);
		minMarginalValue = maxMarginalValue = latestCountedMarginalValue = 0;
		this.size = valueMap.size();
		this.counted = false;
	}

	/**
	 * Constructor for temporary rule that only has the values, and no other associated information.
	 */
	public Rule(List<Integer> values) {
		this.values = values;
		this.count = 0;
		this.valueMap = new HashMap<Integer, Integer>();
		
		for (int i = 0; i < length(); i++) {
			valueMap.put(i, values.get(i));
		}
		
		this.size = valueMap.size();
		this.counted = false;
	}

	public Rule(TableInfo table, List<Integer> values, Scorer scorer) {
		this.values = values;
		this.count = 0;
		this.valueMap = new HashMap<Integer, Integer>();
		
		for (int i = 0; i < length(); i++) {
			valueMap.put(i, values.get(i));
		}
		
		setScore(table, scorer);
		minMarginalValue = maxMarginalValue = latestCountedMarginalValue = 0;
		this.size = valueMap.size();
		this.counted = false;
	}

	public Rule(TableInfo table, Map<Integer, Integer> valueMap, Integer length, Integer count, Boolean counted, Scorer scorer) {
		this.values = new ArrayList<Integer>();
		this.count = count;
		this.valueMap = valueMap;
		
		for (int i = 0; i < length; i++) {
			if (valueMap.containsKey(i)) {
				values.add(valueMap.get(i));
			} else {
				values.add(-1);
			}
		}
		
		this.size = valueMap.size();
		setScore(table, scorer);
		minMarginalValue = maxMarginalValue = latestCountedMarginalValue = score * count;
		this.counted = counted;
	}

	@Override
	/**
	 * Two rules are equal if the rule string is equal. Otherwise, we compare counts. In case of equal counts, we compare rule strings 
	 */
	public int compareTo(Rule r2) {
		if (valueMap.equals(r2.valueMap)) {
			return 0;
		}
		if (maxMarginalValue != r2.maxMarginalValue) {
			return maxMarginalValue.compareTo(r2.maxMarginalValue);			
		} else if (minMarginalValue != r2.minMarginalValue) {
			return minMarginalValue.compareTo(r2.minMarginalValue);			
		} else {
			if (length() != r2.length()) {
				throw new IllegalArgumentException("Rules must be of same length");
			}
			for (int i = 0; i < length(); i++) {
				if (get(i) != r2.get(i)) {
					return get(i).compareTo(r2.get(i));
				}
			}
			return 0;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (!o.getClass().equals(Rule.class)) {
			return false;
		} else {
			return valueMap.equals(((Rule)o).valueMap);
		}
		
	}
	
	@Override
	public int hashCode() {
		return valueMap.hashCode();
	}
	
	@Override
	public String toString() {
		String toReturn = "";
		toReturn = toReturn + valueMap.toString();
		toReturn = toReturn + "\t" + size +"\t" + count;
		return toReturn;
	}
	
	public String ruleString (TableInfo table) {
		String ruleString = "";
		for (Integer i = 0; i < length(); i++) {
			final Integer val = get(i);
			if (i > 0) {
				ruleString = ruleString + ",";
			}
			if (val == -1) {
				ruleString = ruleString + "\"*\"";
			} else {
				ruleString = ruleString + "\"" + table.names.get(i).get(table.dictionary.get(i).get(val)) + "\"";
			}
		}
		return ruleString;
	}
	
	public String ruleStringSparse (TableInfo table) {
		String ruleString = "";
		for (int col : valueMap.keySet()) {
			final Integer val = get(col);
			ruleString = ruleString + table.names.get(col).get("column") + ":" + table.names.get(col).get(table.dictionary.get(col).get(val)) + ", ";
		}
		ruleString = ruleString + "Count:" + count;
		return ruleString;
	}
	
	public String ruleString (TableInfo table, List<Integer> columns) {
		String ruleString = "";
		for (Integer i = 0; i < columns.size(); i++) {
			final Integer index = columns.get(i);
			Integer val = get(index);
			if (i > 0) {
				ruleString = ruleString + ",";
			}
			if (val == -1) {
				ruleString = ruleString + "\"*\"";
			} else {
				ruleString = ruleString + "\"" + table.names.get(index).get(table.dictionary.get(index).get(val)) +  "\"";
			}
		}
		return ruleString;
	}

	/**
	 * Full Rull string, including rule itself, total count, marginal count, and so on. In CSV format.
	 */
	public String fullRuleStringCSV (TableInfo table, Map<Rule, Integer> marginalCounts) {
		String ruleString = "";
		for (Integer i = 0; i < length(); i++) {
			final Integer val = get(i);
			if (i > 0) {
				ruleString = ruleString + ",";
			}
			if (val == -1) {
				ruleString = ruleString + "\"*\"";
			} else {
				ruleString = ruleString + "\"" + table.names.get(i).get(table.dictionary.get(i).get(val)) + "\"";
			}
		}
		String marginal = "NULL";
		if (marginalCounts.get(this) != null) {
			marginal = marginalCounts.get(this).toString();
		}
		ruleString = ruleString + ",\"" + marginal + "\",\"" + count + "\",\"" + score + "\"";
		return ruleString;
	}

	/**
	 * Full Rull string, including rule itself, total count, and so on (but not marginal count). In CSV format.
	 */
	public String fullRuleStringCSV (TableInfo table) {
		String ruleString = "";
		for (Integer i = 0; i < length(); i++) {
			final Integer val = get(i);
			if (i > 0) {
				ruleString = ruleString + ",";
			}
			if (val == -1) {
				ruleString = ruleString + "\"*\"";
			} else {
				ruleString = ruleString + "\"" + table.names.get(i).get(table.dictionary.get(i).get(val)) + "\"";
			}
		}
		ruleString = ruleString + ",\"" + count + "\",\"" + score + "\"";
		return ruleString;
	}
	
	/**
	 * Like fullRuleStringCSV, but uses latex table format.
	 */
	public String fullRuleStringTex (TableInfo table, Map<Rule, Integer> marginalCounts) {
		String ruleString = "";
		for (Integer i = 0; i < length(); i++) {
			final Integer index = i;
			final Integer val = get(i);
			if (i > 0) {
				ruleString = ruleString + " & ";
			}
			if (val == -1) {
				ruleString = ruleString + "$\\star$";
			} else {
				ruleString = ruleString + (table.names.get(index).get(table.dictionary.get(index).get(val))).replaceAll("\\$", "\\\\\\$");
			}
		}
		String marginal = "NULL";
		if (marginalCounts.get(this) != null) {
			marginal = marginalCounts.get(this).toString();
		}
		ruleString = ruleString + " & $" + marginal + "$ & $" + count + "$ & $" + score + "$ \\\\";
		return ruleString;
	}

	/**
	 * Like fullRuleStringTex, but without marginal counts.
	 */
	public String fullRuleStringTex (TableInfo table) {
		String ruleString = "";
		for (Integer i = 0; i < length(); i++) {
			final Integer index = i;
			final Integer val = get(i);
			if (i > 0) {
				ruleString = ruleString + " & ";
			}
			if (val == -1) {
				ruleString = ruleString + "$\\star$";
			} else {
				ruleString = ruleString + (table.names.get(index).get(table.dictionary.get(index).get(val))).replaceAll("\\$", "\\\\\\$");
			}
		}
		ruleString = ruleString + " & $" + count + "$ & $" + score + "$ \\\\";
		return ruleString;
	}

	/**
	 * Like fullRuleStringCSV, but uses latex table format.
	 */
	public String fullRuleStringTex (TableInfo table, List<Integer> columns, Map<Rule, Integer> marginalCounts) {
		String ruleString = "";
		for (Integer i = 0; i < columns.size(); i++) {
			final Integer index = columns.get(i);
			Integer val = get(index);
			if (i > 0) {
				ruleString = ruleString + " & ";
			}
			if (val == -1) {
				ruleString = ruleString + "$\\star$";
			} else {
				ruleString = ruleString + (table.names.get(index).get(table.dictionary.get(index).get(val))).replaceAll("\\$", "\\\\\\$");
			}
		}
		String marginal = "NULL";
		if (marginalCounts.get(this) != null) {
			marginal = marginalCounts.get(this).toString();
		}
		ruleString = ruleString + " & $" + marginal + "$ & $" + count + "$ & $" + score + "$ \\\\";
		return ruleString;
	}

	/**
	 * Like fullRuleStringCSV for specific columns and without marginal count. 
	 */
	public String fullRuleStringTex (TableInfo table, List<Integer> columns) {
		String ruleString = "";
		for (Integer i = 0; i < columns.size(); i++) {
			final Integer index = columns.get(i);
			Integer val = get(index);
			if (i > 0) {
				ruleString = ruleString + " & ";
			}
			if (val == -1) {
				ruleString = ruleString + "$\\star$";
			} else {
				ruleString = ruleString + (table.names.get(index).get(table.dictionary.get(index).get(val))).replaceAll("\\$", "\\\\\\$");
			}
		}
		ruleString = ruleString + " & $" + count + "$ & $" + score + "$ \\\\";
		return ruleString;
	}

	
	public String fullRuleStringTex (TableInfo table, List<Integer> columns, Integer indentationDepth) {
		String ruleString = "";
		for (Integer i = 0; i < columns.size(); i++) {
			final Integer index = columns.get(i);
			Integer val = get(index);
			if (i > 0) {
				ruleString = ruleString + " & ";
			}
			if (i < indentationDepth) {
				ruleString = ruleString + "$\\triangleright$ ";
			}
			if (val == -1) {
				ruleString = ruleString + "$\\star$";
			} else {
				ruleString = ruleString + (table.names.get(index).get(table.dictionary.get(index).get(val))).replaceAll("\\$", "\\\\\\$");
			}
		}
		ruleString = ruleString + " & $" + count + "$ & $" + score + "$ \\\\";
		return ruleString;
	}

	public String fullRuleStringTex (TableInfo table, List<Integer> columns, List<Integer> parentRule) {
		String ruleString = "";
		for (Integer i = 0; i < columns.size(); i++) {
			final Integer index = columns.get(i);
			Integer val = get(index);
			if (i > 0) {
				ruleString = ruleString + " & ";
			}
			if (val == -1) {
				ruleString = ruleString + "$\\star$";
			} else {
				if (parentRule.get(index) != -1) {
					ruleString = ruleString + "$\\triangleright$ ";
				}
				ruleString = ruleString + (table.names.get(index).get(table.dictionary.get(index).get(val))).replaceAll("\\$", "\\\\\\$");
			}
		}
		ruleString = ruleString + " & $" + count + "$ & $" + score + "$ \\\\";
		return ruleString;
	}
	
	public String fullRuleStringTex (TableInfo table, List<Integer> columns, Rule parentRule) {
		String ruleString = "";
		for (Integer i = 0; i < columns.size(); i++) {
			final Integer index = columns.get(i);
			Integer val = get(index);
			if (i > 0) {
				ruleString = ruleString + " & ";
			}
			if (val == -1) {
				ruleString = ruleString + "$\\star$";
			} else {
				if (parentRule.get(index) != -1) {
					ruleString = ruleString + "$\\triangleright$ ";
				}
				ruleString = ruleString + (table.names.get(index).get(table.dictionary.get(index).get(val))).replaceAll("\\$", "\\\\\\$");
			}
		}
		ruleString = ruleString + " & $" + count + "$ & $" + score + "$ \\\\";
		return ruleString;
	}

	/**
	 * Rules are consistent if they do not differ in any index where they both have non-star values
	 */
	public static boolean areConsistent (Rule r1, Rule r2) {
		boolean result = true;
		if (r1.size() < r2.size()) {
			for (Integer key : r1.valueMap.keySet()) {
				if (r2.valueMap.containsKey(key) && r2.get(key) != r1.get(key)) {
					result = false;
					break;
				}
			}
		} else {
			for (Integer key : r1.valueMap.keySet()) {
				if (r1.valueMap.containsKey(key) && r1.get(key) != r2.get(key)) {
					result = false;
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * A set of rules is consistent if every pair of rules in the set is consistent 
	 */
	public static boolean areConsistent (Set<Rule> rules) {
		for (int i = 0; i < rules.iterator().next().length(); i++) {
			int val = -1;
			for (Rule r : rules) {
				if (r.get(i) != -1) {
					if (val == -1) {
						val = r.get(i);
					} else  if (val != r.get(i)) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * r1 is a subRule of r2 if for every non-star value of r1, r2 has the same value at that index.
	 */
	public static boolean isSubRule (Rule r1, Rule r2) {
		Boolean isSubRule = true;
		for (int i : r1.valueMap.keySet()) {
			if (r1.get(i) != r2.get(i)) {
				isSubRule = false;
				break;
			}
		}
		return isSubRule;
	}
	
	public static boolean isSubRule (Rule r1, List<Integer> r2) {
		Boolean isSubRule = true;
		for (int i : r1.valueMap.keySet()) {
			if (r1.get(i) != r2.get(i)) {
				isSubRule = false;
				break;
			}
		}
		return isSubRule;
	}
	
	/**
	 * Union of two consistent rules is the smallest sized rule which is a super-rule of both input rules.
	 */
	public static Rule ruleUnion (Rule r1, Rule r2) {
		Map<Integer, Integer> valueMap = new HashMap<Integer, Integer>();
		valueMap.putAll(r1.valueMap);
		valueMap.putAll(r2.valueMap);
		return new Rule(valueMap, r1.length()); 
	}
	
	public static Rule ruleUnion (Set<Rule> rset) {
		Map<Integer, Integer> valueMap = new HashMap<Integer, Integer>();
		for (Rule r : rset) {
			valueMap.putAll(r.valueMap);
		}
		return new Rule(valueMap, rset.iterator().next().length()); 
	}
	
	/**
	 * Find sub-rules of a particular size (size refers to number of non-zero elements).
	 */
	public Set<Rule> findSubRules (Integer size) {
		Set<Rule> subRules = new HashSet<Rule>();
		if (size < 0) {
			// Do nothing. Return empty set.
		} else if (size == 0) {
			Map<Integer, Integer> subRuleValueMap = new HashMap<Integer, Integer>();
			subRules.add(new Rule(subRuleValueMap, length()));
		} else if (size == 1) {
			for (int i : valueMap.keySet()) {
				Map<Integer, Integer> subRuleValueMap = new HashMap<Integer, Integer>();
				subRuleValueMap.put(i, get(i));
				subRules.add(new Rule(subRuleValueMap, length()));
			}
		} else if (size == size()) {
			subRules.add(deepValuesCopy());
		} else if (size > size()) {
			// Do nothing. Return empty set.
		} else {
			int index = valueMap.keySet().iterator().next();
			int value = get(index);
			deleteVal(index);
			Set<Rule> subRules1 = findSubRules(size - 1);
			for (Rule subrule : subRules1) { // Do this better. also above. Make function to mutate rule.
				subrule.addVal(index, value);
			}
			subRules.addAll(subRules1);
			Set<Rule> subRules2 = findSubRules(size);
			subRules.addAll(subRules2);
			addVal(index, value);
		}
		return subRules;
	}
	
	/**
	 * Find super-rules of a particular size (size refers to number of non-zero elements).
	 */
	public Set<Rule> findSuperRules (TableInfo ti, Integer size) {
		Set<Rule> superRules = new HashSet<Rule>();
		if (size > length() || size < size()) {
			// Do nothing. Return empty set.
		} else if (size == size()) {
			superRules.add(deepValuesCopy());
		} else {
			int index;
			for (index = 0; index < length(); index++) {
				if (get(index) == -1) {
					break;
				}
			}
			addVal(index, 1); // Some non--1 value.
			Set<Rule> superRules1 = findSuperRules(ti, size);
			for (Rule superRule : superRules1) {
				for (int value = 0; value < ti.dictionary.get(index).size(); value++) {
					Rule newSuperRule = superRule.deepValuesCopy();
					newSuperRule.deleteVal(index);
					newSuperRule.addVal(index, value);
					superRules.add(newSuperRule);
				}
			}
			Set<Rule> superRules2 = findSuperRules(ti, size + 1);
			for (Rule superRule : superRules2) {
				superRule.deleteVal(index);
			}
			superRules.addAll(superRules2);
			deleteVal(index);
		}
		return superRules;
	}
	
	/**
	 * Find rules of a particular size that are subRules of the superRule and superRules of the subRule.
	 */
	public static Set<Rule> findSubSuperRules (Rule subRule, Rule superRule, Integer size) {
		Rule diffRule = superRule.deepValuesCopy();
		for (Integer index : subRule.valueMap.keySet()) {
			diffRule.deleteVal(index);
		}
		Set<Rule> subSuperRules = diffRule.findSubRules(size - subRule.size());
		for (Rule rule : subSuperRules) {
			for (Integer index : subRule.valueMap.keySet()) {
				rule.addVal(index, subRule.get(index));
			}
		}
		return subSuperRules;
	}
	
	/**
	 * Generates a comparator for sorting the list of rules. sortColumns is the list of columns over which to sort. 
	 * an integer from 0 to numCols - 1 specifies a data table column (where numCols is rule.rule.size()). numCols is marginal Count
	 * numCols + 1 is totalCount. numCols + 2 is size. A negative of a number indicates sort in descending order (default is ascending). 
	 */
	public static Comparator<Rule> sortComparator (TableInfo table, Map<Rule, Integer> marginalCounts, List<Integer> sortColumns) {
		final TableInfo finalTable = table;
		final Map<Rule, Integer> finalMarginalCounts = marginalCounts;
		final List<Integer> finalSortColumns = sortColumns;
		Comparator<Rule> comp = new Comparator<Rule> () {
			@Override
			public int compare(Rule o1, Rule o2) {
				for (Integer col : finalSortColumns) {
					int asc = col > 0 ? 1 : -1;
					col *= asc;
					if (col < o1.length()) {
						String val1, val2;
						if (o1.get(col) == -1) {
							val1 = "$\\star$";
						} else{
							val1 = finalTable.names.get(col).get(finalTable.dictionary.get(col).get(o1.get(col)));
						}
						if (o2.get(col) == -1) {
							val2 = "$\\star$";
						} else{
							val2 = finalTable.names.get(col).get(finalTable.dictionary.get(col).get(o2.get(col)));
						}
						if (!val1.equals(val2)) {
							return asc * val1.compareTo(val2);
						}
					} else if (col == o1.length()) {
						int val1 = finalMarginalCounts.get(o1);
						int val2 = finalMarginalCounts.get(o2);
						if (val1 != val2) {
							return asc * Integer.compare(val1, val2);							
						}
					} else if (col == o1.length() + 1) {
						int val1 = o1.count;
						int val2 = o2.count;
						if (val1 != val2) {
							return asc * Integer.compare(val1, val2);							
						}
					} else if (col == o1.length() + 2) {
						int val1 = o1.size;
						int val2 = o2.size;
						if (val1 != val2) {
							return asc * Integer.compare(val1, val2);							
						}
					}
				}
				return 0;
			}
		};
		return comp;
	}
}
