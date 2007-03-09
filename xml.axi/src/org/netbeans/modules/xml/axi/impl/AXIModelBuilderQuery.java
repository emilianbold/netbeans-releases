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
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor;

/**
 * Helper class that exposes query-like APIs. Various queries can be made
 * on schema components such as whether or not the a component has any affect
 * on the AXI model OR whether or not a component can be viewed in the editor.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AXIModelBuilderQuery extends AbstractModelBuilder {
    
    public AXIModelBuilderQuery(AXIModelImpl model) {
        super(model);
    }
    
    /**
     * Returns true for all schema components that are viewable,
     * false otherwise. Not all schema components have corresponding AXI
     * components and not all AXI components are viewable.
     */
    public boolean canView(SchemaComponent schemaComponent) {
        canView = false;
        schemaComponent.accept(this);
        return canView;
    }
    
    /**
     * Returns true if the schema component has an impact on AXI model,
     * false otherwise. Not all schema components affects AXI model.
     */
    public boolean affectsModel(SchemaComponent schemaComponent) {
        affectsModel = false;
        schemaComponent.accept(this);
        return affectsModel;
    }
    
    public void visit(Schema schema) {
        affectsModel = true;
        canView = true;
    }
    
    public void visit(AnyElement schemaComponent) {
        affectsModel = true;
        canView = true;
    }
    
    public void visit(AnyAttribute schemaComponent) {
        affectsModel = true;
        canView = true;
    }
    
    public void visit(GlobalElement schemaComponent) {
        affectsModel = true;
        canView = true;
    }
    
    public void visit(LocalElement component) {
        affectsModel = true;
        canView = true;
    }
    
    public void visit(ElementReference component) {
        affectsModel = true;
        canView = true;
    }
    
    public void visit(GlobalAttribute schemaComponent) {
        affectsModel = true;
        canView = true;
    }
    
    public void visit(LocalAttribute component) {
        affectsModel = true;
        canView = true;
    }
    
    public void visit(AttributeReference component) {
        affectsModel = true;
        canView = true;
    }
    
    public void visit(Sequence component) {
        affectsModel = true;
        canView = true;
    }
    
    public void visit(Choice component) {
        affectsModel = true;
        canView = true;
    }
    
    public void visit(All component) {
        affectsModel = true;
        canView = true;
    }
    
    public void visit(GlobalGroup schemaComponent) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(GroupReference component) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(GlobalAttributeGroup schemaComponent) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(AttributeGroupReference component) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(GlobalComplexType schemaComponent) {
        affectsModel = true;
        canView = true;
    }
    
    public void visit(LocalComplexType component) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(ComplexContent component) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(SimpleContent component) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(SimpleExtension component) {
        affectsModel = true;
        canView = false;
    }
    
    public void visit(ComplexExtension component) {
        affectsModel = true;
        canView = false;
    }

    ////////////////////////////////////////////////////////////////////
    ////////////////////////// member variables ////////////////////////
    ////////////////////////////////////////////////////////////////////
    private boolean affectsModel;
    private boolean canView;
}
