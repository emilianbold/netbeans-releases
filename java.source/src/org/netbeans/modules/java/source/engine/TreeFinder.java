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

package org.netbeans.modules.java.source.engine;

import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;

/**
 * Finds a specified tree and then stops scanning.
 */
public class TreeFinder extends TreeScanner<Boolean,Object> {
    protected Tree target;
    protected boolean found = false;

    /**
     * Create a new TreeFinder.
     *
     * @param target the tree to be found.
     */
    public TreeFinder(Tree target) {
        this.target = target;
    }

    /**
     * Scan a tree for a previously specified target.
     *
     * @param tree the tree to search, such as the root tree.
     * @param o  an ignored parameter, specified by the TreeVisitor interface.
     */
    public Boolean scan(Tree tree, Object o) {
        found = found || tree == target;
        if (!found && tree != null)
            tree.accept(this, o);
        return found;
    }

    /**
     * Scan a list of trees for a previously specified target.
     *
     * @param trees the list to search.
     * @param o  an ignored parameter, specified by the TreeVisitor interface.
     */
    public Boolean scan(Iterable<? extends Tree> trees, Object o) {
        if (!found && trees != null)
            for (Tree tree : trees)
                if (scan(tree, o))
                    return true;
        return found;
    }
}
