package com.github.lovasoa.bloomfilter;

public class Files {
	int id;
	double score;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	
	public Files(int id, double score) { //constructor
		super();
		this.id = id;
		this.score = score;
	}
	@Override
	public String toString() {
		return "Files [id=" + id + ", score=" + score + "]";
	}
	
	
	
	
}
