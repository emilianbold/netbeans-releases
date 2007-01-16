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

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
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
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
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
public class RefactoringActionsProvider extends ActionsImplementationProvider{
    
    /** Creates a new instance of RefactoringActionsProvider */
    public RefactoringActionsProvider() {
    }
    @Override
    public Runnable renameImpl(final Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        final Dictionary dictionary = lookup.lookup(Dictionary.class);
        if (isFromEditor(ec)) {
            return new TextComponentRunnable(ec) {
                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, CompilationInfo info) {
                    Element selected = selectedElement.resolveElement(info);
                    if (selected==null)
                        return null;
                    if (selected.getKind() == ElementKind.CONSTRUCTOR) {
                        selected = selected.getEnclosingElement();
                    }
                    if (selected.getKind() == ElementKind.PACKAGE || selected.getEnclosingElement().getKind() == ElementKind.PACKAGE) {
                        FileObject f = SourceUtils.getFile(selected, info.getClasspathInfo());
                        return new RenameRefactoringUI(f==null?info.getFileObject():f);
                    } else {
                        return new RenameRefactoringUI(selectedElement, info);
                    }
                }
            };
        } else {
            return new NodeToFileObject(lookup.lookupAll(Node.class)) {
                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements) {
                    String newName = getName(dictionary);
                    if (newName!=null) {
                        if (pkg[0]!= null)
                            return new RenameRefactoringUI(pkg[0], newName);
                        else
                            return new RenameRefactoringUI(selectedElements[0], newName);
                    }
                    else 
                        if (pkg[0]!= null)
                            return new RenameRefactoringUI(pkg[0]);
                        else
                            return new RenameRefactoringUI(selectedElements[0]);
                }
            };
        }
    }

    /**
     * returns true if exactly one refactorable file is selected
     */
    @Override
    public boolean canRename(Lookup lookup) {
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
    public Runnable copyImpl(final Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        final Dictionary dictionary = lookup.lookup(Dictionary.class);
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
            return new NodeToFileObject(lookup.lookupAll(Node.class)) {
                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements) {
                    return new CopyClassRefactoringUI(selectedElements[0], getTarget(dictionary), getPaste(dictionary));
                }
            };
//        }
    }

    /**
     * returns true if exactly one refactorable file is selected
     */
    @Override
    public boolean canCopy(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        if (nodes.size() != 1) {
            return false;
        }
        Node n = nodes.iterator().next();
        DataObject dob = n.getCookie(DataObject.class);
        if (dob==null) {
            return false;
        }
        
        Dictionary dict = lookup.lookup(Dictionary.class);
        FileObject fob = getTarget(dict);
        if (dict!=null && dict.get("target") != null && fob==null) { //NOI18N
            //unknown target
            return false;
        }
        if (fob != null) {
            if (!fob.isFolder())
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
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        if (nodes.size() != 1) {
            return false;
        }
        Node n = nodes.iterator().next();
        DataObject dob = n.getCookie(DataObject.class);
        if ((dob!=null) && RetoucheUtils.isJavaFile(dob.getPrimaryFile())) { //NOI18N
            return true;
        }
        return false;
    }

    @Override
    public Runnable findUsagesImpl(Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (isFromEditor(ec)) {
            return new TextComponentRunnable(ec) {
                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, CompilationInfo info) {
                    return new WhereUsedQueryUI(selectedElement, info);
                }
            };
        } else {
            return new NodeToElement(lookup.lookupAll(Node.class)) {
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement, CompilationInfo info) {
                    return new WhereUsedQueryUI(selectedElement, info);
                }
            };
        }
    }

    /**
     * returns true iff all selected file are refactorable java files
     **/

    @Override
    public boolean canDelete(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        for (Node n:nodes) {
            DataObject dob = n.getCookie(DataObject.class);
            if (dob==null)
                return false;
            
            if (!RetoucheUtils.isRefactorable(dob.getPrimaryFile())) {
                return false;
            }
        }
        return !nodes.isEmpty();
    }

    @Override
    public Runnable deleteImpl(final Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (isFromEditor(ec)) {
            return new TextComponentRunnable(ec) {
                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, CompilationInfo info) {
                    Element selected = selectedElement.resolveElement(info);
                    if (selected.getKind() == ElementKind.PACKAGE || selected.getEnclosingElement().getKind() == ElementKind.PACKAGE) {
                        return new SafeDeleteUI(new FileObject[]{info.getFileObject()});
                    } else {
                        return new SafeDeleteUI(new TreePathHandle[]{selectedElement}, info);
                    }
                }
            };
        } else {
            return new NodeToFileObject(lookup.lookupAll(Node.class)) {
                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements) {
                    return new SafeDeleteUI(selectedElements);
                }
                
            };
        }
    }
    
    private FileObject getTarget(Dictionary dict) {
        if (dict==null)
            return null;
        Node n = (Node) dict.get("target"); //NOI18N
        if (n==null)
            return null;
        DataObject dob = n.getCookie(DataObject.class);
        if (dob!=null)
            return dob.getPrimaryFile();
        return null;
    }
    
    private PasteType getPaste(Dictionary dict) {
        if (dict==null) 
            return null;
        Transferable orig = (Transferable) dict.get("transferable"); //NOI18N
        if (orig==null)
            return null;
        Node n = (Node) dict.get("target");
        if (n==null)
            return null;
        PasteType[] pt = n.getPasteTypes(orig);
        return pt[1];
    }
    
    private String getName(Dictionary dict) {
        if (dict==null) 
            return null;
        return (String) dict.get("name"); //NOI18N
    }
    
    /**
     * returns true if there is at least one java file in the selection
     * and all java files are refactorable
     */
    @Override
    public boolean canMove(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        Dictionary dict = lookup.lookup(Dictionary.class);
        FileObject fo = getTarget(dict);
        if (fo != null) {
            if (!fo.isFolder())
                return false;
            //it is drag and drop
            Set<DataFolder> folders = new HashSet();
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
                    return false;
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
    public Runnable moveImpl(final Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        final Dictionary dictionary = lookup.lookup(Dictionary.class);
        if (isFromEditor(ec)) {
            return new TextComponentRunnable(ec) {
                @Override
                protected RefactoringUI createRefactoringUI(TreePathHandle selectedElement,int startOffset,int endOffset, CompilationInfo info) {
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
            return new NodeToFileObject(lookup.lookupAll(Node.class)) {
                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements) {
                    PasteType paste = getPaste(dictionary);
                    FileObject tar=getTarget(dictionary);
                    if (selectedElements.length == 1) {
                        try {
                            return new MoveClassUI(DataObject.find(selectedElements[0]), tar, paste);
                        } catch (DataObjectNotFoundException ex) {
                            throw (RuntimeException) new RuntimeException().initCause(ex);
                        }
                    } else {
                        Set s = new HashSet();
                        s.addAll(Arrays.asList(selectedElements));
                        return new MoveClassesUI(s, tar, paste);
                    }
                }
                
            };
        }
    }    

    
    public abstract class TextComponentRunnable implements Runnable {
        private JTextComponent textC;
        private int caret;
        private int start;
        private int end;
        private RefactoringUI ui;
        
        public TextComponentRunnable(EditorCookie ec) {
            this.textC = ec.getOpenedPanes()[0];
            this.caret = textC.getCaretPosition();
            this.start = textC.getSelectionStart();
            this.end = textC.getSelectionEnd();
            assert caret != -1;
            assert start != -1;
            assert end != -1;
        }
        
        public final void run() {
            try {
                JavaSource source = JavaSource.forDocument(textC.getDocument());
                source.runUserActionTask(new CancellableTask<CompilationController>() {
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
                }, false);
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
    
    public abstract class NodeToElement implements Runnable {
        private Node node;
        private RefactoringUI ui;
        
        public NodeToElement(Collection<? extends Node> nodes) {
            assert nodes.size() == 1;
            this.node = nodes.iterator().next();
        }
        
        public final void run() {
            DataObject o = node.getCookie(DataObject.class);
            JavaSource source = JavaSource.forFileObject(o.getPrimaryFile());
            assert source != null;
            try {
                source.runUserActionTask(new CancellableTask<CompilationController>() {
                    public void cancel() {
                    }

                    public void run(CompilationController info) throws Exception {
                        info.toPhase(Phase.ELEMENTS_RESOLVED);
                        CompilationUnitTree unit = info.getCompilationUnit();
                        TreePathHandle representedObject = TreePathHandle.create(TreePath.getPath(unit, unit.getTypeDecls().get(0)),info);
                        ui = createRefactoringUI(representedObject, info);
                    }
                    
                }, false);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            UI.openRefactoringUI(ui);
        }
        protected abstract RefactoringUI createRefactoringUI(TreePathHandle selectedElement, CompilationInfo info);
    }
    
    public abstract class NodeToFileObject implements Runnable {
        private Collection<? extends Node> nodes;
        private RefactoringUI ui;
        public NonRecursiveFolder pkg[];
        
        public NodeToFileObject(Collection<? extends Node> nodes) {
            this.nodes = nodes;
        }
        
        public void run() {
            FileObject[] fobs = new FileObject[nodes.size()];
            pkg = new NonRecursiveFolder[fobs.length];
            int i = 0;
            for (Node node:nodes) {
                DataObject dob = (DataObject) node.getCookie(DataObject.class);
                if (dob!=null) {
                    fobs[i] = dob.getPrimaryFile();
                    pkg[i++] = node.getLookup().lookup(NonRecursiveFolder.class);
                }
            }
            UI.openRefactoringUI(createRefactoringUI(fobs));
        }

        protected abstract RefactoringUI createRefactoringUI(FileObject[] selectedElement);
    }    
    
    private boolean isFromEditor(EditorCookie ec) {
        if (ec != null && ec.getOpenedPanes() != null) {
            TopComponent activetc = TopComponent.getRegistry().getActivated();
            if (activetc instanceof CloneableEditorSupport.Pane) {
                return true;
            }
        }
        return false;
    }
}
