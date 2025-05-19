package com.github.mikekirillov.tdd.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.MappedCollection;
import java.sql.Date;

@Table("client")
public class Client {
	@Id
	private int id;
	private String name;
	@MappedCollection(idColumn = "id")
	private Gender gender;
	@MappedCollection(idColumn = "id")
	private State state;
	private Date registrationDate;

	@Override
	public String toString() {
		return "Client{" +
			"gender='" + gender + '\'' +
			", name='" + name + '\'' +
			", registrationDate='" + registrationDate + '\'' +
			", id='" + id + '\'' +
			", state='" + state + '\'' +
			'}';
	}
}
