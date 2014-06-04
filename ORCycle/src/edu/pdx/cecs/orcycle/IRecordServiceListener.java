package edu.pdx.cecs.orcycle;

public interface IRecordServiceListener {
	public void updateStatus(int points, float distance, float spdCurrent, float spdMax);
}
