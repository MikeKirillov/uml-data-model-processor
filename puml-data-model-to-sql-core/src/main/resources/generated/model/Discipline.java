package puml-data-model-to-sql-core.src.main.resources.generated.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

@Table("discipline")
public class Discipline {
	@Id
	private int id;
	private String name;
	@Column("training_type_id")
	private AggregateReference<TrainingType, String> trainingType;

	public AggregateReference<TrainingType, String> getTrainingType() {
		return trainingType;
	}

	public void setTrainingType(AggregateReference<TrainingType, String> trainingType) {
		this.trainingType = trainingType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
