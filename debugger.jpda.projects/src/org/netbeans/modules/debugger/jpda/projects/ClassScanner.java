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

package org.netbeans.modules.debugger.jpda.projects;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Search for a class matching the given binary filter.
 * 
 * @author martin
 */
class ClassScanner extends TreePathScanner<TypeElement, Void> {
    
    private Trees trees;
    private Elements elements;
    private String binaryClassName;
    private String[] classExcludeNames;

    public ClassScanner(Trees trees, Elements elements, String binaryClassName, String[] classExcludeNames) {
        this.trees = trees;
        this.elements = elements;
        this.binaryClassName = binaryClassName;
        this.classExcludeNames = classExcludeNames;
    }

    @Override
    public TypeElement reduce(TypeElement arg0, TypeElement arg1) {
        if (arg0 != null) {
            return arg0;
        } else {
            return arg1;
        }
    }

    @Override
    public TypeElement visitClass(ClassTree arg0, Void arg1) {
        TypeElement typeElement = (TypeElement) trees.getElement(getCurrentPath());
        String binaryName = elements.getBinaryName(typeElement).toString();
        if (match(binaryName)) {
            return typeElement;
        } else {
            return super.visitClass(arg0, arg1);
        }
    }


    private boolean match(String binaryName) {
        if (match(binaryName, binaryClassName)) {
            if (classExcludeNames != null) {
                for (String classExcludeName : classExcludeNames) {
                    if (match(binaryName, classExcludeName)) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    private static boolean match (String name, String pattern) {
        if (pattern.startsWith ("*"))
            return name.endsWith (pattern.substring (1));
        else
        if (pattern.endsWith ("*"))
            return name.startsWith (
                pattern.substring (0, pattern.length () - 1)
            );
        return name.equals (pattern);
    }
    
}
