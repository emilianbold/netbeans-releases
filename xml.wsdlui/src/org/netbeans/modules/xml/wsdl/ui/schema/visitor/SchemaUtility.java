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
 * SchemaUtility.java
 *
 * Created on April 17, 2006, 8:53 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.schema.visitor;

import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;

/**
 *
 * @author radval
 */
public class SchemaUtility {
    
    /** Creates a new instance of SchemaUtility */
    public SchemaUtility() {
    }
    
    /**
     * Find Attribute given attribute QName
     * attribute QName should have namespace and local name
     * prefix of attribute QName is ignored
     **/
    public static Attribute findAttribute(QName attrQName, Element element) {
        Attribute attribute = null;
        SchemaElementAttributeFinderVisitor seaFinder = new SchemaElementAttributeFinderVisitor(element);
        element.accept(seaFinder);
        
        List<Attribute> attributes = seaFinder.getAttributes();
        Iterator<Attribute> it = attributes.iterator();
        
        while(it.hasNext()) {
            Attribute attr = it.next();
            if(attr instanceof Nameable) {
                Nameable namedAttr = (Nameable) attr;
                String attrName = namedAttr.getName();
                if (attrName.equals(attrQName.getLocalPart())) return attr;
                
                QName aq = QName.valueOf(attrName);
                String ns = aq.getNamespaceURI();
                String prefix = aq.getPrefix();
                if(ns == null || ns.trim().equals("") && prefix != null) {
                    ns = ((AbstractDocumentComponent) element).lookupNamespaceURI(prefix);
                }
                
                QName normalizedQName = new QName(ns, aq.getLocalPart());
                        
                if(attrQName.equals(normalizedQName))  {
                    attribute = attr;
                    break;
                }
            }
        }
        
        return attribute;
    }
}
