package puml-data-model-to-sql-core.src.main.resources.generated.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.MappedCollection;
import java.util.HashSet;
import java.util.Set;
import java.util.Date;

@Table("training")
public class Training {
	@Id
	private int id;
	private Date dateFrom;
	private Date dateTo;
	@Column("coach_id")
	private AggregateReference<Coach, String> coach;
	@Column("location_id")
	private AggregateReference<Location, String> location;
	@Column("state_id")
	private AggregateReference<State, String> state;
	@MappedCollection(idColumn = "training_id")
	private Set<TrainingClient> trainingClients = new HashSet<>();

	public Set<TrainingClient> getTrainingClients() {
		return trainingClients;
	}

	public void setTrainingClients(Set<TrainingClient> trainingClients) {
		this.trainingClients = trainingClients;
	}

	public Date getDateTo() {
		return dateTo;
	}

	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}

	public AggregateReference<Location, String> getLocation() {
		return location;
	}

	public void setLocation(AggregateReference<Location, String> location) {
		this.location = location;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public AggregateReference<State, String> getState() {
		return state;
	}

	public void setState(AggregateReference<State, String> state) {
		this.state = state;
	}

	public Date getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}

	public AggregateReference<Coach, String> getCoach() {
		return coach;
	}

	public void setCoach(AggregateReference<Coach, String> coach) {
		this.coach = coach;
	}
}
