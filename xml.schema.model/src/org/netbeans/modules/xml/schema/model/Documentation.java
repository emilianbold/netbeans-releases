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

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.xml.schema.model;

import org.w3c.dom.Element;

/**
 * The interface represents human-readable documentation in plain text.
 * @author Chris Webster
 */
public interface Documentation extends SchemaComponent {
	
	public static final String LANGUAGE_PROPERTY = "language";
	public static final String SOURCE_PROPERTY = "source";
	public static final String CONTENT_PROPERTY = "content";
	
	String getSource();
	void setSource(String uri);
	
	//TODO low priority create enum for languages
	String getLanguage();
	void setLanguage(String lang);
        
    /**
     * @return text representation of the documentation element content.
     */
    String getContent();
    
    /**
     * Set the documentation element content to a text node with the given
     * string value.
     */
    void setContent(String content);
    
    /**
     * @return a mutable clone of the documentation element itself.
     */
    Element getDocumentationElement();
    
    /**
     * Sets the documentation element to the given element.
     */
    void setDocumentationElement(Element documentationElement);
}
