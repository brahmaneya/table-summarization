package solvers;

import static java.lang.System.out;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

import dataextraction.Marketing;
import dataextraction.TableInfo;
import solvers.Rule.sizeScorer;
import solvers.RuleTree.RuleNode;

public class RuleTreeDisplay {
	RuleTree ruleTree;
	List<Integer> columnOrder;
	Set<RuleNode> hiddenRows;
	
	public RuleTreeDisplay(RuleTree ruleTree) {
		this.ruleTree = ruleTree;
		columnOrder = new ArrayList<Integer>();
		for (int i = 0; i < ruleTree.table.dictionary.size(); i++) {
			columnOrder.add(i);
		}
		hiddenRows = new HashSet<RuleNode>();
	}

	public void nonStarColumnOrder (List<Integer> keyColumns) {
		Set<Integer> nonStarColumns = findNonStarColumns();
		columnOrder = new ArrayList<Integer>();
		Set<Integer> columnSet = new HashSet<Integer>();
		for (int i : keyColumns) {
			columnOrder.add(i);
			columnSet.add(i);
		}
		for (int i : nonStarColumns) {
			if (!columnSet.contains(i)) {
				columnOrder.add(i);
				columnSet.add(i);
			}
		}
	}
	
	public Set<Integer> findNonStarColumns () {
		Set<Integer> nonStarColumns = new HashSet<Integer>();
		Stack<RuleNode> nodeStack = new Stack<RuleNode>();
		nodeStack.push(ruleTree.root);
		
		while (!nodeStack.empty()) {
			RuleNode currentNode = nodeStack.pop();
			for (int i = 0; i < ruleTree.table.dictionary.size(); i++) {
				if (currentNode.rule.get(i) != -1) {
					nonStarColumns.add(i);
				}
			}
			for (RuleNode childNode : currentNode.children) {
				if (!hiddenRows.contains(childNode)) {
					nodeStack.push(childNode);
				}
			}
		}
		return nonStarColumns;
	}
	
	/**
	 * Marks as hidden extra rows (those after the first maxKeyRepeat) for each key. 
	 * @param maxKeyRepeat
	 */
	public void markHiddenRows (Integer maxKeyRepeat) {
		Stack<RuleNode> nodeStack = new Stack<RuleNode>();
		nodeStack.push(ruleTree.root);
		
		while (!nodeStack.empty()) {
			RuleNode currentNode = nodeStack.pop();
			List<RuleNode> toAdd = new ArrayList<RuleNode>();
			int count = 0;
			for (RuleNode childNode : currentNode.children) {
				if (childNode.children.isEmpty()) {
					count++;
					if (count >= maxKeyRepeat) {
						hiddenRows.add(childNode);
						continue;
					}
				}
				toAdd.add(childNode);
			}
			Collections.reverse(toAdd);
			for (RuleNode childNode : toAdd) {
				nodeStack.push(childNode);
			}
		}
	}

	public String printRuleListTex () {
		String answer = "";
		Stack<RuleNode> nodeStack = new Stack<RuleNode>();
		nodeStack.push(ruleTree.root);
		
		while (!nodeStack.empty()) {
			RuleNode currentNode = nodeStack.pop();
			String cline = "";//"\\cline{" + Math.max((indentationDepth + 1), 1) + "-" + (columnOrder.size() + 2) + "} ";
			final Rule parentRule = currentNode.parent != null ? currentNode.parent.rule : null;
			if (parentRule == null) {
				cline = "\\cline{1-" + (columnOrder.size() + 2) + "} ";
			} else {
				for (int i = 0; i < columnOrder.size(); i++) {
					final Integer index = columnOrder.get(i);
					if (parentRule.get(index) == -1) {
						cline = cline + "\\cline{" + (i + 1) + "-" +  (i + 1) + "} ";
					}
				}
				cline = cline + "\\cline{" + (columnOrder.size() + 1) + "-" + (columnOrder.size() + 2) + "} ";
			}
			answer = answer + cline;
			answer = answer + currentNode.rule.fullRuleStringTex(ruleTree.table, columnOrder, parentRule) + "\n";
			List<RuleNode> toAdd = new ArrayList<RuleNode>();
			for (RuleNode childNode : currentNode.children) {
				if (!hiddenRows.contains(childNode)) {
					toAdd.add(childNode);
				}
			}
			Collections.reverse(toAdd);
			for (RuleNode childNode : toAdd) {
				nodeStack.push(childNode);
			}
		}
		return answer;
	}
	
	public String treeStringSparse (TableInfo table) {
		String answer = "";
		Stack<RuleNode> nodeStack = new Stack<RuleNode>();
		nodeStack.push(ruleTree.root);
		
		while (!nodeStack.empty()) {
			RuleNode currentNode = nodeStack.pop();
			for (int i = 0; i < currentNode.depth; i++) {
				answer = answer + "  ";
			}
			answer = answer + currentNode.rule.ruleStringSparse(table) + "\n";
			List<RuleNode> toAdd = new ArrayList<RuleNode>();
			for (RuleNode childNode : currentNode.children) {
				if (!hiddenRows.contains(childNode)) {
					toAdd.add(childNode);
				}
			}
			Collections.reverse(toAdd);
			for (RuleNode childNode : toAdd) {
				nodeStack.push(childNode);
			}
		}
		return answer;
	}
	
	public static void main (String[] args) throws IOException {
		TableInfo table = Marketing.parseData();
		RuleTree ruleTree = new RuleTree(table);
		RuleTreeDisplay ruleTreeDisplay = new RuleTreeDisplay(ruleTree);
		Integer ruleNums = 3;
		Integer maxRuleScore = 5;
		Scanner scanner = new Scanner(System.in);
		Scorer scorer = new Rule.sizeScorer();
		String input = "0";
		do {
			int ruleNo;
			int colNo;
			boolean toExpand;
			if (input.startsWith("-")) {
				toExpand = false;
				input = input.substring(1);
			} else {
				toExpand = true;
			}
			if (input.contains(",")) {
				String[] parts = input.split(",");
				ruleNo = Integer.parseInt(parts[0]);
				colNo = Integer.parseInt(parts[1]);
			} else {
				ruleNo = Integer.parseInt(input);
				colNo = -1;
			}
			Stack<RuleNode> nodeStack = new Stack<RuleNode>();
			nodeStack.push(ruleTree.root);
			int nodeNo = 0;
			while (!nodeStack.empty()) {
				RuleNode currentNode = nodeStack.pop();
				if(nodeNo == ruleNo) {
					if (toExpand) {
						if (colNo != -1) {
							ruleTree.expandStar(currentNode.rule, ruleNums, maxRuleScore, scorer, colNo);							
						} else {
							ruleTree.expandRow(currentNode.rule, ruleNums, maxRuleScore, scorer);
						}
					} else {
						ruleTree.contractRow(currentNode.rule);
					}	
					break;
				}
				List<RuleNode> toAdd = new ArrayList<RuleNode>();
				for (RuleNode childNode : currentNode.children) {
					if (!ruleTreeDisplay.hiddenRows.contains(childNode)) {
						toAdd.add(childNode);
					}
				}
				Collections.reverse(toAdd);
				for (RuleNode childNode : toAdd) {
					nodeStack.push(childNode);
				}
				nodeNo++;
			}	
			out.println(ruleTreeDisplay.treeStringSparse(table));
			input = scanner.nextLine();
		} while (!input.equals("end")); 
		scanner.close();
	}
}
