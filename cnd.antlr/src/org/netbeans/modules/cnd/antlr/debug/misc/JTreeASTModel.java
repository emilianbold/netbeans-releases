/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.antlr.debug.misc;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

import org.netbeans.modules.cnd.antlr.collections.AST;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public class JTreeASTModel implements TreeModel {

    AST root = null;

    public JTreeASTModel(AST t) {
        if (t == null) {
            throw new IllegalArgumentException("root is null");
        }
        root = t;
    }

    public void addTreeModelListener(TreeModelListener l) {
    }

    public Object getChild(Object parent, int index) {
        if (parent == null) {
            return null;
        }
        AST p = (AST)parent;
        AST c = p.getFirstChild();
        if (c == null) {
            throw new ArrayIndexOutOfBoundsException("node has no children");
        }
        int i = 0;
        while (c != null && i < index) {
            c = c.getNextSibling();
            i++;
        }
        return c;
    }

    public int getChildCount(Object parent) {
        if (parent == null) {
            throw new IllegalArgumentException("root is null");
        }
        AST p = (AST)parent;
        AST c = p.getFirstChild();
        int i = 0;
        while (c != null) {
            c = c.getNextSibling();
            i++;
        }
        return i;
    }

    public int getIndexOfChild(Object parent, Object child) {
        if (parent == null || child == null) {
            throw new IllegalArgumentException("root or child is null");
        }
        AST p = (AST)parent;
        AST c = p.getFirstChild();
        if (c == null) {
            throw new ArrayIndexOutOfBoundsException("node has no children");
        }
        int i = 0;
        while (c != null && c != child) {
            c = c.getNextSibling();
            i++;
        }
        if (c == child) {
            return i;
        }
        throw new java.util.NoSuchElementException("node is not a child");
    }

    public Object getRoot() {
        return root;
    }

    public boolean isLeaf(Object node) {
        if (node == null) {
            throw new IllegalArgumentException("node is null");
        }
        AST t = (AST)node;
        return t.getFirstChild() == null;
    }

    public void removeTreeModelListener(TreeModelListener l) {
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        System.out.println("heh, who is calling this mystery method?");
    }
}
