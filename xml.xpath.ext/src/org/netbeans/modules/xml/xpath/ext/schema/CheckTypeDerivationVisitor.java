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

package org.netbeans.modules.xml.xpath.ext.schema;

import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexContentRestriction;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleContentRestriction;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;
import org.netbeans.modules.xml.schema.model.visitor.DefaultSchemaVisitor;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * This schema visitor is inteneded to check if one type is derived from another.
 * 
 * @author nk160297
 */
public class CheckTypeDerivationVisitor extends DefaultSchemaVisitor {
    
    private GlobalType mBaseType; // Base type has to be global
    private SchemaComponent mDerivedType;
    private boolean isDerived = false;
    
    public CheckTypeDerivationVisitor(GlobalType baseType, 
            SchemaComponent derivedType) {
        assert baseType != null: "The baseType has to be specified!"; // NOI18N
        assert derivedType != null && 
                (derivedType instanceof GlobalType || 
                derivedType instanceof LocalType):
            "The derivedType has to be a Type!"; // NOI18N
        //
        mBaseType = baseType;
        mDerivedType = derivedType;
    }
    
    public boolean isDerived() {
        try {
            visitChildren(mDerivedType);
        } catch (InterruptSearchException ex) {
            // do nothing here
        }
        return isDerived;
    }

    //=========================================================================
    
    @Override
    public void visit(GlobalSimpleType type) {
        checkType(type);
        visitChildren(type);
    }

    @Override
    public void visit(GlobalComplexType type) {
        checkType(type);
        visitChildren(type);
    }
    
    //=========================================================================
    
    @Override
    public void visit(LocalSimpleType type) {
        visitChildren(type);
    }

    @Override
    public void visit(LocalComplexType type) {
        visitChildren(type);
    }
    
    //=========================================================================
    
    @Override
    public void visit(SimpleContent sc) {
        visitChildren(sc);
    }
    
    @Override
    public void visit(ComplexContent cc) {
        visitChildren(cc);
    }
    
    @Override
    public void visit(SimpleExtension se) {
        visitBaseTypeRef(se.getBase());
    }
    
    @Override
    public void visit(ComplexExtension ce) {
        visitBaseTypeRef(ce.getBase());
    }
    
    @Override
    public void visit(SimpleContentRestriction scr) {
        visitBaseTypeRef(scr.getBase());
    }
    
    @Override
    public void visit(ComplexContentRestriction ccr) {
        visitBaseTypeRef(ccr.getBase());
    }
    
    @Override
    public void visit(SimpleTypeRestriction str) {
        visitBaseTypeRef(str.getBase());
    }
    
    // ----------------------------------------------
    
    protected void checkType(GlobalType gt) {
        if (gt == mBaseType) {
            isDerived = true;
        }
        throw new InterruptSearchException();
    }
    
    protected void visitBaseTypeRef(
            NamedComponentReference<? extends GlobalType> gTypeRef) {
        if (gTypeRef != null) {
            GlobalType gt = gTypeRef.get();
            if (gt != null) {
                gt.accept(this);
            }
        }
    }
    
    protected void visitChildren(SchemaComponent sc) {
        for (SchemaComponent child: sc.getChildren()) {
            child.accept(this);
        }
    }
    
    private class InterruptSearchException extends RuntimeException {
    }
    
}
