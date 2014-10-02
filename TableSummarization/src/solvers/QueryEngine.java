package solvers;

import static java.lang.System.out;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataextraction.TableInfo;

public class QueryEngine {
	public static RuleTree drillDownTree (TableInfo table, Map<Rule, Rule> ruleMap, List<Rule> rules, List<Integer> drillColumns) {
		final Integer numCols = table.dictionary.size();
		Rule rootRule = new Rule(table, new HashMap<Integer, Integer>(), numCols, table.contents.size(), true, null);
		Map<Rule, List<Rule>> childrenRules = new HashMap<Rule, List<Rule>>();
		childrenRules.put(rootRule, new ArrayList<Rule>());
		
		Set<Integer> drillColumnsSet = new HashSet<Integer>(drillColumns);
		Set<Integer> excludeColumns = new HashSet<Integer>();
		for (int col = 0; col < numCols; col++) {
			if (!drillColumnsSet.contains(col)) {
				excludeColumns.add(col);
			}
		}
		
		List<Rule> filteredRules = filterByColumn(rules, drillColumnsSet);
		excludeColumns.add(drillColumns.get(drillColumns.size() - 1));
		filteredRules.addAll(filterByColumnList(rules, drillColumns.subList(0, drillColumns.size() - 1), excludeColumns));
		List<Integer> sortColumns = new ArrayList<Integer>();
		for (int i : drillColumns){
			sortColumns.add(i);
		}
		sortColumns.add(-numCols - 1);
		for (int i = 0; i < numCols; i++) { // We add the remaining columns to put star rules first when counts and keys are equal.
			sortColumns.add(i);
		}
		Collections.sort(filteredRules, Rule.sortComparator(table, null, sortColumns));
		
		Rule keyRule = new Rule(new HashMap<Integer, Integer>(), numCols);
		
		List<Integer> lastKey = new ArrayList<Integer>();
		for (Integer j : drillColumns) {
			lastKey.add(filteredRules.get(0).get(j));
		}
		
		//Map<List<Integer>, Rule> compoundRuleMap = new HashMap<List<Integer>, Rule>();
		//Skipping first rule, because it is the empty rule.
		for (int i = 1; i < filteredRules.size(); i++) {
			final Rule currentRule = filteredRules.get(i);
			List<Integer> key = new ArrayList<Integer>();
			for (Integer j : drillColumns) {
				key.add(currentRule.get(j));
				keyRule.addVal(j, currentRule.get(j));
			}
			
			// The number of elements common between current and previous key. Tells us which subtotals to compute.
			int commonSize = key.size();
			for (int j = 0; j < key.size(); j++) {
				if (key.get(j) != lastKey.get(j)) {
					commonSize = j;
					break;
				}
			}
			
			for (int j = lastKey.size() - 2; j >= commonSize; j--) {
				Rule rule = new Rule(new HashMap<Integer, Integer>(), numCols);
				for (int k = 0; k <= j; k++) {
					rule.addVal(drillColumns.get(k), lastKey.get(k));
				}
				if (ruleMap.containsKey(rule)) {
					int totalCount = ruleMap.get(rule).count;
					Set<Integer> missing = new HashSet<Integer>();
					for (int val = 0; val < table.dictionary.get(drillColumns.get(j+1)).size(); val ++) {
						rule.addVal(drillColumns.get(j + 1), val);
						if (ruleMap.containsKey(rule)) {
							totalCount -= ruleMap.get(rule).count;
							// need to print some of these. modify the filter function to accept the incomplete ones.
						} else {
							missing.add(val);
						}
						rule.deleteVal(drillColumns.get(j + 1));
					}
					if (!missing.isEmpty()) {
						Map<Integer, Set<Integer>> valueSets = new HashMap<Integer, Set<Integer>>();
						valueSets.put(drillColumns.get(j+1), missing);
						final Rule parent = ruleMap.get(rule);
						Rule missingRule = new CompoundRule(table,  rule.deepValuesCopy().valueMap, totalCount, true, valueSets, null);
						try{
						childrenRules.get(parent).add(missingRule);
						} catch(Exception e) {
							out.println(missingRule.fullRuleStringCSV(table));
							out.println(rule.toString());
						}
						//rule.set(drillColumns.get(j + 1),  -2);
						//compoundRuleMap.put(rule, missingRule);
					}
				}
			}
			if (!childrenRules.containsKey(currentRule)) {
				childrenRules.put(currentRule, new ArrayList<Rule>());
			}
			if (filteredRules.get(i).size > drillColumns.size()) {
				Rule parent = ruleMap.get(keyRule);
				if (parent == null) {
					for (int j = drillColumns.size()-1; j >= 0; j--) {
						keyRule.deleteVal(drillColumns.get(j));
						if (ruleMap.containsKey(keyRule)) {
							parent = ruleMap.get(keyRule);
							break;
						}
					}
				}
				childrenRules.get(parent).add(currentRule);
			} else {
				keyRule.deleteVal(drillColumns.get(currentRule.size - 1));
				Rule parent = ruleMap.get(keyRule);
				if (parent == null) {
					for (int j = currentRule.size - 1; j >= 0; j--) {
						keyRule.deleteVal(drillColumns.get(j));
						if (ruleMap.containsKey(keyRule)) {
							parent = ruleMap.get(keyRule);
							break;
						}
					}
				}
				childrenRules.get(parent).add(currentRule);
			}
			lastKey = key;
		}
		
		for (int j = lastKey.size() - 2; j >= 0; j--) {
			Rule rule = new Rule(new HashMap<Integer, Integer>(), numCols);
			for (int k = 0; k <= j; k++) {
				rule.addVal(drillColumns.get(k), lastKey.get(drillColumns.get(k)));
			}
			if (ruleMap.containsKey(rule)) {
				int totalCount = ruleMap.get(rule).count;
				Set<Integer> missing = new HashSet<Integer>();
				for (int val = 0; val < table.dictionary.get(drillColumns.get(j+1)).size(); val ++) {
					rule.addVal(drillColumns.get(j + 1), val);
					if (ruleMap.containsKey(rule)) {
						totalCount -= ruleMap.get(rule).count;
					} else {
						missing.add(val);
					}
					rule.deleteVal(drillColumns.get(j + 1));
				}
				if (!missing.isEmpty()) {
					Map<Integer, Set<Integer>> valueSets = new HashMap<Integer, Set<Integer>>();
					valueSets.put(drillColumns.get(j+1), missing);
					final Rule parent = ruleMap.get(rule);
					Rule missingRule = new CompoundRule(table, rule.deepValuesCopy().valueMap, totalCount, true, valueSets, null);
					childrenRules.get(parent).add(missingRule);	
				}
			}
		}
		return new RuleTree (table, rootRule, childrenRules);
	}

	/**
	 * returns set of rules that have non-star values in includeColumns.
	 */
	public static List<Rule> filterByColumn (List<Rule> rules, Set<Integer> includeColumns) {
		return filterByColumn(rules, includeColumns, new HashSet<Integer>());
	}
	

	/**
	 * returns set of rules that have non-star values only in includeColumns and star values in excludeColumns.
	 */
	public static List<Rule> filterByColumn (List<Rule> rules, Set<Integer> includeColumns, Set<Integer> excludeColumns) {
		List<Rule> ruleSet = new ArrayList<Rule>();
		for (Rule rule : rules) {
			Boolean toSkip = false;
			for (int i = 0; i < rule.length(); i++) {
				if ((excludeColumns.contains(i) && rule.get(i) != -1) || (includeColumns.contains(i) && rule.get(i) == -1)) {
					toSkip = true;
					break;
				} 
			}
			if (!toSkip) {
				ruleSet.add(rule);
			}
		}
		return ruleSet;
	}	
	
	/**
	 * returns set of rules that have non-star values in a prefix sublist of includeColumns.
	 */
	public static List<Rule> filterByColumnList (List<Rule> rules, List<Integer> includeColumns) {
		return filterByColumnList (rules, includeColumns, new HashSet<Integer>()); 
	}	
	
	/**
	 * returns set of rules that have non-star values in a prefix sublist of includeColumns and star values in excludeColumns.
	 */
	public static List<Rule> filterByColumnList (List<Rule> rules, List<Integer> includeColumns, Set<Integer> excludeColumns) {
		List<Rule> ruleSet = new ArrayList<Rule>();
		for (Rule rule : rules) {
			Boolean toSkip = false;
			for (int i : excludeColumns) {
				if (rule.get(i) != -1) {
					toSkip = true;
					break;
				}
			}
			boolean starBegun = false;
			for (int i : includeColumns) {
				if (starBegun && rule.get(i) != -1) {
					toSkip = true;
					break;
				} else if (!starBegun && rule.get(i) == -1) {
					starBegun = true;
				}
			}
			if (!toSkip) {
				ruleSet.add(rule);
			}
		}
		return ruleSet;
	}
	
}
