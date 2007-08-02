package org.netbeans.modules.iep.model;

import org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement.StringAttribute;
import org.netbeans.modules.xml.xam.dom.Attribute;

public interface Property extends IEPComponent {

	static final String NAME_PROPERTY = "name";

	static final String VALUE_PROPERTY = "value";

        static final Attribute ATTR_NAME = new StringAttribute(NAME_PROPERTY);
        
        static final Attribute ATTR_VALUE = new StringAttribute(VALUE_PROPERTY);
        
        
	String getName();
	
	void setName(String name);
	
	String getValue();
	
	void setValue(String value);
	
	Component getParentComponent();
}
