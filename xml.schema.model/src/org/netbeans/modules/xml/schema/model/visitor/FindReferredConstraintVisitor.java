/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * KeyReferenceVisitor.java
 *
 * Created on November 5, 2005, 12:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.visitor;

import java.util.Collection;
import org.netbeans.modules.xml.schema.model.Constraint;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 *
 * @author rico
 * Visitor that searches schema elements for a key or unique.
 * This is used by KeyRef to look for keys or unique that
 * it refers to in its "refer" attribute.
 */
public class FindReferredConstraintVisitor extends DeepSchemaVisitor{
    
    private Constraint constraint;
    private String name;
    private boolean found;
    
    /**
     * Creates a new instance of FindReferredConstraintVisitor
     */
    public FindReferredConstraintVisitor() {
    }
    
    /**
     * Recursively searches from parent for the Constraint (unique or key) that
     * has the same name as the name parameter.
     * @param parent Node where searching will start. 
     * @param name name of Constraint to look for
     */
    public Constraint findReferredConstraint(SchemaComponent parent, String name){
        this.name = name;
        found = false;
        parent.accept(this);
        return constraint;
    }
    
    public void visit(LocalElement le) {
        if(findConstraint(le.getConstraints())){
            return;
        }
        super.visit(le);
    }
    
    public void visit(GlobalElement ge) {
        if(findConstraint(ge.getConstraints())){
            return;
        }
        super.visit(ge);
    }
    
    protected void visitChildren(SchemaComponent sc) {
        for (SchemaComponent child: sc.getChildren()) {
            child.accept(this);
            if(found) return;
        }
    }
    
    private boolean findConstraint(Collection<Constraint> constraints){
        for(Constraint c : constraints){
            if(c.getName().equals(name)){
                constraint = c;
                found = true;
                return true;
            }
        }
        return false;
    }
}
