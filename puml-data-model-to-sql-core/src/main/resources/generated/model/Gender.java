package com.github.mikekirillov.tdd.model;

import org.springframework.data.annotation.Id;

public class Gender {
	@Id
	private int id;
	private String name;

	public Gender() {}

	public Gender(int id) {
		this.id = id;
	}

	public Gender(String name, int id) {
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
