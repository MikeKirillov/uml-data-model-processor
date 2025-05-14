package com.github.mikekirillov.tdd.model;


public class Gender {
	private int id;
	private String name;

	@Override
	public String toString() {
		return "Gender{" +
			"name='" + name + '\'' +
			", id='" + id + '\'' +
			'}';
	}
}
