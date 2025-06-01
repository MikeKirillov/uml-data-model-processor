package puml-data-model-to-sql-core.src.main.resources.generated.model;

import java.sql.Date;

public class Client {
	private int id;
	private String name;
	private int genderId;
	private int stateId;
	private Date registrationDate;

	public Client() {}

	public Client(int stateId, String name, int genderId, Date registrationDate, int id) {
		this.stateId = stateId;
		this.name = name;
		this.genderId = genderId;
		this.registrationDate = registrationDate;
		this.id = id;
	}
}
