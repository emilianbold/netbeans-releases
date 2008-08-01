package org.netbeans.modules.iep.model;

import java.util.List;

public interface SchemaComponent extends Component {

    
    void setSchemaAttributes(List<SchemaAttribute> columns);
    
    List<SchemaAttribute> getSchemaAttributes();
    
    SchemaAttribute findSchemaAttribute(String attributeName);
    
    void addSchemaAttribute(SchemaAttribute sa);
    
    void removeSchemaAttribute(SchemaAttribute sa); 
}
