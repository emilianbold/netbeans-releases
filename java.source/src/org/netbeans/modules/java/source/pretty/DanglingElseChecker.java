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
package org.netbeans.modules.java.source.pretty;

import java.io.*;

import com.sun.tools.javac.util.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;

public class DanglingElseChecker extends Visitor {
    boolean foundDanglingElse;
    public boolean hasDanglingElse(JCTree t) {
	if(t==null) return false;
	foundDanglingElse = false;
	t.accept(this);
	return foundDanglingElse;
    }
    @Override
    public void visitTree(JCTree tree) {
    }
    @Override
    public void visitIf(JCIf tree) {
	if(tree.elsepart==null) foundDanglingElse = true;
	else tree.elsepart.accept(this);
    }
    @Override
    public void visitWhileLoop(JCWhileLoop tree) {
	tree.body.accept(this);
    }
    @Override
    public void visitDoLoop(JCDoWhileLoop tree) {
	tree.body.accept(this);
    }
    @Override
    public void visitForLoop(JCForLoop tree) {
	tree.body.accept(this);
    }
    @Override
    public void visitSynchronized(JCSynchronized tree) {
	tree.body.accept(this);
    }
    @Override
    public void visitLabelled(JCLabeledStatement tree) {
	tree.body.accept(this);
    }
    @Override
    public void visitBlock(JCBlock tree) {
	// Do dangling else checks on single statement blocks since
	// they often get eliminated and replaced by their constained statement
	if(!tree.stats.isEmpty() && tree.stats.tail.isEmpty())
	    tree.stats.head.accept(this);
    }
}
