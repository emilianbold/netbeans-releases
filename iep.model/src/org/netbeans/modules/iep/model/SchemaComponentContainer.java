package org.netbeans.modules.iep.model;

import java.util.List;

public interface SchemaComponentContainer extends Component {

	List<SchemaComponent> getAllSchemaComponents();
	
	void addSchemaComponent(SchemaComponent schema);
	
	void removeSchemaComponent(SchemaComponent schema);
	
}
