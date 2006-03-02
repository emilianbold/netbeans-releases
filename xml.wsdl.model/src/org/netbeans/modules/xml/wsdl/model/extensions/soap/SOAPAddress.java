/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model.extensions.soap;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;

/**
 *
 * @author rico
 * Represents the address element under the wsdl port for SOAP binding
 */
public interface SOAPAddress extends ExtensibilityElement{
    public static final String LOCATION_PROPERTY = "location";
    
    String getLocation();
    void setLocation(String locationURI);
}
