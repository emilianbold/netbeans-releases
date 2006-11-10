/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.model.extensions.soap;

import java.util.Collection;

/**
 *
 * @author Nam Nguyen
 */
public interface SOAPMessageBase extends SOAPComponent {
    public static final String NAMESPACE_PROPERTY = "namespace";
    public static final String USE_PROPERTY = "use";
    public static final String ENCODING_STYLE_PROPERTY = "encodingStyle";
    
    void setNamespace(String namespaceURI);
    String getNamespace();
    
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
