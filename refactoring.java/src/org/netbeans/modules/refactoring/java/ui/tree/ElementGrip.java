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

package org.netbeans.modules.refactoring.java.ui.tree;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import javax.lang.model.element.Element;
import javax.swing.Icon;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.UiUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 */
public final class ElementGrip {
    private TreePathHandle delegateElementHandle;
    private String toString;
    private FileObject fileObject;
    private Icon icon;
    
    /**
     * Creates a new instance of ElementGrip
     */
    public ElementGrip(TreePath treePath, CompilationInfo info) {
        this.delegateElementHandle = TreePathHandle.create(treePath, info);
        this.toString = UiUtils.getHeader(treePath, info, UiUtils.PrintPart.NAME);
        this.fileObject = info.getFileObject();
        this.icon = UiUtils.getDeclarationIcon(info.getTrees().getElement(treePath));
    }
    
    public Icon getIcon() {
        return icon;
    }
    public String toString() {
        return toString;
    }

    public ElementGrip getParent() {
        return ElementGripFactory.getDefault().getParent(this);
    }

    public TreePath resolve(CompilationInfo info) {
        return delegateElementHandle.resolve(info);
    } 

    public Element resolveElement(CompilationInfo info) {
        return delegateElementHandle.resolveElement(info);
    } 

    public Tree.Kind getKind() {
        return delegateElementHandle.getKind();
    }
    
    public FileObject getFileObject() {
        return fileObject;
    }
    
    public TreePathHandle getHandle() {
        return delegateElementHandle;
    }
    
}
