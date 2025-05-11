package com.github.mikekirillov.tdd.model;

import org.springframework.data.annotation.Id;

public class Gender {
	@Id
	private int id;
	private String name;

	public Gender() {}

	@Override
	public String toString() {
		return "Gender{" +
			"name='" + name + '\'' +
			", id='" + id + '\'' +
		'}';
	}
}
