package puml-data-model-to-sql-core.src.main.resources.generated.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("location")
public class Location {
	@Id
	private int id;
	private String name;

	@Override
	public String toString() {
		return "Location{" +
			"name='" + name + '\'' +
			", id='" + id + '\'' +
			'}';
	}
}
