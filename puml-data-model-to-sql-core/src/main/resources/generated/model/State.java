package com.github.mikekirillov.tdd.model;


public class State {
	private int id;
	private String name;
	private Gender gender;

	@Override
	public String toString() {
		return "State{" +
			"gender='" + gender + '\'' +
			", name='" + name + '\'' +
			", id='" + id + '\'' +
			'}';
	}
}
