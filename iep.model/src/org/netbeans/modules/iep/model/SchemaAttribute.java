package org.netbeans.modules.iep.model;

public interface SchemaAttribute extends Component {

	public static final String PROP_NAME = "name";
	
	public static final String PROP_TYPE = "type";
	
	public static final String PROP_SIZE = "size";
	
	public static final String PROP_SCALE = "scale";
	
	public static final String PROP_COMMENT = "comment";
	
	String getAttributeName() throws Exception;
    
    String getAttributeType() throws Exception;
    
    String getAttributeSize() throws Exception;
    
    String getAttributeScale() throws Exception;
    
    String getAttributeComment() throws Exception;
}
