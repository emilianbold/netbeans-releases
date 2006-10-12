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

package org.netbeans.modules.xml.schema.model.visitor;

import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.impl.SchemaComponentImpl;
import org.netbeans.modules.xml.schema.model.impl.SchemaModelImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 *
 * @author ajit
 */
public class FindSchemaComponentFromDOM extends DeepSchemaVisitor {

    /** Creates a new instance of XMLModelMapperVisitor */
    public FindSchemaComponentFromDOM() {
    }
    
    public static <T extends SchemaComponent> T find(Class<T> type, SchemaComponent root, String xpath) {
        SchemaComponent ret = new FindSchemaComponentFromDOM().findComponent(root, xpath);
        return type.cast(ret);
    }
    
    public SchemaComponent findComponent(SchemaComponent root, Element xmlNode) {
        assert root instanceof Schema;
        assert xmlNode != null;
        
        this.xmlNode = xmlNode;
        result = null;
        root.accept(this);
        return result;
    }
    
    public SchemaComponent findComponent(SchemaComponent root, String xpath) {
        Document doc = getDocument(root);
        if (doc == null) {
            return null;
        }
        
        Node result = ((SchemaModelImpl)root.getModel()).getAccess().findNode(doc, xpath);
        if (result instanceof Element) {
            return findComponent(root, (Element) result);
        } else {
            return null;
        }
    }

    private Document getDocument(SchemaComponent root) {
        return (Document)root.getModel().getDocument();
    }

    private Element getElement(SchemaComponent c) {
        return (Element) c.getPeer();
    }
    
    public String getXPathForComponent(SchemaComponent root, SchemaComponent target) {
        Document doc = getDocument(root);
        Element element = getElement(target);
        if (doc == null || element == null) {
            return null;
        }
        return ((SchemaModelImpl)root.getModel()).getAccess().getXPath(doc, element);
    }

    protected void visitChildren(SchemaComponent component) {
        if(result != null) return;
        if (component.referencesSameNode(xmlNode)) {
            result = component;
        } else {
            super.visitChildren(component);
        }
    }
    
    private SchemaComponent result;
    private Element xmlNode;

}
