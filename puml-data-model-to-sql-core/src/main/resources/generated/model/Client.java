package com.github.mikekirillov.tdd.model;

import java.sql.Date;

public class Client {
	private int id;
	private String name;
	private Gender gender;
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
