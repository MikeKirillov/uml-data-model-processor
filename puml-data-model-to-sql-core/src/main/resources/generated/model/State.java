package com.github.mikekirillov.tdd.model;

import org.springframework.data.annotation.Id;

public class State {
	@Id
	private int id;
	private String name;

	public State() {}
}
