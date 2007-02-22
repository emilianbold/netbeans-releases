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

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIComponentFactory;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.visitor.DefaultSchemaVisitor;

/**
 * This is a visitor, which visits a specified schema component
 * and creates an AXI component. Not every schema component will
 * have a corresponding AXI component. The things we care in AXI
 * are, element, attribute, compositor, references and schema constructs
 * that will yield these as children.
 * 
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AXIComponentCreator extends DefaultSchemaVisitor {

    /**
     * Creates a new instance of AXIComponentCreator
     */
    public AXIComponentCreator(AXIModelImpl model) {
        this.model = model;
        this.factory = model.getComponentFactory();
    }

    /**
     * Create an AXI component from a schema component.
     */
    AXIComponent createNew(SchemaComponent schemaComponent) {
        schemaComponent.accept(this);
        
        return newAXIComponent;
    }
    
    /**
     * Visit Schema.
     */
    public void visit(Schema schema) {
        newAXIComponent = new AXIDocumentImpl(model, schema);
    }
    
    /**
     * Visit AnyElement.
     */
    public void visit(AnyElement schemaComponent) {
        org.netbeans.modules.xml.axi.AnyElement element = factory.
                createAnyElement(schemaComponent);
        Util.updateAnyElement(element);
        newAXIComponent = element;
    }
    
    /**
     * Visit AnyAttribute.
     */
    public void visit(AnyAttribute schemaComponent) {
        org.netbeans.modules.xml.axi.AnyAttribute attribute = factory.
                createAnyAttribute(schemaComponent);
        Util.updateAnyAttribute(attribute);
        newAXIComponent = attribute;
    }
    
    /**
     * Visit GlobalElement.
     */
    public void visit(GlobalElement schemaComponent) {
        if(!model.fromSameSchemaModel(schemaComponent)) {
            newAXIComponent = model.lookupFromOtherModel(schemaComponent);
            return;
        }
        
        Element element = factory.createElement(schemaComponent);
        Util.updateGlobalElement(element);
        newAXIComponent = element;
    }
    
    /**
     * Visit LocalElement.
     */
    public void visit(LocalElement component) {
        Element element = factory.createElement(component);
        Util.updateLocalElement(element);
        newAXIComponent = element;
    }
    
    /**
     * Visit ElementReference.
     */
    public void visit(ElementReference component) {
        SchemaComponent originalElement = component.getRef().get();
        if(originalElement == null)
            return;
        AXIComponent referent = null;
        if(!model.fromSameSchemaModel(originalElement)) {
            referent = model.lookupFromOtherModel(originalElement);
        } else {
            referent = model.lookup(originalElement);
        }
        assert (referent != null);
        Element element = factory.createElementReference(component, (Element)referent);
        Util.updateElementReference(element);
        newAXIComponent = element;
    }
    
    /**
     * Visit GlobalAttribute.
     */
    public void visit(GlobalAttribute schemaComponent) {
        if(!model.fromSameSchemaModel(schemaComponent)) {
            newAXIComponent = model.lookupFromOtherModel(schemaComponent);
            return;
        }
        Attribute attribute = factory.createAttribute(schemaComponent);
        Util.updateGlobalAttribute(attribute);
        newAXIComponent = attribute;
    }
    
    /**
     * Visit LocalAttribute.
     */
    public void visit(LocalAttribute component) {
        Attribute attribute = factory.createAttribute(component);
        Util.updateLocalAttribute(attribute);
        newAXIComponent = attribute;
    }
    
    /**
     * Visit AttributeReference.
     */
    public void visit(AttributeReference component) {
        SchemaComponent originalElement = component.getRef().get();
        if(originalElement == null)
            return;
        AXIComponent referent = null;
        if(!model.fromSameSchemaModel(originalElement)) {
            referent = model.lookupFromOtherModel(originalElement);
        } else {
            referent = model.lookup(originalElement);
        }
        assert(referent != null);
        Attribute attribute = factory.createAttributeReference(component,
                (Attribute)referent);
        Util.updateAttributeReference(attribute);
        newAXIComponent = attribute;
    }
    
    /**
     * Visit Sequence.
     */
    public void visit(Sequence component) {
        Compositor compositor = factory.createSequence(component);
        Util.updateCompositor(compositor);
        newAXIComponent = compositor;
    }
    
    /**
     * Visit Choice.
     */
    public void visit(Choice component) {
        Compositor compositor = factory.createChoice(component);
        Util.updateCompositor(compositor);
        newAXIComponent = compositor;
    }
    
    /**
     * Visit All.
     */
    public void visit(All component) {
        Compositor compositor = factory.createAll(component);
        Util.updateCompositor(compositor);
        newAXIComponent = compositor;
    }
    
    /**
     * Visit GlobalGroup.
     */
    public void visit(GlobalGroup schemaComponent) {
        if(!model.fromSameSchemaModel(schemaComponent)) {
            newAXIComponent = model.lookupFromOtherModel(schemaComponent);
            return;
        }
        ContentModel cm = factory.createContentModel(schemaComponent);
        Util.updateContentModel(cm);
        newAXIComponent = cm;
    }
    
    /**
     * Visit GroupReference.
     */
    public void visit(GroupReference component) {
        SchemaComponent sc = component.getRef().get();
        if(sc == null)
            return;
        AXIComponent referent = new AXIComponentCreator(model).
                createNew(sc);
        newAXIComponent = referent;
    }
    
    /**
     * Visit AttributeGroup.
     */
    public void visit(GlobalAttributeGroup schemaComponent) {
        if(!model.fromSameSchemaModel(schemaComponent)) {
            newAXIComponent = model.lookupFromOtherModel(schemaComponent);
            return;
        }
        ContentModel cm = factory.createContentModel(schemaComponent);
        Util.updateContentModel(cm);
        newAXIComponent = cm;
    }
    
    /**
     * Visit AttributeGroupReference.
     */
    public void visit(AttributeGroupReference component) {
        SchemaComponent sc = component.getGroup().get();
        if(sc == null)
            return;
        AXIComponent referent = new AXIComponentCreator(model).
                createNew(sc);
        newAXIComponent = referent;        
    }
        
    /**
     * Visit GlobalComplexType.
     */
    public void visit(GlobalComplexType schemaComponent) {
        if(!model.fromSameSchemaModel(schemaComponent)) {
            newAXIComponent = model.lookupFromOtherModel(schemaComponent);
            return;
        }
        ContentModel cm = factory.createContentModel(schemaComponent);
        Util.updateContentModel(cm);
        newAXIComponent = cm;
    }
            
    ////////////////////////////////////////////////////////////////////
    ////////////////////////// member variables ////////////////////////
    ////////////////////////////////////////////////////////////////////
    /**
     * Newly created component.
     */
    private AXIComponent newAXIComponent;
    
    /**
     * AXIModel.
     */
    private AXIModelImpl model;
    
    /**
     * AXIComponentFactory.
     */
    private AXIComponentFactory factory;
}
