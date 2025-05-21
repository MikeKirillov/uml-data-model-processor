package puml-data-model-to-sql-core.src.main.resources.generated.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("state")
public class State {
	@Id
	private int id;
	private String name;

	@Override
	public String toString() {
		return "State{" +
			"name='" + name + '\'' +
			", id='" + id + '\'' +
			'}';
	}
}
