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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
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
import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jan Becicka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider.class, position=400)
public class RefactoringActionsProvider extends ActionsImplementationProvider {

    private static final Logger LOG = Logger.getLogger(RefactoringActionsProvider.class.getName());
    
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
                protected RefactoringUI createRefactoringUI(JsElementCtx selectedElement,int startOffset,int endOffset, JsParseResult info) {
                    // If you're trying to rename a constructor, rename the enclosing class instead
                    return new RenameRefactoringUI(selectedElement);
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
                            return new RenameRefactoringUI(selectedElements[0], newName, handles==null||handles.isEmpty()?null:handles.iterator().next());
                    }
                    else 
                        if (pkg[0]!= null)
                            return new RenameRefactoringUI(pkg[0]);
                        else
                            return new RenameRefactoringUI(selectedElements[0], handles==null||handles.isEmpty()?null:handles.iterator().next());
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
                
            } else {
                return true; //we are on non js or json file
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
                protected RefactoringUI createRefactoringUI(JsElementCtx selectedElement,int startOffset,int endOffset, JsParseResult info) {
                    return new WhereUsedQueryUI(selectedElement);
                }
            };
        } else {
            task = new NodeToElementTask(lookup.lookupAll(Node.class)) {
                protected RefactoringUI createRefactoringUI(JsElementCtx selectedElement, JsParseResult info) {
                    return new WhereUsedQueryUI(selectedElement);
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
    
    public static abstract class TextComponentTask extends UserTask implements Runnable {
        private final JTextComponent textC;
        private final int caret;
        private final int start;
        private final int end;
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
        
        public void run(ResultIterator ri) throws ParseException {
            if (ri.getSnapshot().getMimeType().equals(JsTokenId.JAVASCRIPT_MIME_TYPE)) {
                JsParseResult jspr = AstUtilities.getParseResult(ri.getParserResult());
                org.mozilla.nb.javascript.Node root = jspr.getRootNode();
                if (root != null) {
                    JsElementCtx ctx = new JsElementCtx(jspr, caret);
                    if (ctx.getSimpleName() != null) {
                        ui = createRefactoringUI(ctx, start, end, jspr);
                    }
                } else {
                    // TODO How do I add some kind of error message?
                    System.out.println("FAILURE - can't refactor uncompileable sources");
                }
            } else {
                for(Embedding e : ri.getEmbeddings()) {
                    run(ri.getResultIterator(e));
                }
            }
        }
        
        public final void run() {
            try {
                Source source = Source.create(textC.getDocument());
                ParserManager.parse(Collections.singleton(source), this);
            } catch (ParseException e) {
                LOG.log(Level.WARNING, null, e);
                return ;
            }

            TopComponent activetc = TopComponent.getRegistry().getActivated();
            
            if (ui != null) {
// XXX: what is this supposed to do??
//                if (fo != null) {
//                    ClasspathInfo classpathInfoFor = RetoucheUtils.getClasspathInfoFor(fo);
//                    if (classpathInfoFor == null) {
//                        JOptionPane.showMessageDialog(null, NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_CannotFindClasspath"));
//                        return;
//                    }
//                }
                
                UI.openRefactoringUI(ui, activetc);
            } else {
                String key = "ERR_CannotRenameLoc"; // NOI18N
                if (isFindUsages) {
                    key = "ERR_CannotFindUsages"; // NOI18N
                }
                JOptionPane.showMessageDialog(null,NbBundle.getMessage(RefactoringActionsProvider.class, key));
            }
        }
        
        protected abstract RefactoringUI createRefactoringUI(JsElementCtx selectedElement,int startOffset,int endOffset, JsParseResult info);
    }
    
    public static abstract class NodeToElementTask extends UserTask implements Runnable  {
        private final Node node;
        private RefactoringUI ui;
        
        public NodeToElementTask(Collection<? extends Node> nodes) {
            assert nodes.size() == 1;
            this.node = nodes.iterator().next();
        }
        
        public void run(ResultIterator ri) throws ParseException {
            if (ri.getSnapshot().getMimeType().equals(JsTokenId.JAVASCRIPT_MIME_TYPE)) {
                JsParseResult jspr = AstUtilities.getParseResult(ri.getParserResult());
                org.mozilla.nb.javascript.Node root = jspr.getRootNode();
                if (root != null) {
                    Element element = AstElement.getElement(jspr, root);
                    JsElementCtx fileCtx = new JsElementCtx(root, root, element, ri.getSnapshot().getSource().getFileObject(), jspr);
                    ui = createRefactoringUI(fileCtx, jspr);
                }
            } else {
                for(Embedding e : ri.getEmbeddings()) {
                    run(ri.getResultIterator(e));
                }
            }
        }
        
        public final void run() {
            try {
                DataObject o = node.getCookie(DataObject.class);
                Source source = Source.create(o.getPrimaryFile());
                ParserManager.parse(Collections.singleton(source), this);
            } catch (ParseException e) {
                LOG.log(Level.WARNING, null, e);
                return ;
            }

            if (ui != null) {
                UI.openRefactoringUI(ui);
            } else {
                String key = "ERR_CannotRenameLoc"; // NOI18N
                if (isFindUsages) {
                    key = "ERR_CannotFindUsages"; // NOI18N
                }
                JOptionPane.showMessageDialog(null,NbBundle.getMessage(RefactoringActionsProvider.class, key));
            }
        }
        protected abstract RefactoringUI createRefactoringUI(JsElementCtx selectedElement, JsParseResult info);
    }
    
    public static abstract class NodeToFileObjectTask extends UserTask implements Runnable {
        private final Collection<? extends Node> nodes;
//        private RefactoringUI ui;
        protected final NonRecursiveFolder pkg[];
//        public WeakReference<JsParseResult> cinfo;
        Collection<JsElementCtx> handles = new ArrayList<JsElementCtx>();
     
        public NodeToFileObjectTask(Collection<? extends Node> nodes) {
            assert nodes != null;
            this.nodes = nodes;
            this.pkg = new NonRecursiveFolder[nodes.size()];
        }
        
        public void run(ResultIterator ri) throws Exception {
            if (ri.getSnapshot().getMimeType().equals(JsTokenId.JAVASCRIPT_MIME_TYPE)) {
                JsParseResult jspr = AstUtilities.getParseResult(ri.getParserResult());
                org.mozilla.nb.javascript.Node root = jspr.getRootNode();
                if (root != null) {
                    AnalysisResult ar = jspr.getStructure();
                    List<? extends AstElement> els = ar.getElements();
                    if (els.size() > 0) {
                        // TODO - try to find the outermost or most "relevant" module/class in the file?
                        // In Java, we look for a class with the name corresponding to the file.
                        // It's not as simple in Ruby.
                        AstElement element = null;
			for (AstElement astElement : els) {
			    if (astElement.getName().equals(ri.getSnapshot().getSource().getFileObject().getName())) {
				element = astElement;
			    }
			}
			if (element != null) {
			    org.mozilla.nb.javascript.Node node = element.getNode();
			    JsElementCtx representedObject = new JsElementCtx(root, node, element, ri.getSnapshot().getSource().getFileObject(), jspr);
			    representedObject.setNames(element.getFqn(), element.getName());
			    handles.add(representedObject);
			}
                    }
                }
//                cinfo=new WeakReference<CompilationInfo>(info);
            } else {
                for(Embedding e : ri.getEmbeddings()) {
                    run(ri.getResultIterator(e));
                }
            }
        }
        
        public void run() {
            FileObject[] fobs = new FileObject[nodes.size()];
            int i = 0;
            for(Node node : nodes) {
                DataObject dob = node.getCookie(DataObject.class);
                if (dob != null) {
                    fobs[i] = dob.getPrimaryFile();
                    Source source = Source.create(fobs[i]);
                    try {
                        ParserManager.parse(Collections.singleton(source), this);
                    } catch (ParseException ex) {
                        LOG.log(Level.WARNING, null, ex);
                    }
                    
                    pkg[i++] = node.getLookup().lookup(NonRecursiveFolder.class);
                }
            }
            UI.openRefactoringUI(createRefactoringUI(fobs, handles));
        }

        protected abstract RefactoringUI createRefactoringUI(FileObject[] selectedElement, Collection<JsElementCtx> handles);
    }    
    
     private static boolean isFromEditor(EditorCookie ec) {
        if (ec != null && ec.getOpenedPanes() != null) {
            TopComponent activetc = TopComponent.getRegistry().getActivated();
            if (activetc instanceof CloneableEditorSupport.Pane) {
                return true;
            }
        }
        return false;
    }
}
