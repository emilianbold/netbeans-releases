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
package org.netbeans.modules.java.editor.overridden;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.api.java.source.ClassIndex;

/**
 *
 * @author Jan Lahoda
 */
class IsOverriddenVisitor extends CancellableTreePathScanner<Void, Tree> {
    
    private CompilationInfo info;
    private ClassIndex uq;
    private Document doc;
    
    Map<ElementHandle<TypeElement>, List<ElementHandle<ExecutableElement>>> type2Declaration;
    Map<ElementHandle<ExecutableElement>, MethodTree> declaration2Tree;
    Map<ElementHandle<TypeElement>, ClassTree> declaration2Class;
    
    private Map<TypeElement, ElementHandle<TypeElement>> type2Handle;
    
    IsOverriddenVisitor(Document doc, CompilationInfo info) {
        this.doc = doc;
        this.info = info;
        this.uq = info.getJavaSource().getClasspathInfo().getClassIndex();
        
        type2Declaration = new HashMap<ElementHandle<TypeElement>, List<ElementHandle<ExecutableElement>>>();
        declaration2Tree = new HashMap<ElementHandle<ExecutableElement>, MethodTree>();
        declaration2Class = new HashMap<ElementHandle<TypeElement>, ClassTree>();
        
        type2Handle = new HashMap<TypeElement, ElementHandle<TypeElement>>();
    }
    
    private ElementHandle<TypeElement> getHandle(TypeElement type) {
        ElementHandle<TypeElement> result = type2Handle.get(type);
        
        if (result == null) {
            type2Handle.put(type, result = ElementHandle.create(type));
        }
        
        return result;
    }
    
    @Override
    public Void visitMethod(MethodTree tree, Tree d) {
        if (currentClass != null) {
            Element el = info.getTrees().getElement(getCurrentPath());
            
            if (el != null && el.getKind()  == ElementKind.METHOD) {
                if (!el.getModifiers().contains(Modifier.PRIVATE) && !el.getModifiers().contains(Modifier.STATIC)) {
                    ExecutableElement overridee = (ExecutableElement) el;
                    List<ElementHandle<ExecutableElement>> methods = type2Declaration.get(currentClass);
                    
                    if (methods == null) {
                        type2Declaration.put(currentClass, methods = new ArrayList<ElementHandle<ExecutableElement>>());
                    }
                    
                    ElementHandle<ExecutableElement> methodHandle = ElementHandle.create(overridee);
                    
                    methods.add(methodHandle);
                    declaration2Tree.put(methodHandle, tree);
                }
            }
        }
        
        super.visitMethod(tree, tree);
        return null;
    }
    
    @Override
    public Void visitClass(ClassTree tree, Tree d) {
        Element decl = info.getTrees().getElement(getCurrentPath());
        
        if (decl != null && (decl.getKind().isClass() || decl.getKind().isInterface())) {
            ElementHandle<TypeElement> oldCurrentClass = currentClass;
            
            currentClass = getHandle((TypeElement) decl);
            declaration2Class.put(currentClass, tree);
            super.visitClass(tree, d);
            currentClass = oldCurrentClass;
        } else {
            super.visitClass(tree, d);
        }
        
        return null;
    }
    
    private ElementHandle<TypeElement> currentClass;
    
}