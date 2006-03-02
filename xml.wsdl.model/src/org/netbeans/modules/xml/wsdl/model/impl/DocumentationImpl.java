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

package org.netbeans.modules.xml.wsdl.model.impl;

import org.netbeans.modules.xml.wsdl.model.Documentation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public class DocumentationImpl extends WSDLComponentBase implements Documentation{
    
    /** Creates a new instance of DocumentationImpl */
    public DocumentationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    public DocumentationImpl(WSDLModel model){
        this(model, createNewElement(WSDLQNames.DOCUMENTATION.getQName(), model));
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    // Documentation cannot have Documentation
    public void setDocumentation(Documentation doc) {}
    public Documentation getDocumentation() {  return null;  }
    
    public void setTextContent(String content) {
        super.setText(CONTENT_PROPERTY, content);
    }

    public String getTextContent() {
        return getText();
    }

    public Element getDocumentationElement() {
        return Element.class.cast(getPeer().cloneNode(true));
    }
    
    public void setDocumentationElement(Element documentationElement) {
        super.updatePeer(CONTENT_PROPERTY, documentationElement);
    }
}
