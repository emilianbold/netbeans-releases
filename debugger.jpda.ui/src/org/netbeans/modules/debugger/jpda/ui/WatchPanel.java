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

import java.awt.AWTKeyStroke;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
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
import javax.swing.text.StyledDocument;
import org.netbeans.api.debugger.Session;
import org.netbeans.editor.EditorUI;
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
                            setupContext(editorPane, c.url, c.line);
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
                if (c != null) setupContext(editorPane, c.url, c.line);
                else setupUI(editorPane);
                return ;
            } else {
                en = null;
            }
        }
        Context c = retrieveContext(en);
        if (c != null) {
            setupContext(editorPane, c.url, c.line);
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
        if (en != null) {
            JPDADebugger d = en.lookupFirst(null, JPDADebugger.class);
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
                    return c;
                } else {
                    return null;
                }
            }
        }
    }
    
    public static void setupContext(final JEditorPane editorPane, String url, int line) {
        FileObject file;
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
    }
    
}

