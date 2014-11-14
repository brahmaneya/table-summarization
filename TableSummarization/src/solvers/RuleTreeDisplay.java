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
import dataextraction.SampleHandler;
import dataextraction.TableInfo;
import solvers.Rule.sizeBitsScorer;
import solvers.Rule.sizeMinusKScorer;
import solvers.Rule.sizeScorer;
import solvers.RuleTree.RuleNode;
import utils.Experiments;

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

	private String tableHeaderTex () {
		String answer = "";
		answer = answer + "\\begin{table*} \n\\centering \n\\begin{tabular}{|";
		for (int i = 0; i < columnOrder.size(); i++) {
			answer = answer + " p{1.5cm} |";
		}
		answer = answer + " l | l |} \n\\hline ";
		boolean first = true;
		for (int i = 0; i < columnOrder.size(); i++) {
			if (!first) {
				answer = answer + " & ";
			}
			answer = answer + ruleTree.table.names.get(columnOrder.get(i)).get("column");
			first = false;
		}
		answer = answer + " & Count & Weight \\\\ \\hline \n";
		return answer;
	}
	
	private String tableFooterTex () {
		String answer = "\\hline \n\\end{tabular} \n\\caption{ADD CAPTION} \n\\end{table*} \n";
		return answer;
	}
	
	public String printRuleListTex () {
		String answer = tableHeaderTex();
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
			for (int i = 0; i < currentNode.depth; i++) {
				answer = answer + "$\\triangleright$ ";
			}
			answer = answer + currentNode.rule.fullRuleStringTex(ruleTree.table, columnOrder) + "\n";
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
		return answer + tableFooterTex();
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
		final String mw_bits_outFile = "Data_Graphs/mw_speed_bits";
		final String mw_size_outFile = "Data_Graphs/mw_speed_size";
		final String minSS_size_outFile = "Data_Graphs/minSS_speed_size";
		final String minSS_bits_outFile = "Data_Graphs/minSS_speed_bits";
		final String minSS_size_error_outFile = "Data_Graphs/minSS_error_size";
		final String minSS_bits_error_outFile = "Data_Graphs/minSS_error_bits";
		List<Integer> columns = new ArrayList<Integer>();
		final Integer firstNumColumns = 7;
		for (int i = 1; i < firstNumColumns; i++) {
			columns.add(i);
		}
		TableInfo fullTable = Marketing.parseData();
		TableInfo table = fullTable.getSubTable(columns);
		Integer ruleNums = 4;
		//Experiments.mwSpeedTest(table, 1, 20, ruleNums, new Rule.sizeBitsScorer(), mw_bits_outFile);
		//Experiments.mwSpeedTest(table, 1, 20, ruleNums, new Rule.sizeScorer(), mw_size_outFile);
		
		//Experiments.minSSSpeedTest(table, 8, 500, 8000, 500, ruleNums, new Rule.sizeScorer(), minSS_size_outFile);
		//Experiments.minSSSpeedTest(table, 20, 500, 8000, 500, ruleNums, new Rule.sizeBitsScorer(), minSS_bits_outFile);
		
		Experiments.minSSErrorTest(table, 8, 500, 8000, 500, ruleNums, new Rule.sizeScorer(), minSS_size_error_outFile);
		Experiments.minSSErrorTest(table, 20, 500, 8000, 500, ruleNums, new Rule.sizeBitsScorer(), minSS_bits_error_outFile);
				
		if(1!=2)return;
		int minSampleSize = Integer.MAX_VALUE;
		int capacity = Integer.MAX_VALUE;
		SampleHandler sampleHandler = new SampleHandler(table, capacity, minSampleSize);
		RuleTree ruleTree = new RuleTree(table);
		RuleTreeDisplay ruleTreeDisplay = new RuleTreeDisplay(ruleTree);
		Integer maxRuleScore = 5;
		Scanner scanner = new Scanner(System.in);
		Scorer scorer = new Rule.sizeBitsScorer();
		String input = "0";
		do {
			long timer = System.currentTimeMillis();
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
							ruleTree.expandStar(currentNode.rule, ruleNums, maxRuleScore, scorer, colNo, sampleHandler);							
						} else {
							ruleTree.expandRow(currentNode.rule, ruleNums, maxRuleScore, scorer, sampleHandler);
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
			//out.println(ruleTreeDisplay.treeStringSparse(table));
			out.println(ruleTreeDisplay.printRuleListTex());
			out.println("Time: " + (System.currentTimeMillis() - timer));
			input = scanner.nextLine();
		} while (!input.equals("end")); 
		scanner.close();
	}
}
