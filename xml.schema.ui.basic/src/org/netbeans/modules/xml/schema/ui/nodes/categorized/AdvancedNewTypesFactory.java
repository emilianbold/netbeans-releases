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

/*
 * AdvancedNewTypesFactory.java
 *
 * Created on May 2, 2006, 5:08 PM
 *
 */

package org.netbeans.modules.xml.schema.ui.nodes.categorized;

import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.AppInfo;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.Documentation;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.Enumeration;
import org.netbeans.modules.xml.schema.model.Field;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.Key;
import org.netbeans.modules.xml.schema.model.KeyRef;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.Selector;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.Unique;
import org.netbeans.modules.xml.schema.ui.nodes.NewTypesFactory;

/**
 *
 * @author Ajit Bhate
 */
public class AdvancedNewTypesFactory extends NewTypesFactory {
    /**
     * Creates a new instance of AdvancedNewTypesFactory
     */
    public AdvancedNewTypesFactory() {
    }
    
    public void visit(All all) {
        addChildType(LocalElement.class);
        addChildType(ElementReference.class);
        super.visit(all);
    }
    
    public void visit(Sequence s) {
        addChildType(LocalElement.class);
        addChildType(ElementReference.class);
        addChildType(AnyElement.class);
        addChildType(GroupReference.class);
        addChildType(Choice.class);
        addChildType(Sequence.class);
        super.visit(s);
    }
    
    public void visit(Choice choice) {
        addChildType(LocalElement.class);
        addChildType(ElementReference.class);
        addChildType(AnyElement.class);
        addChildType(GroupReference.class);
        addChildType(Choice.class);
        addChildType(Sequence.class);
        super.visit(choice);
    }
    
    public void visit(GlobalComplexType gct) {
        addChildType(LocalElement.class);
        addChildType(ElementReference.class);
        addChildType(AnyElement.class);
        addChildType(LocalAttribute.class);
        addChildType(AttributeReference.class);
        addChildType(AttributeGroupReference.class);
        addChildType(AnyAttribute.class);
        addChildType(GroupReference.class);
        addChildType(All.class);
        addChildType(Choice.class);
        addChildType(Sequence.class);
        super.visit(gct);
    }
    
    public void visit(LocalComplexType type) {
        addChildType(LocalElement.class);
        addChildType(ElementReference.class);
        addChildType(AnyElement.class);
        addChildType(LocalAttribute.class);
        addChildType(AttributeReference.class);
        addChildType(AttributeGroupReference.class);
        addChildType(AnyAttribute.class);
        addChildType(GroupReference.class);
        addChildType(All.class);
        addChildType(Choice.class);
        addChildType(Sequence.class);
        super.visit(type);
    }
    
    public void visit(GlobalAttributeGroup gag) {
        addChildType(LocalAttribute.class);
        addChildType(AttributeReference.class);
        addChildType(AttributeGroupReference.class);
        addChildType(AnyAttribute.class);
        super.visit(gag);
    }
    
    public void visit(GlobalSimpleType gst) {
        addChildType(Enumeration.class);
        super.visit(gst);
    }
    
    public void visit(LocalSimpleType gst) {
        addChildType(Enumeration.class);
        super.visit(gst);
    }
    
    public void visit(GlobalElement ge) {
        addChildType(LocalElement.class);
        addChildType(ElementReference.class);
        addChildType(AnyElement.class);
        addChildType(LocalAttribute.class);
        addChildType(AttributeReference.class);
        addChildType(AttributeGroupReference.class);
        addChildType(AnyAttribute.class);
        addChildType(LocalComplexType.class);
        addChildType(LocalSimpleType.class);
        addChildType(GroupReference.class);
        addChildType(All.class);
        addChildType(Choice.class);
        addChildType(Sequence.class);
        addChildType(Key.class);
        addChildType(KeyRef.class);
        addChildType(Unique.class);
        super.visit(ge);
    }
    
    public void visit(LocalElement le) {
        addChildType(LocalElement.class);
        addChildType(ElementReference.class);
        addChildType(AnyElement.class);
        addChildType(LocalAttribute.class);
        addChildType(AttributeReference.class);
        addChildType(AttributeGroupReference.class);
        addChildType(AnyAttribute.class);
        addChildType(LocalComplexType.class);
        addChildType(LocalSimpleType.class);
        addChildType(GroupReference.class);
        addChildType(All.class);
        addChildType(Choice.class);
        addChildType(Sequence.class);
        addChildType(Key.class);
        addChildType(KeyRef.class);
        addChildType(Unique.class);
        super.visit(le);
    }
    
    public void visit(GlobalGroup gd) {
        addChildType(All.class);
        addChildType(Sequence.class);
        addChildType(Choice.class);
        super.visit(gd);
    }
    
    public void visit(Schema s) {
        addChildType(GlobalComplexType.class);
        addChildType(GlobalSimpleType.class);
        addChildType(GlobalElement.class);
        addChildType(GlobalAttribute.class);
        addChildType(GlobalAttributeGroup.class);
        addChildType(GlobalGroup.class);
        addChildType(Import.class);
        addChildType(Include.class);
        addChildType(Redefine.class);
        super.visit(s);
    }
    
    public void visit(Redefine rd) {
        addChildType(GlobalComplexType.class);
        addChildType(GlobalSimpleType.class);
        addChildType(GlobalAttributeGroup.class);
        addChildType(GlobalGroup.class);
        super.visit(rd);
    }
    
    public void visit(Key key) {
        addChildType(Selector.class);
        addChildType(Field.class);
        super.visit(key);
    }
    
    public void visit(Unique u) {
        addChildType(Selector.class);
        addChildType(Field.class);
        super.visit(u);
    }
    
    public void visit(KeyRef kr) {
        addChildType(Selector.class);
        addChildType(Field.class);
        super.visit(kr);
    }

    public void visit(Annotation ann) {
        addChildType(Documentation.class);
        addChildType(AppInfo.class);
        super.visit(ann);
    }
    
}
