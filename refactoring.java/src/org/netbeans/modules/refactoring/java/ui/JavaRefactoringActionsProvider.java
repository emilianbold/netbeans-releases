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

package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.Collection;
import java.util.Dictionary;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.ui.JavaRefactoringActionsFactory;
import org.netbeans.modules.refactoring.java.spi.ui.JavaActionsImplementationProvider;
import org.netbeans.modules.refactoring.java.ui.RefactoringActionsProvider.NodeToFileObjectTask;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
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
        Runnable task;
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (RefactoringActionsProvider.isFromEditor(ec)) {
            task = new RefactoringActionsProvider.TextComponentTask(ec) {
                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, CompilationInfo info) {
                    return new ExtractInterfaceRefactoringUI(selectedElement, info);
                }
            };
        } else {
            task = new NodeToFileObjectTask(lookup.lookupAll(Node.class)) {
                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<TreePathHandle> handles) {
                    TreePathHandle tph = handles.iterator().next();
                    return new ExtractInterfaceRefactoringUI(tph, cinfo.get());
                }
            };
        }
        RetoucheUtils.invokeAfterScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.extractInterfaceAction()));
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
        return false;
    }

    @Override
    public void doExtractSuperclass(Lookup lookup) {
        Runnable task;
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (RefactoringActionsProvider.isFromEditor(ec)) {
            task = new RefactoringActionsProvider.TextComponentTask(ec) {
                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, CompilationInfo info) {
                    return new ExtractSuperclassRefactoringUI(selectedElement, info);
                }
            };
        } else {
            task = new NodeToFileObjectTask(lookup.lookupAll(Node.class)) {
                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<TreePathHandle> handles) {
                    TreePathHandle tph = handles.iterator().next();
                    return new ExtractSuperclassRefactoringUI(tph, cinfo.get());
                }
            };
        }
        RetoucheUtils.invokeAfterScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.extractSuperclassAction()));
    }

    @Override
    public boolean canExtractSuperclass(Lookup lookup) {
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
        return false;
    }
    
    @Override
    public void doPushDown(final Lookup lookup) {
        Runnable task;
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        final Dictionary dictionary = lookup.lookup(Dictionary.class);
        if (RefactoringActionsProvider.isFromEditor(ec)) {
            task = new RefactoringActionsProvider.TextComponentTask(ec) {
                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, CompilationInfo info) {
                    Element selected = selectedElement.resolveElement(info);
                    if(selected.getKind()==ElementKind.PACKAGE) {
                        List<? extends Tree> typeDecls = info.getCompilationUnit().getTypeDecls();
                        if (typeDecls==null || typeDecls.isEmpty()) {
                            return null;
                        }
                        selectedElement = TreePathHandle.create(info.getTrees().getPath(info.getCompilationUnit(), typeDecls.get(0)), info);
                    } else if (selected.getKind() == ElementKind.LOCAL_VARIABLE || selected.getKind() == ElementKind.PARAMETER || selected.getKind() == ElementKind.TYPE_PARAMETER) {
                        TreePath path = info.getTrees().getPath(info.getTypes().asElement(selected.asType()));
                        if (path==null)
                            return null;
                        selectedElement = TreePathHandle.create(path, info);
                    }
                    return new PushDownRefactoringUI(new TreePathHandle[]{selectedElement}, info);
                }
            };
        } else {
            task = new NodeToFileObjectTask(lookup.lookupAll(Node.class)) {
                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<TreePathHandle> handles) {
                    return new PushDownRefactoringUI(new TreePathHandle[]{handles.iterator().next()}, cinfo.get());
                }
            };
        }
        RetoucheUtils.invokeAfterScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.pushDownAction()));
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
        return false;
    }
    
    @Override
    public void doPullUp(final Lookup lookup) {
        Runnable task;
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        final Dictionary dictionary = lookup.lookup(Dictionary.class);
        if (RefactoringActionsProvider.isFromEditor(ec)) {
            task = new RefactoringActionsProvider.TextComponentTask(ec) {
                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, CompilationInfo info) {
                    Element selected = selectedElement.resolveElement(info);
                    
                    if(selected.getKind()==ElementKind.PACKAGE) {
                        List<? extends Tree> typeDecls = info.getCompilationUnit().getTypeDecls();
                        if (typeDecls==null || typeDecls.isEmpty()) {
                            return null;
                        }
                        selectedElement = TreePathHandle.create(info.getTrees().getPath(info.getCompilationUnit(), typeDecls.get(0)), info);
                    } else if (selected.getKind() == ElementKind.LOCAL_VARIABLE || selected.getKind() == ElementKind.PARAMETER || selected.getKind() == ElementKind.TYPE_PARAMETER) {
                        TreePath path = info.getTrees().getPath(info.getTypes().asElement(selected.asType()));
                        if (path==null)
                            return null;
                        selectedElement = TreePathHandle.create(path, info);
                    }
                    return new PullUpRefactoringUI(new TreePathHandle[]{selectedElement}, info);
                }
            };
        } else {
            task = new NodeToFileObjectTask(lookup.lookupAll(Node.class)) {
                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<TreePathHandle> handles) {
                    return new PullUpRefactoringUI(new TreePathHandle[]{handles.iterator().next()}, cinfo.get());
                }
            };
        }
        RetoucheUtils.invokeAfterScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.pullUpAction()));
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
        return false;
    }    

    @Override
    public boolean canUseSuperType(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        if(nodes.size() != 1)
            return false;
        Node node = nodes.iterator().next();
        DataObject dObj = node.getCookie(DataObject.class);
        if(null == dObj)
            return false;
        FileObject fileObj = dObj.getPrimaryFile();
        if(null == fileObj || !RetoucheUtils.isRefactorable(fileObj))
            return false;
        
        return true;
    }

    @Override
    public void doUseSuperType(Lookup lookup) {
        Runnable task;
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        final Dictionary dictionary = lookup.lookup(Dictionary.class);
        if (RefactoringActionsProvider.isFromEditor(ec)) {
            task = new RefactoringActionsProvider.TextComponentTask(ec){
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,
                                                            int startOffset,
                                                            int endOffset,
                                                            CompilationInfo info) {
                    Element selected = selectedElement.resolveElement(info);
                    TreePathHandle s = selectedElement;
                    if (!(selected.getKind().isClass() || selected.getKind().isInterface())) {
                        s = TreePathHandle.create(RetoucheUtils.findEnclosingClass(info, selectedElement.resolve(info), true, true, true, true, true), info);
                    }
                    return new UseSuperTypeRefactoringUI(s);
                }
            };
        RetoucheUtils.invokeAfterScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.useSuperTypeAction()));
        }
    }
    
    @Override
    public boolean canChangeParameters(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        if(nodes.size() != 1)
            return false;
        Node node = nodes.iterator().next();
        DataObject dObj = node.getCookie(DataObject.class);
        if(null == dObj)
            return false;
        FileObject fileObj = dObj.getPrimaryFile();
        if(null == fileObj || !RetoucheUtils.isRefactorable(fileObj))
            return false;
        
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (RefactoringActionsProvider.isFromEditor(ec)) {
            return true;
        }
        return false;
    }

    @Override
    public void doChangeParameters(Lookup lookup) {
        Runnable task;
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        task = new RefactoringActionsProvider.TextComponentTask(ec){
            protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,
                    int startOffset,
                    int endOffset,
                    CompilationInfo info) {
                Element selected = selectedElement.resolveElement(info);
                return new ChangeParametersUI(selectedElement, info);
            }
        };
        RetoucheUtils.invokeAfterScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.changeParametersAction()));
    }
    
    @Override
    public boolean canInnerToOuter(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        if(nodes.size() != 1)
            return false;
        Node node = nodes.iterator().next();
        DataObject dObj = node.getCookie(DataObject.class);
        if(null == dObj)
            return false;
        FileObject fileObj = dObj.getPrimaryFile();
        if(null == fileObj || !RetoucheUtils.isRefactorable(fileObj))
            return false;
        
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (RefactoringActionsProvider.isFromEditor(ec)) {
            return true;
        }
        return false;    }

    @Override
    public void doInnerToOuter(Lookup lookup) {
        Runnable task;
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        task = new RefactoringActionsProvider.TextComponentTask(ec){
            protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,
                    int startOffset,
                    int endOffset,
                    CompilationInfo info) {
                Element selected = selectedElement.resolveElement(info);
                return new InnerToOuterRefactoringUI(selectedElement, info);
            }
        };
        RetoucheUtils.invokeAfterScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.innerToOuterAction()));
    }

    @Override
    public boolean canEncapsulateFields(Lookup lookup) {
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
        return false;
    }

    @Override
    public void doEncapsulateFields(Lookup lookup) {
        Runnable task;
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (RefactoringActionsProvider.isFromEditor(ec)) {
            task = new RefactoringActionsProvider.TextComponentTask(ec) {
                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, CompilationInfo info) {
                    return new EncapsulateFieldUI(selectedElement, info);
                }
            };
        } else {
            task = new NodeToFileObjectTask(lookup.lookupAll(Node.class)) {
                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<TreePathHandle> handles) {
                    TreePathHandle tph = handles.iterator().next();
                    return new EncapsulateFieldUI(tph, cinfo.get());
                }
            };
        }
        RetoucheUtils.invokeAfterScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.encapsulateFieldsAction()));
    }
}
