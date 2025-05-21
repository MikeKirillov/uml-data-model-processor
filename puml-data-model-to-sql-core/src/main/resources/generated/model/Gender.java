package puml-data-model-to-sql-core.src.main.resources.generated.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("gender")
public class Gender {
	@Id
	private int id;
	private String name;

	@Override
	public String toString() {
		return "Gender{" +
			"name='" + name + '\'' +
			", id='" + id + '\'' +
			'}';
	}
}
