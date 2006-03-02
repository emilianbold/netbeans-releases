/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.extensions.soap.impl;

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.xam.Attribute;

/**
 *
 * @author Nam Nguyen
 */
public enum SOAPAttribute implements Attribute {
        ENCODING_STYLE("encodingStyle", Collection.class, String.class),
        MESSAGE("message"),
        NAME("name"),
        NAMESPACE("namespace"),
        PART("part"),
        PARTS("parts"),
        USE("use", SOAPMessageBase.Use.class),
        SOAP_ACTION("soapAction"),
        STYLE("style", SOAPBinding.Style.class),
        TRANSPORT_URI("transport");
    
    private String name;
    private Class type;
    private Class subtype;
    
    /** Creates a new instance of SOAPAttribute */
    SOAPAttribute(String name) {
        this(name, String.class);
    }
    SOAPAttribute(String name, Class type) {
        this(name, type, null);
    }
    SOAPAttribute(String name, Class type, Class subtype) {
        this.name = name;
        this.type = type;
        this.subtype = subtype;
    }
    
    public String toString() { return name; }

    public Class getType() {
        return type;
    }

    public String getName() { return name; }

    public Class getMemberType() { return subtype; }
}
