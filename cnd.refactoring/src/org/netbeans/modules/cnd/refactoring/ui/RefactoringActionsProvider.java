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

import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JOptionPane;
import javax.swing.text.JTextComponent;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * provides support for refactoring actions
 * 
 * @author Vladimir Voskresensky
 */
public class RefactoringActionsProvider extends ActionsImplementationProvider {
    
    /** Creates a new instance of RefactoringActionsProvider */
    public RefactoringActionsProvider() {
    }
        
    @Override
    public boolean canFindUsages(Lookup lookup) {
        CsmReference ctx = CsmRefactoringUtils.findReference(lookup);
        if (CsmRefactoringUtils.isSupportedReference(ctx)) {
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
                protected RefactoringUI createRefactoringUI(CsmObject selectedElement,int startOffset,int endOffset) {
                     return new WhereUsedQueryUI(selectedElement);
                }
            };
        } else {
            task = new NodeToElementTask(lookup.lookupAll(Node.class)) {
                protected RefactoringUI createRefactoringUI(CsmObject selectedElement) {
                    return new WhereUsedQueryUI(selectedElement);
                }
            };
        }
        task.run();
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
            CsmReference ctx = CsmRefactoringUtils.findReference(lookup);
            if (!CsmRefactoringUtils.isSupportedReference(ctx)) {
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
