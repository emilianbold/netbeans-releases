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

package org.netbeans.modules.xml.schema.model.visitor;

import java.util.Collection;
import java.util.LinkedList;

import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.xam.GlobalReference;
import org.netbeans.modules.xml.xam.Referenceable;

/**
 *
 * @author Samaresh
 */
public class FindUsageVisitor extends DeepSchemaVisitor {
    
    /**
     * The global component being modified.
     */
    private Referenceable<SchemaComponent> globalSchemaComponent         = null;
            
    /**
     * Usage found.
     */
    private boolean found                               = false;
    
    /**
     * Preview
     */
    private PreviewImpl preview                         = null;
    
    /**
     * Find usages for the specified type in the list of the schemas.
     */
    public Preview findUsages(Collection<Schema> roots, 
	Referenceable<SchemaComponent> component ) {
	preview = new PreviewImpl();
        this.globalSchemaComponent = component;
        return findUsages(roots);
    }

    /**
     * All the usage methods eventually call this to get the preview.
     */
    private Preview findUsages(Collection<Schema> roots) {
        for(Schema schema : roots) {
            schema.accept(this);
        }
        
        return preview;
    }
        
    /**
     * For CommonSimpleRestriction, GlobalReference will be:
     * getBase(), when a GlobalSimpleType is modified.
     */
    public void visit(SimpleTypeRestriction str) {
        if(globalSchemaComponent instanceof GlobalSimpleType)
            checkReference(str.getBase(), str);
        
        super.visit(str);
    }
        
    /**
     * For LocalElement, GlobalReference will be:
     * getType(), when a GlobalType is modified,
     * getRef(), when a GlobalElement is modified.
     */
    public void visit(LocalElement element) {
        if(globalSchemaComponent instanceof GlobalType)
            checkReference(element.getType(), element);
                
        super.visit(element);
    }
    
    /**
     * For LocalElement, GlobalReference will be:
     * getType(), when a GlobalType is modified,
     * getRef(), when a GlobalElement is modified.
     */
    public void visit(ElementReference element) {
        if(globalSchemaComponent instanceof GlobalElement)
            checkReference(element.getRef(), element);
                
        super.visit(element);
    }
    
    /**
     * For AllElement, GlobalReference will be:
     * getType(), when a GlobalType is modified,
     * getRef(), when a GlobalElement is modified.
     */
    public void visit(AllElement element) {
        if(globalSchemaComponent instanceof GlobalType)
            checkReference(element.getType(), element);
                
        super.visit(element);
    }
    
    /**
     * For AllElement, GlobalReference will be:
     * getType(), when a GlobalType is modified,
     * getRef(), when a GlobalElement is modified.
     */
    public void visit(AllElementReference element) {
        if(globalSchemaComponent instanceof GlobalElement)
            checkReference(element.getRef(), element);
                
        super.visit(element);
    }
    
    /**
     * For GlobalElement, GlobalReference will be:
     * getType(), when a GlobalType is modified,
     * getSubstitutionGroup(), when a GlobalElement is modified.
     */
    public void visit(GlobalElement element) {
        if(globalSchemaComponent instanceof GlobalType)
            checkReference(element.getType(), element);
        else if(globalSchemaComponent instanceof GlobalElement)
            checkReference(element.getSubstitutionGroup(), element);
                
        super.visit(element);
    }
        
    /**
     * For LocalAttribute, GlobalReference will be:
     * getType(), when a GlobalSimpleType is modified,
     * getRef(), when a GlobalAttribute is modified.
     */
    public void visit(LocalAttribute attribute) {
        if(globalSchemaComponent instanceof GlobalSimpleType)
            checkReference(attribute.getType(), attribute);
        
        super.visit(attribute);
    }
    
     /**
     * For LocalAttribute, GlobalReference will be:
     * getType(), when a GlobalSimpleType is modified,
     * getRef(), when a GlobalAttribute is modified.
     */
    public void visit(AttributeReference attribute) {
       if(globalSchemaComponent instanceof AttributeReference)
            checkReference(attribute.getRef(),attribute);
        
        super.visit(attribute);
    }
        
    /**
     * For AttributeGroupReference, GlobalReference will be:
     * getGroup(), when a GlobalAttributeGroup is modified.
     */
    public void visit(AttributeGroupReference agr) {
        if(globalSchemaComponent instanceof GlobalAttributeGroup)
            checkReference(agr.getGroup(),agr);
        
        super.visit(agr);
    }
        
    /**
     * For ComplexContentRestriction, GlobalReference will be:
     * getBase(), when a GlobalComplexType is modified.
     */
    public void visit(ComplexContentRestriction ccr) {
        if(globalSchemaComponent instanceof GlobalComplexType)
            checkReference(ccr.getBase(),ccr);
                
        super.visit(ccr);
    }
    
    /**
     * For SimpleExtension, GlobalReference will be:
     * getBase(), when a GlobalType is modified.
     */
    public void visit(SimpleExtension extension) {
        if(globalSchemaComponent instanceof GlobalType)
            checkReference(extension.getBase(), extension);
                
        super.visit(extension);
    }
    
    /**
     * For ComplexExtension, GlobalReference will be:
     * getBase(), when a GlobalType is modified.
     */
    public void visit(ComplexExtension extension) {
        if (globalSchemaComponent instanceof GlobalType) {
            checkReference(extension.getBase(), extension);
        }     
        super.visit(extension);
    }
    
    /**
     * For GroupReference, GlobalReference will be:
     * getRef(), when a GlobalGroup is modified.
     */
    public void visit(GroupReference gr) {
        if(globalSchemaComponent instanceof GlobalGroup)
            checkReference(gr.getRef(), gr);
                
        super.visit(gr);
    }
    
    /**
     * For List, GlobalReference will be:
     * getType(), when a GlobalSimpleType is modified.
     */
    public void visit(List list) {
        if(globalSchemaComponent instanceof GlobalSimpleType)
            checkReference(list.getType(), list);
                
        super.visit(list);
    }
    
    private void checkReference(GlobalReference gr, 
        SchemaComponent component) {
        if (gr != null && gr.references(globalSchemaComponent)) {
            found = true;
            preview.addToUsage(component);            
        }
    }
    
    protected void visitChildren(SchemaComponent sc) {
        if(found && sc.getChildren().size() == 0) {
            found = false;
            return;
        }
                        
        super.visitChildren(sc);
    }
    
}
