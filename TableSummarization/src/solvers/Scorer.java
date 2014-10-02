package solvers;

import dataextraction.TableInfo;

public interface Scorer {
	public void setScore (TableInfo table, Rule rule);
}
