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

/*
 * XMLModelMapperVisitor.java
 *
 * Created on October 28, 2005, 3:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.visitor;

import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.impl.SchemaComponentImpl;
import org.netbeans.modules.xml.schema.model.impl.SchemaModelImpl;
import org.netbeans.modules.xml.xam.xdm.ComponentFinder;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.visitor.XPathFinder;

/**
 *
 * @author ajit
 */
public class FindSchemaComponentFromDOM extends DeepSchemaVisitor 
         implements ComponentFinder<SchemaComponent>{
    
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
        
        Node result = new XPathFinder().findNode(doc, xpath);
        if (result instanceof Element) {
            return findComponent(root, (Element) result);
        } else {
            return null;
        }
    }

    private Document getDocument(SchemaComponent root) {
        Document doc = null;
        if (root.getSchemaModel() instanceof SchemaModelImpl) {
            org.w3c.dom.Document w3cDoc = ((SchemaModelImpl) root.getSchemaModel()).getDocument();
            if (w3cDoc instanceof Document) {
                doc = (Document) w3cDoc;
            }
        }
        return doc;
    }

    private Element getElement(SchemaComponent c) {
        Element element = null;
        if (c instanceof SchemaComponentImpl) {
            org.w3c.dom.Element el = ((SchemaComponentImpl) c).getPeer();
            if (el instanceof Element) {
                element = (Element) el;
            }
        }
        return element;
    }
    
    public String getXPathForComponent(SchemaComponent root, SchemaComponent target) {
        Document doc = getDocument(root);
        Node element = getElement(target);
        if (doc == null || element == null) {
            return null;
        }
        return new XPathFinder().getXpath(doc, element);
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
