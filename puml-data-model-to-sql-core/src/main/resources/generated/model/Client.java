package com.github.mikekirillov.tdd.model;

import org.springframework.data.annotation.Id;
import java.sql.Date;

public class Client {
	@Id
	private int id;
	private String lastName;
	private String firstName;
	private String middleName;
	private String fullName;
	private Gender gender;
	private State state;
	private Date registrationDate;

	public Client() {}

	@Override
	public String toString() {
		return "Client{" +
			"lastName='" + lastName + '\'' +
			", firstName='" + firstName + '\'' +
			", gender='" + gender + '\'' +
			", registrationDate='" + registrationDate + '\'' +
			", fullName='" + fullName + '\'' +
			", middleName='" + middleName + '\'' +
			", id='" + id + '\'' +
			", state='" + state + '\'' +
		'}';
	}
}
