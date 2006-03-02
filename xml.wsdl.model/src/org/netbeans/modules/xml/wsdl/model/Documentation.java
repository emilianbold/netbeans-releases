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

package org.netbeans.modules.xml.wsdl.model;

import org.w3c.dom.Element;

/**
 * @author rico
 * @author Nam Nguyen
 * Represents the documentation element in a WSDL document
 */
public interface Documentation extends WSDLComponent{
    public static final String CONTENT_PROPERTY = "content";
    
    /**
     * @return text representation of the documentation element content.
     */
    String getTextContent();
    
    /**
     * Set the documentation element content to a text node with the given
     * string value.
     */
    void setTextContent(String content);
    
    /**
     * @return a mutable clone of the documentation element itself.
     */
    Element getDocumentationElement();
    
    /**
     * Sets the documentation element to the given element.
     */
    void setDocumentationElement(Element documentationElement);
}
