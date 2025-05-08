package com.github.mikekirillov.tdd.model;

import java.sql.Date;
import org.springframework.data.annotation.Id;

public class Client {
	@Id
	private int id;
	private String lastName;
	private String firstName;
	private String middleName;
	private String fullName;
	private int genderId;
	private int stateId;
	private Date registrationDate;

	public Client() {}

	public Client(int id) {
		this.id = id;
	}

	public Client(String lastName, String firstName, int stateId, int genderId, Date registrationDate, String fullName, String middleName, int id) {
		this.lastName = lastName;
		this.firstName = firstName;
		this.stateId = stateId;
		this.genderId = genderId;
		this.registrationDate = registrationDate;
		this.fullName = fullName;
		this.middleName = middleName;
		this.id = id;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public int getStateId() {
		return stateId;
	}

	public void setStateId(int stateId) {
		this.stateId = stateId;
	}

	public int getGenderId() {
		return genderId;
	}

	public void setGenderId(int genderId) {
		this.genderId = genderId;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
