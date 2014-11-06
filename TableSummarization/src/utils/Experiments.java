package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

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
		{
			final int minSS = Integer.MAX_VALUE;
			SampleHandler sampleHandler = new SampleHandler(table, capacity, minSS);
			RuleTree ruleTree = new RuleTree(table);
			ruleTree.expandRow(emptyRule, ruleNums, mw, scorer, sampleHandler);
			for (RuleNode child : ruleTree.root.children) {
				ruleCounts.put(child.rule, child.rule.count);
			}
		}
		for (int minSS = minSSMin; minSS < minSSMax; minSS += minSSStep) {
			SampleHandler sampleHandler = new SampleHandler(table, capacity, minSS);
			long countError = 0;
			int ruleError = 0;
			for (int iter = 0; iter < numIters; iter++ ) {
				RuleTree ruleTree = new RuleTree(table);
				ruleTree.expandRow(emptyRule, ruleNums, mw, scorer, sampleHandler);
				for (RuleNode node : ruleTree.root.children) {
					if (ruleCounts.containsKey(node.rule)) {
						countError += Math.abs(node.rule.count - ruleCounts.get(node.rule));
					} else {
						ruleError++;
					}
				}
			}
			pw.println(minSS + "\t" + countError/numIters + "\t" + (0.0 + ruleError)/numIters);
		}
		pw.close();	
	}
}
