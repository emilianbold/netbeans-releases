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

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import javax.lang.model.element.*;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Becicka
 */
public class FindVisitor extends RefactoringVisitor {

    private Collection<TreePath> usages = new ArrayList<TreePath>();

    public FindVisitor(WorkingCopy workingCopy) {
        try {
            setWorkingCopy(workingCopy);
        } catch (ToPhaseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    
    public Collection<TreePath> getUsages() {
        return usages;
    }
    
    protected void addUsage(TreePath tp) {
        assert tp != null;
        usages.add(tp);
    }
}
