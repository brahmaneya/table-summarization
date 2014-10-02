package dataextraction;

import java.util.HashSet;
import java.util.Set;

import solvers.Rule;

/**
 * This class is responsible for maintaining a set of samples (in-memory), and decided when to generate a new sample, 
 * when to discard a sample to free memory, and which sample(s) to use for evaluating a query. 
 */
public class SampleHandler {
	TableInfo table;
	int minSampleSize; // Minimum sample size we need (depends on how accurate we want our numbers to be, and how low selectivity rules we want to detect.
	int capacity; // Maximum number of rows we can store across all samples. Maybe change this to memory instead of rows.
	Set<TableSample> samples; // Set of current samples.
	
	
	public SampleHandler (TableInfo table, int capacity, int minSampleSize) {
		this.table = table;
		this.capacity = capacity;
		this.minSampleSize = minSampleSize;
		samples = new HashSet<TableSample>();
	}
	
	/**
	 * Returns a sample for querying (finding best covering rules) the portion of the table covered by filterRule. May need to create
	 * this sample from the table, or using existing samples, and may have to remove some existing sample to create space.
	 */
	public TableSample getSampleForRule (Rule filterRule) {
		TableSample result = null;
		int currentNumRows = 0;
		for (TableSample sample : samples) {
			currentNumRows += sample.size();
			if (sample.filterRule.equals(filterRule)) {
				result = sample;
			} else if (Rule.isSubRule(filterRule, sample.filterRule)) {
				// Use particle filtering? Take tuples from all samples of sub-filterRules, 
			}
		}
		if (result != null) {
			return result;
		} else if (currentNumRows + minSampleSize < capacity) {
			TableSample newSample = TableSample.createSample(table, filterRule, minSampleSize);
			return newSample;
		} else {
			// Change this obviously. Delete existing sample?
			// Also check if we can make a sample from existing samples instead of table.
			return null;
		}
	}
}
