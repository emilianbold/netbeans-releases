/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * RefactorVisitor.java
 *
 * Created on October 18, 2005, 2:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.visitor.*;
import org.netbeans.modules.xml.xam.GlobalReference;

/**
 *
 * @author Samaresh
 */
class RefactorVisitor extends DefaultSchemaVisitor {
        
    /**
     * The global component being modified.
     */
    private SchemaComponent global_component = null;
            
    /**
     * Creates a new instance of RefactorVisitor
     */
    public RefactorVisitor() {
    }
    
    /**
     * Sets the global component that has been renamed.
     */
    public void setRenamedElement(SchemaComponent component) {
        this.global_component = component;
    }
    
    /**
     * Fixes the referneces for all schema components in this preview
     * to the new global component that has been renamed.
     *
     * User must call setRenamedElement() prior to this call.
     */
    public void rename(Preview preview) {
        if(global_component == null)
            return;
        
        for(SchemaComponent component : preview.getUsages().keySet()) {
            component.accept(this);
        }
    }
    
    /**
     * For CommonSimpleRestriction, GlobalReference will be:
     * getBase(), when a GlobalSimpleType is modified.
     */
    //public void visit(CommonSimpleRestriction csr) { //this is not a SchemaComponent
    public void visit(SimpleTypeRestriction str) {
        if(global_component instanceof GlobalSimpleType) {
            SchemaComponentFactory factory = global_component.getSchemaModel().getFactory();
            GlobalReference<GlobalSimpleType> ref = 
                factory.createGlobalReference(
                    GlobalSimpleType.class.cast(global_component), 
                    GlobalSimpleType.class, str);
            str.setBase(ref);
        }
    }
        
    /**
     * For LocalElement, GlobalReference will be:
     * getType(), when a GlobalType is modified,
     * getRef(), when a GlobalElement is modified.
     */
    public void visit(LocalElement element) {
        if(global_component instanceof GlobalSimpleType) {
            SchemaComponentFactory factory = global_component.getSchemaModel().getFactory();
            GlobalReference<GlobalSimpleType> ref = 
                factory.createGlobalReference(
                GlobalSimpleType.class.cast(global_component),
                GlobalSimpleType.class,
                element);
            element.setType(ref);
        }
        
        if(global_component instanceof GlobalComplexType) {
            SchemaComponentFactory factory = global_component.getSchemaModel().getFactory();
            GlobalReference<GlobalComplexType> ref = 
                factory.createGlobalReference(
                    GlobalComplexType.class.cast(global_component),
                    GlobalComplexType.class, element);
            element.setType(ref);
        }
        
    }
    
     public void visit(ElementReference element) {
        if(global_component instanceof GlobalElement) {
            SchemaComponentFactory factory = global_component.getSchemaModel().getFactory();
            GlobalReference<GlobalElement> ref = 
                factory.createGlobalReference(
                    GlobalElement.class.cast(global_component),
                    GlobalElement.class,
                    element);
            element.setRef(ref);            
        }
    }
    
    /**
     * For AllElement, GlobalReference will be:
     * getType(), when a GlobalType is modified,
     * getRef(), when a GlobalElement is modified.
     */
    public void visit(AllElement element) {
        if(global_component instanceof GlobalSimpleType) {
            SchemaComponentFactory factory = global_component.getSchemaModel().getFactory();
            GlobalReference<GlobalSimpleType> ref = 
                factory.createGlobalReference(
                    GlobalSimpleType.class.cast(global_component),
                    GlobalSimpleType.class,
                    element);
            element.setType(ref);
        }
        
        if(global_component instanceof GlobalComplexType) {
            SchemaComponentFactory factory = global_component.getSchemaModel().getFactory();
            GlobalReference<GlobalComplexType> ref = 
                factory.createGlobalReference(
                    GlobalComplexType.class.cast(global_component),
                    GlobalComplexType.class,
                    element);
            element.setType(ref);
        }
        
    }
    
    /**
     * For AllElement, GlobalReference will be:
     * getType(), when a GlobalType is modified,
     * getRef(), when a GlobalElement is modified.
     */
    public void visit(AllElementReference element) {
        if(global_component instanceof GlobalElement) {
            SchemaComponentFactory factory = global_component.getSchemaModel().getFactory();
            GlobalReference<GlobalElement> ref = 
                factory.createGlobalReference(
                    GlobalElement.class.cast(global_component),
                    GlobalElement.class,
                    element);
            element.setRef(ref);            
        }
    }
    
    /**
     * For GlobalElement, GlobalReference will be:
     * getType(), when a GlobalType is modified,
     * getSubstitutionGroup(), when a GlobalElement is modified.
     */
    public void visit(GlobalElement element) {
        if(global_component instanceof GlobalSimpleType) {
            SchemaComponentFactory factory = global_component.getSchemaModel().getFactory();
            GlobalReference<GlobalSimpleType> ref = 
                factory.createGlobalReference(
                    GlobalSimpleType.class.cast(global_component),
                    GlobalSimpleType.class,
                    element);
            element.setType(ref);
        }
        
        if(global_component instanceof GlobalComplexType) {
            SchemaComponentFactory factory = global_component.getSchemaModel().getFactory();
            GlobalReference<GlobalComplexType> ref = 
                factory.createGlobalReference(
                    GlobalComplexType.class.cast(global_component),
                    GlobalComplexType.class,
                    element);
            element.setType(ref);
        }
        
        if(global_component instanceof GlobalElement) {
            SchemaComponentFactory factory = global_component.getSchemaModel().getFactory();
            GlobalReference<GlobalElement> ref = 
                factory.createGlobalReference(
                    GlobalElement.class.cast(global_component),
                    GlobalElement.class, element);
            element.setSubstitutionGroup(ref);            
        }
    }
        
    /**
     * For LocalAttribute, GlobalReference will be:
     * getType(), when a GlobalSimpleType is modified,
     * getRef(), when a GlobalAttribute is modified.
     */
    public void visit(LocalAttribute attribute) {
        if(global_component instanceof GlobalSimpleType) {
            SchemaComponentFactory factory = global_component.getSchemaModel().getFactory();
            GlobalReference<GlobalSimpleType> ref = 
                factory.createGlobalReference(
                    GlobalSimpleType.class.cast(global_component),
                    GlobalSimpleType.class,
                    attribute);
            attribute.setType(ref);
        }
	
    }
    
    /**
     * For LocalAttribute, GlobalReference will be:
     * getType(), when a GlobalSimpleType is modified,
     * getRef(), when a GlobalAttribute is modified.
     */
    public void visit(AttributeReference attribute) {
	
        if(global_component instanceof GlobalAttribute) {
            SchemaComponentFactory factory = global_component.getSchemaModel().getFactory();
            GlobalReference<GlobalAttribute> ref = 
                factory.createGlobalReference(
                    GlobalAttribute.class.cast(global_component),
                    GlobalAttribute.class,
                    attribute);
            attribute.setRef(ref);
        }
    }
        
    /**
     * For AttributeGroupReference, GlobalReference will be:
     * getGroup(), when a GlobalAttributeGroup is modified.
     */
    public void visit(AttributeGroupReference agr) {
        if(global_component instanceof GlobalAttributeGroup) {
            SchemaComponentFactory factory = global_component.getSchemaModel().getFactory();
            GlobalReference<GlobalAttributeGroup> ref = 
                factory.createGlobalReference(
                    GlobalAttributeGroup.class.cast(global_component),
                    GlobalAttributeGroup.class,
                    agr);
            agr.setGroup(ref);
        }        
    }
        
    /**
     * For ComplexContentRestriction, GlobalReference will be:
     * getBase(), when a GlobalComplexType is modified.
     */
    public void visit(ComplexContentRestriction ccr) {
        if(global_component instanceof GlobalComplexType) {
            SchemaComponentFactory factory = global_component.getSchemaModel().getFactory();
            GlobalReference<GlobalComplexType> ref = 
                factory.createGlobalReference(
                    GlobalComplexType.class.cast(global_component),
                    GlobalComplexType.class,
                    ccr);
            ccr.setBase(ref);
        }
    }
    
    /**
     * For SimpleExtension, GlobalReference will be:
     * getBase(), when a GlobalType is modified.
     */
    public void visit(SimpleExtension extension) {
        if(global_component instanceof GlobalSimpleType ||
           global_component instanceof GlobalComplexType) {
            SchemaComponentFactory factory = global_component.getSchemaModel().getFactory();
            GlobalReference<GlobalType> ref = 
                factory.createGlobalReference(
                    GlobalType.class.cast(global_component),
                    GlobalType.class,
                    extension);
            extension.setBase(ref);
        }
    }
    
    /**
     * For ComplexExtension, GlobalReference will be:
     * getBase(), when a GlobalType is modified.
     */
    public void visit(ComplexExtension extension) {
        if(global_component instanceof GlobalSimpleType ||
           global_component instanceof GlobalComplexType) {
            SchemaComponentFactory factory = global_component.getSchemaModel().getFactory();
            GlobalReference<GlobalType> ref = 
                factory.createGlobalReference(
               GlobalType.class.cast(global_component),
               GlobalType.class, extension);
            extension.setBase(ref);
        }
    }
    
    /**
     * For GroupReference, GlobalReference will be:
     * getRef(), when a GlobalGroup is modified.
     */
    public void visit(GroupReference gr) {
        if(global_component instanceof GlobalGroup) {
            SchemaComponentFactory factory = global_component.getSchemaModel().getFactory();
            GlobalReference<GlobalGroup> ref = 
                factory.createGlobalReference(
                    GlobalGroup.class.cast(global_component),
                    GlobalGroup.class, gr);
            gr.setRef(ref);
        }
    }
    
    /**
     * For List, GlobalReference will be:
     * getType(), when a GlobalSimpleType is modified.
     */
    public void visit(List list) {
        if(global_component instanceof GlobalSimpleType) {
            SchemaComponentFactory factory = global_component.getSchemaModel().getFactory();
            GlobalReference<GlobalSimpleType> ref = 
                factory.createGlobalReference(
                    GlobalSimpleType.class.cast(global_component),
                    GlobalSimpleType.class, list); 
            list.setType(ref);
        }
    }
}
