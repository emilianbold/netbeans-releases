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
