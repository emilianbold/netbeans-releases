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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * Created on Jun 23, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.cookies.WSDLDefinitionNodeCookie;
import org.netbeans.modules.xml.wsdl.ui.schema.visitor.AbstractXSDVisitor;
import org.netbeans.modules.xml.wsdl.ui.schema.visitor.SchemaElementAttributeFinderVisitor;
import org.netbeans.modules.xml.xam.Nameable;
import org.openide.nodes.Node;
import org.w3c.dom.NamedNodeMap;



/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Utils {
	
	public static WSDLDefinitionNodeCookie getWSDLDefinitionNodeCookie(Node node) {
		Node parent = node;
		if(parent != null) {
			while(parent != null) {
				WSDLDefinitionNodeCookie cookie = (WSDLDefinitionNodeCookie) parent.getCookie(WSDLDefinitionNodeCookie.class);
				if(cookie != null) {
					return cookie;
				} else {
					parent = parent.getParentNode();
				}
			}
		}
		
		return null;
	}
	
	public static boolean isMissingAttributes(WSDLComponent element, Element schemaElement) {
		boolean result = false;
		
//		//go through attributes defined in schema element
//		//and check if they are already available in WSDLElement
//		//if so, skip them and add if not.
		NamedNodeMap elementAttrs = element.getPeer().getAttributes();
		SchemaElementAttributeFinderVisitor seaFinder = new SchemaElementAttributeFinderVisitor(schemaElement);
		schemaElement.accept(seaFinder);
		List<Attribute> attrs = seaFinder.getAttributes();
		Iterator<Attribute> it = attrs.iterator();
		while(it.hasNext()) {
		    Attribute attr = it.next();
		    Nameable namedAttr = (Nameable) attr;
		    //check if attribute is already added
		    //TODO: need to check namespace as well
		    if(elementAttrs.getNamedItem(namedAttr.getName())== null) {
		        result = true;
		        break;
		    }
		}
		
		return result;
	}
    
    public static boolean isExtensionAttributesAllowed(Element element) {
        AnyAttributesVisitor visitor = new AnyAttributesVisitor();
        element.accept(visitor);
        return visitor.isExtensionAttributesAllowed();
    }
    
    


}

class AnyAttributesVisitor extends AbstractXSDVisitor{
    private boolean hasAnyAttributes = false;
    @Override
    public void visit(AnyAttribute anyAttr) {
        hasAnyAttributes = true;
    }
    public boolean isExtensionAttributesAllowed() {
        return hasAnyAttributes;
    }
};
