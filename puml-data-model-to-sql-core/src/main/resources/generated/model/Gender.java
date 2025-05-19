package com.github.mikekirillov.tdd.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.MappedCollection;

@Table("gender")
public class Gender {
	@Id
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
