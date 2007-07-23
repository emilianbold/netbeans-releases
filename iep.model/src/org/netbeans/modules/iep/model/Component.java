package org.netbeans.modules.iep.model;

public interface Component extends IEPComponent {

	static final String NAME_PROPERTY = "name";

	static final String TITLE_PROPERTY = "title";

	static final String TYPE_PROPERTY = "type";
	
	String getName();
        
	void setName(String name);
	
    String getTitle();
    
    void setTitle(String title);
    
    String getType();
    
    void setType(String type);

}
