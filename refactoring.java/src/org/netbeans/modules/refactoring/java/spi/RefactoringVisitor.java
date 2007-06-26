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

package org.netbeans.modules.refactoring.java.spi;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import javax.lang.model.element.*;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.openide.ErrorManager;

/**
 *
 * @author Jan Becicka
 */
public class RefactoringVisitor extends TreePathScanner<Tree, Element> {
    /**
     * 
     */
    protected WorkingCopy workingCopy;
    /**
     * 
     */
    protected TreeMaker make;
    
    /**
     * 
     * @param workingCopy 
     * @throws org.netbeans.modules.refactoring.java.spi.ToPhaseException 
     */
    public void setWorkingCopy(WorkingCopy workingCopy) throws ToPhaseException {
        this.workingCopy = workingCopy;
        try {
            if (this.workingCopy.toPhase(JavaSource.Phase.RESOLVED) != JavaSource.Phase.RESOLVED) {
                throw new ToPhaseException();
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
        this.make = workingCopy.getTreeMaker();
    }
    
    /**
     * 
     * @param oldTree 
     * @param newTree 
     */
    protected void rewrite(Tree oldTree, Tree newTree) {
        workingCopy.rewrite(oldTree, newTree);
        TreePath current = getCurrentPath();
        if (current.getLeaf() == oldTree) {
            JavaRefactoringUtils.cacheTreePathInfo(current, workingCopy);
        } else {
            TreePath tp = workingCopy.getTrees().getPath(current.getCompilationUnit(), oldTree);
            JavaRefactoringUtils.cacheTreePathInfo(current, workingCopy);
        }
    }
    
}
