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
package org.netbeans.modules.soa.ui.axinodes;

import java.awt.ComponentOrientation;
import org.netbeans.modules.soa.ui.SoaUiUtil;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.Attribute;
import org.openide.nodes.Sheet;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class AttributeNode extends AxiomNode<Attribute> {
    
    public AttributeNode(Attribute reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }
    
    public AttributeNode(Attribute reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.ATTRIBUTE;
    }
    
    public synchronized String getName(){
        Attribute attribute = getReference();
        String name = attribute.getName();
        return name;
    }
    
    public synchronized String getHtmlDisplayName() {
        Attribute attribute = getReference();
        return AxiomUtils.getAttributeHtmlDisplayName(
                attribute, ComponentOrientation.LEFT_TO_RIGHT);
    }
    
    public String getHelpId() {
        return getNodeType().getHelpId();
    }
    
//    protected Sheet createSheet() {
//        Sheet sheet = super.createSheet();
//        if (getReference() == null) {
//            // The related object has been removed!
//            return sheet;
//        }
//        //
//        Sheet.Set mainPropertySet =
//                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
//        //
//        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
//                NamedElement.NAME, PropertyType.NAME, "getName", "setName", null); // NOI18N
//        //
//        return sheet;
//    }
}
