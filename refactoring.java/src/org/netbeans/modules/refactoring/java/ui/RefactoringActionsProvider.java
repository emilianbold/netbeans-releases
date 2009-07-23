/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.text.JTextComponent;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.ui.ExplorerContext;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jan Becicka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider.class, position=100)
public class RefactoringActionsProvider extends ActionsImplementationProvider{

    /** Creates a new instance of RefactoringActionsProvider */
    public RefactoringActionsProvider() {
    }
    @Override
    public void doRename(final Lookup lookup) {
        Runnable task;
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (isFromEditor(ec)) {
            task = new TextComponentTask(ec) {
                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, final CompilationInfo info) {
                    Element selected = selectedElement.resolveElement(info);
                    if (selected==null) {
                        logger().log(Level.INFO, "doRename: " + selectedElement, new NullPointerException("selected")); // NOI18N
                        return null;
                    }
                    if (selected.getKind() == ElementKind.CONSTRUCTOR) {
                        selected = selected.getEnclosingElement();
                        selectedElement = TreePathHandle.create(info.getTrees().getPath(selected), info);
                    } 
                    if (selected.getKind() == ElementKind.PACKAGE) {
                        NonRecursiveFolder folder = new NonRecursiveFolder() {
                            public FileObject getFolder() {
                                return info.getFileObject().getParent();
                            }
                        };
                        return new RenameRefactoringUI(folder);
                    } else if (selected instanceof TypeElement && !((TypeElement)selected).getNestingKind().isNested()) {
                        FileObject f = SourceUtils.getFile(selected, info.getClasspathInfo());
                        if (f!=null && selected.getSimpleName().toString().equals(f.getName())) {
                            return new RenameRefactoringUI(f==null?info.getFileObject():f, selectedElement, info);
                        } else {
                            return new RenameRefactoringUI(selectedElement, info);
                        }
                    } else {
                        return new RenameRefactoringUI(selectedElement, info);
                    }
                }
            };
        } else if (nodeHandle(lookup)) {
            task = new TreePathHandleTask(new HashSet<Node>(lookup.lookupAll(Node.class)), true) {

                RefactoringUI ui;

                @Override
                protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
                    if (renameFile) {
                        ui = new RenameRefactoringUI(handle.getFileObject(), handle, javac);
                    } else {
                        ui = new RenameRefactoringUI(handle, javac);
                    }
                }

                @Override
                protected RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles) {
                    return ui;
                }
                
            };

        } else {
//            task = new NodeToFileObjectTask(new HashSet(lookup.lookupAll(Node.class))) {
            // canRename is valid only for single node
            task = new NodeToFileObjectTask(Collections.singleton(lookup.lookup(Node.class))) {

                RefactoringUI ui;

                @Override
                protected void nodeTranslated(Node node, Collection<TreePathHandle> handles, CompilationInfo javac) {
                    String newName = getName(lookup);
                    ui = newName != null
                            ? new RenameRefactoringUI(javac.getFileObject(), newName, handles==null||handles.isEmpty()?null:handles.iterator().next(), javac)
                            : new RenameRefactoringUI(javac.getFileObject(), handles==null||handles.isEmpty()?null:handles.iterator().next(), javac);
                }

                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<TreePathHandle> handles) {
                    if (ui == null) {
                        String newName = getName(lookup);
                        if (newName != null) {
                            ui = pkg[0] != null
                                    ? new RenameRefactoringUI(pkg[0], newName)
                                    : new RenameRefactoringUI(selectedElements[0], newName, null, null);
                        } else {
                            ui = pkg[0]!= null
                                    ? new RenameRefactoringUI(pkg[0])
                                    : new RenameRefactoringUI(selectedElements[0], null, null);
                        }
                    }
                    return ui;
                }
            };
        }
        RetoucheUtils.invokeAfterScanFinished(task, getActionName(RefactoringActionsFactory.renameAction()));
    }
    
    static String getActionName(Action action) {
        String arg = (String) action.getValue(Action.NAME);
        arg = arg.replace("&", ""); // NOI18N
        return arg.replace("...", ""); // NOI18N
    }


    /**
     * returns true if exactly one refactorable file is selected
     */
    @Override
    public boolean canRename(Lookup lookup) {
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
        if ((dob instanceof DataFolder) && 
                RetoucheUtils.isFileInOpenProject(fo) && 
                RetoucheUtils.isOnSourceClasspath(fo) &&
                !RetoucheUtils.isClasspathRoot(fo))
            return true;
        return false;
    }
    
    @Override
    public void doCopy(final Lookup lookup) {
        Runnable task;
//        EditorCookie ec = lookup.lookup(EditorCookie.class);
//        if (isFromEditor(ec)) {
//            return new TextComponentRunnable(ec) {
//                @Override
//                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, CompilationInfo info) {
//                    Element selected = selectedElement.resolveElement(info);
//                    if (selected.getKind() == ElementKind.PACKAGE || selected.getEnclosingElement().getKind() == ElementKind.PACKAGE) {
//                        FileObject f = SourceUtils.getFile(selected, info.getClasspathInfo());
//                        return new RenameRefactoringUI(f==null?info.getFileObject():f);
//                    } else {
//                        return new RenameRefactoringUI(selectedElement, info);
//                    }
//                }
//            };
//        } else {
            task = new NodeToFileObjectTask(new HashSet<Node>(lookup.lookupAll(Node.class))) {
                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<TreePathHandle> handle) {
                    return new CopyClassRefactoringUI(selectedElements[0], getTarget(lookup), getPaste(lookup));
                }
            };
//        }
        RetoucheUtils.invokeAfterScanFinished(task, getActionName(RefactoringActionsFactory.copyAction()));
    }

    /**
     * returns true if exactly one refactorable file is selected
     */
    @Override
    public boolean canCopy(Lookup lookup) {
        Collection<? extends Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
        if (nodes.size() != 1) {
            return false;
        }
        Node n = nodes.iterator().next();
        DataObject dob = n.getCookie(DataObject.class);
        if (dob==null) {
            return false;
        }
        
        ExplorerContext dict = lookup.lookup(ExplorerContext.class);
        FileObject fob = getTarget(lookup);
        if (dict!=null && dict.getTargetNode() != null && fob==null) { //NOI18N
            //unknown target
            return false;
        }
        if (fob != null) {
            if (!fob.isFolder() || !RetoucheUtils.isOnSourceClasspath(fob))
                return false;
            FileObject fo = dob.getPrimaryFile();
            if (RetoucheUtils.isRefactorable(fo)) { //NOI18N
                return true;
            }

        } else {
            FileObject fo = dob.getPrimaryFile();
            if (RetoucheUtils.isRefactorable(fo)) { //NOI18N
                return true;
            }
        }

        return false;
    }    

    @Override
    public boolean canFindUsages(Lookup lookup) {
        Collection<? extends Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
        if (nodes.size() != 1) {
            return false;
        }
        Node n = nodes.iterator().next();
        if (n.getLookup().lookup(TreePathHandle.class) != null) {
            return true;
        }
        DataObject dob = n.getCookie(DataObject.class);
        if ((dob!=null) && RetoucheUtils.isJavaFile(dob.getPrimaryFile()) && !"package-info".equals(dob.getName())) { //NOI18N
            return true;
        }
        return false;
    }

    @Override
    public void doFindUsages(Lookup lookup) {
        Runnable task;
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (isFromEditor(ec)) {
            task = new TextComponentTask(ec) {
                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, CompilationInfo info) {
                    return new WhereUsedQueryUI(selectedElement, info);
                }
            };
        } else if (nodeHandle(lookup)) {
            task = new TreePathHandleTask(new HashSet<Node>(lookup.lookupAll(Node.class)), true) {

                RefactoringUI ui;

                @Override
                protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
                    ui = new WhereUsedQueryUI(handle, javac);
                }

                @Override
                protected RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles) {
                    return ui;
                }
                
            };
        } else {
            task = new NodeToElementTask(new HashSet<Node>(lookup.lookupAll(Node.class))) {
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement, CompilationInfo info) {
                    if (selectedElement==null)
                        return null;
                    return new WhereUsedQueryUI(selectedElement, info);
                }
            };
        }
        RetoucheUtils.invokeAfterScanFinished(task, getActionName(RefactoringActionsFactory.whereUsedAction()));
    }

    /**
     * returns true iff all selected file are refactorable java files
     **/

    @Override
    public boolean canDelete(Lookup lookup) {
        Collection<? extends Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
        //We live with a 2 pass validation of the selected nodes for now since
        //the code will become unreadable if we attempt to implement all checks
        //in one pass.
        if (isSelectionHeterogeneous(nodes)) {
            return false;
        }
        for (Node n:nodes) {
            TreePathHandle tph = n.getLookup().lookup(TreePathHandle.class);
            if (tph != null) {
                return RetoucheUtils.isRefactorable(tph.getFileObject());
            }
            DataObject dataObject = n.getCookie(DataObject.class);
            if (dataObject == null){
                return false;
            }
            FileObject fileObject = dataObject.getPrimaryFile();
            if (isRefactorableFolder(dataObject)){
                return true;
            }
            if (!RetoucheUtils.isRefactorable(fileObject)) {
                return false;
            }
        }
        return !nodes.isEmpty();
    }

    @Override
    public void doDelete(final Lookup lookup) {
        Runnable task;
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        final boolean b = lookup.lookup(ExplorerContext.class)!=null;
        if (isFromEditor(ec)) {
            task = new TextComponentTask(ec) {
                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, CompilationInfo info) {
                    Element selected = selectedElement.resolveElement(info);
                    if (selected == null) {
                        logger().log(Level.INFO, "doDelete: " + selectedElement, new NullPointerException("selected")); // NOI18N
                        return null;
                    }
                    if (selected.getKind() == ElementKind.PACKAGE || selected.getEnclosingElement().getKind() == ElementKind.PACKAGE) {
                        FileObject file = SourceUtils.getFile(selected, info.getClasspathInfo());
                        if (file==null) {
                            return null;
                        }
                        if (file.getName().equals(selected.getSimpleName().toString())) {
                            return new SafeDeleteUI(new FileObject[]{file}, Collections.singleton(selectedElement), b);
                        }
                    }
                        return new SafeDeleteUI(new TreePathHandle[]{selectedElement});
                    }
            };
        } else if (nodeHandle(lookup)) {
            task = new TreePathHandleTask(new HashSet<Node>(lookup.lookupAll(Node.class))) {

                @Override
                protected RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles) {
                    if (renameFile) {
                        FileObject[] files = new FileObject[handles.size()];
                        int i=0;
                        for (TreePathHandle handle:handles) {
                            files[i++] = handle.getFileObject();
                        }
                        return new SafeDeleteUI(files, handles, b);
                    } else {
                        return new SafeDeleteUI(handles.toArray(new TreePathHandle[handles.size()]));
                    }
                }
                
            };
        } else {
            task = new NodeToFileObjectTask(new HashSet<Node>(lookup.lookupAll(Node.class))) {
                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<TreePathHandle> handles) {
                    if (pkg[0]!= null) {
                        return new SafeDeleteUI(pkg[0],b);
                    } else{                
                        return new SafeDeleteUI(selectedElements, handles, b);
                    }
                }

            };
        }
        RetoucheUtils.invokeAfterScanFinished(task, getActionName(RefactoringActionsFactory.safeDeleteAction()));
    }
    
    private FileObject getTarget(Lookup look) {
        ExplorerContext drop = look.lookup(ExplorerContext.class);
        if (drop==null)
            return null;
        Node n = drop.getTargetNode();
        if (n==null)
            return null;
        DataObject dob = n.getCookie(DataObject.class);
        if (dob!=null)
            return dob.getPrimaryFile();
        return null;
    }
    
    private PasteType getPaste(Lookup look) {
        ExplorerContext drop = look.lookup(ExplorerContext.class);
        if (drop==null)
            return null;
        Transferable orig = drop.getTransferable();
        if (orig==null)
            return null;
        Node n = drop.getTargetNode();
        if (n==null)
            return null;
        PasteType[] pt = n.getPasteTypes(orig);
        if (pt.length==1) {
            return null;
        }
        return pt[1];
    }

    static String getName(Lookup look) {
        ExplorerContext ren = look.lookup(ExplorerContext.class); 
        if (ren==null) 
            return null;
        return ren.getNewName(); //NOI18N
    }
    
    /**
     * returns true if there is at least one java file in the selection
     * and all java files are refactorable
     */
    @Override
    public boolean canMove(Lookup lookup) {
        Collection<? extends Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
        ExplorerContext drop = lookup.lookup(ExplorerContext.class);
        FileObject fo = getTarget(lookup);
        if (fo != null) {
            if (!fo.isFolder())
                return false;
            if (!RetoucheUtils.isOnSourceClasspath(fo)) 
                return false;
            
            //it is drag and drop
            Set<DataFolder> folders = new HashSet<DataFolder>();
            boolean jdoFound = false;
            for (Node n:nodes) {
                DataObject dob = n.getCookie(DataObject.class);
                if (dob==null) {
                    return false;
                }
                if (!RetoucheUtils.isOnSourceClasspath(dob.getPrimaryFile())) {
                    return false;
                }
                if (dob instanceof DataFolder) {
                    if (FileUtil.getRelativePath(dob.getPrimaryFile(), fo)!=null)
                        return false;
                    folders.add((DataFolder)dob);
                } else if (RetoucheUtils.isJavaFile(dob.getPrimaryFile())) {
                    jdoFound = true;
                }
            }
            if (jdoFound)
                return true;
            for (DataFolder fold:folders) {
                for (Enumeration<DataObject> e = (fold).children(true); e.hasMoreElements();) {
                    if (RetoucheUtils.isJavaFile(e.nextElement().getPrimaryFile())) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            //regular invokation
            boolean result = false;
            for (Node n:nodes) {
                DataObject dob = n.getCookie(DataObject.class);
                if (dob==null) {
                    return false;
                }
                if (dob instanceof DataFolder) {
                    return drop!=null;
                }
                if (!RetoucheUtils.isOnSourceClasspath(dob.getPrimaryFile())) {
                    return false;
                }
                if (RetoucheUtils.isJavaFile(dob.getPrimaryFile())) {
                    result = true;
                }
            }
            return result;
        }
    }

    @Override
    public void doMove(final Lookup lookup) {
        Runnable task;
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (isFromEditor(ec)) {
            task = new TextComponentTask(ec) {
                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, CompilationInfo info) {
                    Element e = selectedElement.resolveElement(info);
                    if (e == null) {
                        logger().log(Level.INFO, "doMove: " + selectedElement, new NullPointerException("e")); // NOI18N
                        return null;
                    }
                    if ((e.getKind().isClass() || e.getKind().isInterface()) &&
                            SourceUtils.getOutermostEnclosingTypeElement(e)==e) {
                        try {
                            FileObject fo = SourceUtils.getFile(e, info.getClasspathInfo());
                            if (fo!=null) {
                                DataObject d = DataObject.find(SourceUtils.getFile(e, info.getClasspathInfo()));
                                if (d.getName().equals(e.getSimpleName().toString())) {
                                    return new MoveClassUI(d);
                                }
                            }
                        } catch (DataObjectNotFoundException ex) {
                            throw (RuntimeException) new RuntimeException().initCause(ex);
                        }
                    }
                    if (selectedElement.resolve(info).getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
                        try {
                            return new MoveClassUI(DataObject.find(info.getFileObject()));
                        } catch (DataObjectNotFoundException ex) {
                            throw (RuntimeException) new RuntimeException().initCause(ex);
                        }
                    } else {
                        try {
                            return new MoveClassUI(DataObject.find(info.getFileObject()));
                        } catch (DataObjectNotFoundException ex) {
                            throw (RuntimeException) new RuntimeException().initCause(ex);
                        }
                    }
                }
            };
        } else {
            task = new NodeToFileObjectTask(new HashSet<Node>(lookup.lookupAll(Node.class))) {
                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<TreePathHandle> handles) {
                    PasteType paste = getPaste(lookup);
                    FileObject tar=getTarget(lookup);
                    if (selectedElements.length == 1) {
                        if (!selectedElements[0].isFolder()) {
                            try {
                                return new MoveClassUI(DataObject.find(selectedElements[0]), tar, paste, handles);
                            } catch (DataObjectNotFoundException ex) {
                                throw (RuntimeException) new RuntimeException().initCause(ex);
                            }
                        } else {
                            Set<FileObject> s = new HashSet<FileObject>();
                            s.addAll(Arrays.asList(selectedElements));
                            return new MoveClassesUI(s, tar, paste);
                        }
                    } else {
                        Set<FileObject> s = new HashSet<FileObject>();
                        s.addAll(Arrays.asList(selectedElements));
                        return new MoveClassesUI(s, tar, paste);
                    }
                }
                
            };
        }
        RetoucheUtils.invokeAfterScanFinished(task, getActionName(RefactoringActionsFactory.renameAction()));
    }    

    public static abstract class TreePathHandleTask implements Runnable, CancellableTask<CompilationController> {
        private Collection<TreePathHandle> handles = new ArrayList<TreePathHandle>();
        private TreePathHandle current;
        boolean renameFile;
     
        public TreePathHandleTask(Collection<? extends Node> nodes) {
            this(nodes, false);
        }
        
        public TreePathHandleTask(Collection<? extends Node> nodes, boolean useFirstHandle) {
            for (Node n:nodes) {
                TreePathHandle temp = n.getLookup().lookup(TreePathHandle.class);
                if (temp!=null) {
                    handles.add(temp);
                    if (useFirstHandle) {
                        break;
                    }
                }
            }
        }
        
        public void cancel() {
        }
        
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.ELEMENTS_RESOLVED);
            Element el = current.resolveElement(info);
            if (el!=null && el instanceof TypeElement && !((TypeElement)el).getNestingKind().isNested()) {
                if (info.getFileObject().getName().equals(el.getSimpleName().toString())) {
                    renameFile = true;
                }
            }
            treePathHandleResolved(current, info);
        }
        
        public void run() {
            for (TreePathHandle handle:handles) {
                FileObject f = handle.getFileObject();
                current = handle;
                JavaSource source = JavaSource.forFileObject(f);
                assert source != null;
                try {
                    source.runUserActionTask(this, true);
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            TopComponent activetc = TopComponent.getRegistry().getActivated();

            RefactoringUI ui = createRefactoringUI(handles);
            if (ui!=null) {
                UI.openRefactoringUI(ui, activetc);
            } else {
                JOptionPane.showMessageDialog(null,NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_CannotRenameKeyword"));
            }
        }

        /**
         * This is the place where subclasses may collect info about handles.
         * @param handle handle
         * @param javac context of running transaction
         */
        protected void treePathHandleResolved(TreePathHandle handle, CompilationInfo javac) {
        }

        protected abstract RefactoringUI createRefactoringUI(Collection<TreePathHandle> handles);
    }    
    
    public static abstract class TextComponentTask implements Runnable, CancellableTask<CompilationController> {
        private JTextComponent textC;
        private int caret;
        private int start;
        private int end;
        private RefactoringUI ui;
        
        public TextComponentTask(EditorCookie ec) {
            this.textC = ec.getOpenedPanes()[0];
            this.caret = textC.getCaretPosition();
            this.start = textC.getSelectionStart();
            this.end = textC.getSelectionEnd();
            assert caret != -1;
            assert start != -1;
            assert end != -1;
        }
        
        public void cancel() {
        }
        
        public void run(CompilationController cc) throws Exception {
            TreePath selectedElement = null;
            cc.toPhase(Phase.RESOLVED);
            selectedElement = cc.getTreeUtilities().pathFor(caret);
            //workaround for issue 89064
            if (selectedElement.getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
                List<? extends Tree> decls = cc.getCompilationUnit().getTypeDecls();
                if (!decls.isEmpty()) {
                    selectedElement = TreePath.getPath(cc.getCompilationUnit(), decls.get(0));
                }
            }
            ui = createRefactoringUI(TreePathHandle.create(selectedElement, cc), start, end, cc);
        }
        
        public final void run() {
            try {
                JavaSource source = JavaSource.forDocument(textC.getDocument());
                source.runUserActionTask(this, true);
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
                return ;
            }
            TopComponent activetc = TopComponent.getRegistry().getActivated();
            
            if (ui!=null) {
                UI.openRefactoringUI(ui, activetc);
            } else {
                JOptionPane.showMessageDialog(null,NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_CannotRenameKeyword"));
            }
        }
        
        protected abstract RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, CompilationInfo info);
    }
    
    public static abstract class NodeToElementTask implements Runnable, CancellableTask<CompilationController>  {
        private Node node;
        private RefactoringUI ui;
        
        public NodeToElementTask(Collection<? extends Node> nodes) {
            assert nodes.size() == 1;
            this.node = nodes.iterator().next();
        }
        
        public void cancel() {
        }
        
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.ELEMENTS_RESOLVED);
            CompilationUnitTree unit = info.getCompilationUnit();
            if (unit.getTypeDecls().isEmpty()) {
                ui = createRefactoringUI(null, info);
            } else {
                TreePathHandle representedObject = TreePathHandle.create(TreePath.getPath(unit, unit.getTypeDecls().get(0)),info);
                ui = createRefactoringUI(representedObject, info);
            }
        }
        
        public final void run() {
            DataObject o = node.getCookie(DataObject.class);
            JavaSource source = JavaSource.forFileObject(o.getPrimaryFile());
            assert source != null;
            try {
                source.runUserActionTask(this, true);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (ui!=null) {
                UI.openRefactoringUI(ui);
            } else {
                JOptionPane.showMessageDialog(null,NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_NoTypeDecls"));
            }
        }
        protected abstract RefactoringUI createRefactoringUI(TreePathHandle selectedElement, CompilationInfo info);
    }
    
    public static abstract class NodeToFileObjectTask implements Runnable, CancellableTask<CompilationController> {
        private Collection<? extends Node> nodes;
        public NonRecursiveFolder pkg[];
        Collection<TreePathHandle> handles = new ArrayList<TreePathHandle>();
        private Node currentNode;
     
        public NodeToFileObjectTask(Collection<? extends Node> nodes) {
            this.nodes = nodes;
        }
        
        public void cancel() {
        }
        
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.ELEMENTS_RESOLVED);
            Collection<TreePathHandle> handlesPerNode = new ArrayList<TreePathHandle>();
            CompilationUnitTree unit = info.getCompilationUnit();
            Collection<TreePathHandle> publicHandles = new ArrayList<TreePathHandle>();
            Collection<TreePathHandle> sameNameHandles = new ArrayList<TreePathHandle>();
            for (Tree t: unit.getTypeDecls()) {
                Element e = info.getTrees().getElement(TreePath.getPath(unit, t));
                if (e == null || !(e.getKind().isClass() || e.getKind().isInterface())) {
                    // syntax errors #111195
                    continue;
                }
                if (e.getSimpleName().toString().equals(info.getFileObject().getName())) {
                    TreePathHandle representedObject = TreePathHandle.create(TreePath.getPath(unit,t),info);
                    sameNameHandles.add(representedObject);
                }
                if (e.getModifiers().contains(Modifier.PUBLIC)) {
                    TreePathHandle representedObject = TreePathHandle.create(TreePath.getPath(unit,t),info);
                    publicHandles.add(representedObject);
                }
            }
            if (!publicHandles.isEmpty()) {
                handlesPerNode.addAll(publicHandles);
            } else {
                handlesPerNode.addAll(sameNameHandles);
            }

            if (!handlesPerNode.isEmpty()) {
                handles.addAll(handlesPerNode);
                nodeTranslated(currentNode, handlesPerNode, info);
            }
        }
        
        public void run() {
            FileObject[] fobs = new FileObject[nodes.size()];
            pkg = new NonRecursiveFolder[fobs.length];
            int i = 0;
            for (Node node:nodes) {
                DataObject dob = node.getCookie(DataObject.class);
                if (dob!=null) {
                    fobs[i] = dob.getPrimaryFile();
                    if (RetoucheUtils.isJavaFile(fobs[i])) {
                        JavaSource source = JavaSource.forFileObject(fobs[i]);
                        assert source != null;
                        try {
                            currentNode = node;
                            // XXX this could be optimize by ClasspasthInfo in case of more than one file
                            source.runUserActionTask(this, true);
                        } catch (IllegalArgumentException ex) {
                            ex.printStackTrace();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } finally {
                            currentNode = null;
                        }
                    }
                    
                    pkg[i++] = node.getLookup().lookup(NonRecursiveFolder.class);
                }
            }
            RefactoringUI ui = createRefactoringUI(fobs, handles);
            if (ui!=null) {
                UI.openRefactoringUI(ui);
            } else {
                JOptionPane.showMessageDialog(null,NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_NoTypeDecls"));
            }
        }

        /**
         * Notifies subclasses about the translation.
         * This is the place where subclasses may collect info about handles.
         * @param node node that is translated
         * @param handles handles translated from the node
         * @param javac context of running translation
         */
        protected void nodeTranslated(Node node, Collection<TreePathHandle> handles, CompilationInfo javac) {
        }

        protected abstract RefactoringUI createRefactoringUI(FileObject[] selectedElement, Collection<TreePathHandle> handles);
    }    

    private static boolean isSelectionHeterogeneous(Collection<? extends Node> nodes){
        boolean folderSelected = false;
        boolean nonFolderNodeSelected = false;
        for (Node node : nodes) {
            DataObject dataObject = node.getCookie(DataObject.class);
            if (dataObject == null){
                continue;
            }
            if (isRefactorableFolder(dataObject)){
                if (folderSelected || nonFolderNodeSelected) {
                    return true;
                }else{
                    folderSelected = true;
                }
            }else{
                nonFolderNodeSelected = true;
            }
        }

        return false;
    }
    
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

    private static boolean isRefactorableFolder(DataObject dataObject) {
        FileObject fileObject = dataObject.getPrimaryFile();
        if (/* #159628 */!Boolean.TRUE.equals(fileObject.getAttribute("isRemoteAndSlow"))) { // NOI18N
            FileObject[] children = fileObject.getChildren();
            if (children == null || children.length <= 0) {
                return false;
            }
        }
        
        return (dataObject instanceof DataFolder) && 
                RetoucheUtils.isFileInOpenProject(fileObject) && 
                RetoucheUtils.isOnSourceClasspath(fileObject) && 
                !RetoucheUtils.isClasspathRoot(fileObject);
    }

    private static Logger logger() {
        return Logger.getLogger(RefactoringActionsProvider.class.getName());
    }
}
