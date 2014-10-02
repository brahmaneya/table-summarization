package solvers;

import static java.lang.System.out;

import java.util.List;
import java.util.Map;
import java.util.Set;

import dataextraction.TableInfo;

/**
 * A compound rule allowd some fields of the 'rule' vector to be sets of integers instead of integers. So it is effectively a union over
 * multiple rules. e.g. a rule (a, {b,c}, d) covers both tuples covered by (a,b,d) and (a,c,d).
 * 
 * These sets are stored in the ValueSets map. The 'rule' integer list has a -2 when there is a set instead of a single number.
 */
public class CompoundRule extends Rule {

	final static Integer SETVALUE = -2;
	
	Map<Integer, Set<Integer>> valueSets; // Maps field number to the corresponding set of integers.
	
	public CompoundRule(TableInfo table, List<Integer> rule, Integer count,
			Boolean counted, Map<Integer, Set<Integer>> valueSets, Scorer scorer) {
		super(table, rule, count, counted, scorer);
		
		this.valueSets = valueSets;
		for (Integer i : valueSets.keySet()) {
			rule.set(i,  SETVALUE);
			valueMap.put(i, SETVALUE);
		}
	}
	
	public CompoundRule(TableInfo table, Map<Integer, Integer> valueMap, Integer count,
			Boolean counted, Map<Integer, Set<Integer>> valueSets, Scorer scorer) {
		super(table, valueMap, table.dictionary.size(), count, counted, scorer);
		
		this.valueSets = valueSets;
		for (Integer i : valueSets.keySet()) {
			values.set(i,  SETVALUE);
			valueMap.put(i, SETVALUE);
		}
		size = valueMap.keySet().size();
		setScore(table, scorer);
		minMarginalValue = maxMarginalValue = latestCountedMarginalValue = score * count;
	}
	
	@Override
	public String fullRuleStringCSV (TableInfo table) {
		String ruleString = "";
		for (Integer i = 0; i < length(); i++) {
			final Integer index = i;
			final Integer val = values.get(i);
			if (i > 0) {
				ruleString = ruleString + ",";
			}
			if (val == -1) {
				ruleString = ruleString + "\"*\"";
			} else if (val == -2) {
				ruleString = ruleString + "\"" ;
				Set<Integer> values = valueSets.get(i);
				boolean started = false;
				for (int value : values) {
					if (started) {
						ruleString = ruleString + " or ";
					}
					ruleString = ruleString + table.names.get(index).get(table.dictionary.get(index).get(value));
					started = true;
				}
				ruleString = ruleString + "\"" ;
			} else {
				ruleString = ruleString + "\"" + table.names.get(index).get(table.dictionary.get(index).get(val)) + "\"";
			}
		}
		ruleString = ruleString + ",\"" + count + "\",\"" + size + "\"";
		return ruleString;
	}
	
	public String fullRuleStringTex (TableInfo table, List<Integer> columns, Integer indentationDepth) {
		String ruleString = "";
		for (Integer i = 0; i < columns.size(); i++) {
			final Integer index = columns.get(i);
			Integer val = values.get(index);
			if (i > 0) {
				ruleString = ruleString + " & ";
			}
			if (i < indentationDepth) {
				ruleString = ruleString + "$\\triangleright$ ";
			}
			if (val == -1) {
				ruleString = ruleString + "$\\star$";
			} else if (val == -2) {
				final Set<Integer> valueSet = valueSets.get(index);
				boolean started = false;
				for (int value : valueSet) {
					if (started) {
						ruleString = ruleString + " or ";
					}
					ruleString = ruleString + (table.names.get(index).get(table.dictionary.get(index).get(value))).replaceAll("\\$", "\\\\\\$");
					started = true;
				}
			} else {
				ruleString = ruleString + (table.names.get(index).get(table.dictionary.get(index).get(val))).replaceAll("\\$", "\\\\\\$");
			}
		}
		ruleString = ruleString + " & $" + count + "$ & $" + size + "$ \\\\";
		return ruleString;
	}
	
	public String fullRuleStringTex (TableInfo table, List<Integer> columns, List<Integer> parentRule) {
		String ruleString = "";
		for (Integer i = 0; i < columns.size(); i++) {
			final Integer index = columns.get(i);
			Integer val = values.get(index);
			if (i > 0) {
				ruleString = ruleString + " & ";
			}
			if (val == -1) {
				ruleString = ruleString + "$\\star$";
			} else if (val == -2) {
				Set<Integer> values = valueSets.get(index);
				boolean started = false;
				for (int value : values) {
					if (started) {
						ruleString = ruleString + " or ";
					}
					ruleString = ruleString + (table.names.get(index).get(table.dictionary.get(index).get(value))).replaceAll("\\$", "\\\\\\$");
					started = true;
				}
			} else {
				if (parentRule.get(index) != -1) {
					ruleString = ruleString + "$\\triangleright$ ";
				}
				ruleString = ruleString + (table.names.get(index).get(table.dictionary.get(index).get(val))).replaceAll("\\$", "\\\\\\$");
			}
		}
		ruleString = ruleString + " & $" + count + "$ & $" + size + "$ \\\\";
		return ruleString;
	}
	
	public String fullRuleStringTex (TableInfo table, List<Integer> columns) {
		String ruleString = "";
		for (Integer i = 0; i < columns.size(); i++) {
			final Integer index = columns.get(i);
			Integer val = values.get(index);
			if (i > 0) {
				ruleString = ruleString + " & ";
			}
			if (val == -1) {
				ruleString = ruleString + "$\\star$";
			} else if (val == -2) {
				final Set<Integer> valueSet = valueSets.get(index);
				boolean started = false;
				for (int value : valueSet) {
					if (started) {
						ruleString = ruleString + " or ";
					}
					ruleString = ruleString + (table.names.get(index).get(table.dictionary.get(index).get(value))).replaceAll("\\$", "\\\\\\$");
					started = true;
				}
			} else {
				ruleString = ruleString + (table.names.get(index).get(table.dictionary.get(index).get(val))).replaceAll("\\$", "\\\\\\$");
			}
		}
		ruleString = ruleString + " & $" + count + "$ & $" + size + "$ \\\\";
		return ruleString;
	}
	
	public String fullRuleStringTex (TableInfo table) {
		String ruleString = "";
		for (Integer i = 0; i < length(); i++) {
			final Integer index = i;
			Integer val = values.get(index);
			if (i > 0) {
				ruleString = ruleString + " & ";
			}
			if (val == -1) {
				ruleString = ruleString + "$\\star$";
			} else if (val == -2) {
				Set<Integer> values = valueSets.get(index);
				boolean started = false;
				for (int value : values) {
					if (started) {
						ruleString = ruleString + " or ";
					}
					ruleString = ruleString + (table.names.get(index).get(table.dictionary.get(index).get(value))).replaceAll("\\$", "\\\\\\$");
					started = true;
				}
			} else {
				ruleString = ruleString + (table.names.get(index).get(table.dictionary.get(index).get(val))).replaceAll("\\$", "\\\\\\$");
			}
		}
		ruleString = ruleString + " & $" + count + "$ & $" + size + "$ \\\\";
		return ruleString;
	}
}
