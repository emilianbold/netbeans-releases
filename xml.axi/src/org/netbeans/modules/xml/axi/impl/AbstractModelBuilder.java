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
package org.netbeans.modules.xml.axi.impl;

import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor;

/**
 * An AXI model can be created with few schema componnets. This class must
 * define a set of visit methods for those components and all the builder
 * implementation must implement those.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class AbstractModelBuilder extends DeepSchemaVisitor {
    
    public AbstractModelBuilder(AXIModelImpl model) {
        this.model = model;
    }
    
    public AXIModelImpl getModel() {
        return model;
    }    
    
    public abstract void visit(Schema schema);
    
    public abstract void visit(AnyElement schemaComponent);
    
    public abstract void visit(AnyAttribute schemaComponent);
    
    public abstract void visit(GlobalElement schemaComponent);
    
    public abstract void visit(LocalElement component);
    
    public abstract void visit(ElementReference component);
    
    public abstract void visit(GlobalAttribute schemaComponent);
    
    public abstract void visit(LocalAttribute component);
    
    public abstract void visit(AttributeReference component);
    
    public abstract void visit(Sequence component);
    
    public abstract void visit(Choice component);
    
    public abstract void visit(All component);
    
    public abstract void visit(GlobalGroup schemaComponent);
    
    public abstract void visit(GroupReference component);
    
    public abstract void visit(GlobalAttributeGroup schemaComponent);
    
    public abstract void visit(AttributeGroupReference component);
    
    public abstract void visit(GlobalComplexType schemaComponent);
    
    public abstract void visit(LocalComplexType component);
    
    public abstract void visit(ComplexContent component);
    
    public abstract void visit(SimpleContent component);
    
    public abstract void visit(SimpleExtension component);
    
    public abstract void visit(ComplexExtension component);

    ////////////////////////////////////////////////////////////////////
    ////////////////////////// member variables ////////////////////////
    ////////////////////////////////////////////////////////////////////
    protected AXIModelImpl model;    
}
