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
