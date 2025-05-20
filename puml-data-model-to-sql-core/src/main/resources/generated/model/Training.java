package com.github.mikekirillov.tdd.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.MappedCollection;
import java.util.HashSet;
import java.util.Set;
import java.util.Date;

@Table("training")
public class Training {
	@Id
	private int id;
	@Column("coach_id")
	private AggregateReference<Coach, String> coach;
	@Column("location_id")
	private AggregateReference<Location, String> location;
	private Date dateFrom;
	private Date dateTo;
	@Column("state_id")
	private AggregateReference<State, String> state;
	@MappedCollection(idColumn = "training_id")
	private Set<TrainingClient> trainingClients = new HashSet<>();

	@Override
	public String toString() {
		return "Training{" +
			"trainingClients='" + trainingClients + '\'' +
			", dateTo='" + dateTo + '\'' +
			", location='" + location + '\'' +
			", id='" + id + '\'' +
			", state='" + state + '\'' +
			", dateFrom='" + dateFrom + '\'' +
			", coach='" + coach + '\'' +
			'}';
	}
}
