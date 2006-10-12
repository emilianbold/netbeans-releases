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

package org.netbeans.modules.xml.schema.model.visitor;

import java.util.Collection;

import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author Samaresh
 */
public class FindUsageVisitor extends DeepSchemaVisitor {

    /**
     * The global component being modified.
     */
    private NamedReferenceable<SchemaComponent> globalSchemaComponent         = null;

    /**
     * Preview
     */
    private PreviewImpl preview                         = null;
    
    /**
     * Find usages for the specified type in the list of the schemas.
     */
    public Preview findUsages(Collection<Schema> roots, NamedReferenceable<SchemaComponent> component ) {
	preview = new PreviewImpl();
        globalSchemaComponent = component;
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
        
    public void visit(Union u) {
        if (u.getMemberTypes() != null) {
            for (NamedComponentReference<GlobalSimpleType> t : u.getMemberTypes()) {
                checkReference(t, u);
            }
        }
        super.visit(u);
    }

    /**
     * For CommonSimpleRestriction, GlobalReference will be:
     * getBase(), when a GlobalSimpleType is modified.
     */
    public void visit(SimpleTypeRestriction str) {
        checkReference(str.getBase(), str);
        super.visit(str);
    }
        
    /**
     * For LocalElement, GlobalReference will be:
     * getType(), when a GlobalType is modified,
     * getRef(), when a GlobalElement is modified.
     */
    public void visit(LocalElement element) {
        checkReference((NamedComponentReference<GlobalType>)element.getType(), element);
        super.visit(element);
    }
    
    /**
     * For LocalElement, GlobalReference will be:
     * getType(), when a GlobalType is modified,
     * getRef(), when a GlobalElement is modified.
     */
    public void visit(ElementReference element) {
        checkReference(element.getRef(), element);
        super.visit(element);
    }
    
    /**
     * For GlobalElement, GlobalReference will be:
     * getType(), when a GlobalType is modified,
     * getSubstitutionGroup(), when a GlobalElement is modified.
     */
    public void visit(GlobalElement element) {
        checkReference(element.getType(), element);
        checkReference(element.getSubstitutionGroup(), element);
        super.visit(element);
    }
        
    /**
     * For LocalAttribute, GlobalReference will be:
     * getType(), when a GlobalSimpleType is modified,
     * getRef(), when a GlobalAttribute is modified.
     */
    public void visit(LocalAttribute attribute) {
        checkReference(attribute.getType(), attribute);
        super.visit(attribute);
    }
    
     /**
     * For LocalAttribute, GlobalReference will be:
     * getType(), when a GlobalSimpleType is modified,
     * getRef(), when a GlobalAttribute is modified.
     */
    public void visit(AttributeReference attribute) {
        checkReference(attribute.getRef(), attribute);
        super.visit(attribute);
    }
        
    /**
     * For AttributeGroupReference, GlobalReference will be:
     * getGroup(), when a GlobalAttributeGroup is modified.
     */
    public void visit(AttributeGroupReference agr) {
        checkReference(agr.getGroup(), agr);
        super.visit(agr);
    }
        
    /**
     * For ComplexContentRestriction, GlobalReference will be:
     * getBase(), when a GlobalComplexType is modified.
     */
    public void visit(ComplexContentRestriction ccr) {
        checkReference(ccr.getBase(), ccr);
        super.visit(ccr);
    }
    
    /**
     * For SimpleExtension, GlobalReference will be:
     * getBase(), when a GlobalType is modified.
     */
    public void visit(SimpleExtension extension) {
        checkReference(extension.getBase(), extension);
        super.visit(extension);
    }
    
    /**
     * For ComplexExtension, GlobalReference will be:
     * getBase(), when a GlobalType is modified.
     */
    public void visit(ComplexExtension extension) {
        checkReference(extension.getBase(), extension);
        super.visit(extension);
    }
    
    /**
     * For GroupReference, GlobalReference will be:
     * getRef(), when a GlobalGroup is modified.
     */
    public void visit(GroupReference gr) {
        checkReference(gr.getRef(), gr);
        super.visit(gr);
    }
    
    /**
     * For List, GlobalReference will be:
     * getType(), when a GlobalSimpleType is modified.
     */
    public void visit(List list) {
        checkReference(list.getType(), list);
        super.visit(list);
    }
    
    private <T extends NamedReferenceable<SchemaComponent>> void checkReference(
            NamedComponentReference<T> ref, SchemaComponent component) {
        if (ref == null || ! ref.getType().isAssignableFrom(globalSchemaComponent.getClass())) return;
        if (ref.references(ref.getType().cast(globalSchemaComponent))) {
            preview.addToUsage(component);            
        }
    }
}
