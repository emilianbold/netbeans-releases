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
 * Represents the operation element under wsdl:operation for SOAP binding
 */
public interface SOAPOperation extends ExtensibilityElement {
    public static final String SOAP_ACTION_PROPERTY = "soapAction";
    public static final String STYLE_PROPERTY = "style";
    
    SOAPBinding.Style getStyle();
    void setStyle(SOAPBinding.Style v);
    
    void setSoapAction(String soapActionURI) ;
    String getSoapAction();
}
