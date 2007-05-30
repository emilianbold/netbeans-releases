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

package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.*;
import com.sun.source.util.TreePathScanner;
import javax.lang.model.element.*;
import org.netbeans.api.java.source.CompilationInfo;

/**
 *
 * @author Jan Becicka
 */
public class LocalVarScanner extends TreePathScanner<Boolean, Element> {

    private CompilationInfo info;
    private String newName;
    boolean result = false;
    public LocalVarScanner(CompilationInfo workingCopy, String newName) {
        this.info = workingCopy;
        this.newName = newName;
    }

    @Override
    public Boolean visitVariable(VariableTree variable, Element element) {
        if (variable.getName().toString().equals(newName)) {
            result= true;
        }
        return super.visitVariable(variable, element);
    }
    @Override
    public Boolean visitIdentifier(IdentifierTree node, Element p) {
        Element current = info.getTrees().getElement(getCurrentPath());
        if (current.getKind().isField() && node.getName().toString().equals(newName)) {
            result = true;
        }
        return super.visitIdentifier(node, p);
    }
}
