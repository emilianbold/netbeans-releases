package org.netbeans.modules.iep.model;

public interface SchemaAttribute extends Component {

	public static final String PROP_NAME = "name";
	
	public static final String PROP_TYPE = "type";
	
	public static final String PROP_SIZE = "size";
	
	public static final String PROP_SCALE = "scale";
	
	public static final String PROP_COMMENT = "comment";
	
	String getAttributeName();
    
	void setAttributeName(String attributeName);
	
    String getAttributeType();
    
    void setAttributeType(String attributeType);
    
    String getAttributeSize();
    
    void setAttributeSize(String attributeSize);
    
    String getAttributeScale();
    
    void setAttributeScale(String attributeScale);
    
    String getAttributeComment();
    
    void setAttributeComment(String attributeComment);
}
