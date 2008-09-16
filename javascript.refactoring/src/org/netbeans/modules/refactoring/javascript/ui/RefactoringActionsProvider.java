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

package org.netbeans.modules.refactoring.javascript.ui;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.editor.BaseDocument;
import org.netbeans.napi.gsfret.source.CompilationController;
import org.netbeans.napi.gsfret.source.CompilationInfo;
import org.netbeans.napi.gsfret.source.Phase;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.modules.refactoring.javascript.RetoucheUtils;
import org.netbeans.modules.refactoring.javascript.JsElementCtx;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.javascript.editing.AstUtilities;
import org.netbeans.modules.javascript.editing.Element;
import org.netbeans.modules.javascript.editing.JsAnalyzer.AnalysisResult;
import org.netbeans.modules.javascript.editing.AstElement;
import org.netbeans.modules.javascript.editing.JsParseResult;
import org.netbeans.modules.javascript.editing.JsUtils;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.netbeans.napi.gsfret.source.ClasspathInfo;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jan Becicka
 */
public class RefactoringActionsProvider extends ActionsImplementationProvider{
    private static boolean isFindUsages;
    
    /** Creates a new instance of RefactoringActionsProvider */
    public RefactoringActionsProvider() {
    }
    @Override
    public void doRename(final Lookup lookup) {
        Runnable task;
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        final Dictionary dictionary = lookup.lookup(Dictionary.class);
        if (isFromEditor(ec)) {
            task = new TextComponentTask(ec) {
                @Override
                protected RefactoringUI createRefactoringUI(JsElementCtx selectedElement,int startOffset,int endOffset, final CompilationInfo info) {
                    // If you're trying to rename a constructor, rename the enclosing class instead
                    return new RenameRefactoringUI(selectedElement, info);
                }
            };
        } else {
            task = new NodeToFileObjectTask(lookup.lookupAll(Node.class)) {
                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] selectedElements, Collection<JsElementCtx> handles) {
                    String newName = getName(dictionary);
                    if (newName!=null) {
                        if (pkg[0]!= null)
                            return new RenameRefactoringUI(pkg[0], newName);
                        else
                            return new RenameRefactoringUI(selectedElements[0], newName, handles==null||handles.isEmpty()?null:handles.iterator().next(), cinfo==null?null:cinfo.get());
                    }
                    else 
                        if (pkg[0]!= null)
                            return new RenameRefactoringUI(pkg[0]);
                        else
                            return new RenameRefactoringUI(selectedElements[0], handles==null||handles.isEmpty()?null:handles.iterator().next(), cinfo==null?null:cinfo.get());
                }
            };
        }
        task.run();
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

        if (isOutsideJs(lookup, fo)) {
            return false;
        }
        
        if (RetoucheUtils.isRefactorable(fo)) { //NOI18N
            return true;
        }

        return false;
    }
    
    private boolean isOutsideJs(Lookup lookup, FileObject fo) {
        if (!(JsUtils.isJsFile(fo) || JsUtils.isJsonFile(fo))) {
            // We're attempting to refactor in an embedded scenario...
            // Make sure it's actually in a JavaScript section.
            EditorCookie ec = lookup.lookup(EditorCookie.class);
            if (isFromEditor(ec)) {
                JTextComponent textC = ec.getOpenedPanes()[0];
                Document d = textC.getDocument();
                if (!(d instanceof BaseDocument)) {
                    return true;
                }
                BaseDocument bd = (BaseDocument)d;
                bd.readLock();
                try {
                    int caret = textC.getCaretPosition();
                    if (LexUtilities.getToken((BaseDocument)d, caret) == null) {
                        // Not in JavaScript code!
                        return true;
                    }
                } finally {
                    bd.readUnlock();
                }
                
            }
        }
        
        return false;
    }

    @Override
    public boolean canCopy(Lookup lookup) {
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
        if (dob == null) {
            return false;
        }

        FileObject fo = dob.getPrimaryFile();
        
        if (RetoucheUtils.isJsFile(fo) && isOutsideJs(lookup, fo)) {
            return false;
        }
        
        if ((dob!=null) && RetoucheUtils.isJsFile(fo)) { //NOI18N
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
                protected RefactoringUI createRefactoringUI(JsElementCtx selectedElement,int startOffset,int endOffset, CompilationInfo info) {
                    return new WhereUsedQueryUI(selectedElement, info);
                }
            };
        } else {
            task = new NodeToElementTask(lookup.lookupAll(Node.class)) {
                protected RefactoringUI createRefactoringUI(JsElementCtx selectedElement, CompilationInfo info) {
                    return new WhereUsedQueryUI(selectedElement, info);
                }
            };
        }
        try {
            isFindUsages = true;
            task.run();
        } finally {
            isFindUsages = false;
        }
    }

    @Override
    public boolean canDelete(Lookup lookup) {
        return false;
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
        return false;
    }

    @Override
    public void doMove(final Lookup lookup) {
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
            cc.toPhase(Phase.RESOLVED);
            org.mozilla.nb.javascript.Node root = AstUtilities.getRoot(cc);
            if (root == null) {
                // TODO How do I add some kind of error message?
                System.out.println("FAILURE - can't refactor uncompileable sources");
                return;
            }

            JsElementCtx ctx = new JsElementCtx(cc, caret);
            if (ctx.getSimpleName() == null) {
                return;
            }
            ui = createRefactoringUI(ctx, start, end, cc);
        }
        
        public final void run() {
            FileObject fo = null;
            try {
                Source source = RetoucheUtils.getSource(textC.getDocument());
                source.runUserActionTask(this, false);
                Collection<FileObject> fileObjects = source.getFileObjects();
                if (fileObjects.size() > 0) {
                    fo = fileObjects.iterator().next();
                }
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
                return ;
            }
            TopComponent activetc = TopComponent.getRegistry().getActivated();
            
            if (ui!=null) {
                if (fo != null) {
                    ClasspathInfo classpathInfoFor = RetoucheUtils.getClasspathInfoFor(fo);
                    if (classpathInfoFor == null) {
                        JOptionPane.showMessageDialog(null, NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_CannotFindClasspath"));
                        return;
                    }
                }
                
                UI.openRefactoringUI(ui, activetc);
            } else {
                String key = "ERR_CannotRenameLoc"; // NOI18N
                if (isFindUsages) {
                    key = "ERR_CannotFindUsages"; // NOI18N
                }
                JOptionPane.showMessageDialog(null,NbBundle.getMessage(RefactoringActionsProvider.class, key));
            }
        }
        
        protected abstract RefactoringUI createRefactoringUI(JsElementCtx selectedElement,int startOffset,int endOffset, CompilationInfo info);
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
            org.mozilla.nb.javascript.Node root = AstUtilities.getRoot(info);
            if (root != null) {
                Element element = AstElement.getElement(info, root);
                JsElementCtx fileCtx = new JsElementCtx(root, root, element, info.getFileObject(), info);
                ui = createRefactoringUI(fileCtx, info);
            }
        }
        
        public final void run() {
            DataObject o = node.getCookie(DataObject.class);
            Source source = RetoucheUtils.getSource(o.getPrimaryFile());
            assert source != null;
            try {
                source.runUserActionTask(this, false);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            UI.openRefactoringUI(ui);
        }
        protected abstract RefactoringUI createRefactoringUI(JsElementCtx selectedElement, CompilationInfo info);
    }
    
    public static abstract class NodeToFileObjectTask implements Runnable, CancellableTask<CompilationController> {
        private Collection<? extends Node> nodes;
        private RefactoringUI ui;
        public NonRecursiveFolder pkg[];
        public WeakReference<CompilationInfo> cinfo;
        Collection<JsElementCtx> handles = new ArrayList<JsElementCtx>();
     
        public NodeToFileObjectTask(Collection<? extends Node> nodes) {
            this.nodes = nodes;
        }
        
        public void cancel() {
        }
        
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.ELEMENTS_RESOLVED);
            org.mozilla.nb.javascript.Node root = AstUtilities.getRoot(info);
            if (root != null) {
                JsParseResult rpr = AstUtilities.getParseResult(info);
                if (rpr != null) {
                    AnalysisResult ar = rpr.getStructure();
                    List<? extends AstElement> els = ar.getElements();
                    if (els.size() > 0) {
                        // TODO - try to find the outermost or most "relevant" module/class in the file?
                        // In Java, we look for a class with the name corresponding to the file.
                        // It's not as simple in Ruby.
                        AstElement element = els.get(0);
                        org.mozilla.nb.javascript.Node node = element.getNode();
                        JsElementCtx representedObject = new JsElementCtx(root, node, element, info.getFileObject(), info);
                        representedObject.setNames(element.getFqn(), element.getName());
                        handles.add(representedObject);
                    }
                }
            }
            cinfo=new WeakReference<CompilationInfo>(info);
        }
        
        public void run() {
            FileObject[] fobs = new FileObject[nodes.size()];
            pkg = new NonRecursiveFolder[fobs.length];
            int i = 0;
            for (Node node:nodes) {
                DataObject dob = node.getCookie(DataObject.class);
                if (dob!=null) {
                    fobs[i] = dob.getPrimaryFile();
                    Source source = RetoucheUtils.getSource(fobs[i]);
                    if (source == null) {
                        continue;
                    }
                    assert source != null;
                    try {
                        source.runUserActionTask(this, false);
                    } catch (IllegalArgumentException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    
                    pkg[i++] = node.getLookup().lookup(NonRecursiveFolder.class);
                }
            }
            UI.openRefactoringUI(createRefactoringUI(fobs, handles));
        }

        protected abstract RefactoringUI createRefactoringUI(FileObject[] selectedElement, Collection<JsElementCtx> handles);
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
