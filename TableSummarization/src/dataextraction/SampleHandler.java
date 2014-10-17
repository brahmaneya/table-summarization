package dataextraction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import solvers.Rule;
import solvers.Scorer;

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
		Set<TableSample> superRuleSamples = new HashSet<TableSample>();
		for (TableSample sample : samples) {
			currentNumRows += sample.size();
			if (sample.filterRule.equals(filterRule)) {
				result = sample;
			} else if (Rule.isSubRule(filterRule, sample.filterRule)) {
				superRuleSamples.add(sample);
			}
		}
		if (result != null) {
			return result;
		} else if (!superRuleSamples.isEmpty()) {
			int tupleCount = 0;
			Scorer scorer = new Rule.nullScorer();
			for (TableSample sample : superRuleSamples) {
				Rule contractedRule = sample.truncateRule(filterRule, scorer);
				int sampleTupleCount = 0;
				final int numSelectivitySamples = 100;
				for (int i = 0; i < numSelectivitySamples; i++) {
					final int tupleNo = (int)(Math.random() * sample.contents.size());
					final List<Integer> sampleTuple = sample.contents.get(tupleNo);
					if (Rule.isSubRule(contractedRule, sampleTuple)) {
						sampleTupleCount++;
					}
				}
				sampleTupleCount *= sample.contents.size() / numSelectivitySamples;
				tupleCount += sampleTupleCount;
			}
			if (tupleCount > minSampleSize) {
				// result = union. 
				return result;
			}
		} 
		if (currentNumRows + minSampleSize < capacity) {
			TableSample newSample = TableSample.createSample(table, filterRule, minSampleSize);
			return newSample;
		} else {
			// Change this obviously. Delete existing sample?
			return result;
		}
	}
}
