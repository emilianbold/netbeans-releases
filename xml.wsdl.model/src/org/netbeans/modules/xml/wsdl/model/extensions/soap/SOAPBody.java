/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.extensions.soap;

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;

/**
 *
 * @author rico
 * Represents the body element under the wsdl:input or wsdl:output element for SOAP binding
 */
public interface SOAPBody extends SOAPMessageBase {
    public static final String PARTS_PROPERTY = "parts";
    
    //TODO should be List?
    Collection<String> getParts();
    void addPart(String part);
    void removePart(String part);
}
