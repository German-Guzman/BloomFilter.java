package com.github.lovasoa.bloomfilter;

import java.util.Comparator;

public class ScoreComparator implements Comparator<Files> {

	@Override
	public int compare(Files file1, Files file2) {       //compares scores. 
		return Double.compare(file1.getScore(), file2.getScore());
	}
}
