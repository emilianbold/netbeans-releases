package org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation;

import org.netbeans.modules.xml.xam.dom.Attribute;
/**
 *
 * @author radval
 */

public class StringAttribute implements Attribute {
    private String name;
    public StringAttribute(String name) { this.name = name; }
    public Class getType() { return String.class; }
    public String getName() { return name; }
    public Class getMemberType() { return null; }
}
