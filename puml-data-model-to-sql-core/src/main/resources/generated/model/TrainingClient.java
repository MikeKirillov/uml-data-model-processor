package puml-data-model-to-sql-core.src.main.resources.generated.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

@Table("training_client")
public class TrainingClient {
	@Id
	private int id;
	@Column("client_id")
	private AggregateReference<Client, String> client;

	public AggregateReference<Client, String> getClient() {
		return client;
	}

	public void setClient(AggregateReference<Client, String> client) {
		this.client = client;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
