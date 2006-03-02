/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
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
