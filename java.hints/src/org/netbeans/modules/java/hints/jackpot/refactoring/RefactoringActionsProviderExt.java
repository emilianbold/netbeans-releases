/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.jackpot.refactoring;

import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.modules.java.hints.jackpot.refactoring.InvertBooleanRefactoringUI;
import org.netbeans.modules.java.hints.jackpot.refactoring.ReplaceConstructorRefactoringUI;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.ui.RefactoringActionsProvider;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;


/**
 *
 * @author lahvac
 */
//@ServiceProvider(service=ActionsImplementationProvider.class, supersedes="org.netbeans.modules.refactoring.java.ui.RefactoringActionsProvider")
public class RefactoringActionsProviderExt { //extends RefactoringActionsProvider {

    private static RefactoringUI doReplaceConstructorImpl(TreePathHandle selectedElement, CompilationInfo info) {
        Element selected = selectedElement.resolveElement(info);
        if (selected==null) {
//            logger().log(Level.INFO, "doRename: " + selectedElement, new NullPointerException("selected")); // NOI18N
            return null;
        }
        if (selected.getKind() == ElementKind.CONSTRUCTOR) {
            return new ReplaceConstructorRefactoringUI(selectedElement, selected.getSimpleName().toString());
        }

        return null;
    }
    
    private static RefactoringUI doReplaceConstructorWithBuilderImpl(TreePathHandle selectedElement, CompilationInfo info) {
        Element selected = selectedElement.resolveElement(info);
        if (selected==null) {
//            logger().log(Level.INFO, "doRename: " + selectedElement, new NullPointerException("selected")); // NOI18N
            return null;
        }
        if (selected.getKind() == ElementKind.CONSTRUCTOR) {
            return new ReplaceConstructorWithBuilderUI(selectedElement, info);
        }

        return null;
    }
    

    public static void doReplaceConstructor(final Lookup lookup) {
        Runnable task;
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (isFromEditor(ec)) {
            task = new RefactoringActionsProvider.TextComponentTask(ec) {
                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, final CompilationInfo info) {
                    return doReplaceConstructorImpl(selectedElement, info);
                }
            };
        } else {
            task = new RefactoringActionsProvider.TreePathHandleTask(new HashSet<Node>(lookup.lookupAll(Node.class)), true) {

                RefactoringUI ui;

                @Override
                protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
                    ui = doReplaceConstructorImpl(handle, javac);
                }

                @Override
                protected RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles) {
                    return ui;
                }

            };
        }
        
        ScanDialog.runWhenScanFinished(task, "Remove Constructor");//getActionName(RefactoringActionsFactory.renameAction()));
    }

    public static void doReplaceConstructorWithBuilder(final Lookup lookup) {
        Runnable task;
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (isFromEditor(ec)) {
            task = new RefactoringActionsProvider.TextComponentTask(ec) {
                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, final CompilationInfo info) {
                    return doReplaceConstructorWithBuilderImpl(selectedElement, info);
                }
            };
        } else {
            task = new RefactoringActionsProvider.TreePathHandleTask(new HashSet<Node>(lookup.lookupAll(Node.class)), true) {

                RefactoringUI ui;

                @Override
                protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
                    ui = doReplaceConstructorWithBuilderImpl(handle, javac);
                }

                @Override
                protected RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles) {
                    return ui;
                }

            };
        }
        
        ScanDialog.runWhenScanFinished(task, "Remove Constructor");//getActionName(RefactoringActionsFactory.renameAction()));
    }
    
    private static RefactoringUI doInvertBooleanImpl(TreePathHandle selectedElement, CompilationInfo info) {
        Element selected = selectedElement.resolveElement(info);
        if (selected==null) {
//            logger().log(Level.INFO, "doRename: " + selectedElement, new NullPointerException("selected")); // NOI18N
            return null;
        }
        if (selected.getKind().isField() && ((VariableElement) selected).asType().getKind() == TypeKind.BOOLEAN) {
            return new InvertBooleanRefactoringUI(selectedElement, ((VariableElement) selected).getSimpleName().toString());
        }
        if (selected.getKind() == ElementKind.METHOD && ((ExecutableElement) selected).getReturnType().getKind() == TypeKind.BOOLEAN) {
            return new InvertBooleanRefactoringUI(selectedElement, ((ExecutableElement) selected).getSimpleName().toString());
        }

        return null;
    }

    public static void doInvertBoolean(final Lookup lookup) {
        Runnable task;
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (isFromEditor(ec)) {
            task = new RefactoringActionsProvider.TextComponentTask(ec) {
                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, final CompilationInfo info) {
                    return doInvertBooleanImpl(selectedElement, info);
                }
            };
        } else {
            task = new RefactoringActionsProvider.TreePathHandleTask(new HashSet<Node>(lookup.lookupAll(Node.class)), true) {

                RefactoringUI ui;

                @Override
                protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
                    ui = doInvertBooleanImpl(handle, javac);
                }

                @Override
                protected RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles) {
                    return ui;
                }

            };
        }

        ScanDialog.runWhenScanFinished(task, "Remove Constructor");//getActionName(RefactoringActionsFactory.renameAction()));
    }

//    static String getActionName(Action action) {
//        String arg = (String) action.getValue(Action.NAME);
//        arg = arg.replace("&", ""); // NOI18N
//        return arg.replace("...", ""); // NOI18N
//    }


    public static boolean canReplaceConstructor(Lookup lookup) {
        return canRefactor(lookup);
    }
    
    public static boolean canReplaceConstructorWithBuilder(Lookup lookup) {
        return canRefactor(lookup);
    }
    

    public static boolean canInvertBoolean(Lookup lookup) {
        return canRefactor(lookup);
    }

    private static boolean canRefactor(Lookup lookup) {
        Collection<? extends Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
        if(nodes.size() != 1)
            return false;
        Node node = nodes.iterator().next();
        TreePathHandle tph = node.getLookup().lookup(TreePathHandle.class);
        if (tph != null) {
            return JavaRefactoringUtils.isRefactorable(tph.getFileObject());
        }
        DataObject dObj = node.getCookie(DataObject.class);
        if(null == dObj)
            return false;
        FileObject fileObj = dObj.getPrimaryFile();
        if(null == fileObj || !JavaRefactoringUtils.isRefactorable(fileObj))
            return false;

        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (isFromEditor(ec)) {
            return true;
        }
        return false;
    }

//    @Override
//    protected RefactoringUI wrap(RefactoringUI orig) {
//        if (orig.getRefactoring() instanceof RenameRefactoring) {
//            return new RenameRefactoringWrapper(orig, ((RenameRefactoring) orig.getRefactoring()));
//        }
//        
//        return super.wrap(orig);
//    }


    //XXX: copied from RefactoringActionsProvider:\
    static boolean isFromEditor(EditorCookie ec) {
        if (ec != null && ec.getOpenedPanes() != null) {
            TopComponent activetc = TopComponent.getRegistry().getActivated();
            if (activetc instanceof CloneableEditorSupport.Pane) {
                return true;
            }
        }
        return false;
    }

    static boolean nodeHandle(Lookup lookup) {
        Node n = lookup.lookup(Node.class);
        if (n!=null) {
            if (n.getLookup().lookup(TreePathHandle.class)!=null)
                return true;
        }
        return false;
    }

    private static Logger logger() {
        return Logger.getLogger(RefactoringActionsProvider.class.getName());
    }
}
