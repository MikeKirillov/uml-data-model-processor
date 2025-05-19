package com.github.mikekirillov.tdd.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.MappedCollection;

@Table("state")
public class State {
	@Id
	private int id;
	private String name;
	@MappedCollection(idColumn = "id")
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
