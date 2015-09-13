package edu.pdx.cecs.orcycle;

import java.math.BigDecimal;
import java.util.ArrayList;

public class MyMath {

	public static float getAverageValueI(ArrayList<Integer> readings) {
		int sum = 0;
		for (int reading : readings) {
			sum = sum + reading;
		}
		return (float) sum / (float) readings.size();
	}

	public static float getAverageValueF(ArrayList<Float> readings) {
		float sum = 0.0f;
		for (float reading : readings) {
			sum += reading;
		}
		return sum / readings.size();
	}

	public static float getAverageValueBD(ArrayList<BigDecimal> readings) {
		float sum = 0.0f;
		for (BigDecimal reading : readings) {
			sum = sum + reading.floatValue();
		}
		return sum / readings.size();
	}

	public static float getSumSquareDifferenceI(ArrayList<Integer> values, float average) {

		float sum = 0.0f;
		float diff;

		for (float value : values) {
			diff = value - average;
			sum += (diff * diff);
		}
		return sum;
	}

	public static float getSumSquareDifferenceF(ArrayList<Float> values, float average) {

		float sum = 0.0f;
		float diff;

		for (float value : values) {
			diff = value - average;
			sum += (diff * diff);
		}
		return sum;
	}

	public static float getSumSquareDifferenceBD(ArrayList<BigDecimal> values, float average) {

		float sum = 0.0f;
		float diff;

		for (BigDecimal value : values) {
			diff = value.floatValue() - average;
			sum += (diff * diff);
		}
		return sum;
	}
}
