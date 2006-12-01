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

import java.util.Iterator;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor6;

import com.sun.source.util.TreePath;

import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author Dusan Balek
 */
public class AutoImport extends SimpleTypeVisitor6<Void, Void> {

    private static final String CAPTURED_WILDCARD = "<captured wildcard>"; //NOI18N

    private WorkingCopy copy;
    private StringBuilder builder;
    private TreePath path;

    private AutoImport(WorkingCopy copy) {
        this.copy = copy;
    }

    public static AutoImport get(WorkingCopy copy) {
        return new AutoImport(copy);
    }
    
    public static CharSequence resolveImport(WorkingCopy copy, TreePath treePath, TypeMirror type) {
        AutoImport imp = new AutoImport(copy);
        return imp.resolveImport(treePath, type);
    }
    
    public CharSequence resolveImport(TreePath treePath, TypeMirror type) {
        this.builder = new StringBuilder();
        this.path = treePath;
        visit(type, null);
        return builder;
    }
    
    @Override
    public Void defaultAction(TypeMirror type, Void p) {
        builder.append(type);
        return null;
    }
        
    @Override
    public Void visitArray(ArrayType type, Void p) {
        visit(type.getComponentType());
        builder.append("[]"); //NOI18N
        return null;
    }
    
    @Override
    public Void visitDeclared(DeclaredType type, Void p) {
        String name = ((TypeElement)type.asElement()).getQualifiedName().toString();
        try {
            name = SourceUtils.resolveImport(copy, path, name);
        } catch (Exception e) {
        }
        builder.append(name);
        Iterator<? extends TypeMirror> it = type.getTypeArguments().iterator();
        if (it.hasNext()) {
            builder.append('<'); //NOI18N
            while(it.hasNext()) {
                visit(it.next());
                if (it.hasNext())
                    builder.append(", "); //NOI18N
            }
            builder.append('>'); //NOI18N
        }
        return null;
    }
    
    @Override
    public Void visitTypeVariable(TypeVariable type, Void p) {
        Element e = type.asElement();
        if (e != null) {
            CharSequence name = e.getSimpleName();
            if (!CAPTURED_WILDCARD.contentEquals(name)) {
                builder.append(name);
                return null;
            }
        }
        builder.append("?"); //NOI18N
        TypeMirror bound = type.getLowerBound();
        if (bound != null && bound.getKind() != TypeKind.NULL) {
            builder.append(" super "); //NOI18N
            visit(bound);
        } else {
            bound = type.getUpperBound();
            if (bound != null && bound.getKind() != TypeKind.NULL) {
                builder.append(" extends "); //NOI18N
                if (bound.getKind() == TypeKind.TYPEVAR)
                    bound = ((TypeVariable)bound).getLowerBound();
                visit(bound);
            }
        }
        return null;
    }

    @Override
    public Void visitWildcard(WildcardType type, Void p) {
        builder.append("?"); //NOI18N
        TypeMirror bound = type.getSuperBound();
        if (bound == null) {
            bound = type.getExtendsBound();
            if (bound != null) {
                builder.append(" extends "); //NOI18N
                if (bound.getKind() == TypeKind.WILDCARD)
                    bound = ((WildcardType)bound).getSuperBound();
                visit(bound);
            }
        } else {
            builder.append(" super "); //NOI18N
            visit(bound);
        }
        return null;
    }

    @Override
    public Void visitError(ErrorType type, Void p) {
        Element e = type.asElement();
        if (e instanceof TypeElement) {
            TypeElement te = (TypeElement)e;
            builder.append(te.getSimpleName());            
        }
        return null;
    }
}
