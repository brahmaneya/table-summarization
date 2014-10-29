package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import solvers.Rule;
import solvers.RuleTree;
import solvers.RuleTreeDisplay;
import solvers.Scorer;
import dataextraction.TableInfo;

public class Experiments {
	public static void mwSpeedTest (TableInfo table, int mwMin, int mwMax, int ruleNums, Scorer scorer, String outFile) throws IOException {
		PrintWriter pw = new PrintWriter(new FileWriter(outFile));
		Rule emptyRule = new Rule(new HashMap<Integer, Integer>(), table.dictionary.size());
		int numIters = 10;
		for (int mw = mwMin; mw < mwMax; mw++) {
			long totalTime = 0;
			for (int iter = 0; iter < numIters; iter++ ) {
				RuleTree ruleTree = new RuleTree(table);
				long timer = System.currentTimeMillis();
				ruleTree.expandRow(emptyRule, ruleNums, mw, scorer);
				totalTime += System.currentTimeMillis() - timer;	
			}
			pw.println(mw + "\t" + totalTime/numIters);
		}
		pw.close();	
	}
}
