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

/*
 * XMLModelMapperVisitor.java
 *
 * Created on October 28, 2005, 3:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.model.visitor;

import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author ajit
 */
public class FindWSDLComponent extends ChildVisitor {
    
    /** Creates a new instance of XMLModelMapperVisitor */
    public FindWSDLComponent() {
    }
    
    public static <T extends WSDLComponent> T findComponent(Class<T> type, WSDLComponent root, String xpath) {
        WSDLComponent ret = new FindWSDLComponent().findComponent(root, xpath);
        if (ret == null) {
            return null;
        } else {
            return type.cast(ret);
        }
    }
    
    public WSDLComponent findComponent(WSDLComponent root, Element xmlNode) {
        assert xmlNode != null;
        
        this.xmlNode = xmlNode;
        result = null;
        root.accept(this);
        return result;
    }
    
    public WSDLComponent findComponent(WSDLComponent root, String xpath) {
        Document doc = (Document) root.getModel().getDocument();
        if (doc == null) {
            return null;
        }
        
        Node result = root.getModel().getAccess().findNode(doc, xpath);
        if (result instanceof Element) {
            return findComponent(root, (Element) result);
        } else {
            return null;
        }
    }

    protected void visitComponent(WSDLComponent component) {
        if (result != null) return;
        if (component.referencesSameNode(xmlNode)) {
            result = component;
            return;
        } else {
            super.visitComponent(component);
        }
    }
    
    private WSDLComponent result;
    private Element xmlNode;
}
