/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
