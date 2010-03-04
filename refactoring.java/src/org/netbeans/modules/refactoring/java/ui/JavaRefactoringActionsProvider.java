/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.ui.JavaRefactoringActionsFactory;
import org.netbeans.modules.refactoring.java.spi.ui.JavaActionsImplementationProvider;
import org.netbeans.modules.refactoring.java.ui.RefactoringActionsProvider.NodeToFileObjectTask;
import org.netbeans.modules.refactoring.java.ui.RefactoringActionsProvider.TreePathHandleTask;
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
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.java.spi.ui.JavaActionsImplementationProvider.class, position=100)
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
                    return wrap(new ExtractInterfaceRefactoringUI(selectedElement, info));
                }
            };
        } else if (RefactoringActionsProvider.nodeHandle(lookup)) {
            task = new TreePathHandleTask(new HashSet<Node>(lookup.lookupAll(Node.class)), true) {

                RefactoringUI ui;

                @Override
                protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
                    ui = new ExtractInterfaceRefactoringUI(handle, javac);
                }

                @Override
                protected RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles) {
                    return wrap(ui);
                }
            };
        } else {
            task = new NodeToFileObjectTask(Collections.singleton(lookup.lookup(Node.class))) {

                RefactoringUI ui;

                @Override
                protected void nodeTranslated(Node node, Collection<TreePathHandle> handles, CompilationInfo javac) {
                    TreePathHandle tph = handles.iterator().next();
                    ui = new ExtractInterfaceRefactoringUI(tph, javac);
                }

                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<TreePathHandle> handles) {
                    return wrap(ui);
                }
            };
        }
        RetoucheUtils.invokeAfterScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.extractInterfaceAction()));
    }

    @Override
    public boolean canExtractInterface(Lookup lookup) {
        Collection<? extends Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
        if (nodes.size() != 1) {
            return false;
        }
        Node n = nodes.iterator().next();
        TreePathHandle tph = n.getLookup().lookup(TreePathHandle.class);
        if (tph != null) {
            return RetoucheUtils.isRefactorable(tph.getFileObject());
        }
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
                    return wrap(new ExtractSuperclassRefactoringUI(selectedElement, info));
                }
            };
        } else if (RefactoringActionsProvider.nodeHandle(lookup)) {
            task = new TreePathHandleTask(new HashSet<Node>(lookup.lookupAll(Node.class)), true) {

                RefactoringUI ui;

                @Override
                protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
                    ui = new ExtractSuperclassRefactoringUI(handle, javac);
                }
                
                @Override
                protected RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles) {
                    return wrap(ui);
                }
            };
        } else {
            task = new NodeToFileObjectTask(Collections.singleton(lookup.lookup(Node.class))) {

                RefactoringUI ui;

                @Override
                protected void nodeTranslated(Node node, Collection<TreePathHandle> handles, CompilationInfo javac) {
                    TreePathHandle tph = handles.iterator().next();
                    ui = new ExtractSuperclassRefactoringUI(tph, javac);
                }

                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<TreePathHandle> handles) {
                    return wrap(ui);
                }
            };
        }
        RetoucheUtils.invokeAfterScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.extractSuperclassAction()));
    }

    @Override
    public boolean canExtractSuperclass(Lookup lookup) {
        Collection<? extends Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
        if (nodes.size() != 1) {
            return false;
        }
        Node n = nodes.iterator().next();
        TreePathHandle tph = n.getLookup().lookup(TreePathHandle.class);
        if (tph != null) {
            return RetoucheUtils.isRefactorable(tph.getFileObject());
        }
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
        if (RefactoringActionsProvider.isFromEditor(ec)) {
            task = new RefactoringActionsProvider.TextComponentTask(ec) {
                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, CompilationInfo info) {
                    selectedElement = findSelectedClassMemberDeclaration(selectedElement, info);
                    return selectedElement != null
                            ? wrap(new PushDownRefactoringUI(selectedElement, info))
                            : null;
                    
                }
            };
        } else if (RefactoringActionsProvider.nodeHandle(lookup)) {
            task = new TreePathHandleTask(new HashSet<Node>(lookup.lookupAll(Node.class)), true) {

                RefactoringUI ui;

                @Override
                protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
                    ui = new PushDownRefactoringUI(handle, javac);
                }

                @Override
                protected RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles) {
                    return wrap(ui);
                }
                
            };
        } else {
            task = new NodeToFileObjectTask(Collections.singleton(lookup.lookup(Node.class))) {

                RefactoringUI ui;

                @Override
                protected void nodeTranslated(Node node, Collection<TreePathHandle> handles, CompilationInfo javac) {
                    ui = new PushDownRefactoringUI(handles.iterator().next(), javac);
                }

                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<TreePathHandle> handles) {
                    return wrap(ui);
                }
            };
        }
        RetoucheUtils.invokeAfterScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.pushDownAction()));
    }

    @Override
    public boolean canPushDown(Lookup lookup) {
        Collection<? extends Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
        if (nodes.size() != 1) {
            return false;
        }
        Node n = nodes.iterator().next();
        TreePathHandle tph = n.getLookup().lookup(TreePathHandle.class);
        if (tph != null) {
            return RetoucheUtils.isRefactorable(tph.getFileObject());
        }
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
        if (RefactoringActionsProvider.isFromEditor(ec)) {
            task = new RefactoringActionsProvider.TextComponentTask(ec) {
                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, CompilationInfo info) {
                    selectedElement = findSelectedClassMemberDeclaration(selectedElement, info);
                    return selectedElement != null
                            ? wrap(new PullUpRefactoringUI(selectedElement, info))
                            : null;
                }
            };
        } else if (RefactoringActionsProvider.nodeHandle(lookup)) {
            task = new TreePathHandleTask(new HashSet<Node>(lookup.lookupAll(Node.class)), true) {

                RefactoringUI ui;

                @Override
                protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
                    ui = new PullUpRefactoringUI(handle, javac);
                }

                @Override
                protected RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles) {
                    return wrap(ui);
                }
                
            };
        } else {
            task = new NodeToFileObjectTask(Collections.singleton(lookup.lookup(Node.class))) {

                RefactoringUI ui;

                @Override
                protected void nodeTranslated(Node node, Collection<TreePathHandle> handles, CompilationInfo javac) {
                    ui = new PullUpRefactoringUI(handles.iterator().next(), javac);
                }

                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<TreePathHandle> handles) {
                    return wrap(ui);
                }
            };
        }
        RetoucheUtils.invokeAfterScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.pullUpAction()));
    }

    @Override
    public boolean canPullUp(Lookup lookup) {
        Collection<? extends Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
        if (nodes.size() != 1) {
            return false;
        }
        Node n = nodes.iterator().next();
        TreePathHandle tph = n.getLookup().lookup(TreePathHandle.class);
        if (tph != null) {
            return RetoucheUtils.isRefactorable(tph.getFileObject());
        }
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
        Collection<? extends Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
        if(nodes.size() != 1)
            return false;
        Node node = nodes.iterator().next();
        TreePathHandle tph = node.getLookup().lookup(TreePathHandle.class);
        if (tph != null) {
            return RetoucheUtils.isRefactorable(tph.getFileObject());
        }
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
        if (RefactoringActionsProvider.isFromEditor(ec)) {
            task = new RefactoringActionsProvider.TextComponentTask(ec){
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,
                                                            int startOffset,
                                                            int endOffset,
                                                            CompilationInfo info) {
                    Element selected = selectedElement.resolveElement(info);
                    TreePathHandle s = selectedElement;
                    if (selected == null || !(selected.getKind().isClass() || selected.getKind().isInterface())) {
                        s = TreePathHandle.create(RetoucheUtils.findEnclosingClass(info, selectedElement.resolve(info), true, true, true, true, true), info);
                    }
                    return wrap(new UseSuperTypeRefactoringUI(s));
                }
            };
        } else if (RefactoringActionsProvider.nodeHandle(lookup)) {
            task = new TreePathHandleTask(new HashSet<Node>(lookup.lookupAll(Node.class)), true) {

                RefactoringUI ui;

                @Override
                protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
                    ui = new UseSuperTypeRefactoringUI(handle);
                }

                @Override
                protected RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles) {
                    return wrap(ui);
                }
            };
        } else {
            task = new NodeToFileObjectTask(Collections.singleton(lookup.lookup(Node.class))) {

                RefactoringUI ui;

                @Override
                protected void nodeTranslated(Node node, Collection<TreePathHandle> handles, CompilationInfo javac) {
                    TreePathHandle tph = handles.iterator().next();
                    ui = new UseSuperTypeRefactoringUI(tph);
                }

                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<TreePathHandle> handles) {
                    return wrap(ui);
                }
            };
        }
        RetoucheUtils.invokeAfterScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.useSuperTypeAction()));
    }
    
    @Override
    public boolean canChangeParameters(Lookup lookup) {
        Collection<? extends Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
        if(nodes.size() != 1)
            return false;
        Node node = nodes.iterator().next();
        TreePathHandle tph = node.getLookup().lookup(TreePathHandle.class);
        if (tph != null) {
            return RetoucheUtils.isRefactorable(tph.getFileObject());
        }
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
        if (ec != null) {
            task = new RefactoringActionsProvider.TextComponentTask(ec) {
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,
                        int startOffset,
                        int endOffset,
                        CompilationInfo info) {
                    Element selected = selectedElement.resolveElement(info);
                    return wrap(ChangeParametersUI.create(selectedElement, info));
                }
            };
        } else {
            task = new TreePathHandleTask(new HashSet<Node>(lookup.lookupAll(Node.class)), true) {

                RefactoringUI ui;

                @Override
                protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
                    ui = ChangeParametersUI.create(handle, javac);
                }

                @Override
                protected RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles) {
                    return wrap(ui);
                }
            };
        }
        RetoucheUtils.invokeAfterScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.changeParametersAction()));
    }
    
    @Override
    public boolean canInnerToOuter(Lookup lookup) {
        Collection<? extends Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
        if(nodes.size() != 1)
            return false;
        Node node = nodes.iterator().next();
        TreePathHandle tph = node.getLookup().lookup(TreePathHandle.class);
        if (tph != null) {
            return RetoucheUtils.isRefactorable(tph.getFileObject());
        }
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
        if (ec!=null) {
            task = new RefactoringActionsProvider.TextComponentTask(ec) {

                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,
                        int startOffset,
                        int endOffset,
                        CompilationInfo info) {

                    TreePath resolved = selectedElement.resolve(info);
                    TreePath enclosing = resolved == null
                            ? null
                            : RetoucheUtils.findEnclosingClass(info, resolved, true, true, true, true, false);
                    if (enclosing != null && enclosing != resolved) {
                        selectedElement = TreePathHandle.create(enclosing, info);
                    }
                    return selectedElement != null && resolved !=null
                            ? wrap(new InnerToOuterRefactoringUI(selectedElement, info))
                            : null;
                }
            };
        } else {
            task = new TreePathHandleTask(new HashSet<Node>(lookup.lookupAll(Node.class)), true) {

                RefactoringUI ui;

                @Override
                protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
                    ui = new InnerToOuterRefactoringUI(handle, javac);
                }

                @Override
                protected RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles) {
                    return wrap(ui);
                }
                
            };
        }
        RetoucheUtils.invokeAfterScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.innerToOuterAction()));
    }

    @Override
    public boolean canEncapsulateFields(Lookup lookup) {
        Collection<? extends Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
        if (nodes.size() != 1) {
            return false;
        }
        Node n = nodes.iterator().next();
        TreePathHandle tph = n.getLookup().lookup(TreePathHandle.class);
        if (tph != null) {
            return RetoucheUtils.isRefactorable(tph.getFileObject());
        }
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
                    return wrap(new EncapsulateFieldUI(selectedElement, info));
                }
            };
        } else if (RefactoringActionsProvider.nodeHandle(lookup)) {
            task = new TreePathHandleTask(new HashSet<Node>(lookup.lookupAll(Node.class)), true) {

                RefactoringUI ui;

                @Override
                protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
                    ui = new EncapsulateFieldUI(handle, javac);
                }

                @Override
                protected RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles) {
                    return wrap(ui);
                }
                
            };
        } else {
            task = new NodeToFileObjectTask(Collections.singleton(lookup.lookup(Node.class))) {

                RefactoringUI ui;

                @Override
                protected void nodeTranslated(Node node, Collection<TreePathHandle> handles, CompilationInfo javac) {
                    TreePathHandle tph = handles.iterator().next();
                    ui = new EncapsulateFieldUI(tph, javac);
                }

                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<TreePathHandle> handles) {
                    return wrap(ui);
                }
            };
        }
        RetoucheUtils.invokeAfterScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.encapsulateFieldsAction()));
    }
    
    protected RefactoringUI wrap(RefactoringUI orig) {
        return orig;
    }
    
    private static TreePathHandle findSelectedClassMemberDeclaration(TreePathHandle path, final CompilationInfo info) {
        TreePath resolved = path.resolve(info);
        TreePath selected = findSelectedClassMemberDeclaration(resolved ,info);
        if (selected == null) {
            path = null;
        } else if (selected != resolved) {
            path = TreePathHandle.create(selected, info);
        }
        return path;
    }

    private static TreePath findSelectedClassMemberDeclaration(final TreePath path, final CompilationInfo javac) {
        TreePath currentPath = path;
        TreePath selection = null;
        while (currentPath != null && selection == null) {
            switch (currentPath.getLeaf().getKind()) {
                case CLASS:
                case NEW_CLASS:
                case METHOD:
                    selection = currentPath;
                    break;
                case VARIABLE:
                    Element elm = javac.getTrees().getElement(currentPath);
                    if (elm != null && elm.getKind().isField()) {
                        selection = currentPath;
                    }
                    break;
            }
            if (selection != null && javac.getTreeUtilities().isSynthetic(selection)) {
                selection = null;
            }
            if (selection == null) {
                currentPath = currentPath.getParentPath();
            }
        }
        
        if (selection == null && path != null) {
            List<? extends Tree> typeDecls = path.getCompilationUnit().getTypeDecls();
            if (!typeDecls.isEmpty()) {
                selection = TreePath.getPath(path.getCompilationUnit(), typeDecls.get(0));
            }
        }
        return selection;
    }
    
}
