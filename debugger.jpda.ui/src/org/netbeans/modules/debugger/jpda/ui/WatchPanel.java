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
package org.netbeans.modules.debugger.jpda.ui;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.awt.AWTKeyStroke;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.tools.Diagnostic.Kind;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;

import org.netbeans.api.editor.DialogBinding;

import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.concurrent.Future;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.editor.EditorUI;
import org.netbeans.modules.java.preprocessorbridge.spi.WrapperFactory;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;

/**
 * A GUI panel for customizing a Watch.

 * @author Maros Sandor
 */
public class WatchPanel {

    private JPanel panel;
    private JEditorPane editorPane;
    private String expression;

    public WatchPanel(String expression) {
        this.expression = expression;
    }
    
    public static void setupContext(final JEditorPane editorPane, final ActionListener contextSetUp) {
        //EditorKit kit = CloneableEditorSupport.getEditorKit("text/x-java");
        //editorPane.setEditorKit(kit); - Do not set it, setupContext() will do the job.
        DebuggerEngine en = DebuggerManager.getDebuggerManager ().getCurrentEngine();
        if (EventQueue.isDispatchThread() && en != null) {
            RequestProcessor contextRetrievalRP = en.lookupFirst(null, RequestProcessor.class);
            if (contextRetrievalRP != null) {
                final DebuggerEngine den = en;
                contextRetrievalRP.post(new Runnable() {
                    public void run() {
                        final Context c = retrieveContext(den);
                        if (c != null) {
                            setupContext(editorPane, c.url, c.line, c.debugger);
                            if (contextSetUp != null) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        contextSetUp.actionPerformed(null);
                                    }
                                });
                            }
                        }
                    }
                });
                Context c = retrieveContext(null);
                if (c != null) setupContext(editorPane, c.url, c.line, c.debugger);
                else setupUI(editorPane);
                return ;
            } else {
                en = null;
            }
        }
        Context c = retrieveContext(en);
        if (c != null) {
            setupContext(editorPane, c.url, c.line, c.debugger);
        } else {
            setupUI(editorPane);
        }
        if (contextSetUp != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    contextSetUp.actionPerformed(null);
                }
            });
        }
    }

    private static Context retrieveContext(DebuggerEngine en) {
        CallStackFrame csf = null;
        JPDADebugger d = null;
        if (en != null) {
            d = en.lookupFirst(null, JPDADebugger.class);
            if (d != null) {
                csf = d.getCurrentCallStackFrame();
            }
        }
        if (csf != null) {
            Session session = en.lookupFirst(null, Session.class);
            String language = session.getCurrentLanguage();
            SourcePath sp = en.lookupFirst(null, SourcePath.class);
            Context c = new Context();
            c.url = sp.getURL(csf, language);
            c.line = csf.getLineNumber(language);
            if (c.line == -1) {
                c.line = 1;
            }
            c.debugger = d;
            return c;
        } else {
            EditorContext context = EditorContextBridge.getContext();
            String url = context.getCurrentURL();
            if (url != null && url.length() > 0) {
                Context c = new Context();
                c.url = url;
                c.line = context.getCurrentLineNumber();
                if (c.line == -1) {
                    c.line = 1;
                }
                c.debugger = d;
                return c;
            } else {
                url = EditorContextDispatcher.getDefault().getMostRecentURLAsString();
                if (url != null && url.length() > 0) {
                    Context c = new Context();
                    c.url = url;
                    c.line = EditorContextDispatcher.getDefault().getMostRecentLineNumber();
                    if (c.line == -1) {
                        c.line = 1;
                    }
                    c.debugger = d;
                    return c;
                } else {
                    return null;
                }
            }
        }
    }
    
    public static void setupContext(final JEditorPane editorPane, String url, int line) {
        setupContext(editorPane, url, line, null);
    }

    public static void setupContext(final JEditorPane editorPane, String url, int line, final JPDADebugger debugger) {
        final FileObject file;
        final StyledDocument doc;
        try {
            file = URLMapper.findFileObject (new URL (url));
            if (file == null) {
                return;
            }
            try {
                DataObject dobj = DataObject.find (file);
                EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
                if (ec == null) {
                    return;
                }
                try {
                    doc = ec.openDocument();
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                    return;
                }
            } catch (DataObjectNotFoundException ex) {
                // null dobj
                return;
            }
        } catch (MalformedURLException e) {
            // null dobj
            return;
        }
        try {
            final int offset = NbDocument.findLineOffset(doc, line - 1);
            //editorPane.getDocument().putProperty(javax.swing.text.Document.StreamDescriptionProperty, dobj);
            //System.err.println("WatchPanel.setupContext("+file+", "+line+", "+offset+")");
            Runnable bindComponentToDocument = new Runnable() {
                public void run() {
                    String origText = editorPane.getText();
                    DialogBinding.bindComponentToDocument(doc, offset, 0, editorPane);
                    Document editPaneDoc = editorPane.getDocument();
                    editPaneDoc.putProperty("org.netbeans.modules.editor.java.JavaCompletionProvider.skipAccessibilityCheck", "true");
                    editPaneDoc.putProperty(WrapperFactory.class,
                            debugger != null ? new MyWrapperFactory(debugger, file, doc) : null);
                    editorPane.setText(origText);
                }
            };
            if (EventQueue.isDispatchThread()) {
                bindComponentToDocument.run();
            } else {
                try {
                    SwingUtilities.invokeAndWait(bindComponentToDocument);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } catch (IndexOutOfBoundsException ioobex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioobex);
        }
        setupUI(editorPane);
    }
    
    private static void setupUI(final JEditorPane editorPane) {
        Runnable runnable = new Runnable() {
            public void run() {
                EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(editorPane);
                if (eui == null) {
                    return ;
                }
                editorPane.putClientProperty(
                    "HighlightsLayerExcludes", //NOI18N
                    "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$" //NOI18N
                );
                // Do not draw text limit line
                try {
                    java.lang.reflect.Field textLimitLineField = EditorUI.class.getDeclaredField("textLimitLineVisible"); // NOI18N
                    textLimitLineField.setAccessible(true);
                    textLimitLineField.set(eui, false);
                } catch (Exception ex) {}
                editorPane.repaint();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    public JComponent getPanel() {
        if (panel != null) return panel;

        panel = new JPanel();
        ResourceBundle bundle = NbBundle.getBundle(WatchPanel.class);

        panel.getAccessibleContext ().setAccessibleDescription (bundle.getString ("ACSD_WatchPanel")); // NOI18N
        JLabel textLabel = new JLabel();
        Mnemonics.setLocalizedText(textLabel, bundle.getString ("CTL_Watch_Name")); // NOI18N
        if (expression != null && expression.trim().length() == 0) {
            JEditorPane editor = EditorContextDispatcher.getDefault().getMostRecentEditor();
            if (editor != null && editor.getDocument() instanceof StyledDocument) {
                StyledDocument doc = (StyledDocument) editor.getDocument();
                String selectedExpression = getSelectedIdentifier(doc, editor, editor.getCaret ().getDot ());
                if (selectedExpression != null) {
                    expression = selectedExpression;
                }
            }
        }
        editorPane = new JEditorPane();//expression); // NOI18N
        editorPane.setText(expression);

        ActionListener editorPaneUpdated = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editorPane.setText (expression);
                editorPane.selectAll ();
            }
        };
        setupContext(editorPane, editorPaneUpdated);
        
        JScrollPane sp = createScrollableLineEditor(editorPane);
        int h = sp.getPreferredSize().height;
        int w = Math.min(70*editorPane.getFontMetrics(editorPane.getFont()).charWidth('a'),
                         org.openide.windows.WindowManager.getDefault().getMainWindow().getSize().width);
        sp.setPreferredSize(new Dimension(w, h));
        /*
        FontMetrics fm = editorPane.getFontMetrics(editorPane.getFont());
        int size = 2*fm.getLeading() + fm.getMaxAscent() + fm.getMaxDescent();
        Insets eInsets = editorPane.getInsets();
        Insets spInsets = sp.getInsets();
        sp.setPreferredSize(new Dimension(30*size,
                size +
                eInsets.bottom + eInsets.top +
                spInsets.bottom + spInsets.top));
        */
        textLabel.setBorder (new EmptyBorder (0, 0, 5, 0));
        panel.setLayout (new BorderLayout ());
        panel.setBorder (new EmptyBorder (11, 12, 1, 11));
        panel.add (BorderLayout.NORTH, textLabel);
        panel.add (BorderLayout.CENTER, sp);
        
        editorPane.getAccessibleContext ().setAccessibleDescription (bundle.getString ("ACSD_CTL_Watch_Name")); // NOI18N
        editorPane.setText (expression);
        editorPane.selectAll ();

        textLabel.setLabelFor (editorPane);
        HelpCtx.setHelpIDString(editorPane, "debug.customize.watch");
        editorPane.requestFocus ();
        
        return panel;
    }

    public String getExpression() {
        return editorPane.getText().trim();
    }
    
    public static JScrollPane createScrollableLineEditor(JEditorPane editorPane) {
        // Remove control keys:
        KeyStroke enterKs = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        KeyStroke escKs = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        KeyStroke tabKs = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
        InputMap im = editorPane.getInputMap();
        im.put(enterKs, "none");
        im.put(escKs, "none");
        im.put(tabKs, "none");
        
        final JScrollPane sp = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                                         JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                
        editorPane.setBorder (
            new CompoundBorder (editorPane.getBorder(),
            new EmptyBorder (0, 0, 0, 0))
        );
        
        JTextField referenceTextField = new JTextField("M");
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(referenceTextField.getBackground());
        sp.setBorder(referenceTextField.getBorder());
        sp.setBackground(referenceTextField.getBackground());
        
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        panel.add(editorPane, gridBagConstraints);
        sp.setViewportView(panel);
        
        int preferredHeight = referenceTextField.getPreferredSize().height;
        Dimension spDim = sp.getPreferredSize();
        if (spDim.height < preferredHeight) {
            sp.setPreferredSize(referenceTextField.getPreferredSize());
        } else {
            sp.setPreferredSize(spDim);
        }
        sp.setMinimumSize(spDim);
        
        setupUI(editorPane);
        
        Set<AWTKeyStroke> tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        editorPane.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, tfkeys);
        tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
        editorPane.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, tfkeys);
        return sp;
    }

    private static String getSelectedIdentifier (
        StyledDocument doc,
        JEditorPane ep,
        int offset
    ) {
        String t = null;
        if ( (ep.getSelectionStart () <= offset) &&
             (offset <= ep.getSelectionEnd ())
        )   t = ep.getSelectedText ();
        if (t != null) return t;

        int line = NbDocument.findLineNumber (
            doc,
            offset
        );
        int col = NbDocument.findLineColumn (
            doc,
            offset
        );
        try {
            javax.swing.text.Element lineElem =
                org.openide.text.NbDocument.findLineRootElement (doc).
                getElement (line);

            if (lineElem == null) return null;
            int lineStartOffset = lineElem.getStartOffset ();
            int lineLen = lineElem.getEndOffset() - lineStartOffset;
            t = doc.getText (lineStartOffset, lineLen);
            int identStart = col;
            while (identStart > 0 &&
                (Character.isJavaIdentifierPart (
                    t.charAt (identStart - 1)
                ) ||
                (t.charAt (identStart - 1) == '.'))) {
                identStart--;
            }
            int identEnd = col;
            while (identEnd < lineLen &&
                   Character.isJavaIdentifierPart(t.charAt(identEnd))
            ) {
                identEnd++;
            }

            if (identStart == identEnd) return null;
            return t.substring (identStart, identEnd);
        } catch (javax.swing.text.BadLocationException e) {
            return null;
        }
    }


    private static final class Context {
        public String url;
        public int line;
        public JPDADebugger debugger;
    }

    private static class MyWrapperFactory implements WrapperFactory {

        private WeakReference<JPDADebugger> debuggerRef;
        private FileObject fileObject;

        public MyWrapperFactory(JPDADebugger debugger, FileObject file, StyledDocument doc) {
            debuggerRef = new WeakReference(debugger);
            this.fileObject = file;
        }

        private CompilationController findController(FileObject fileObj) {
            JavaSource javaSource = JavaSource.forFileObject(fileObj);
            if (javaSource == null) return null;
            final CompilationController[] result = new CompilationController[1];
            result[0] = null;
            final Future<Void> parsingTask;
            try {
                parsingTask = javaSource.runWhenScanFinished(new CancellableTask<CompilationController>() {
                    @Override
                    public void cancel() {
                    }
                    @Override
                    public void run(CompilationController ci) throws Exception {
                        if (ci.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                            ErrorManager.getDefault().log(ErrorManager.WARNING,
                                    "Unable to resolve "+ci.getFileObject()+" to phase "+Phase.RESOLVED+", current phase = "+ci.getPhase()+
                                    "\nDiagnostics = "+ci.getDiagnostics()+
                                    "\nFree memory = "+Runtime.getRuntime().freeMemory());
                            return;
                        }
                        result[0] = ci;
                    }
                }, true);
                if (!parsingTask.isDone()) {
                    parsingTask.cancel(true);
                    return null;
                }
            } catch (IOException ioex) {
                ErrorManager.getDefault().notify(ioex);
                return null;
            }
            return result[0];
        }

        @Override
        public Trees wrapTrees(Trees trees) {
            JPDADebugger debugger = debuggerRef.get();
            if (debugger == null) return trees;
            return new MyTrees(trees, findController(fileObject), debugger);
        }

    }

    private static class MyTrees extends Trees {

        Trees trees;
        private CompilationController controller;
        private JPDADebugger debugger;

        MyTrees(Trees trees, CompilationController controller, JPDADebugger debugger) {
            this.trees = trees;
            this.controller = controller;
            this.debugger = debugger;
        }

        @Override
        public SourcePositions getSourcePositions() {
            return trees.getSourcePositions();
        }

        @Override
        public Tree getTree(Element arg0) {
            return trees.getTree(arg0);
        }

        @Override
        public ClassTree getTree(TypeElement arg0) {
            return trees.getTree(arg0);
        }

        @Override
        public MethodTree getTree(ExecutableElement arg0) {
            return trees.getTree(arg0);
        }

        @Override
        public Tree getTree(Element arg0, AnnotationMirror arg1) {
            return trees.getTree(arg0, arg1);
        }

        @Override
        public Tree getTree(Element arg0, AnnotationMirror arg1, AnnotationValue arg2) {
            return trees.getTree(arg0, arg1, arg2);
        }

        @Override
        public TreePath getPath(CompilationUnitTree arg0, Tree arg1) {
            return trees.getPath(arg0, arg1);
        }

        @Override
        public TreePath getPath(Element arg0) {
            return trees.getPath(arg0);
        }

        @Override
        public TreePath getPath(Element arg0, AnnotationMirror arg1) {
            return trees.getPath(arg0, arg1);
        }

        @Override
        public TreePath getPath(Element arg0, AnnotationMirror arg1, AnnotationValue arg2) {
            return trees.getPath(arg0, arg1, arg2);
        }

        @Override
        public Element getElement(TreePath arg0) {
            return trees.getElement(arg0);
        }

        @Override
        public TypeMirror getTypeMirror(TreePath arg0) {
            Tree tree = arg0.getLeaf();
            if (tree.getKind() == Tree.Kind.IDENTIFIER) {
                Map<String, ObjectVariable> map = null;
                try {
                    // [TODO] add JPDADebuggerImpl.getAllLabels() to API
                    Method method = debugger.getClass().getMethod("getAllLabels"); // NOI18N
                    map = (Map<String, ObjectVariable>) method.invoke(debugger);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (map != null) {
                    String name = ((IdentifierTree)tree).getName().toString();
                    ObjectVariable var = map.get(name);
                    if (var != null) {
                        Elements elements = controller.getElements();
                        TypeElement typeElem = elements.getTypeElement(var.getClassType().getName());
                        if (typeElem != null)
                            return typeElem.asType();
                    }
                }
            }
            return trees.getTypeMirror(arg0);
        }

        @Override
        public Scope getScope(TreePath arg0) {
            return trees.getScope(arg0);
        }

        @Override
        public boolean isAccessible(Scope arg0, TypeElement arg1) {
            return trees.isAccessible(arg0, arg1);
        }

        @Override
        public boolean isAccessible(Scope arg0, Element arg1, DeclaredType arg2) {
            return trees.isAccessible(arg0, arg1, arg2);
        }

        @Override
        public TypeMirror getOriginalType(ErrorType arg0) {
            return trees.getOriginalType(arg0);
        }

        @Override
        public void printMessage(Kind arg0, CharSequence arg1, Tree arg2, CompilationUnitTree arg3) {
            trees.printMessage(arg0, arg1, arg2, arg3);
        }

    }

}

