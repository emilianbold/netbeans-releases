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

package org.netbeans.modules.refactoring.java.ui;

import java.util.Collection;
import java.util.Dictionary;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.spi.ui.JavaActionsImplementationProvider;
import org.netbeans.modules.refactoring.java.ui.ExtractInterfaceRefactoringUI;
import org.netbeans.modules.refactoring.java.ui.RefactoringActionsProvider.NodeToFileObject;
import org.netbeans.modules.refactoring.java.ui.RefactoringActionsProvider.TextComponentRunnable;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Becicka
 */
public class JavaRefactoringActionsProvider extends JavaActionsImplementationProvider{
    
    public JavaRefactoringActionsProvider() {
    }
    @Override
    public void doExtractInterface(final Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        final Dictionary dictionary = lookup.lookup(Dictionary.class);
        if (RefactoringActionsProvider.isFromEditor(ec)) {
            new RefactoringActionsProvider.TextComponentRunnable(ec) {
                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, CompilationInfo info) {
                    Element selected = selectedElement.resolveElement(info);
                    return new ExtractInterfaceRefactoringUI(selectedElement, info);
                }
            }.run();
        } else {
            new NodeToFileObject(lookup.lookupAll(Node.class)) {
                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements) {
                    throw new UnsupportedOperationException("Not supported yet!");
                    //return new ExtractInterfaceRefactoringUI(selectedElements[0],(CompilationInfo) null);
                }
            }.run();
        }
    }

    @Override
    public boolean canExtractInterface(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        if (nodes.size() != 1) {
            return false;
        }
        Node n = nodes.iterator().next();
        DataObject dob = n.getCookie(DataObject.class);
        if (dob==null) {
            return false;
        }
        FileObject fo = dob.getPrimaryFile();
        if (RetoucheUtils.isRefactorable(fo)) { //NOI18N
            return true;
        }
        if ((dob instanceof DataFolder) && 
                RetoucheUtils.isFileInOpenProject(fo) && 
                RetoucheUtils.isOnSourceClasspath(fo) &&
                !RetoucheUtils.isClasspathRoot(fo))
            return true;
        return false;
    }
    
    @Override
    public void doPushDown(final Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        final Dictionary dictionary = lookup.lookup(Dictionary.class);
        if (RefactoringActionsProvider.isFromEditor(ec)) {
            new RefactoringActionsProvider.TextComponentRunnable(ec) {
                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, CompilationInfo info) {
                    Element selected = selectedElement.resolveElement(info);
                    return new PushDownRefactoringUI(new TreePathHandle[]{selectedElement}, info);
                }
            }.run();
        } else {
            new NodeToFileObject(lookup.lookupAll(Node.class)) {
                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements) {
                    throw new UnsupportedOperationException("Not supported yet!");
                    //return new ExtractInterfaceRefactoringUI(selectedElements[0],(CompilationInfo) null);
                }
            }.run();
        }
    }

    @Override
    public boolean canPushDown(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        if (nodes.size() != 1) {
            return false;
        }
        Node n = nodes.iterator().next();
        DataObject dob = n.getCookie(DataObject.class);
        if (dob==null) {
            return false;
        }
        FileObject fo = dob.getPrimaryFile();
        if (RetoucheUtils.isRefactorable(fo)) { //NOI18N
            return true;
        }
        if ((dob instanceof DataFolder) && 
                RetoucheUtils.isFileInOpenProject(fo) && 
                RetoucheUtils.isOnSourceClasspath(fo) &&
                !RetoucheUtils.isClasspathRoot(fo))
            return true;
        return false;
    }
    
    @Override
    public void doPullUp(final Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        final Dictionary dictionary = lookup.lookup(Dictionary.class);
        if (RefactoringActionsProvider.isFromEditor(ec)) {
            new RefactoringActionsProvider.TextComponentRunnable(ec) {
                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, CompilationInfo info) {
                    Element selected = selectedElement.resolveElement(info);
                    return new PullUpRefactoringUI(new TreePathHandle[]{selectedElement}, info);
                }
            }.run();
        } else {
            new NodeToFileObject(lookup.lookupAll(Node.class)) {
                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements) {
                    throw new UnsupportedOperationException("Not supported yet!");
                    //return new ExtractInterfaceRefactoringUI(selectedElements[0],(CompilationInfo) null);
                }
            }.run();
        }
    }

    @Override
    public boolean canPullUp(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        if (nodes.size() != 1) {
            return false;
        }
        Node n = nodes.iterator().next();
        DataObject dob = n.getCookie(DataObject.class);
        if (dob==null) {
            return false;
        }
        FileObject fo = dob.getPrimaryFile();
        if (RetoucheUtils.isRefactorable(fo)) { //NOI18N
            return true;
        }
        if ((dob instanceof DataFolder) && 
                RetoucheUtils.isFileInOpenProject(fo) && 
                RetoucheUtils.isOnSourceClasspath(fo) &&
                !RetoucheUtils.isClasspathRoot(fo))
            return true;
        return false;
    }    

}
