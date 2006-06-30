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
