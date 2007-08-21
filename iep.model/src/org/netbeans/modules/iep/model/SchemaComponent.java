package org.netbeans.modules.iep.model;

import java.util.List;

public interface SchemaComponent extends Component {

	int getAttributeCount();
	
    SchemaAttribute getSchemaAttribute(int i);
    
    void setSchemaAttributes(List<SchemaAttribute> columns);
    
    List<SchemaAttribute> getSchemaAttributes();
    
    boolean hasSameSchemaAttribute(List<SchemaAttribute> columns);
    
    SchemaComponent duplicateSchema(String name);
    
    SchemaAttribute findSchemaAttribute(String attributeName);
    
    void addSchemaAttribute(SchemaAttribute sa);
    
    void removeSchemaAttribute(SchemaAttribute sa); 
}
