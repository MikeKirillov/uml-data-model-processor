package com.github.mikekirillov.tdd.model;

import org.springframework.data.annotation.Id;

public class State {
	@Id
	private int id;
	private String name;

	public State() {}

	public State(int id) {
		this.id = id;
	}

	public State(String name, int id) {
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
