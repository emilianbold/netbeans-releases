package org.netbeans.modules.iep.model;

import java.util.List;

public interface SchemaComponent extends Component {

	int getAttributeCount() throws Exception;
	
    SchemaAttribute getSchemaAttribute(int i) throws Exception;
    
    void setSchemaAttributes(List columns) throws Exception;
    
    List<SchemaAttribute> getSchemaAttributes() throws Exception;
    
    boolean hasSameSchemaAttribute(List columns) throws Exception;
    
    SchemaComponent duplicateSchema(String name);
}
