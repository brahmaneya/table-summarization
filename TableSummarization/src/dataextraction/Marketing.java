package dataextraction;

import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

import solvers.Rule;
import solvers.Rule.sizeBitsScorer;
import solvers.Rule.sizeSquareScorer;
import solvers.NonStarCountSolvers;
import solvers.RuleTree;
import solvers.RuleTreeDisplay;
import solvers.Scorer;
import solvers.RuleTree.RuleNode;
import utils.Experiments;

public class Marketing {
	final static String DATAFILELOCATION = "TestDatasets/Marketing/marketing.data.txt";
	
	public static void addNames (TableInfo table) {
		List<Map<String, String>> names = new ArrayList<Map<String, String>>();
		
		Map<String, String> incomes = new HashMap<String, String>();
		incomes.put("column", "Income");
		incomes.put("1", "Less than $10,000");
		incomes.put("2", "$10,000-$14,999");
		incomes.put("3", "$15,000-$19,999");
		incomes.put("4", "$20,000-$24,999");
		incomes.put("5", "$25,000-$29,999");
		incomes.put("6", "$30,000-$39,999");
		incomes.put("7", "$40,000-$49,999");
		incomes.put("8", "$50,000-$74,999");
		incomes.put("9", ">$75,000");
		incomes.put("NA", "NULL");
		names.add(incomes);
		
		Map<String, String> sex = new HashMap<String, String>();
		sex.put("column", "Gender");
		sex.put("1", "Male");
		sex.put("2", "Female");
		sex.put("NA", "NULL");
		names.add(sex);
		
		Map<String, String> maritalStatus = new HashMap<String, String>();
		maritalStatus.put("column", "Marital Status");
		maritalStatus.put("1", "Married");
		maritalStatus.put("2", "Living together");
		maritalStatus.put("3", "Divorced / separated");
		maritalStatus.put("4", "Widowed");
		maritalStatus.put("5", "Never married");
		maritalStatus.put("NA", "NULL");
		names.add(maritalStatus);
		
		Map<String, String> age = new HashMap<String, String>();
		age.put("column", "Age");
		age.put("1", "14-17");
		age.put("2", "18-24");
		age.put("3", "25-34");
		age.put("4", "35-44");
		age.put("5", "45-54");
		age.put("6", "55-64");
		age.put("7", "64+");
		age.put("NA", "NULL");
		names.add(age);
		
		Map<String, String> education = new HashMap<String, String>();
		education.put("column", "Education");
		education.put("1", "< Grade 8");
		education.put("2", "Grades 9-11");
		education.put("3", "High school");
		education.put("4", "1-3 years college");
		education.put("5", "College graduate");
		education.put("6", "Grad Study");
		education.put("NA", "NULL");
		names.add(education);
		
		Map<String, String> occupation = new HashMap<String, String>();
		occupation.put("column", "Occupation");
		occupation.put("1", "Professional / Managerial");
		occupation.put("2", "Sales Worker");
		occupation.put("3", "Factory Worker / Laborer / Driver");
		occupation.put("4", "Clerical / Service Worker");
		occupation.put("5", "Homemaker");
		occupation.put("6", "Student");
		occupation.put("7", "Military");
		occupation.put("8", "Retired");
		occupation.put("9", "Unemployed");
		occupation.put("NA", "NULL");
		names.add(occupation);
		
		Map<String, String> lived = new HashMap<String, String>();
		lived.put("column", "Time in Bay Area");
		lived.put("1", "< 1 year");
		lived.put("2", "1-3 years");
		lived.put("3", "4-6 years");
		lived.put("4", "7-10 years");
		lived.put("5", "> 10 years");
		lived.put("NA", "NULL");
		names.add(lived);
		
		Map<String, String> dualIncome = new HashMap<String, String>();
		dualIncome.put("column", "Dual Income?");
		dualIncome.put("1", "Unmarried");
		dualIncome.put("2", "Yes");
		dualIncome.put("3", "No");
		dualIncome.put("NA", "NULL");
		names.add(dualIncome);
		
		Map<String, String> personsInHouseHold = new HashMap<String, String>();
		personsInHouseHold.put("column", "Persons in household");
		for (Integer i = 1; i <= 8; i++) {
			personsInHouseHold.put(i.toString(), i.toString());
		}
		personsInHouseHold.put("9", ">= 9");
		personsInHouseHold.put("NA", "NULL");
		names.add(personsInHouseHold);
		
		Map<String, String> personsUnder18 = new HashMap<String, String>();
		personsUnder18.put("column", "Persons under 18");
		for (Integer i = 0; i < 8; i++) {
			personsUnder18.put(i.toString(), i.toString());
		}
		personsUnder18.put("9", ">= 9");
		personsUnder18.put("NA", "NULL");
		names.add(personsUnder18);
		
		Map<String, String> housing = new HashMap<String, String>();
		housing.put("column", "Housing");
		housing.put("1", "Own");
		housing.put("2", "Rent");
		housing.put("3", "with parents/family");
		housing.put("NA", "NULL");
		names.add(housing);
		

		Map<String, String> houseType = new HashMap<String, String>();
		houseType.put("column", "House Type");
		houseType.put("1", "House");
		houseType.put("2", "Condominium");
		houseType.put("3", "Apartment");
		houseType.put("4", "Mobile Home");
		houseType.put("5", "Other");
		names.add(houseType);

		Map<String, String> ethnic = new HashMap<String, String>();
		ethnic.put("column", "Ethnicity");
		ethnic.put("1", "American Indian");
		ethnic.put("2", "Asian");
		ethnic.put("3", "Black");
		ethnic.put("4", "East Indian");
		ethnic.put("5", "Hispanic");
		ethnic.put("6", "Pacific Islander");
		ethnic.put("7", "White");
		ethnic.put("8", "Other");
		ethnic.put("NA", "NULL");
		names.add(ethnic);

		Map<String, String> language = new HashMap<String, String>();
		language.put("column", "Language");
		language.put("1", "English");
		language.put("2", "Spanish");
		language.put("3", "Other");
		language.put("NA", "NULL");
		names.add(language);
		
		table.names = names;
	}
	
	public static TableInfo parseData() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(DATAFILELOCATION));
		List<List<String>> dictionary = new ArrayList<List<String>>();
		List<Map<String, Integer>> reverseDictionary = new ArrayList<Map<String, Integer>>();
		List<List<Integer>> contents = new ArrayList<List<Integer>>();
		
		String line = br.readLine();
		{
			String[] vals = line.split(" ");
			for (int i = 0; i < vals.length; i++) {
				dictionary.add(new ArrayList<String>());
				reverseDictionary.add(new HashMap<String, Integer>());
			}
		}
	
		do {
			String[] vals = line.split(" ");
			List<Integer> tuple = new ArrayList<Integer>(vals.length);
			for (int i = 0; i < vals.length; i++) {
				final String value = vals[i];
				/*if (value.equals("03")) {
					out.println(line);
					System.exit(1);
				}*/
				Map<String, Integer> columnDictionary = reverseDictionary.get(i);
				if (columnDictionary.containsKey(value)) {
					tuple.add(columnDictionary.get(value));
				} else {
					columnDictionary.put(value, columnDictionary.keySet().size());
					dictionary.get(i).add(value);
					tuple.add(columnDictionary.get(value));
				}
			}
			contents.add(tuple);
		} while ((line = br.readLine()) != null);
		
		br.close();
		
		TableInfo table = new TableInfo(dictionary, reverseDictionary, contents);
		addNames(table);
		return table;
	}
	
	public static void main(String[] args) throws IOException {
		TableInfo fullTable = Marketing.parseData();
		//out.println(System.currentTimeMillis() - timer);
		List<Integer> columns = new ArrayList<Integer>();
		final Integer firstNumColumns = 7;//9;
		for (int i = 1; i < firstNumColumns; i++) {
			columns.add(i);
		}
		TableInfo table = fullTable.getSubTable(columns);
		Integer ruleNums = Integer.parseInt(args[0]); 
		Integer maxRuleScore = Integer.parseInt(args[1]); // program input
		final Scorer scorer; // program input
		switch(args[2]) {
			case "Size" :
				scorer = new Rule.sizeScorer(); 
				break;
			case "Bits" :
				scorer = new Rule.sizeBitsScorer();
				break;
			case "Square" :
				scorer = new Rule.sizeSquareScorer();
				break;
			case "Min2" : 
				scorer = new Rule.sizeMinusKScorer(1);
				break;
			default :
				scorer = new Rule.sizeScorer();
				break;
		}
		String ruleString = args[3];
		ruleString = ruleString.substring(1, ruleString.length() - 1);
		String[] vals = ruleString.split(",");
		List<Integer> ruleVals = new ArrayList<Integer>();
		for (int col = 0; col < table.dictionary.size(); col++) {
			final String valString = vals[col];
			ruleVals.add(-1);
			for (int val = 0; val < table.dictionary.get(col).size(); val++) {
				if (table.getName(col, val).equals(valString)) {
					ruleVals.set(col, val);
					break;
				}
			}
		}
		Rule rule = new Rule(ruleVals);
		
		final List<Integer> defaultCols = new ArrayList<Integer>();
		final List<Integer> ignoreCols = new ArrayList<Integer>();
		final List<Integer> forceCols = new ArrayList<Integer>();
		String coloptsString = args[4];
		coloptsString = coloptsString.substring(1, coloptsString.length() - 1);
		String[] colopts = coloptsString.split(",");
		for (String colopt : colopts) {
			int colonLocation = colopt.lastIndexOf(":");
			final String colName = colopt.substring(0, colonLocation);
			final String status = colopt.substring(colonLocation + 1); 
			int colNo = -1;
			for (colNo = 0; colNo < table.dictionary.size(); colNo++) {
				if(table.names.get(colNo).get("column").equals(colName)) {
					break;
				}
			}
			switch (status) {
				case "Default" :
					defaultCols.add(colNo);
					break;
				case "Ignore" :
					ignoreCols.add(colNo);
					break;
				case "Force" :
					forceCols.add(colNo);
					break;
			}
		}
		Scorer modifiedScorer = new Scorer () {
			@Override
			public void setScore(TableInfo table, Rule rule) {
				for (Integer col : forceCols) {
					if (rule.get(col) == -1) {
						rule.score = 0;
						return;
					}
				}
				Rule tempRule = rule.deepValuesCopy();
				for (Integer col : ignoreCols) {
					tempRule.addVal(col, -1);
				}
				scorer.setScore(table, tempRule);
				rule.score = tempRule.score;
			}
		};
		int minSampleSize = Integer.MAX_VALUE;
		int capacity = Integer.MAX_VALUE;
		SampleHandler sampleHandler = new SampleHandler(table, capacity, minSampleSize);
		Set<Rule> solutionSet = NonStarCountSolvers.getSolution (table, rule, ruleNums, maxRuleScore, modifiedScorer, -1, sampleHandler);
		String solutionString = "[";
		boolean start = true;
		for (Rule solRule : solutionSet) {		
			if(!start) {
				solutionString = solutionString + ",";
			} else {
				start = false;				
			}
			solutionString = solutionString + "{\"vals\":[";
			for (Integer col = 0; col < table.names.size(); col++) {
				if (col > 0) {
					solutionString = solutionString + ",";
				}
				solutionString = solutionString + "\"" + table.getName(col, solRule.get(col)) + "\"";
			}
			solutionString = solutionString + "," + solRule.count;
			solutionString = solutionString + "," + solRule.score;
			solutionString = solutionString + "]}";
		}
		solutionString = solutionString + "]";
		out.println(solutionString);
	}
}
