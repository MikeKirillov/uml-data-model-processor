package puml-data-model-to-sql-core.src.main.resources.generated.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import java.sql.Date;

@Table("coach")
public class Coach {
	@Id
	private int id;
	private String lastName;
	private String firstName;
	private String middleName;
	private String fullName;
	@Column("discipline_id")
	private AggregateReference<Discipline, String> discipline;
	private String phoneNumber;
	private Date registrationDate;
	private Date careerStartDate;

	@Override
	public String toString() {
		return "Coach{" +
			"lastName='" + lastName + '\'' +
			", firstName='" + firstName + '\'' +
			", phoneNumber='" + phoneNumber + '\'' +
			", careerStartDate='" + careerStartDate + '\'' +
			", registrationDate='" + registrationDate + '\'' +
			", fullName='" + fullName + '\'' +
			", middleName='" + middleName + '\'' +
			", id='" + id + '\'' +
			", discipline='" + discipline + '\'' +
			'}';
	}
}
