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
