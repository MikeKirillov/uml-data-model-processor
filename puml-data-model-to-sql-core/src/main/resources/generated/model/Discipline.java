package com.github.mikekirillov.tdd.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

@Table("discipline")
public class Discipline {
	@Id
	private int id;
	private String name;
	@Column("training_type_id")
	private AggregateReference<TrainingType, String> trainingType;

	@Override
	public String toString() {
		return "Discipline{" +
			"trainingType='" + trainingType + '\'' +
			", name='" + name + '\'' +
			", id='" + id + '\'' +
			'}';
	}
}
