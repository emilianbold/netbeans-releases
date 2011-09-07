/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring.ParameterInfo;
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
import sun.awt.windows.ThemeReader;

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
                    return wrap(ExtractInterfaceRefactoringUI.create(selectedElement, info));
                }
            };
        } else if (RefactoringActionsProvider.nodeHandle(lookup)) {
            task = new TreePathHandleTask(new HashSet<Node>(lookup.lookupAll(Node.class)), true) {

                RefactoringUI ui;

                @Override
                protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
                    ui = ExtractInterfaceRefactoringUI.create(handle, javac);
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
                    ui = ExtractInterfaceRefactoringUI.create(tph, javac);
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
                    return wrap(ExtractSuperclassRefactoringUI.create(selectedElement, info));
                }
            };
        } else if (RefactoringActionsProvider.nodeHandle(lookup)) {
            task = new TreePathHandleTask(new HashSet<Node>(lookup.lookupAll(Node.class)), true) {

                RefactoringUI ui;

                @Override
                protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
                    ui = ExtractSuperclassRefactoringUI.create(handle, javac);
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
                    ui = ExtractSuperclassRefactoringUI.create(tph, javac);
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
                        TreePath classTreePath = RetoucheUtils.findEnclosingClass(info, selectedElement.resolve(info), true, true, true, true, true);

                        if (classTreePath == null) {
                            return null;
                        }
                        s = TreePathHandle.create(classTreePath, info);
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
        TreePathHandle tph = lookup.lookup(TreePathHandle.class);
        if (tph != null) {
            return RetoucheUtils.isRefactorable(tph.getFileObject());
        }
        Collection<? extends Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
        if(nodes.size() != 1)
            return false;
        Node node = nodes.iterator().next();
        tph = node.getLookup().lookup(TreePathHandle.class);
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
        Collection<? extends ParameterInfo> params = lookup.lookupAll(ParameterInfo.class);
        final ParameterInfo[] configuration = params.isEmpty()? null : new ParameterInfo[params.size()];
        int index = 0;
        for (ParameterInfo parameterInfo : params) {
            configuration[index] = parameterInfo;
            index++;
        }
        if (ec != null) {
            task = new RefactoringActionsProvider.TextComponentTask(ec) {
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,
                        int startOffset,
                        int endOffset,
                        CompilationInfo info) {
                    Element selected = selectedElement.resolveElement(info);
                    if (selected!=null && selected.getKind() == ElementKind.PACKAGE) {
                        //TODO: can happen (see test190101 for an example) - is this correct or
                        //should the TreePathHandle handle elements of kind PACKAGE?
                        return null;
                    }
                    return wrap(ChangeParametersUI.create(selectedElement, info, configuration));
                }
            };
        } else {
            TreePathHandle tph = lookup.lookup(TreePathHandle.class);
            if(tph != null) {
                task = new TreePathHandleTask(tph) {

                    RefactoringUI ui;

                    @Override
                    protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
                        ui = ChangeParametersUI.create(handle, javac, configuration);
                    }

                    @Override
                    protected RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles) {
                        return wrap(ui);
                    }
                };
            } else {
                task = new TreePathHandleTask(new HashSet<Node>(lookup.lookupAll(Node.class)), true) {

                    RefactoringUI ui;

                    @Override
                    protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
                        ui = ChangeParametersUI.create(handle, javac, configuration);
                    }

                    @Override
                    protected RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles) {
                        return wrap(ui);
                    }
                };
            }
        }
        RetoucheUtils.invokeAfterScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.changeParametersAction()));
    }
    
    @Override
    public boolean canIntroduceParameter(Lookup lookup) {
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
    public void doIntroduceParameter(Lookup lookup) {
        Runnable task;
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (ec != null) {
            task = new RefactoringActionsProvider.TextComponentTask(ec) {
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,
                        int startOffset,
                        int endOffset,
                        CompilationInfo info) {
                    
                    TreePath tp = validateSelection(info, startOffset, endOffset);
                    if (tp==null)
                        return null;
                    return wrap(IntroduceParameterUI.create(TreePathHandle.create(tp, info), info));
                }
            };
        } else {
            task = new TreePathHandleTask(new HashSet<Node>(lookup.lookupAll(Node.class)), true) {

                RefactoringUI ui;

                @Override
                protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
                    ui = IntroduceParameterUI.create(handle, javac);
                }

                @Override
                protected RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles) {
                    return wrap(ui);
                }
            };
        }
        RetoucheUtils.invokeAfterScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.changeParametersAction()));
    }    
    
    private static final Set<TypeKind> NOT_ACCEPTED_TYPES = EnumSet.of(TypeKind.ERROR, TypeKind.NONE, TypeKind.OTHER, TypeKind.VOID, TypeKind.EXECUTABLE);
 
    static TreePath validateSelection(CompilationInfo ci, int start, int end) {
        return validateSelection(ci, start, end, NOT_ACCEPTED_TYPES);
    }

    public static TreePath validateSelection(CompilationInfo ci, int start, int end, Set<TypeKind> ignoredTypes) {
        TreePath tp = ci.getTreeUtilities().pathFor((start + end) / 2);

        for ( ; tp != null; tp = tp.getParentPath()) {
            Tree leaf = tp.getLeaf();

            if (   !ExpressionTree.class.isAssignableFrom(leaf.getKind().asInterface())
                && (leaf.getKind() != Kind.VARIABLE || ((VariableTree) leaf).getInitializer() == null))
               continue;

            long treeStart = ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), leaf);
            long treeEnd   = ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), leaf);

            if (!(treeStart <= start) || !(treeEnd >= end)) {
                continue;
            }

            TypeMirror type = ci.getTrees().getTypeMirror(tp);

            if (type != null && type.getKind() == TypeKind.ERROR) {
                type = ci.getTrees().getOriginalType((ErrorType) type);
            }

            if (type == null || ignoredTypes.contains(type.getKind()))
                continue;

            if(tp.getLeaf().getKind() == Kind.ASSIGNMENT)
                continue;

            if (tp.getLeaf().getKind() == Kind.ANNOTATION)
                continue;

            if (!isInsideClass(tp))
                return null;

            TreePath candidate = tp;

            tp = tp.getParentPath();

            while (tp != null) {
                switch (tp.getLeaf().getKind()) {
                    case VARIABLE:
                        VariableTree vt = (VariableTree) tp.getLeaf();
                        if (vt.getInitializer() == leaf) {
                            return candidate;
                        } else {
                            return null;
                        }
                    case NEW_CLASS:
                        NewClassTree nct = (NewClassTree) tp.getLeaf();
                        
                        if (nct.getIdentifier().equals(candidate.getLeaf())) { //avoid disabling hint ie inside of anonymous class higher in treepath
                            for (Tree p : nct.getArguments()) {
                                if (p == leaf) {
                                    return candidate;
                                }
                            }

                            return null;
                        }
                }
                leaf = tp.getLeaf();
                tp = tp.getParentPath();
            }
            return candidate;
        }
        return null;
    }

    private static boolean isInsideClass(TreePath tp) {
        while (tp != null) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
                return true;
            }

            tp = tp.getParentPath();
        }

        return false;
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
            task = new RefactoringActionsProvider.TextComponentTask(ec, true) {
                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, CompilationInfo info) {   
                    if (startOffset == endOffset) {
                        //cursor position
                        return wrap(EncapsulateFieldUI.create(info, selectedElement));
                    }
                    
                    //editor selection
                    
                    Element el = selectedElement.resolveElement(info);
                    if (el==null) {
                        return null;
                    }
                    if (!(el.getKind().isClass() || el.getKind().isInterface())) {
                        el = info.getElementUtilities().enclosingTypeElement(el);
                    }
                    Collection<TreePathHandle> handles = new ArrayList<TreePathHandle>();
                    Element last = null;
                    for (Element e :ElementFilter.fieldsIn(el.getEnclosedElements())) {
                        SourcePositions sourcePositions = info.getTrees().getSourcePositions();
                        Tree leaf = info.getTrees().getPath(e).getLeaf();
                        long start = sourcePositions.getStartPosition(info.getCompilationUnit(), leaf);
                        long end = sourcePositions.getEndPosition(info.getCompilationUnit(), leaf);
                        if (start >= startOffset && end <=endOffset) {
                            handles.add(TreePathHandle.create(e, info));
                        }
                    }
                    if (handles.isEmpty()) {
                        return wrap(EncapsulateFieldUI.create(info, selectedElement));
                    }
                    return wrap(EncapsulateFieldUI.create(info, handles.toArray(new TreePathHandle[handles.size()])));
                }
            };
        } else if (RefactoringActionsProvider.nodeHandle(lookup)) {
            task = new TreePathHandleTask(new HashSet<Node>(lookup.lookupAll(Node.class)), true) {

                RefactoringUI ui;

                @Override
                protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
                    ui = EncapsulateFieldUI.create(javac, handle);
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
                    ui = EncapsulateFieldUI.create(javac, tph);
                }

                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<TreePathHandle> handles) {
                    return wrap(ui);
                }
            };
        }
        RetoucheUtils.invokeAfterScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.encapsulateFieldsAction()));
    }
    
    @Override
    public boolean canInline(Lookup lookup) {
        Collection<? extends Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
        if (nodes.size() != 1) {
            return false;
        }
        Node node = nodes.iterator().next();
        TreePathHandle tph = node.getLookup().lookup(TreePathHandle.class);
        if (tph != null) {
            return RetoucheUtils.isRefactorable(tph.getFileObject());
        }
        DataObject dObj = node.getCookie(DataObject.class);
        if (null == dObj) {
            return false;
        }
        FileObject fileObj = dObj.getPrimaryFile();
        if (null == fileObj || !RetoucheUtils.isRefactorable(fileObj)) {
            return false;
        }

        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (RefactoringActionsProvider.isFromEditor(ec)) {
            return true;
        }
        return false;
    }

    @Override
    public void doInline(Lookup lookup) {
        Runnable task;
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (RefactoringActionsProvider.isFromEditor(ec)) { // From Editor
            task = new RefactoringActionsProvider.TextComponentTask(ec) {

                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement, int startOffset, int endOffset, CompilationInfo info) {
                    return new InlineRefactoringUI(selectedElement, info);
                }
            };
        } else if (RefactoringActionsProvider.nodeHandle(lookup)) { // From Navigator
            task = new TreePathHandleTask(new HashSet<Node>(lookup.lookupAll(Node.class)), true) {

                RefactoringUI ui;

                @Override
                protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo info) {
                    ui = new InlineRefactoringUI(handle, info);
                }

                @Override
                protected RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles) {
                    return ui;
                }
            };
        } else { // From Projects / Files
            task = new NodeToFileObjectTask(Collections.singleton(lookup.lookup(Node.class))) {

                RefactoringUI ui;

                @Override
                protected void nodeTranslated(Node node, Collection<TreePathHandle> handles, CompilationInfo info) {
                    TreePathHandle tph = handles.iterator().next();
                    ui = new InlineRefactoringUI(tph, info);
                }

                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<TreePathHandle> handles) {
                    return ui;
                }
            };
        }
        RetoucheUtils.invokeAfterScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.inlineAction()));
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
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
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
            if (!typeDecls.isEmpty() && typeDecls.get(0).getKind().asInterface() == ClassTree.class) {
                selection = TreePath.getPath(path.getCompilationUnit(), typeDecls.get(0));
            }
        }
        return selection;
    }
    
}
