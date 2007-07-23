package org.netbeans.modules.iep.model;

public interface Property extends IEPComponent {

	static final String NAME_PROPERTY = "name";

	static final String VALUE_PROPERTY = "value";

	String getName();
	
	void setName(String name);
	
	String getValue();
	
	void setValue(String value);

}
