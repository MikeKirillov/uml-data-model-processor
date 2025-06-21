package com.github.mikekirillov.icebox.pojo.model;

import java.time.LocalDateTime;

public class Client {
	private int id;
	private String name;
	private int genderId;
	private int stateId;
	private LocalDateTime registrationDate;

	public Client() {}

	public Client(int stateId, String name, int genderId, LocalDateTime registrationDate, int id) {
		this.stateId = stateId;
		this.name = name;
		this.genderId = genderId;
		this.registrationDate = registrationDate;
		this.id = id;
	}
}
