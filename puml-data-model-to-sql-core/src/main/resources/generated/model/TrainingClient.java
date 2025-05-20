package com.github.mikekirillov.tdd.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

@Table("training_client")
public class TrainingClient {
	@Id
	private int id;
	@Column("client_id")
	private AggregateReference<Client, String> client;

	@Override
	public String toString() {
		return "TrainingClient{" +
			"client='" + client + '\'' +
			", id='" + id + '\'' +
			'}';
	}
}
