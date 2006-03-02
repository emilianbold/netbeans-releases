/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.extensions.soap;

import java.util.Collection;

/**
 *
 * @author rico
 * Represents the fault element under the wsdl:fault element for SOAP binding
 */
public interface SOAPFault extends SOAPMessageBase {
    public static final String NAME_PROPERTY = "name";
    
    //TODO this could be turned into reference
    void setName(String name);
    String getName();
}
