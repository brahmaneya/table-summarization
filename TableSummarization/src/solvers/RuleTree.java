package solvers;

import static java.lang.System.out;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

import solvers.RuleTree.RuleNode;
import dataextraction.SampleHandler;
import dataextraction.TableInfo;

public class RuleTree {
	public static class RuleNode {
		final public Rule rule;
		public List<RuleNode> children;
		public RuleNode parent;
		public Integer depth;
		
		public RuleNode (Rule rule) {
			this.rule = rule;
			this.children = new ArrayList<RuleNode>();
			depth = -1;
			parent = null;
		}
		
		@Override
		public String toString() {
			return String.format("Rule:\t %s", rule.toString());
		}
	}
	
	TableInfo table;
	Map<Rule, RuleNode> nodeMap;
	final public RuleNode root;
	
	/**
	 * Creates a new ruletree with only a single node, which is the empty rule. 
	 */
	public RuleTree(TableInfo table) {
		this.table = table;
		Map<Integer, Integer> valueMap = new HashMap<Integer, Integer>();
		final Integer length = table.dictionary.size();
		Rule emptyRule = new Rule(valueMap, length);
		emptyRule.score = 0;
		emptyRule.count = table.contents.size();
		emptyRule.counted = true;
		nodeMap = new HashMap<Rule, RuleNode>();
		root = new RuleNode(emptyRule);
		nodeMap.put(emptyRule, this.root);
		root.depth = 0;		
	}
	
	public RuleTree(TableInfo table, Rule rootRule, Map<Rule, List<Rule>> childrenRules) {
		this.table = table;
		
		nodeMap = new HashMap<Rule, RuleNode>();
		root = new RuleNode(rootRule);
		nodeMap.put(rootRule, this.root);
		
		for (Rule rule : childrenRules.keySet()) {
			for (Rule child : childrenRules.get(rule)) {
				RuleNode childNode = new RuleNode(child);
				nodeMap.put(child, childNode);
			}		
		}
		
		for (Rule rule : childrenRules.keySet()) {
			final RuleNode ruleNode = nodeMap.get(rule);
			for (Rule child : childrenRules.get(rule)) {
				final RuleNode childNode = nodeMap.get(child);
				ruleNode.children.add(childNode);
				childNode.parent = ruleNode; 
			}
		}
		
		Queue<RuleNode> queue = new ArrayDeque<RuleNode>();
		queue.add(root);
		root.depth = 0;
		while (!queue.isEmpty()) {
			RuleNode currentNode = queue.poll();
			final Integer currentDepth = currentNode.depth;
			for (RuleNode childNode : currentNode.children) {
				childNode.depth = currentDepth + 1;
				queue.add(childNode);
			}
		}
	}
	
	
	
	private void deleteChildren (Rule parentRule) {
		RuleNode parentNode = nodeMap.get(parentRule);
		for (RuleNode childNode : parentNode.children) {
			nodeMap.remove(childNode.rule);
		}
		parentNode.children = new ArrayList<RuleNode>();
	}
	
	private void deleteChild (Rule parentRule, Integer index) {
		final RuleNode parentNode = nodeMap.get(parentRule);
		final RuleNode childNode = parentNode.children.get(index);
		nodeMap.remove(childNode.rule);
		parentNode.children.remove(index);
	}
	
	private void addChild (Rule parentRule, Rule childRule) {
		RuleNode parentNode = nodeMap.get(parentRule);
		RuleNode childNode;
		if (nodeMap.containsKey(childRule)) {
			childNode = nodeMap.get(childRule);
			final RuleNode oldParentNode = childNode.parent;
			oldParentNode.children.remove(childNode);
		} else {
			childNode = new RuleNode(childRule);
			nodeMap.put(childRule, childNode);	
		}

		parentNode.children.add(childNode);
		childNode.parent = parentNode;
		childNode.depth = parentNode.depth + 1;
	}
	
	public void sortChildren () {
		Queue<RuleNode> queue = new ArrayDeque<RuleNode>();
		queue.add(root);
		while (!queue.isEmpty()) {
			RuleNode currentNode = queue.poll();
			final Integer currentDepth = currentNode.depth;
			final List<RuleNode> childrenList = currentNode.children;
			Collections.sort(childrenList, new Comparator<RuleNode>() {
				@Override
				public int compare(RuleNode arg0, RuleNode arg1) {
					return -arg0.rule.count.compareTo(arg1.rule.count);
				}});
			
			for (RuleNode childNode : currentNode.children) {
				queue.add(childNode);
			}
		}
	}
	
	public void contractRow (Rule rule) {
		deleteChildren(rule);
	}
	
	public void expandStar (Rule rule, Integer ruleNums, Integer maxRuleScore, final Scorer scorer, final Integer col, SampleHandler sampleHandler) throws IOException {
		Set<Rule> solutionSet = NonStarCountSolvers.getSolution (table, rule, ruleNums, maxRuleScore, scorer, col, sampleHandler);
		for (Rule solutionRule : solutionSet) {
			addChild (rule, solutionRule);
		}
	}

	public void expandRow (Rule rule, Integer ruleNums, Integer maxRuleScore, Scorer scorer, SampleHandler sampleHandler) throws IOException {
		// Modify this later to intelligently use exiting samples, etc.
		Set<Rule> solutionSet = NonStarCountSolvers.getSolution (table, rule, ruleNums, maxRuleScore, scorer, -1, sampleHandler);
		for (Rule solutionRule : solutionSet) {
			addChild (rule, solutionRule);
		}
	}
	
	public Rule getChild (Rule parentRule, Integer index) {
		final RuleNode parentNode = nodeMap.get(parentRule);
		final RuleNode childNode = parentNode.children.get(index);
		return childNode.rule;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub		
	}
}
