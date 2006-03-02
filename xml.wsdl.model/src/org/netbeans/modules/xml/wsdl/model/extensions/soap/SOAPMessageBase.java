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

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;

/**
 *
 * @author Nam Nguyen
 */
public interface SOAPMessageBase extends ExtensibilityElement {
    public static final String NAMESPACE_PROPERTY = "namespace";
    public static final String USE_PROPERTY = "use";
    public static final String ENCODING_STYLE_PROPERTY = "encodingStyle";
    
    void setNamespaceURI(String namespaceURI);
    String getNamespaceURI();
    
    public enum Use {
        LITERAL("literal"), ENCODED("encoded");
        Use(String tag) {
            this.tag = tag;
        }
        public String toString() { return tag; }
        private String tag;
    }
    
    void setUse(Use use);
    Use getUse();   
    
    Collection<String> getEncodingStyles();
    void addEncodingStyle(String encodingStyle);
    void removeEncodingStyle(String encodingStyle);
}
