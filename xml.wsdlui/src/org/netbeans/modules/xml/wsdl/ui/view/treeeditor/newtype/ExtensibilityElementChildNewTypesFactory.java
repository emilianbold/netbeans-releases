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

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.schema.visitor.CreateAddSchemaElementActionVisitor;
import org.netbeans.modules.xml.wsdl.ui.schema.visitor.SchemaElementMinMaxOccursFinderVisitor;
import org.netbeans.modules.xml.xam.Nameable;
import org.openide.util.datatransfer.NewType;

/**
 *
 * @author skini
 */
public class ExtensibilityElementChildNewTypesFactory implements NewTypesFactory {
    
    private Element mElement;
    
    /** Creates a new instance of ExtensibilityElementNewTypesFactory */
    public ExtensibilityElementChildNewTypesFactory(Element element) {
        mElement = element;
    }
    
    @SuppressWarnings("boxing")
    public NewType[] getNewTypes(WSDLComponent component) {
        ArrayList<ExtensibilityElementChildNewType> eeNewTypeList = new ArrayList<ExtensibilityElementChildNewType>();
        if(mElement != null) {
            CreateAddSchemaElementActionVisitor casActionVisitor = new CreateAddSchemaElementActionVisitor();
            mElement.accept(casActionVisitor);
            List<Element> elements = casActionVisitor.getElements();
            if (elements != null) {
                List<ExtensibilityElement> children = component.getChildren(ExtensibilityElement.class);
                HashMap<QName, Integer> qnameMap= new HashMap<QName, Integer>();
                if (children != null) {
                    for (ExtensibilityElement child : children) {
                        QName qname = child.getQName();
                        if (qnameMap.containsKey(qname)) {
                            qnameMap.put(qname, qnameMap.get(qname).intValue() + 1);
                        } else {
                            qnameMap.put(qname, 1);
                        }
                    }
                }
                
                for (Element element : elements) {
                    SchemaElementMinMaxOccursFinderVisitor semmFinder = new SchemaElementMinMaxOccursFinderVisitor();
                    element.accept(semmFinder);
                    int maxOccurs = semmFinder.getMaxOccurs();
                    
                    if (element instanceof Nameable) {
                        Nameable nameable = (Nameable) element;
                        QName elementQName = new QName(element.getModel().getSchema().getTargetNamespace(), nameable.getName());
                        if (!qnameMap.containsKey(elementQName) || maxOccurs > qnameMap.get(elementQName).intValue()) {
                            eeNewTypeList.add(new ExtensibilityElementChildNewType(component, element));
                        }
                    }
                    
                }
            }
        }
        return eeNewTypeList.toArray(new NewType[eeNewTypeList.size()]);
    }
    
}
