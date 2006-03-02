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
 * Represents the binding element under the wsdl binding element for SOAP binding
 */
public interface SOAPBinding extends ExtensibilityElement{
    public static final String STYLE_PROPERTY = "style";
    public static final String TRANSPORT_URI_PROPERTY = "transportURI";
    
    Style getStyle();
    void setStyle(Style style); 
    
    String getTransportURI();
    void setTransportURI(String transportURI);

    public enum Style { 
        RPC("rpc"), DOCUMENT("document");
        
        private String tag;
        Style(String tag) {
            this.tag = tag;
        }
        public String toString() {
            return tag;
        }
    }
}
