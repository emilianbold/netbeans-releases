/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
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
