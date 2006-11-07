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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.java;

import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;

import java.util.Iterator;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;

import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author Dusan Balek
 */
public class AutoImport extends TreeScanner<Void, Void> {

    private WorkingCopy copy;
    private TypeMirror type;

    private AutoImport(WorkingCopy copy) {
        this.copy = copy;
    }

    public static AutoImport get(WorkingCopy copy) {
        return new AutoImport(copy);
    }
    
    public static void resolveImport(WorkingCopy copy, Tree tree, TypeMirror type) {
        AutoImport imp = new AutoImport(copy);
        imp.resolveImport(tree, type);
    }
    
    public void resolveImport(Tree tree, TypeMirror type) {
        switch(tree.getKind()) {
            case IDENTIFIER:
            case PARAMETERIZED_TYPE:
                this.type = type;
                scan(tree, null);                
        }
    }
    
    @Override
    public Void visitIdentifier(IdentifierTree node, Void p) {
        assert type.getKind() == TypeKind.DECLARED;
        ElementHandle<TypeElement> handle = ElementHandle.create((TypeElement)((DeclaredType)type).asElement());
        TypeElement e = handle.resolve(copy);        
        assert node.getName().contentEquals(e.getSimpleName());
        Tree t = copy.getTreeMaker().QualIdent(e);
        copy.rewrite(node, t);
        return null;
    }

    @Override
    public Void visitParameterizedType(ParameterizedTypeTree node, Void p) {
        assert type.getKind() == TypeKind.DECLARED;
	    scan(node.getType(), null);
        Iterator<? extends TypeMirror> args = ((DeclaredType)type).getTypeArguments().iterator();
        for (Tree ta : node.getTypeArguments())
            if (args.hasNext()) {
                type = args.next();
                scan(ta, null);
            }
        return null;
    }
    
    @Override
    public Void visitWildcard(WildcardTree node, Void p) {
        if (type.getKind() == TypeKind.WILDCARD) {
            TypeMirror bound = ((WildcardType) type).getExtendsBound();
            if (bound == null)
                bound = ((WildcardType) type).getSuperBound();
            type = bound;
        }
        else if (type.getKind() == TypeKind.TYPEVAR) {
            TypeMirror bound = ((TypeVariable) type).getLowerBound();
            if (bound == null || bound.getKind() == TypeKind.NULL)
                bound = ((TypeVariable) type).getUpperBound();
            type = bound.getKind() == TypeKind.NULL ? null : bound;
        } else {
            type = null;
        }
        if (type != null)
	        scan(node.getBound(), null);
        return null;
    }    

    @Override
    public java.lang.Void visitArrayType(ArrayTypeTree node, Void p) {
        assert type.getKind() == TypeKind.ARRAY;
        type = ((ArrayType) type).getComponentType();
        scan(node.getType(), null);
        return null;
    }
}
