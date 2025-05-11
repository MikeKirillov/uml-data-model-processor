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
}
