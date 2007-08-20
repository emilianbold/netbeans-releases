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

package org.netbeans.modules.cnd.refactoring.ui;

import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import javax.swing.JOptionPane;
import javax.swing.text.JTextComponent;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;
import org.openide.windows.TopComponent;

/**
 * provides support for refactoring actions
 * 
 * @author Jan Becicka
 * @author Vladimir Voskresensky
 */
public class RefactoringActionsProvider extends ActionsImplementationProvider {
    
    /** Creates a new instance of RefactoringActionsProvider */
    public RefactoringActionsProvider() {
    }
    
    @Override
    public void doRename(final Lookup lookup) {
//        Runnable task;
//        EditorCookie ec = lookup.lookup(EditorCookie.class);
//        final Dictionary dictionary = lookup.lookup(Dictionary.class);
//        if (isFromEditor(ec)) {
//            task = new TextComponentTask(ec) {
//                @Override
//                protected RefactoringUI createRefactoringUI(RubyElementCtx selectedElement,int startOffset,int endOffset, final CompilationInfo info) {
//                    // If you're trying to rename a constructor, rename the enclosing class instead
//                    return new RenameRefactoringUI(selectedElement, info);
//                }
//            };
//        } else {
//            task = new NodeToFileObjectTask(lookup.lookupAll(Node.class)) {
//                @Override
//                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<RubyElementCtx> handles) {
//                    String newName = getName(dictionary);
//                    if (newName!=null) {
//                        if (pkg[0]!= null)
//                            return new RenameRefactoringUI(pkg[0], newName);
//                        else
//                            return new RenameRefactoringUI(selectedElements[0], newName, handles==null||handles.isEmpty()?null:handles.iterator().next(), cinfo==null?null:cinfo.get());
//                    }
//                    else 
//                        if (pkg[0]!= null)
//                            return new RenameRefactoringUI(pkg[0]);
//                        else
//                            return new RenameRefactoringUI(selectedElements[0], handles==null||handles.isEmpty()?null:handles.iterator().next(), cinfo==null?null:cinfo.get());
//                }
//            };
//        }
//        task.run();
    }

    /**
     * returns true if exactly one refactorable file is selected
     */
    @Override
    public boolean canRename(Lookup lookup) {
//        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
//        if (nodes.size() != 1) {
//            return false;
//        }
//        Node n = nodes.iterator().next();
//        DataObject dob = n.getCookie(DataObject.class);
//        if (dob==null) {
//            return false;
//        }
//        FileObject fo = dob.getPrimaryFile();
//        if (RetoucheUtils.isRefactorable(fo)) { //NOI18N
//            return true;
//        }
//        if ((dob instanceof DataFolder) && 
//                RetoucheUtils.isFileInOpenProject(fo) && 
//                RetoucheUtils.isOnSourceClasspath(fo) &&
//                !RetoucheUtils.isClasspathRoot(fo))
//            return true;
        return false;
    }
    
    @Override
    public void doCopy(final Lookup lookup) {
//        Runnable task;
//        EditorCookie ec = lookup.lookup(EditorCookie.class);
//        final Dictionary dictionary = lookup.lookup(Dictionary.class);
////        if (isFromEditor(ec)) {
////            return new TextComponentRunnable(ec) {
////                @Override
////                protected RefactoringUI createRefactoringUI(RubyElementCtx selectedElement,int startOffset,int endOffset, CompilationInfo info) {
////                    Element selected = selectedElement.resolveElement(info);
////                    if (selected.getKind() == ElementKind.PACKAGE || selected.getEnclosingElement().getKind() == ElementKind.PACKAGE) {
////                        FileObject f = SourceUtils.getFile(selected, info.getClasspathInfo());
////                        return new RenameRefactoringUI(f==null?info.getFileObject():f);
////                    } else {
////                        return new RenameRefactoringUI(selectedElement, info);
////                    }
////                }
////            };
////        } else {
//            task = new NodeToFileObjectTask(lookup.lookupAll(Node.class)) {
//                @Override
//                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<RubyElementCtx> handle) {
//                    return new CopyClassRefactoringUI(selectedElements[0], getTarget(dictionary), getPaste(dictionary));
//                }
//            };
////        }
//        task.run();
    }

    /**
     * returns true if exactly one refactorable file is selected
     */
    @Override
    public boolean canCopy(Lookup lookup) {
//        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
//        if (nodes.size() != 1) {
//            return false;
//        }
//        Node n = nodes.iterator().next();
//        DataObject dob = n.getCookie(DataObject.class);
//        if (dob==null) {
//            return false;
//        }
//        
//        Dictionary dict = lookup.lookup(Dictionary.class);
//        FileObject fob = getTarget(dict);
//        if (dict!=null && dict.get("target") != null && fob==null) { //NOI18N
//            //unknown target
//            return false;
//        }
//        if (fob != null) {
//            if (!fob.isFolder())
//                return false;
//            FileObject fo = dob.getPrimaryFile();
//            if (RetoucheUtils.isRefactorable(fo)) { //NOI18N
//                return true;
//            }
//
//        } else {
//            FileObject fo = dob.getPrimaryFile();
//            if (RetoucheUtils.isRefactorable(fo)) { //NOI18N
//                return true;
//            }
//        }
//
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
        if ((dob!=null) && (CsmUtilities.getCsmFile(dob, false) != null)/*RubyUtils.isRubyOrRhtmlFile(dob.getPrimaryFile())*/) {
            return true;
        }
        return false;
    }

    @Override
    public void doFindUsages(final Lookup lookup) {
        Runnable task;
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (isFromEditor(ec)) {
            task = new TextComponentTask(ec, lookup) {
                @Override
                protected RefactoringUI createRefactoringUI(CsmObject selectedElement/*RubyElementCtx selectedElement*/,int startOffset,int endOffset/*, CompilationInfo info*/) {
                     return new WhereUsedQueryUI(selectedElement/*, info*/);
                }
            };
        } else {
            task = new NodeToElementTask(lookup.lookupAll(Node.class)) {
                protected RefactoringUI createRefactoringUI(CsmObject selectedElement/*RubyElementCtx selectedElement, CompilationInfo info*/) {
                    return new WhereUsedQueryUI(selectedElement/*, info*/);
                }
            };
        }
        task.run();
    }

    /**
     * returns true iff all selected file are refactorable java files
     **/

    @Override
    public boolean canDelete(Lookup lookup) {
        return false;
//        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
//        for (Node n:nodes) {
//            DataObject dob = n.getCookie(DataObject.class);
//            if (dob==null)
//                return false;
//            
//            if (!RetoucheUtils.isRefactorable(dob.getPrimaryFile())) {
//                return false;
//            }
//        }
//        return !nodes.isEmpty();
    }

    @Override
    public void doDelete(final Lookup lookup) {
//        Runnable task;
//        EditorCookie ec = lookup.lookup(EditorCookie.class);
//        if (isFromEditor(ec)) {
//            task = new TextComponentTask(ec) {
//                @Override
//                protected RefactoringUI createRefactoringUI(RubyElementCtx selectedElement,int startOffset,int endOffset, CompilationInfo info) {
//                    Element selected = selectedElement.resolveElement(info);
//                    if (selected.getKind() == ElementKind.PACKAGE || selected.getEnclosingElement().getKind() == ElementKind.PACKAGE) {
//                        return new SafeDeleteUI(new FileObject[]{info.getFileObject()}, Collections.singleton(selectedElement));
//                    } else {
//                        return new SafeDeleteUI(new RubyElementCtx[]{selectedElement}, info);
//                    }
//                }
//            };
//        } else {
//            task = new NodeToFileObjectTask(lookup.lookupAll(Node.class)) {
//                @Override
//                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<RubyElementCtx> handles) {
//                    return new SafeDeleteUI(selectedElements, handles);
//                }
//                
//            };
//        }
//        task.run();
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
        if (pt.length==1) {
            return null;
        }
        return pt[1];
    }

    static String getName(Dictionary dict) {
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
//        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
//        Dictionary dict = lookup.lookup(Dictionary.class);
//        FileObject fo = getTarget(dict);
//        if (fo != null) {
//            if (!fo.isFolder())
//                return false;
//            //it is drag and drop
//            Set<DataFolder> folders = new HashSet();
//            boolean jdoFound = false;
//            for (Node n:nodes) {
//                DataObject dob = n.getCookie(DataObject.class);
//                if (dob==null) {
//                    return false;
//                }
//                if (!RetoucheUtils.isOnSourceClasspath(dob.getPrimaryFile())) {
//                    return false;
//                }
//                if (dob instanceof DataFolder) {
//                    folders.add((DataFolder)dob);
//                } else if (RetoucheUtils.isRubyOrRhtmlFile(dob.getPrimaryFile())) {
//                    jdoFound = true;
//                }
//            }
//            if (jdoFound)
//                return true;
//            for (DataFolder fold:folders) {
//                for (Enumeration<DataObject> e = (fold).children(true); e.hasMoreElements();) {
//                    if (RetoucheUtils.isRubyOrRhtmlFile(e.nextElement().getPrimaryFile())) {
//                        return true;
//                    }
//                }
//            }
//            return false;
//        } else {
//            //regular invokation
//            boolean result = false;
//            for (Node n:nodes) {
//                DataObject dob = n.getCookie(DataObject.class);
//                if (dob==null) {
//                    return false;
//                }
//                if (dob instanceof DataFolder) {
//                    Object b = dict.get("DnD"); //NOI18N
//                    return b==null?false: (Boolean) b;
//                }
//                if (!RetoucheUtils.isOnSourceClasspath(dob.getPrimaryFile())) {
//                    return false;
//                }
//                if (RetoucheUtils.isRubyOrRhtmlFile(dob.getPrimaryFile())) {
//                    result = true;
//                }
//            }
//            return result;
//        }
        return false;
    }

    @Override
    public void doMove(final Lookup lookup) {
//        Runnable task;
//        EditorCookie ec = lookup.lookup(EditorCookie.class);
//        final Dictionary dictionary = lookup.lookup(Dictionary.class);
//        if (isFromEditor(ec)) {
//            task = new TextComponentTask(ec) {
//                @Override
//                protected RefactoringUI createRefactoringUI(RubyElementCtx selectedElement,int startOffset,int endOffset, CompilationInfo info) {
//
//                    Element e = selectedElement.resolveElement(info);
//                    if ((e.getKind().isClass() || e.getKind().isInterface()) &&
//                            SourceUtils.getOutermostEnclosingTypeElement(e)==e) {
//                        try {
//                            FileObject fo = SourceUtils.getFile(e, info.getClasspathInfo());
//                            if (fo!=null) {
//                                DataObject d = DataObject.find(SourceUtils.getFile(e, info.getClasspathInfo()));
//                                if (d.getName().equals(e.getSimpleName().toString())) {
//                                    return new MoveClassUI(d);
//                                }
//                            }
//                        } catch (DataObjectNotFoundException ex) {
//                            throw (RuntimeException) new RuntimeException().initCause(ex);
//                        }
//                    }
//                    if (selectedElement.resolve(info).getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
//                        try {
//                            return new MoveClassUI(DataObject.find(info.getFileObject()));
//                        } catch (DataObjectNotFoundException ex) {
//                            throw (RuntimeException) new RuntimeException().initCause(ex);
//                        }
//                    } else {
//                        try {
//                            return new MoveClassUI(DataObject.find(info.getFileObject()));
//                        } catch (DataObjectNotFoundException ex) {
//                            throw (RuntimeException) new RuntimeException().initCause(ex);
//                        }
//                    }
//                }
//            };
//        } else {
//            task = new NodeToFileObjectTask(lookup.lookupAll(Node.class)) {
//                @Override
//                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<RubyElementCtx> handles) {
//                    PasteType paste = getPaste(dictionary);
//                    FileObject tar=getTarget(dictionary);
//                    if (selectedElements.length == 1) {
//                        try {
//                            return new MoveClassUI(DataObject.find(selectedElements[0]), tar, paste, handles);
//                        } catch (DataObjectNotFoundException ex) {
//                            throw (RuntimeException) new RuntimeException().initCause(ex);
//                        }
//                    } else {
//                        Set<FileObject> s = new HashSet<FileObject>();
//                        s.addAll(Arrays.asList(selectedElements));
//                        return new MoveClassesUI(s, tar, paste);
//                    }
//                }
//                
//            };
//        }
//        task.run();
    }    

    private static CsmReference findReference(Node activatedNode) {
        return CsmReferenceResolver.getDefault().findReference(activatedNode);
    }
    
    private static boolean isSupportedReference(CsmReference ref) {
        return ref != null/* && !CsmKindUtilities.isInclude(ref.getOwner())*/;
    }
    
    public static abstract class TextComponentTask implements Runnable {
        private JTextComponent textC;
        private int caret;
        private int start;
        private int end;
        private RefactoringUI ui;
        private Lookup lookup;
        
        public TextComponentTask(EditorCookie ec, Lookup lkp) {
            this.textC = ec.getOpenedPanes()[0];
            this.caret = textC.getCaretPosition();
            this.start = textC.getSelectionStart();
            this.end = textC.getSelectionEnd();
            this.lookup = lkp;
            assert caret != -1;
            assert start != -1;
            assert end != -1;
        }
               
//        public void run(CompilationController cc) throws Exception {
//            cc.toPhase(Phase.RESOLVED);
//            org.jruby.ast.Node root = AstUtilities.getRoot(cc);
//            if (root == null) {
//                // TODO How do I add some kind of error message?
//                System.out.println("FAILURE - can't refactor uncompileable sources");
//                return;
//            }
//
//            RubyElementCtx ctx = new RubyElementCtx(cc, caret);
//            if (ctx.getSimpleName() == null) {
//                return;
//            }
//            ui = createRefactoringUI(ctx, start, end, cc);
//        }
        
        public final void run() {
            CsmReference ctx = findReference(lookup.lookup(Node.class));
            if (!isSupportedReference(ctx)) {
                return;
            }
            ui = createRefactoringUI(ctx, start, end);
//            try {
//                Source source = RetoucheUtils.getSource(textC.getDocument());
//                source.runUserActionTask(this, false);
//            } catch (IOException ioe) {
//                ErrorManager.getDefault().notify(ioe);
//                return ;
//            }
            TopComponent activetc = TopComponent.getRegistry().getActivated();
            
            if (ui!=null) {
                UI.openRefactoringUI(ui, activetc);
            } else {
                JOptionPane.showMessageDialog(null,NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_CannotRenameLoc"));
            }
        }
        
        protected abstract RefactoringUI createRefactoringUI(CsmObject selectedElement/*RubyElementCtx selectedElement*/,int startOffset,int endOffset/*, CompilationInfo info*/);
    }
    
    public static abstract class NodeToElementTask implements Runnable/*, CancellableTask<CompilationController>*/  {
        private Node node;
        private RefactoringUI ui;
        
        public NodeToElementTask(Collection<? extends Node> nodes) {
            assert nodes.size() == 1;
            this.node = nodes.iterator().next();
        }
        
        public void cancel() {
        }
        
//        public void run(CompilationController info) throws Exception {
//            info.toPhase(Phase.ELEMENTS_RESOLVED);
//            org.jruby.ast.Node root = AstUtilities.getRoot(info);
//            if (root != null) {
//                Element element = AstElement.create(root);
//                RubyElementCtx fileCtx = new RubyElementCtx(root, root, element, info.getFileObject(), info);
//                ui = createRefactoringUI(fileCtx, info);
//            }
//        }
        
        public final void run() {
            DataObject o = node.getCookie(DataObject.class);
//            Source source = RetoucheUtils.getSource(o.getPrimaryFile());
//            assert source != null;
//            try {
//                source.runUserActionTask(this, false);
//            } catch (IllegalArgumentException ex) {
//                ex.printStackTrace();
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
            UI.openRefactoringUI(ui);
        }
        protected abstract RefactoringUI createRefactoringUI(CsmObject selectedElement/*RubyElementCtx selectedElement, CompilationInfo info*/);
    }
    
    public static abstract class NodeToFileObjectTask implements Runnable/*, CancellableTask<CompilationController>*/ {
        private Collection<? extends Node> nodes;
        private RefactoringUI ui;
        public NonRecursiveFolder pkg[];
//        public WeakReference<CompilationInfo> cinfo;
//        Collection<RubyElementCtx> handles = new ArrayList<RubyElementCtx>();
        Collection<CsmObject> handles = new ArrayList<CsmObject>();
     
        public NodeToFileObjectTask(Collection<? extends Node> nodes) {
            this.nodes = nodes;
        }
        
        public void cancel() {
        }
        
//        public void run(CompilationController info) throws Exception {
//            info.toPhase(Phase.ELEMENTS_RESOLVED);
//            org.jruby.ast.Node root = AstUtilities.getRoot(info);
//            if (root != null) {
//                RubyParseResult rpr = (RubyParseResult)info.getParserResult();
//                if (rpr != null) {
//                    AnalysisResult ar = rpr.getStructure();
//                    List<? extends AstElement> els = ar.getElements();
//                    if (els.size() > 0) {
//                        // TODO - try to find the outermost or most "relevant" module/class in the file?
//                        // In Java, we look for a class with the name corresponding to the file.
//                        // It's not as simple in Ruby.
//                        AstElement element = els.get(0);
//                        org.jruby.ast.Node node = element.getNode();
//                        RubyElementCtx representedObject = new RubyElementCtx(root, node, element, info.getFileObject(), info);
//                        handles.add(representedObject);
//                    }
//                }
//            }
//            cinfo=new WeakReference<CompilationInfo>(info);
//        }
        
        public void run() {
            FileObject[] fobs = new FileObject[nodes.size()];
            pkg = new NonRecursiveFolder[fobs.length];
            int i = 0;
            for (Node node:nodes) {
                DataObject dob = node.getCookie(DataObject.class);
                if (dob!=null) {
                    fobs[i] = dob.getPrimaryFile();
//                    Source source = RetoucheUtils.getSource(fobs[i]);
//                    assert source != null;
//                    try {
//                        source.runUserActionTask(this, false);
//                    } catch (IllegalArgumentException ex) {
//                        ex.printStackTrace();
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                    }
                    
                    pkg[i++] = node.getLookup().lookup(NonRecursiveFolder.class);
                }
            }
            UI.openRefactoringUI(createRefactoringUI(fobs, handles));
        }

        protected abstract RefactoringUI createRefactoringUI(FileObject[] selectedElement, Collection<CsmObject> handles/*Collection<RubyElementCtx> handles*/);
    }    
    
    static boolean isFromEditor(EditorCookie ec) {
        if (ec != null && ec.getOpenedPanes() != null) {
            // This doesn't seem to work well - a lot of the time, I'm right clicking
            // on the editor and it still has another activated view (this is on the mac)
            // and as a result does file-oriented refactoring rather than the specific
            // editor node...
            //            TopComponent activetc = TopComponent.getRegistry().getActivated();
            //            if (activetc instanceof CloneableEditorSupport.Pane) {
            //
            return true;
            //            }
        }

        return false;
    }
}
