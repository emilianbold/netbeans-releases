/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.xml.schema.model;

import java.io.IOException;
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
     * @return XML fragment text of documentation element content.
     */
    String getContentFragment();
    
    /**
     * Sets the XML fragment text of documentation element content.
     * The XML fragment will be parsed and the resulting nodes will
     * replace the current children of this documentation element.
     * @param text XML fragment text.
     * @exception IOException if the fragment text is not well-form.
     */
    void setContentFragment(String text) throws IOException;
    
    /**
     * @return a mutable clone of the documentation element itself.
     */
    Element getDocumentationElement();
    
    /**
     * Sets the documentation element to the given element.
     */
    void setDocumentationElement(Element documentationElement);
}
