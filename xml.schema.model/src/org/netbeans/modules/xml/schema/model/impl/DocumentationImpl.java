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

package org.netbeans.modules.xml.schema.model.impl;

import java.io.IOException;
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

    public void setContentFragment(String text) throws IOException {
        super.setXmlFragment(CONTENT_PROPERTY, text);
    }

    public String getContentFragment() {
        return super.getXmlFragment();
    }
}
