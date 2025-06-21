package com.github.mikekirillov.icebox.pojo.model;


public class State {
	private int id;
	private String name;
	private int genderId;

	public State() {}

	public State(String name, int genderId, int id) {
		this.name = name;
		this.genderId = genderId;
		this.id = id;
	}
}
