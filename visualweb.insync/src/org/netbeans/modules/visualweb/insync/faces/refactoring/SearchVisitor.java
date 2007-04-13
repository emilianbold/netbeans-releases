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

package org.netbeans.modules.visualweb.insync.faces.refactoring;

import java.util.ArrayList;
import java.util.Collection;

import javax.lang.model.element.Element;

import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;

/**
 *
 * @author Sandip Chitale
 */
public class SearchVisitor extends TreePathScanner<Tree, Element> {
    private Collection<TreePath> usages = new ArrayList<TreePath>();
    protected final WorkingCopy workingCopy;
    protected final TreeMaker make;
    
    public SearchVisitor(WorkingCopy workingCopy) {
        super();
        this.workingCopy = workingCopy;
        this.make = workingCopy.getTreeMaker();
    }
    
    public Collection<TreePath> getUsages() {
        return usages;
    }
    
    protected void addUsage(TreePath tp) {
        assert tp != null;
        usages.add(tp);
    }
}
