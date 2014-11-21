package solvers;

import static java.lang.System.out;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import solvers.Rule.sizeScorer;
import dataextraction.Marketing;
import dataextraction.SampleHandler;
import dataextraction.TableInfo;
import dataextraction.TableSample;

public class NonStarCountSolvers {
	public static Integer intersectionCount (Rule r1, Rule r2, Map<Rule, Rule> ruleMap) {
		if (!Rule.areConsistent(r1, r2)) {
			return 0;
		}
		Rule union = Rule.ruleUnion(r1, r2);
		if (ruleMap.containsKey(union)) {
			return ruleMap.get(union).count;
		}
		return -1; // This means we don't know the intersection count.
	}
	
	public static Integer intersectionCount (Set<Rule> rules, Map<Rule, Rule> ruleMap) {
		if (!Rule.areConsistent(rules)) {
			return 0;
		}
		Rule union = Rule.ruleUnion(rules);
		if (ruleMap.containsKey(union)) {
			return ruleMap.get(union).count;
		}
		return -1; // This means we don't know the intersection count.
	}
	
	/**
	 * Find upper bound on the count of a rule. Finds subrules of the rule using findSubRules function for size 1, 
	 * and the superRules map for bigger sizes. ruleMap is used to get counts of the subrules.
	 */
	public static Integer countUpperBound (Rule r, Map<Rule, Rule> ruleMap) {
		int maxCount = Integer.MAX_VALUE;
		Set<Rule> subRules = r.findSubRules(1);
		for (int i = 1; i <= r.length(); i++) {
			Set<Rule> superRuleSet = new HashSet<Rule>();
			for (Rule subRule : subRules) {
				Set<Rule> subSuperRuleSet = Rule.findSubSuperRules(subRule, r, subRule.size() + 1);
				for (Rule subSuperRule : subSuperRuleSet) {
					if (ruleMap.containsKey(subSuperRule)) {
						superRuleSet.add(subSuperRule);
					}
				}
				maxCount = Math.min(maxCount, ruleMap.get(subRule).count);
			}
			subRules = superRuleSet;
		}
		return maxCount;
	}

	/**
	 * Find upper bound on the marginal value of all rule r. 
	 */
	public static Integer marginalValueUpperBound (Rule r, Map<Rule, Rule> ruleMap) {
		Integer ruleCount = Integer.MAX_VALUE;
		if (ruleMap.get(r).counted) {
			ruleCount = ruleMap.get(r).count;
		}
		int maxValue = Integer.MAX_VALUE;
		Set<Rule> subRules = r.findSubRules(1);
		for (int i = 1; i <= r.length(); i++) {
			Set<Rule> superRuleSet = new HashSet<Rule>();
			for (Rule subRule : subRules) {
				Set<Rule> subSuperRuleSet = Rule.findSubSuperRules(subRule, r, subRule.size() + 1);
				for (Rule subSuperRule : subSuperRuleSet) {
					if (ruleMap.containsKey(subSuperRule)) {
						superRuleSet.add(subSuperRule);
					}
				}
				final Rule subRuleWithInfo = ruleMap.get(subRule);
				if (subRuleWithInfo == null) {
					continue;
				}
				final int value = Math.min(ruleCount, subRuleWithInfo.count ) * (r.score - subRuleWithInfo.score) + subRuleWithInfo.maxMarginalValue;
				maxValue = Math.min(maxValue, value);
			}
			subRules = superRuleSet;
		}
		return maxValue;
	}
	
	/**
	 * Set upper bound on the marginal value of all rule r (This bound is transferred to ruleMap.get(r) later too). 
	 * Takes an upper bound on the count of the rule r as input, which allows us to get a sharper upper bound on marginal 
	 * value. Also, if count of r is already known, then putting it here helps us make the marginal value bound even tighter.
	 */
	public static void setMarginalValueUpperBound (Rule r, Integer ruleCountBound, Map<Rule, Rule> ruleMap) {
		int maxValue = Integer.MAX_VALUE;
		Set<Rule> subRules = r.findSubRules(1);
		for (int i = 1; i <= r.length(); i++) {
			Set<Rule> superRuleSet = new HashSet<Rule>();
			for (Rule subRule : subRules) {
				Set<Rule> subSuperRuleSet = Rule.findSubSuperRules(subRule, r, subRule.size() + 1);
				for (Rule subSuperRule : subSuperRuleSet) {
					if (ruleMap.containsKey(subSuperRule)) {
						superRuleSet.add(subSuperRule);
					}
				}
				final Rule subRuleWithInfo = ruleMap.get(subRule);
				if (subRuleWithInfo == null) {
					continue;
				}
				final int value = ruleCountBound * (r.score - subRuleWithInfo.score) + subRuleWithInfo.maxMarginalValue;
				maxValue = Math.min(maxValue, value);
			}
			subRules = superRuleSet;
		}
		r.maxMarginalValue = maxValue;
		if (ruleMap.containsKey(r)) {
			ruleMap.get(r).maxMarginalValue = maxValue;
		}
	}
	

	/**
	 * Find upper bound on the marginal value of all subrules of r having score <= maxRuleScore. 
	 */
	public static Integer subRuleMarginalValueUpperBound (Rule r, Map<Rule, Rule> ruleMap, Integer maxRuleScore) {
		Integer ruleCount = Integer.MAX_VALUE;
		Rule rule = ruleMap.get(r);
		if (rule != null && rule.counted) {
			ruleCount = rule.count;
		}
		int maxValue = Integer.MAX_VALUE;
		Set<Rule> subRules = r.findSubRules(1);
		for (int i = 1; i <= r.length(); i++) {
			Set<Rule> superRuleSet = new HashSet<Rule>();
			for (Rule subRule : subRules) {
				Set<Rule> subSuperRuleSet = Rule.findSubSuperRules(subRule, r, subRule.size() + 1);
				for (Rule subSuperRule : subSuperRuleSet) {
					if (ruleMap.containsKey(subSuperRule)) {
						superRuleSet.add(subSuperRule);
					}
				}
				final Rule subRuleWithInfo = ruleMap.get(subRule);
				if (subRuleWithInfo == null) {
					continue;
				}
				final int value = Math.min(ruleCount, subRuleWithInfo.count)  * (maxRuleScore - subRuleWithInfo.score) + subRuleWithInfo.maxMarginalValue;
				maxValue = Math.min(maxValue, value);
			}
			subRules = superRuleSet;
		}
		return maxValue;
	}
	
	/**
	 * Find upper bound on the marginal value of all subrules of r having score <= maxRuleScore. 
	 */
	public static Integer subRuleMarginalValueUpperBound (Rule r, Map<Rule, Rule> ruleMap, Map<Rule, Set<Rule>> superRules, 
			Integer maxRuleScore) {
		Integer ruleCount = Integer.MAX_VALUE;
		Rule rule = ruleMap.get(r);
		if (rule != null && rule.counted) {
			ruleCount = rule.count;
		}
		int maxValue = Integer.MAX_VALUE;
		Set<Rule> subRules = r.findSubRules(1);
		for (int i = 1; i <= r.length(); i++) {
			Set<Rule> superRuleSet = new HashSet<Rule>();
			for (Rule subRule : subRules) {
				final Set<Rule> ruleSuperRuleSet = superRules.get(subRule);
				for (Rule superRule : ruleSuperRuleSet) {
					if (Rule.isSubRule(superRule, r)) {
						superRuleSet.add(superRule);
					}
				}
				final Rule subRuleWithInfo = ruleMap.get(subRule);
				if (subRuleWithInfo == null) {
					continue;
				}
				final int value = Math.min(ruleCount, subRuleWithInfo.count)  * (maxRuleScore - subRuleWithInfo.score) + subRuleWithInfo.maxMarginalValue;
				maxValue = Math.min(maxValue, value);
			}
			subRules = superRuleSet;
		}
		return maxValue;
	}
	
	/**
	 * Similar to subRuleMarginalValueUpperBound but only tries to find a bound upto bestRuleMarginalValue, then terminates, since
	 * if the bound is less than bestRuleMarginalValue, we are going to delete the rule anyway.
	 */
	public static Integer subRuleMarginalValueUpperBoundLimited (Rule r, Map<List<Integer>, Rule> ruleListMap, Map<Rule, Rule> ruleMap, Map<Rule, Set<Rule>> superRules, 
			Integer maxRuleScore, Integer bestRuleMarginalValue) {
		Integer ruleCount = Integer.MAX_VALUE;
		Rule rule = ruleMap.get(r);
		if (rule != null && rule.counted) {
			ruleCount = rule.count;
		}
		int maxValue = Integer.MAX_VALUE;
		
		// First we look at immediate sub-rules of r, and hope that those will be enough to give us a bound below bestRuleMarginalValue
		List<Integer> immediateSubRuleValues = new ArrayList<Integer>();
		for (int i = 0; i < r.length(); i++) {
			immediateSubRuleValues.add(r.get(i));
		}
		for (int i : r.valueMap.keySet()) {
			immediateSubRuleValues.set(i, -1);
			Rule subRuleWithInfo = ruleListMap.get(immediateSubRuleValues);
			if (subRuleWithInfo != null) {
				final int value = Math.min(ruleCount, subRuleWithInfo.count)  * (maxRuleScore - subRuleWithInfo.score) + subRuleWithInfo.maxMarginalValue;
				maxValue = Math.min(maxValue, value);
				if (maxValue < bestRuleMarginalValue) {
					return maxValue;
				}
			}
			immediateSubRuleValues.set(i, r.get(i));
		}
		/*
		Set<Rule> immediateSubRules = r.findSubRules(r.size() - 1);
		for (Rule subRule : immediateSubRules) {
			Rule subRuleWithInfo = ruleMap.get(subRule);
			if (subRuleWithInfo != null) {
				final int value = Math.min(ruleCount, subRuleWithInfo.count)  * (maxRuleScore - subRuleWithInfo.score) + subRuleWithInfo.maxMarginalValue;
				maxValue = Math.min(maxValue, value);
				if (maxValue < bestRuleMarginalValue) {
					return maxValue;
				}
			}
		}*/
		
		//long ini = System.currentTimeMillis();
		//for (int jj = 0; jj < 10000; jj++) {} out.println("boo " + (System.currentTimeMillis() - ini)); System.exit(1);
		Set<Rule> subRules = r.findSubRules(1);
		for (int i = 1; i <= r.size() - 1; i++) {
			Set<Rule> superRuleSet = new HashSet<Rule>();
			for (Rule subRule : subRules) {
				final Set<Rule> ruleSuperRuleSet = superRules.get(subRule);
				if (ruleSuperRuleSet == null) {
					continue;
				}
				for (Rule superRule : ruleSuperRuleSet) {
					if (Rule.isSubRule(superRule, r)) {
						superRuleSet.add(superRule);
					}
				}
				final Rule subRuleWithInfo = ruleMap.get(subRule);
				if (subRuleWithInfo == null) {
					continue;
				}
				final int value = Math.min(ruleCount, subRuleWithInfo.count)  * (maxRuleScore - subRuleWithInfo.score) + subRuleWithInfo.maxMarginalValue;
				maxValue = Math.min(maxValue, value);
				if (maxValue < bestRuleMarginalValue) {
					return maxValue;
				}
			}
			subRules = superRuleSet;
		}
		return maxValue;
	}
	
	public static List<List<Integer>> getSingleCounts (TableInfo table) {
		List<List<Integer>> counts = new ArrayList<List<Integer>>();
		int numColumns = table.dictionary.size();
		for (int i = 0; i < numColumns; i++) {
			final List<Integer> colCounts = new ArrayList<Integer>();
			counts.add(colCounts);
			for (int j = 0; j < table.dictionary.get(i).size(); j++) {
				colCounts.add(0);
			}
		}
		for (List<Integer> tuple : table.contents) {
			int i=0;
			for (Integer val : tuple) {
				final List<Integer> colCounts = counts.get(i);
				colCounts.set(val, colCounts.get(val) + 1);
				i++;
			}
		}
		return counts;
	}

	public static List<List<Rule>> getSingleRulesWithMarginalValues (TableInfo table, Set<Rule> solution, Scorer scorer) {
		final Integer length = table.dictionary.size();
		List<List<Rule>> singleRules = new ArrayList<List<Rule>>();
		int numColumns = table.dictionary.size();
		for (int i = 0; i < numColumns; i++) {
			final List<Rule> colRules = new ArrayList<Rule>();
			singleRules .add(colRules);
			for (int j = 0; j < table.dictionary.get(i).size(); j++) {
				Map<Integer, Integer> valueMap = new HashMap<Integer, Integer>();
				valueMap.put(i, j);
				colRules.add(new Rule (table, valueMap, length, 0, false, scorer));
			}
		}
		for (List<Integer> tuple : table.contents) {
			int i=0;
			for (Integer val : tuple) {
				Integer maxScore = 0;
				for (Rule r : solution) {
					if (Rule.isSubRule(r, tuple) && r.score > maxScore) {
						maxScore = r.score;
					}
				}
				final Rule singleRule = singleRules.get(i).get(val);
				singleRule.count++;
				singleRule.latestCountedMarginalValue += singleRule.score - Math.min(singleRule.score, maxScore);
				i++;
			}
		}
		for (List<Rule> colRules : singleRules) {
			for (Rule rule : colRules) {
				rule.minMarginalValue = rule.maxMarginalValue = rule.latestCountedMarginalValue;
				rule.counted = true;
			}
		}
		return singleRules;
	}
	
	/**
	 * Does counting for rules that of size ruleSizeToCount that have counted set to false.
	 */
	public static void updateCounts (TableInfo table, Map<Rule, Rule> ruleMap, Integer ruleSizeToCount, Map<Rule, Set<Rule>> superRules) {
		for (List<Integer> tuple : table.contents) {
			Rule tupleRule = new Rule(tuple);
			int currentSize = 1;
			Set<Rule> rules = new HashSet<Rule>();
			Set<Rule> nextRules = tupleRule.findSubRules(currentSize);
			Set<Rule> nextToNextRules;
			do {
				currentSize++;
				if (currentSize > ruleSizeToCount) {
					rules.addAll(nextRules);
					break;
				}
				nextToNextRules = new HashSet<Rule>();
				for (Rule nextRule : nextRules) {
					final Set<Rule> successors = superRules.get(nextRule);
					for (Rule successor : successors) {
						if (Rule.isSubRule(successor, tuple)) {
							nextToNextRules.add(successor);
						}
					}
				}
				nextRules = nextToNextRules;
				//out.println(nextRules.size());
			} while (!nextToNextRules.isEmpty());
			for (Rule rule : rules) {
				if (!ruleMap.get(rule).counted) {
					ruleMap.get(rule).count++;
				}
			}
		}
		for (Rule rule : ruleMap.values()) {
			rule.counted = true;
		}
	}
	
	/**
	 * Hashes rules to be coutned according to a single value in an arbitrary column. Uses those to access potential rules coverign each tuple.
	 */
	public static void updateCountsSingleHash (TableInfo table, Map<Rule, Rule> ruleMap) {
		final Integer length = table.dictionary.size();
		Map<Integer, Map<Integer, Set<Rule>>> singleHashMap = new HashMap<Integer, Map<Integer, Set<Rule>>>();
		for (int i = 0; i < length; i++) {
			singleHashMap.put(i, new HashMap<Integer, Set<Rule>>());
			for (int j = 0; j < table.dictionary.get(i).size(); j++) {
				singleHashMap.get(i).put(j, new HashSet<Rule>());
			}
		}
		for (Rule rule : ruleMap.values()) {
			if (!rule.counted) {
				int ruleSize = rule.size();
				for (int key : rule.valueMap.keySet()) {
					if (Math.random() < 1.0/ruleSize) {
						singleHashMap.get(key).get(rule.get(key)).add(rule);
						break;
					} else {
						ruleSize--;
					}
				}
			}
		}
		
		for (List<Integer> tuple : table.contents) {
			for (int i = 0; i < length; i++) {
				final int val = tuple.get(i);
				for (Rule rule : singleHashMap.get(i).get(val)) {
					if (Rule.isSubRule(rule, tuple)) {
						rule.count++;
					}
				}
			}
		}
		for (Rule rule : ruleMap.values()) {
			rule.counted = true;
		}
	}
	
	// More efficient function for updating counts of rules of size 2. In the ends, sets all rules.counted to true.
	public static void updateCountsSizeTwo (TableInfo table, Map<Rule, Rule> ruleMap) {
		Map<List<Integer>, Rule> smallerRuleMap = new HashMap<List<Integer>, Rule>();
		for (Rule rule : ruleMap.values()) {
			if (!rule.counted && rule.size() == 2) {
				smallerRuleMap.put(rule.values, rule);
			}
		}
		List<Integer> ruleList = new ArrayList<Integer>();
		final int length = table.dictionary.size();
		for (int i = 0; i < length; i++) {
			ruleList.add(-1);
		}
		for (List<Integer> tuple : table.contents) {
			for (int i = 0; i < length; i++) {
				ruleList.set(i, tuple.get(i));
				for (int j = i + 1; j < length; j++) {
					ruleList.set(j, tuple.get(j));
					final Rule rule = smallerRuleMap.get(ruleList);
					if (rule != null) {
						rule.count++;
					}
					ruleList.set(j, -1);
				}
				ruleList.set(i, -1);
			}
		}
		for (Rule rule : ruleMap.values()) {
			if (!rule.counted && rule.size() == 2) {
				rule.counted = true;
			}
		}
	}
	
	// More efficient function for updating counts of rules of size 2. In the ends, sets all rules.counted to true.
	public static void updateCountsAndMarginalValuesSizeTwo (TableInfo table, Map<Rule, Rule> ruleMap, Set<Rule> solution) {
		Map<List<Integer>, Rule> smallerRuleMap = new HashMap<List<Integer>, Rule>();
		for (Rule rule : ruleMap.values()) {
			smallerRuleMap.put(rule.values, rule);
			if (!rule.counted && rule.size() == 2) {
				rule.count = 0;
				rule.latestCountedMarginalValue = 0;
			}
		}
		List<Integer> ruleList = new ArrayList<Integer>();
		final int length = table.dictionary.size();
		for (int i = 0; i < length; i++) {
			ruleList.add(-1);
		}
		for (List<Integer> tuple : table.contents) {
			Integer maxScore = 0;
			for (Rule r : solution) {
				if (Rule.isSubRule(r, tuple));
				maxScore = Math.max(maxScore, r.score);
			}
			
			for (int i = 0; i < length; i++) {
				ruleList.set(i, tuple.get(i));
				for (int j = i + 1; j < length; j++) {
					ruleList.set(j, tuple.get(j));
					final Rule rule = smallerRuleMap.get(ruleList);
					if (rule != null && !rule.counted) {
						rule.count++;
						rule.latestCountedMarginalValue += rule.score - Math.min(rule.score, maxScore) ;
					}
					ruleList.set(j, -1);
				}
				ruleList.set(i, -1);
			}
		}
		for (Rule rule : ruleMap.values()) {
			if (!rule.counted && rule.size() == 2) {
				rule.counted = true;
				rule.minMarginalValue = rule.maxMarginalValue = rule.latestCountedMarginalValue;
			}
		}
	}
		
	/**
	 * Updates counts and marginal values (for rules with counted set to false.
	 */
	public static void updateCountsAndMarginalValues (TableInfo table, Map<Rule, Rule> ruleMap, Integer ruleSizeToCount, 
			Map<Rule, Set<Rule>> superRules, Set<Rule> solution) {
		for (Rule rule : ruleMap.values()) {
			if (!rule.counted) {
				rule.count = 0;
				rule.latestCountedMarginalValue = 0;
			}
		}
		for (List<Integer> tuple : table.contents) {
			Integer maxScore = 0;
			for (Rule rule : solution) {
				if (Rule.isSubRule(rule, tuple)) {
					maxScore = Math.max(maxScore, rule.score);
				}
			}
			Rule tupleRule = new Rule(tuple);
			int currentSize = 1;
			Set<Rule> rules = new HashSet<Rule>();
			Set<Rule> nextRules = tupleRule.findSubRules(currentSize);
			Set<Rule> nextToNextRules;
			do {
				currentSize++;
				if (currentSize > ruleSizeToCount) {
					rules.addAll(nextRules);
					break;
				}
				nextToNextRules = new HashSet<Rule>();
				for (Rule nextRule : nextRules) {
					final Set<Rule> successors = superRules.get(nextRule);
					for (Rule successor : successors) {
						if (Rule.isSubRule(successor, tuple)) {
							nextToNextRules.add(successor);
						}
					}
				}
				nextRules = nextToNextRules;
				//out.println(nextRules.size());
			} while (!nextToNextRules.isEmpty());
			for (Rule r : rules) {
				final Rule rule = ruleMap.get(r);
				if (!rule.counted) {
					rule.count++;
					rule.latestCountedMarginalValue += rule.score - Math.min(rule.score, maxScore);
				}
			}
		}
		for (Rule rule : ruleMap.values()) {
			if (!rule.counted) {
				rule.counted = true;
				rule.minMarginalValue = rule.maxMarginalValue = rule.latestCountedMarginalValue;
			}
		}
	}

	/**
	 * Hashes rules to be counted according to a single value in an arbitrary column. Uses those to access potential rules coverign each tuple.
	 */
	public static void updateCountsAndMarginalValuesSingleHash (TableInfo table, Map<Rule, Rule> ruleMap, Set<Rule> solution) {
		final Integer length = table.dictionary.size();
		Map<Integer, Map<Integer, Set<Rule>>> singleHashMap = new HashMap<Integer, Map<Integer, Set<Rule>>>();
		for (int i = 0; i < length; i++) {
			singleHashMap.put(i, new HashMap<Integer, Set<Rule>>());
			for (int j = 0; j < table.dictionary.get(i).size(); j++) {
				singleHashMap.get(i).put(j, new HashSet<Rule>());
			}
		}
		for (Rule rule : ruleMap.values()) {
			if (!rule.counted) {
				rule.count = 0;
				rule.latestCountedMarginalValue = 0;
				int ruleSize = rule.size();
				for (int key : rule.valueMap.keySet()) {
					if (Math.random() < 1.0/ruleSize) {
						singleHashMap.get(key).get(rule.get(key)).add(rule);
						break;
					} else {
						ruleSize--;
					}
				}
			}
		}
		
		for (List<Integer> tuple : table.contents) {
			int maxScore = 0;
			for (Rule rule : solution) {
				if (Rule.isSubRule(rule, tuple)) {
					maxScore = Math.max(maxScore, rule.score);
				}
			}
			
			for (int i = 0; i < length; i++) {
				final int val = tuple.get(i);
				for (Rule rule : singleHashMap.get(i).get(val)) {
					if (Rule.isSubRule(rule, tuple)) {
						rule.count++;
						rule.latestCountedMarginalValue += rule.score - Math.min(rule.score, maxScore);
					}
				}
			}
		}
		for (Rule rule : ruleMap.values()) {
			rule.counted = true;
			rule.minMarginalValue = rule.maxMarginalValue = rule.latestCountedMarginalValue;
		}
	}
	
	/**
	 * Updates marginal values by actually making a pass through the table.
	 */
	public static void countMarginalValues (TableInfo table, List<Rule> rules, Map<Rule, Rule> ruleMap, Set<Rule> solution) {
		for (Rule rule : rules) {
			if (!solution.contains(rule)) {
				rule.latestCountedMarginalValue = 0;
			}
		}
		
		List<Rule> solutionList = new ArrayList<Rule>(solution);
		Collections.sort(solutionList, new Comparator<Rule>() {
			@Override
			public int compare(Rule arg0, Rule arg1) {
				return -arg0.score.compareTo(arg1.score);
			}
		});
		
		for (List<Integer> tuple : table.contents) {
			Rule tupleRule = new Rule(tuple);
			int solScore = 0;
			for (Rule sol : solutionList) {
				if (Rule.isSubRule(sol, tuple)){
					solScore = sol.score;
					break;
				}
			}	
			
			Set<Rule> allRules = new HashSet<Rule>();
			Set<Rule> nextRules = tupleRule.findSubRules(1);
			Set<Rule> nextToNextRules;
			do {
				nextToNextRules = new HashSet<Rule>();
				for (Rule nextRule : nextRules) {
					Set<Rule> successors = Rule.findSubSuperRules(nextRule, tupleRule, nextRule.size() + 1);
					for (Rule successor : successors) {
						if (ruleMap.containsKey(successor)) {
							nextToNextRules.add(successor);
						}
					}
				}
				allRules.addAll(nextRules);
				nextRules = nextToNextRules;
			} while (!nextToNextRules.isEmpty());
			for (Rule rule : allRules) {
				Rule r = ruleMap.get(rule);
				if (!solution.contains(rule)){
					r.latestCountedMarginalValue += r.score - Math.min(r.score, solScore);
				}
			}
		}
		
		for (Rule rule : rules) {
			rule.minMarginalValue = rule.maxMarginalValue = rule.latestCountedMarginalValue;
		}
	}
	
	/**
	 * Updates marginal score value of rule (increase is score if we add this rule to solution) when solution is the current chosen set of rules.
	 * Starts update from the topmost rule, and keeps updating until there is an unambiguous top-marginal-value rule. 
	 * We only perform step 1 and 2 of the inclusion-exclusion principle. 
	 */
	public static void estimateMarginalValues (TableInfo table, List<Rule> rules, Map<Rule, Rule> ruleMap, Set<Rule> newSolution) {
		int bestMarginalValue = 0;
		for (Rule r : rules) {
			if (r.maxMarginalValue < bestMarginalValue) {
				break;
			}
			r.minMarginalValue = r.maxMarginalValue = r.latestCountedMarginalValue;
			for (Rule sol : newSolution) {
				Integer intersectionCount = intersectionCount(r, sol, ruleMap);
				int coverScore = Math.min(r.score, sol.score);
				if (intersectionCount != -1) {
					r.maxMarginalValue -= coverScore * intersectionCount;
					r.minMarginalValue -= coverScore * intersectionCount;
				} else {
					r.minMarginalValue -= coverScore * countUpperBound(Rule.ruleUnion(r, sol), ruleMap);	
				}
			}
			bestMarginalValue = Math.max(bestMarginalValue, r.minMarginalValue);
		}
	}
	
	public static Map<Rule, Integer> getMarginalCounts (Set<Rule> solution, TableInfo table) {
		Map<Rule, Integer> marginalCounts = new HashMap<Rule, Integer>();
		List<Rule> solutionList = new ArrayList<Rule>(solution);
		Collections.sort(solutionList, new Comparator<Rule>() {
			@Override
			public int compare(Rule arg0, Rule arg1) {
				return -arg0.score.compareTo(arg1.score);
			}
		});
		for (Rule r : solutionList) {
			marginalCounts.put(r, 0);
		}
		for (List<Integer> tuple : table.contents) {
			for (Rule sol : solutionList) {
				if (Rule.isSubRule(sol, tuple)){
					marginalCounts.put(sol, marginalCounts.get(sol) + 1);
					break;
				}
			}
		}
		return marginalCounts;
	}
	
	/**
	 * Finds rule with score < maxRuleScore, that maximizes score * count, 
	 */
	public static Rule getBestRule (TableInfo table, Map<Rule, Rule> ruleMap, Map<Rule, Set<Rule>> superRules, 
			Integer maxRuleScore, Scorer scorer) throws IOException {
		final Integer length = table.dictionary.size();
		Rule bestRule = null;
		Integer bestRuleTotalScore = -1;
		Rule emptyRule = new Rule(table, new HashMap<Integer, Integer>(), length, table.contents.size(), true, scorer);
		superRules.put(emptyRule, new HashSet<Rule>());
		
		List<List<Integer>> counts = getSingleCounts(table);
		for (int col = 0; col < counts.size(); col++) {
			final List<Integer> colCounts = counts.get(col);
			for (int val = 0; val < colCounts.size(); val++) {
				Map<Integer, Integer> ruleValuesMap = new HashMap<Integer, Integer>();
				ruleValuesMap.put(col, val);
				Rule newRule = new Rule(table, ruleValuesMap, length, colCounts.get(val), true, scorer);
				ruleMap.put(newRule, newRule);
				superRules.put(newRule, new HashSet<Rule>());
				superRules.get(emptyRule).add(newRule);
				if (newRule.minMarginalValue > bestRuleTotalScore) {
					bestRuleTotalScore = newRule.minMarginalValue;
					bestRule = newRule;
				}
			}
		}
		
		Set<Rule> latestSuperRules = ruleMap.keySet();
		ruleMap.put(emptyRule, emptyRule);
		
		for (int currentSize = 2; currentSize < 7; currentSize++) {
			out.println(bestRuleTotalScore);
			Set<Rule> nextSuperRules = new HashSet<Rule>();
			for (Rule r : latestSuperRules) {
				if ((ruleMap.get(r).count * maxRuleScore >= bestRuleTotalScore)) {
					nextSuperRules.addAll(r.findSuperRules(table, currentSize));					
				}
			}
			out.println("Generated " + nextSuperRules.size() + " candidate rules of size " + currentSize);
			Iterator<Rule> ruleIter = nextSuperRules.iterator();
			while (ruleIter.hasNext()) {
				Rule rule = ruleIter.next();
				Integer maxCount = countUpperBound(rule, ruleMap);
				if (maxCount * maxRuleScore < bestRuleTotalScore) {
					ruleIter.remove();
				}
			}
			if (nextSuperRules.isEmpty()) {
				break;
			}
			out.println("Accepted " + nextSuperRules.size() + " candidate rules of size " + currentSize);
			for (Rule rule : nextSuperRules) {
				Rule r = new Rule(table, rule.valueMap, rule.length(), 0, false, scorer); // Unnecessary doubling of memory?
				Set<Rule> subRules = r.findSubRules(currentSize - 1);
				for (Rule subRule : subRules) {
					if (ruleMap.containsKey(subRule)) {
						superRules.get(subRule).add(r);
						// Breaking to avoid multiple subrules linking ot the superRule, as it is wasteful to visit it repeatedly when traversing 
						break;
					}
				}
				ruleMap.put(r, r);
				superRules.put(r, new HashSet<Rule>());
			}
			long initial = System.currentTimeMillis();
			if (currentSize == 2) {
				updateCountsSizeTwo(table, ruleMap);
			} else {
				updateCounts(table, ruleMap, currentSize, superRules);
			}
			/* TODO: Potential optimizations to try :
			 * Try using map from List<Integer> (or just integer?) whenever possible, not from Rule. Especially at bottlenecks
			 * which now includes the part between generating and accepting rules. 
			*/
			out.println(System.currentTimeMillis() - initial);
			
			latestSuperRules = nextSuperRules;
			for (Rule r : nextSuperRules) {
				Rule rule = ruleMap.get(r);
				rule.minMarginalValue = rule.count * rule.score;
				rule.maxMarginalValue = rule.count * rule.score;
				rule.latestCountedMarginalValue = rule.count * rule.score;
				if (rule.minMarginalValue > bestRuleTotalScore) {
					bestRuleTotalScore = rule.minMarginalValue;
					bestRule = rule;
				}
			}
		}	
		
		return bestRule;
	}
	
	/**
	 * Finds rule that adds the most marginal value, given the chosen solution rules. 
	 * This function starts afresh, not accessing the old values of ruleMap or superRules.
	 */
	public static Rule getBestMarginalRule (TableInfo table, Integer maxRuleScore, Set<Rule> solution, Scorer scorer, 
			Integer requiredColumn) throws IOException {
		final Integer length = table.dictionary.size();
		List<List<Rule>> singleRules = getSingleRulesWithMarginalValues (table, solution, scorer);
		Map<Rule, Set<Rule>> superRules = new HashMap<Rule, Set<Rule>>();
		Map<Rule, Rule> ruleMap = new HashMap<Rule, Rule>();
		Map<List<Integer>, Rule> ruleListMap = new HashMap<List<Integer>, Rule>();
		Rule bestRule = null;
		Integer bestMarginalRuleValue = -1;
		
		if (requiredColumn == -1) {
			for (int col = 0; col < singleRules.size(); col++) {
				final List<Rule> colRules = singleRules.get(col);
				for (int val = 0; val < colRules.size(); val++) {
					Rule rule = colRules.get(val);
					ruleMap.put(rule, rule);
					ruleListMap.put(rule.values, rule);
					superRules.put(rule, new HashSet<Rule>());
					if (rule.minMarginalValue > bestMarginalRuleValue) {
						bestMarginalRuleValue = rule.minMarginalValue;
						bestRule = rule;
					}
				}
			}			
		} else {
			final List<Rule> colRules = singleRules.get(requiredColumn);
			for (int val = 0; val < colRules.size(); val++) {
				Rule rule = colRules.get(val);
				ruleMap.put(rule, rule);
				ruleListMap.put(rule.values, rule);
				superRules.put(rule, new HashSet<Rule>());
				if (rule.minMarginalValue > bestMarginalRuleValue) {
					bestMarginalRuleValue = rule.minMarginalValue;
					bestRule = rule;
				}
			}
		}
		
		Set<Rule> latestSuperRules = ruleMap.keySet();
		
		for (int currentSize = 2; currentSize < 7; currentSize++) {
			//out.println(bestMarginalRuleValue);
			Set<Rule> nextSuperRules = new HashSet<Rule>();
			for (Rule r : latestSuperRules) {
				Rule rule = ruleMap.get(r);
				if (rule.maxMarginalValue + rule.count * (maxRuleScore - rule.score) >= bestMarginalRuleValue) {
					nextSuperRules.addAll(rule.findSuperRules(table, currentSize));					
				}
			}
			//out.println("Generated " + nextSuperRules.size() + " candidate rules of size " + currentSize);
			Iterator<Rule> ruleIter = nextSuperRules.iterator();
			while (ruleIter.hasNext()) {
				Rule rule = ruleIter.next();
				rule.setScore(table, scorer);
				//final Integer countUpperBound = countUpperBound(rule, ruleMap);
				//setMarginalValueUpperBound(rule, countUpperBound, ruleMap);
				//final Integer upperBound = rule.maxMarginalValue + (maxRuleScore - rule.score) * countUpperBound;
				final Integer upperBound = subRuleMarginalValueUpperBoundLimited(rule, ruleListMap, ruleMap, superRules, maxRuleScore, bestMarginalRuleValue);
				if (upperBound < bestMarginalRuleValue) { 
					ruleIter.remove();
				}
			}
			if (nextSuperRules.isEmpty()) {
				break;
			}
			//out.println("Accepted " + nextSuperRules.size() + " candidate rules of size " + currentSize);
			for (Rule rule : nextSuperRules) {
				Rule r = new Rule(table, rule.valueMap, rule.length(), 0, false, scorer); // Unnecessary doubling of memory?
				r.maxMarginalValue = rule.maxMarginalValue;
				Set<Rule> subRules = r.findSubRules(currentSize - 1);
				for (Rule subRule : subRules) {
					if (ruleMap.containsKey(subRule)) {
						superRules.get(subRule).add(r);
						break;
					}
				}
				ruleMap.put(r, r);
				ruleListMap.put(r.values, r);
				superRules.put(r, new HashSet<Rule>());
			}
			//long initial = System.currentTimeMillis();
			if (currentSize == 2) { // Update marginal counts here and below.
				updateCountsAndMarginalValuesSizeTwo (table, ruleMap, solution);
			} else {
				updateCountsAndMarginalValuesSingleHash (table, ruleMap, solution);	
			}
			//out.println(System.currentTimeMillis() - initial);
			
			latestSuperRules = nextSuperRules;
			for (Rule r : nextSuperRules) {
				Rule rule = ruleMap.get(r);
				if (rule.minMarginalValue > bestMarginalRuleValue) {
					bestMarginalRuleValue = rule.minMarginalValue;
					bestRule = rule;
				}
			}
		}	
		
		return bestRule;
	}
	
	/**
	 * Finds rule that adds the most marginal value, given the chosen solution rules. 
	 * This function starts afresh, not accessing the old values of ruleMap or superRules.
	 */
	public static List<Rule> getTopKRules (TableInfo table, Integer maxRuleScore, Scorer scorer, int numRules) throws IOException {
		List<Rule> solution = new ArrayList<Rule>();
		final Integer length = table.dictionary.size();
		List<List<Rule>> singleRules = getSingleRulesWithMarginalValues (table, new HashSet<Rule>(), scorer);
		Map<Rule, Set<Rule>> superRules = new HashMap<Rule, Set<Rule>>();
		Map<Rule, Rule> ruleMap = new HashMap<Rule, Rule>();
		Map<List<Integer>, Rule> ruleListMap = new HashMap<List<Integer>, Rule>();
		Integer valueThreshold = -1;
		List<Rule> candidateRules = new ArrayList<Rule>();
		
		for (int col = 0; col < singleRules.size(); col++) {
			final List<Rule> colRules = singleRules.get(col);
			for (int val = 0; val < colRules.size(); val++) {
				Rule rule = colRules.get(val);
				ruleMap.put(rule, rule);
				ruleListMap.put(rule.values, rule);
				superRules.put(rule, new HashSet<Rule>());
				candidateRules.add(rule);
			}
		}			
		Collections.sort(candidateRules);
		Collections.reverse(candidateRules);
		valueThreshold = candidateRules.get(numRules).maxMarginalValue;
				
		Set<Rule> latestSuperRules = ruleMap.keySet();
		
		for (int currentSize = 2; currentSize < 7; currentSize++) {
			//out.println(bestMarginalRuleValue);
			Set<Rule> nextSuperRules = new HashSet<Rule>();
			for (Rule r : latestSuperRules) {
				Rule rule = ruleMap.get(r);
				if (rule.maxMarginalValue + rule.count * (maxRuleScore - rule.score) >= valueThreshold) {
					nextSuperRules.addAll(rule.findSuperRules(table, currentSize));					
				}
			}
			//out.println("Generated " + nextSuperRules.size() + " candidate rules of size " + currentSize);
			Iterator<Rule> ruleIter = nextSuperRules.iterator();
			while (ruleIter.hasNext()) {
				Rule rule = ruleIter.next();
				rule.setScore(table, scorer);
				//final Integer countUpperBound = countUpperBound(rule, ruleMap);
				//setMarginalValueUpperBound(rule, countUpperBound, ruleMap);
				//final Integer upperBound = rule.maxMarginalValue + (maxRuleScore - rule.score) * countUpperBound;
				final Integer upperBound = subRuleMarginalValueUpperBoundLimited(rule, ruleListMap, ruleMap, superRules, maxRuleScore, valueThreshold);
				if (upperBound < valueThreshold) { 
					ruleIter.remove();
				}
			}
			if (nextSuperRules.isEmpty()) {
				break;
			}
			//out.println("Accepted " + nextSuperRules.size() + " candidate rules of size " + currentSize);
			for (Rule rule : nextSuperRules) {
				Rule r = new Rule(table, rule.valueMap, rule.length(), 0, false, scorer); // Unnecessary doubling of memory?
				r.maxMarginalValue = rule.maxMarginalValue;
				Set<Rule> subRules = r.findSubRules(currentSize - 1);
				for (Rule subRule : subRules) {
					if (ruleMap.containsKey(subRule)) {
						superRules.get(subRule).add(r);
						break;
					}
				}
				ruleMap.put(r, r);
				ruleListMap.put(r.values, r);
				superRules.put(r, new HashSet<Rule>());
			}
			//long initial = System.currentTimeMillis();
			if (currentSize == 2) { // Update marginal counts here and below.
				updateCountsAndMarginalValuesSizeTwo (table, ruleMap, new HashSet<Rule>());
			} else {
				updateCountsAndMarginalValuesSingleHash (table, ruleMap, new HashSet<Rule>());	
			}
			//out.println(System.currentTimeMillis() - initial);
			
			latestSuperRules = nextSuperRules;
			for (Rule r : nextSuperRules) {
				candidateRules.add(ruleMap.get(r));
			}
			Collections.sort(candidateRules);
			Collections.reverse(candidateRules);
			valueThreshold = candidateRules.get(numRules).maxMarginalValue;
		}	
		Collections.sort(candidateRules);
		Collections.reverse(candidateRules);
		return candidateRules.subList(0, numRules);
	}
	
	/**
	 * Finds rule that adds the most marginal value, given the chosen solution rules. 
	 * This function takes the existing ruleMap, etc as input, which already contains several count values and other info. 
	 */
	public static Rule getBestMarginalRule (TableInfo table, Map<Rule, Rule> ruleMap, Map<Rule, Set<Rule>> superRules, 
			Integer maxRuleScore, Set<Rule> solution, Scorer scorer) throws IOException {
		// This function yet to be implemented.
		final Integer length = table.dictionary.size();
		List<List<Rule>> singleRules = getSingleRulesWithMarginalValues (table, solution, scorer);
		Rule bestRule = null;
		Integer bestMarginalRuleValue = -1;
		Rule emptyRule = new Rule(table, new HashMap<Integer, Integer>(), length, table.contents.size(), true, scorer);
		if (!superRules.containsKey(emptyRule)) {
			superRules.put(emptyRule, new HashSet<Rule>());
		}	
		
		for (int col = 0; col < singleRules.size(); col++) {
			final List<Rule> colRules = singleRules.get(col);
			for (int val = 0; val < colRules.size(); val++) {
				Rule rule = colRules.get(val);
				ruleMap.put(rule, rule);
				if (!superRules.containsKey(rule)) {
					superRules.put(rule, new HashSet<Rule>());
				}
				superRules.get(emptyRule).add(rule);
				if (rule.minMarginalValue > bestMarginalRuleValue) {
					bestMarginalRuleValue = rule.minMarginalValue;
					bestRule = rule;
				}
			}
		}
		
		ruleMap.put(emptyRule, emptyRule);
		Set<Rule> latestSuperRules = ruleMap.keySet();
		latestSuperRules.remove(emptyRule);
		
		for (int currentSize = 2; currentSize < 7; currentSize++) {
			//out.println(bestMarginalRuleValue);
			Set<Rule> nextSuperRules = new HashSet<Rule>();
			for (Rule r : latestSuperRules) {
				Rule rule = ruleMap.get(r);
				if (rule.maxMarginalValue + rule.count * (maxRuleScore - rule.score) >= bestMarginalRuleValue) {
					nextSuperRules.addAll(rule.findSuperRules(table, currentSize));
				}
			}
			//out.println("Generated " + nextSuperRules.size() + " candidate rules of size " + currentSize);
			Iterator<Rule> ruleIter = nextSuperRules.iterator();
			while (ruleIter.hasNext()) {
				Rule rule = ruleIter.next();
				rule.setScore(table, scorer);
				final Integer countUpperBound = countUpperBound(rule, ruleMap);
				setMarginalValueUpperBound(rule, countUpperBound, ruleMap);
				final Integer upperBound = rule.maxMarginalValue + (maxRuleScore - rule.score) * countUpperBound;
				if (upperBound < bestMarginalRuleValue) { 
					ruleIter.remove();
				}
			}
			if (nextSuperRules.isEmpty()) {
				break;
			}
			//out.println("Accepted " + nextSuperRules.size() + " candidate rules of size " + currentSize);
			for (Rule rule : nextSuperRules) {
				if (ruleMap.containsKey(rule)) {
					// We don't really need to find count again, only marginal values. But it isn't that costly, and easier to code.
					// since counted = false signals that we need to re-count marginal values.
					Rule r = ruleMap.get(rule);
					r.count = 0;
					r.counted = false;
				} else{
					Rule r = new Rule(table, rule.valueMap, rule.length(), 0, false, scorer); // Unnecessary doubling of memory?
					Set<Rule> subRules = r.findSubRules(currentSize - 1);
					for (Rule subRule : subRules) {
						if (ruleMap.containsKey(subRule)) {
							superRules.get(subRule).add(r);
							break;
						}
					}
					ruleMap.put(r, r);
					superRules.put(r, new HashSet<Rule>());
				}
			}
			//long initial = System.currentTimeMillis();
			if (currentSize == 2) { // Update marginal counts here and below.
				updateCountsAndMarginalValuesSizeTwo (table, ruleMap, solution);
			} else {
				updateCountsAndMarginalValues (table, ruleMap, currentSize, superRules, solution);
			}
			//out.println(System.currentTimeMillis() - initial);
			
			latestSuperRules = nextSuperRules;
			for (Rule r : nextSuperRules) {
				Rule rule = ruleMap.get(r);
				if (rule.minMarginalValue > bestMarginalRuleValue) {
					bestMarginalRuleValue = rule.minMarginalValue;
					bestRule = rule;
				}
			}
		}	
		
		return bestRule;
	}
	
	public static void getCandidateRules (TableInfo table, Integer ruleNums, List<Rule> rules, Map<Rule, 
			Rule> ruleMap, Map<Rule, Set<Rule>> superRules, Integer maxRuleScore, Scorer scorer) throws IOException {
		final Integer length = table.dictionary.size();
		Rule emptyRule = new Rule(table, new HashMap<Integer, Integer>(), length, table.contents.size(), true, scorer);
		superRules.put(emptyRule, new HashSet<Rule>());
		
		List<List<Integer>> counts = getSingleCounts(table);
		for (int col = 0; col < counts.size(); col++) {
			final List<Integer> colCounts = counts.get(col);
			for (int val = 0; val < colCounts.size(); val++) {
				Map<Integer, Integer> ruleValuesMap = new HashMap<Integer, Integer>();
				ruleValuesMap.put(col, val);
				Rule newRule = new Rule(table, ruleValuesMap, length, colCounts.get(val), true, scorer);
				rules.add(newRule);
				ruleMap.put(newRule, newRule);
				superRules.put(newRule, new HashSet<Rule>());
				superRules.get(emptyRule).add(newRule);
			}
		}
		Collections.sort(rules);
		Collections.reverse(rules);
		rules.add(emptyRule);
		
		Set<Rule> latestSuperRules = ruleMap.keySet();
		ruleMap.put(emptyRule, emptyRule);
		
		for (int currentSize = 2; currentSize < 7; currentSize++) {
			Integer threshold = (rules.get(ruleNums - 1)).count * (rules.get(ruleNums - 1)).score;
			out.println(threshold);
			Set<Rule> nextSuperRules = new HashSet<Rule>();
			for (Rule r : latestSuperRules) {
				if ((ruleMap.get(r).count * maxRuleScore >= threshold)) {
					nextSuperRules.addAll(r.findSuperRules(table, currentSize));					
				}
			}
			out.println("Generated " + nextSuperRules.size() + " candidate rules of size " + currentSize);
			Iterator<Rule> ruleIter = nextSuperRules.iterator();
			while (ruleIter.hasNext()) {
				Rule rule = ruleIter.next();
				Integer maxCount = countUpperBound(rule, ruleMap);
				if (maxCount * maxRuleScore < threshold) {
					ruleIter.remove();
				}
			}
			// Add 'important' rules that are not candidates themselves, but have top solution candidates as subrules, since they'll be needed to find marginal values.
			// Removed for now since we still end up mostly doing re-counts. 
			/*
			for (int i = 0; i < ruleNums; i ++) {
				for (int j = i + 1; j < ruleNums; j++) {
					if (Rule.areConsistent(rules.get(i), rules.get(j))) {
						Rule union = Rule.ruleUnion(rules.get(i), rules.get(j));
						final int size = union.size();
						if (size == currentSize) {
							if (!nextSuperRules.contains(union)) {
								nextSuperRules.add(union);
							}							
						}
					}
				}
			}
			*/
			if (nextSuperRules.isEmpty()) {
				break;
			}
			out.println("Accepted " + nextSuperRules.size() + " candidate rules of size " + currentSize);
			for (Rule rule : nextSuperRules) {
				Rule r = new Rule(table, rule.valueMap, rule.length(), 0, false, scorer); // Unnecessary doubling of memory?
				Set<Rule> subRules = r.findSubRules(currentSize - 1);
				for (Rule subRule : subRules) {
					if (ruleMap.containsKey(subRule)) {
						superRules.get(subRule).add(r);
						// Breaking to avoid multiple subrules linking ot the superRule, as it is wasteful to visit it repeatedly when traversing 
						break;
					}
				}
				ruleMap.put(r, r);
				rules.add(r);
				superRules.put(r, new HashSet<Rule>());
			}
			long initial = System.currentTimeMillis();
			if (currentSize == 2) {
				updateCountsSizeTwo(table, ruleMap);
			} else {
				updateCounts(table, ruleMap, currentSize, superRules);
			}
			/* TODO: For update counts for != 2 sized rules, replace Rule with List<Integer>? i.e. at the start, create maps from List<Integer>
			 * instead of rule, and use those in the actual processing. 
			*/
			out.println(System.currentTimeMillis() - initial);
			
			latestSuperRules = nextSuperRules;
			for (Rule r : nextSuperRules) {
				Rule rule = ruleMap.get(r);
				rule.minMarginalValue = rule.count * rule.score;
				rule.maxMarginalValue = rule.count * rule.score;
				rule.latestCountedMarginalValue = rule.count * rule.score;
			}
			Collections.sort(rules);
			Collections.reverse(rules);
		}	
	}
	
	public static List<Rule> getSolution (TableInfo table, Integer ruleNums, List<Rule> rules, Map<Rule, Rule> ruleMap) {
		Set<Rule> solution = new HashSet<Rule>();
		List<Rule> solutionList = new ArrayList<Rule>();
		Set<Rule> newSolution = new HashSet<Rule>();
		for (int ruleNo = 0; ruleNo < ruleNums; ruleNo++) {
			Rule rule;
			if (rules.get(0).minMarginalValue < rules.get(1).maxMarginalValue) {
				//out.println("\n" + ruleNo + "\n" + rules.get(0).toString() + "\n" + rules.get(1).toString() + "\n");
				countMarginalValues (table, rules, ruleMap, solution);			
				newSolution = new HashSet<Rule>();
				Collections.sort(rules);
				Collections.reverse(rules);
			} 
			rule = rules.get(0);
			solution.add(rule);
			solutionList.add(rule);
			newSolution.add(rule);
			rules.remove(0);
			estimateMarginalValues (table, rules, ruleMap, newSolution);			
			Collections.sort(rules);
			Collections.reverse(rules);
			//out.println(rule.toString());
			//out.println(rule.ruleString(table) + "\t" + rule.size + "\t" + rule.count + "\t" + rule.minMarginalValue);
		}
		return solutionList;
	}
	
	public static Set<Rule> getSolution (TableInfo table, Integer ruleNums, Integer maxRuleScore, Scorer scorer, 
			Integer requiredColumn) throws IOException {
		Set<Rule> solutionSet = new HashSet<Rule>();
		for (int i = 0; i < ruleNums; i++) {
			//long initial = System.currentTimeMillis();
			//Rule r = getBestMarginalRule(table, ruleMap, superRules, maxRuleScore, solutionSet);
			Rule r = getBestMarginalRule(table, maxRuleScore, solutionSet, scorer, requiredColumn);
			solutionSet.add(r);
			//out.printf("Iteration %d, Time %d\n", i, System.currentTimeMillis() - initial);
		}
		return solutionSet;
	}
	
	/**
	 * Returns solution set for the part of the table covered by baseRule. Each rule in the solution is required to have a non-star
	 * value in the requiredColumn. requiredColumn = -1 means there is not required Column.
	 * First creates a sample of tuples that satisfy baseRule, and runs to normal function for the reduced sample, then 
	 * modifies results to add in the components of baseRule.
	 */
	public static Set<Rule> getSolution (TableInfo table, Rule baseRule, Integer ruleNums, Integer maxRuleScore, final Scorer scorer,
			Integer requiredColumn, SampleHandler sampleHandler) throws IOException {
		Integer sampleSize = sampleHandler.minSampleSize;
		final TableSample sample = TableSample.createSample(table, baseRule, sampleSize);
		final TableInfo origTable = table;
		Scorer sampleScorer = new Scorer () {
			@Override
			public void setScore(TableInfo table, Rule rule) {
				final Rule expandedRule = sample.expandRule(rule);
				scorer.setScore(origTable, expandedRule);
				rule.score = expandedRule.score;
			}
		};
		Integer requiredSampleColumn = requiredColumn == -1 ? -1 : sample.reverseColumnMapping.get(requiredColumn);
		Set<Rule> truncatedSolutionSet = getSolution(sample, ruleNums, maxRuleScore, sampleScorer, requiredSampleColumn);
		Set<Rule> solutionSet = new HashSet<Rule>();
		for (Rule truncatedSolutionRule : truncatedSolutionSet) {
			Rule expandedRule = sample.expandRule(truncatedSolutionRule, scorer);
			solutionSet.add(expandedRule);
		}
		return solutionSet;
	}
	
	public static void main (String[] args) throws IOException {
		TableInfo table = Marketing.parseData();
		List<Integer> cols = new ArrayList<Integer>();
		final Integer firstNumColumns = 7;
		for (int i = 1; i < firstNumColumns; i++) {
			cols.add(i);
		}
		TableInfo subTable = table.getSubTable(cols);
		List<Rule> topKRules = getTopKRules (subTable, 5, new Rule.sizeScorer(), 8);
		for (Rule r : topKRules) {
			out.println(r.fullRuleStringTex(subTable));
		}
		if (1!=2) return;
		Integer maxRuleScore = 5; // We will only consider rules scoring upto this. This parameter is important as it determines out threshold for which smaller rules to drop.
		Map<Rule, Rule> ruleMap = new HashMap<Rule, Rule>();
		
		/* //delstart
		Map<List<Integer>, Rule> ml = new HashMap<List<Integer>, Rule>();
		Map<Rule, Rule> mr = new HashMap<Rule, Rule>();
		for (int i = 0; i < 1000; i++) {			
			List<Integer> l = new ArrayList<Integer>();
			Map<Integer, Integer> m = new HashMap<Integer, Integer>();
			for (int j = 0; j < 15; j++) {
				if (Math.random() < 0.2) {
					final int val = (int) (10 * Math.random());
					l.add(val);
					m.put(j, val);
				} else {
					l.add(-1);
				}
			}
			Rule r = new Rule(l);
			ml.put(l, r);
			mr.put(r, r);
		}
		long init;
		init = System.currentTimeMillis();
		for(int i=0; i < table.contents.size(); i++) {
			int iter = 0;
			for (Rule rule : mr.keySet()) {
				//if (!mr.get(rule).counted) {
				//	mr.get(rule).count++;
				//}
				iter++;
				if (iter > 1000) break;
				mr.get(rule);
			}
		}
		out.println(System.currentTimeMillis() - init);
		init = System.currentTimeMillis();
		for(int i=0 ; i < table.contents.size(); i++) {
			int iter = 0;
			for (List<Integer> rule : ml.keySet()) {
				//if (!ml.get(rule).counted) {
					//ml.get(rule).count++;
				//}
				iter++;
				if (iter > 1000) break;
				ml.get(rule);
			}
		}
		out.println(System.currentTimeMillis() - init );
		System.exit(1);
		//delend */
		
		Map<Integer, Integer> bigRuleMap = new HashMap<Integer, Integer>();
		bigRuleMap.put(9, 0);
		bigRuleMap.put(12, 0);
		bigRuleMap.put(13, 1);
		Scorer scorer = new Rule.sizeScorer();
		Rule bigRule = new Rule(table, bigRuleMap, table.dictionary.size(), 0, false, scorer);
		
		Integer ruleNums = 5;
		//getSolution (table, ruleNums, maxRuleScore);
		Integer requiredColumn = -1;
		int minSampleSize = Integer.MAX_VALUE;
		int capacity = Integer.MAX_VALUE;
		SampleHandler sampleHandler = new SampleHandler(table, capacity, minSampleSize);
		Set<Rule> solutionSet = getSolution (table, bigRule, ruleNums, maxRuleScore, scorer, requiredColumn, sampleHandler);
		out.println(solutionSet.toString());
		if(1!=2)return;
		List<Rule> rules = new ArrayList<Rule>();
		//getBestRule(table, ruleMap, superRules, maxRuleScore);
		getBestMarginalRule(table, maxRuleScore, new HashSet<Rule>(), scorer, requiredColumn);
		//getCandidateRules(table, ruleNums, rules, ruleMap, superRules, maxRuleScore);
		if (1!=2) return;
		
		Map<Rule, Integer> marginalCounts = new HashMap<Rule, Integer>();
		/*
		 * Filtering
		 
		Set<Integer> filterColumns = new HashSet<Integer>();
		filterColumns.add(1);		
		filterColumns.add(2);		
		List<Integer> filterColumnsList = new ArrayList<Integer>(filterColumns);
		out.println(filterColumnsList.toString());
		List<Rule> drillDown = filterByColumn(rules, filterColumns);
		Collections.sort(drillDown, Rule.sortComparator(table, marginalCounts, filterColumnsList));
		for (Rule rule : drillDown) {
			out.println(rule.fullRuleStringTex(table, filterColumnsList, marginalCounts));
		}
		if (1!=2) return;
		*/
		List<Rule> solutionList = getSolution(table, ruleNums, rules, ruleMap);
		Set<Rule> solution = new HashSet<Rule>(solutionList);
		marginalCounts = getMarginalCounts(solution, table);
		
		Integer totalCount = 0;
		Integer totalScore = 0;
		
		List<Integer> columns = new ArrayList<Integer>();
		columns.add(1);
		columns.add(2);
		columns.add(3);
		columns.add(4);
		
		List<Integer> sortColumns = new ArrayList<Integer>();
		sortColumns.add(1);
		sortColumns.add(table.dictionary.size() + 2);
		Collections.sort(solutionList, Rule.sortComparator(table, marginalCounts, sortColumns));
		
		/*Collections.sort(solutionList, new Comparator<Rule>(){
			@Override
			public int compare(Rule arg0, Rule arg1) {
				int index = 1;
				Integer val0 = arg0.rule.get(index);
				Integer val1 = arg1.rule.get(index);
				if (val0 != val1) {
					return Integer.compare(val0,  val1);
				}
				return Integer.compare(arg0.size, arg1.size);
			}
		});*/
		
		for(Rule rule : solutionList) {
			totalCount += marginalCounts.get(rule);
			totalScore += rule.score * marginalCounts.get(rule);
			out.println(rule.fullRuleStringTex(table, columns, marginalCounts));
		}
		out.println(totalCount);
		out.println(totalScore);
	}
}
