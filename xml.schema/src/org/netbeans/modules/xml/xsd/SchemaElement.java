/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.xsd;

import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents an XML element
 * @author  anovak
 */
class SchemaElement extends AbstractResultNode implements Element {

    /** namespace */
    protected final String namespaceURI;
    /** qname */
    protected final String qname;
    /** Attributes of this Element */
    protected final Attributes attributes; 
    /** Sub elements */
    protected final List subelements;

    /** Creates empty element */
    protected SchemaElement() {
        this.namespaceURI = null;
        this.qname = null;
        this.attributes = null;
        this.subelements = null;
    }
    
    /** Creates a new instance of Element */
    protected SchemaElement(String namespaceURI, String qname, Attributes attributes) {
        this.namespaceURI = namespaceURI;
        this.qname = qname;
        this.attributes = (attributes == null ? null : new AttributesImpl(attributes));
        this.subelements = new ArrayList();
    }
    
    /** Creates a new SchemaElement */
    public static final SchemaElement createSchemaElement(String namespaceURI, String qname, Attributes attributes) {
        if (qname.equalsIgnoreCase(Type.XS_SIMPLE_TYPE) || qname.equalsIgnoreCase(Type.XS_COMPLEX_TYPE)) {
            return new Type(namespaceURI, qname, attributes);
        } else {
            return new SchemaElement(namespaceURI, qname, attributes);
        }
    }
    
    public final void addSubelement(SchemaElement e) {
        subelements.add(e);
    }
    
    public final java.util.Iterator getSubelements() {
        return subelements.iterator();
    }
    
    public final boolean isComposite() {
        return (this instanceof Type) || getQname().equalsIgnoreCase("xs:sequence");
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer(100);
        sb.append("SchemaElement ").append(qname);
        
        if (attributes != null) {
            sb.append("Attrs size: " + attributes.getLength());
            for (int i = 0; i < attributes.getLength(); i++) {
                sb.append("\n Attr[" + i + "] localname: " + attributes.getLocalName(i) + " qname: " + attributes.getQName(i) + " value: " + attributes.getValue(i) + " URI: " + attributes.getURI(i) + " type: " + attributes.getType(i));
            }
        }
        
        return  sb.toString();
    }
    
    /**
     * Getter for property qname.
     * @return Value of property qname.
     */
    public java.lang.String getQname() {
        return qname;
    }
    
    /**
     * Getter for property attributes.
     * @return Value of property attributes.
     */
    public org.xml.sax.Attributes getSAXAttributes() {
        return attributes;
    }

    // org.w3c.Node methods
    
    public short getNodeType() {
        return org.w3c.dom.Node.ELEMENT_NODE;
    }

    public String getNodeName() {
        return getSAXAttributes().getValue("name");
    }

    public String getTagName() {
        return this.getNodeName();
    }
}