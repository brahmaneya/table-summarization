package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import solvers.Rule;
import solvers.RuleTree;
import solvers.RuleTree.RuleNode;
import solvers.RuleTreeDisplay;
import solvers.Scorer;
import dataextraction.SampleHandler;
import dataextraction.TableInfo;

public class Experiments {
	public static void mwSpeedTest (TableInfo table, int mwMin, int mwMax, int ruleNums, Scorer scorer, String outFile) throws IOException {
		int minSampleSize = Integer.MAX_VALUE;
		int capacity = Integer.MAX_VALUE;
		SampleHandler sampleHandler = new SampleHandler(table, capacity, minSampleSize);
		PrintWriter pw = new PrintWriter(new FileWriter(outFile));
		Rule emptyRule = new Rule(new HashMap<Integer, Integer>(), table.dictionary.size());
		int numIters = 10;
		for (int mw = mwMin; mw < mwMax; mw++) {
			long totalTime = 0;
			for (int iter = 0; iter < numIters; iter++ ) {
				RuleTree ruleTree = new RuleTree(table);
				long timer = System.currentTimeMillis();
				ruleTree.expandRow(emptyRule, ruleNums, mw, scorer, sampleHandler);
				totalTime += System.currentTimeMillis() - timer;	
			}
			pw.println(mw + "\t" + totalTime/numIters);
		}
		pw.close();	
	}
	
	public static void minSSSpeedTest (TableInfo table, int mw, int minSSMin, int minSSMax, int minSSStep, int ruleNums, Scorer scorer, String outFile) throws IOException {
		int capacity = Integer.MAX_VALUE;
		PrintWriter pw = new PrintWriter(new FileWriter(outFile));
		Rule emptyRule = new Rule(new HashMap<Integer, Integer>(), table.dictionary.size());
		int numIters = 50;
		for (int minSS = minSSMin; minSS < minSSMax; minSS += minSSStep) {
			SampleHandler sampleHandler = new SampleHandler(table, capacity, minSS);
			long totalTime = 0;
			for (int iter = 0; iter < numIters; iter++ ) {
				RuleTree ruleTree = new RuleTree(table);
				long timer = System.currentTimeMillis();
				ruleTree.expandRow(emptyRule, ruleNums, mw, scorer, sampleHandler);
				totalTime += System.currentTimeMillis() - timer;	
			}
			pw.println(minSS + "\t" + totalTime/numIters);
		}
		pw.close();
	}
	
	public static void minSSErrorTest (TableInfo table, int mw, int minSSMin, int minSSMax, int minSSStep, int ruleNums, Scorer scorer, String outFile) throws IOException {
		int capacity = Integer.MAX_VALUE;
		PrintWriter pw = new PrintWriter(new FileWriter(outFile));
		Rule emptyRule = new Rule(new HashMap<Integer, Integer>(), table.dictionary.size());
		int numIters = 50;
		Map<Rule, Integer> ruleCounts = new HashMap<Rule, Integer>();
		Set<Rule> selectedRules = new HashSet<Rule>();
		{
			final int minSS = Integer.MAX_VALUE;
			SampleHandler sampleHandler = new SampleHandler(table, capacity, minSS);
			RuleTree ruleTree = new RuleTree(table);
			ruleTree.expandRow(emptyRule, 2 * ruleNums, mw, scorer, sampleHandler);
			for (RuleNode child : ruleTree.root.children) {
				ruleCounts.put(child.rule, child.rule.count);
			}
			ruleTree.contractRow(emptyRule);
			ruleTree.expandRow(emptyRule, ruleNums, mw, scorer, sampleHandler);
			for (RuleNode child : ruleTree.root.children) {
				selectedRules.add(child.rule);
			}
		}
		for (int minSS = minSSMin; minSS < minSSMax; minSS += minSSStep) {
			SampleHandler sampleHandler = new SampleHandler(table, capacity, minSS);
			long countError = 0;
			Double percentCountError = 0.0;
			Double ruleError = 0.0;
			for (int iter = 0; iter < numIters; iter++ ) {
				RuleTree ruleTree = new RuleTree(table);
				ruleTree.expandRow(emptyRule, ruleNums, mw, scorer, sampleHandler);
				for (RuleNode node : ruleTree.root.children) {
					if (ruleCounts.containsKey(node.rule)) {
						countError += Math.abs(node.rule.count - ruleCounts.get(node.rule));
						percentCountError += Math.abs((node.rule.count - ruleCounts.get(node.rule)) * 100.0 /ruleCounts.get(node.rule));
					}
					if (!selectedRules.contains(node.rule)) {
						ruleError++;
					}
				}
				percentCountError /= ruleTree.root.children.size();
			}
			pw.println(minSS + "\t" + countError/numIters + "\t" + percentCountError/numIters + "\t" + (0.0 + ruleError)/numIters);
			//System.out.println(minSS + "\t" + countError/numIters + "\t" + percentCountError/numIters + "\t" + (ruleError)/numIters);
		}
		pw.close();	
	}
}
