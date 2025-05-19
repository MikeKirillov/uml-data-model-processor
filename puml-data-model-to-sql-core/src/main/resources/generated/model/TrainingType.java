package com.github.mikekirillov.tdd.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("training_type")
public class TrainingType {
	@Id
	private int id;
	private String name;

	@Override
	public String toString() {
		return "TrainingType{" +
			"name='" + name + '\'' +
			", id='" + id + '\'' +
			'}';
	}
}
