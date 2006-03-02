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

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.Documentation;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;
/**
 *
 * @author Vidhya Narayanan
 */
public class DocumentationImpl extends SchemaComponentImpl implements Documentation {
	
        public DocumentationImpl(SchemaModelImpl model) {
            this(model,createNewComponent(SchemaElements.DOCUMENTATION,model));
        }
    
	/**
	 * Creates a new instance of DocumentationImpl
	 */
	public DocumentationImpl(SchemaModelImpl model, Element el) {
		super(model, el);
	}

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return Documentation.class;
	}
	
	/**
	 *
	 */
	public void setLanguage(String lang) {
		setAttribute(LANGUAGE_PROPERTY, SchemaAttributes.LANGUAGE, lang);
	}
	
	/**
	 *
	 */
	public void accept(SchemaVisitor visitor) {
		visitor.visit(this);
	}
	
	/**
	 *
	 */
	public void setSource(String uri) {
		setAttribute(SOURCE_PROPERTY, SchemaAttributes.SOURCE, uri);
	}
	
	/**
	 *
	 */
	public String getSource() {
		return getAttribute(SchemaAttributes.SOURCE);
	}
	
	/**
	 *
	 */
	public String getLanguage() {
		return getAttribute(SchemaAttributes.LANGUAGE);
	}
	
	public void setDocumentationElement(Element content) {
            super.updatePeer(CONTENT_PROPERTY, content);
	}
	
	public Element getDocumentationElement() {
            return Element.class.cast(getPeer().cloneNode(true));
	}
        
	public void setContent(String content) {
            setText(CONTENT_PROPERTY, content);
	}

	public String getContent() {
            return getText();
	}
}
