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
	private String phoneNumber;
	private Date registrationDate;
	private Date careerStartDate;
	@Column("discipline_id")
	private AggregateReference<Discipline, String> discipline;

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

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Date getCareerStartDate() {
		return careerStartDate;
	}

	public void setCareerStartDate(Date careerStartDate) {
		this.careerStartDate = careerStartDate;
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

	public AggregateReference<Discipline, String> getDiscipline() {
		return discipline;
	}

	public void setDiscipline(AggregateReference<Discipline, String> discipline) {
		this.discipline = discipline;
	}
}
