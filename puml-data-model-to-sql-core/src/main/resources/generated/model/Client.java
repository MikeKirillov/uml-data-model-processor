package puml-data-model-to-sql-core.src.main.resources.generated.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import java.sql.Date;

@Table("client")
public class Client {
	@Id
	private int id;
	private String lastName;
	private String firstName;
	private String middleName;
	private String fullName;
	@Column("gender_id")
	private AggregateReference<Gender, String> gender;
	private String phoneNumber;
	private Date registrationDate;

	@Override
	public String toString() {
		return "Client{" +
			"lastName='" + lastName + '\'' +
			", firstName='" + firstName + '\'' +
			", phoneNumber='" + phoneNumber + '\'' +
			", gender='" + gender + '\'' +
			", registrationDate='" + registrationDate + '\'' +
			", fullName='" + fullName + '\'' +
			", middleName='" + middleName + '\'' +
			", id='" + id + '\'' +
			'}';
	}
}
