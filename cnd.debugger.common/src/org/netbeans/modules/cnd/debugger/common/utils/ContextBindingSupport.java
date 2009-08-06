/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.debugger.common.utils;

import java.awt.AWTKeyStroke;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.DialogBinding;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * Base support for editable fields with autocompletion
 *
 * @author Egor Ushakov
 */
public class ContextBindingSupport {
    private static final ContextBindingSupport INSTANCE = new ContextBindingSupport();

    public static final ContextBindingSupport getDefault() {
        return INSTANCE;
    }

    private RequestProcessor contextRetrievalRP;
    
    public final void setupContext(final JEditorPane editorPane, final ActionListener contextSetUp) {
        EditorKit kit = CloneableEditorSupport.getEditorKit(MIMENames.CPLUSPLUS_MIME_TYPE);
        editorPane.setEditorKit(kit);
        if (EventQueue.isDispatchThread()) {
            synchronized (ContextBindingSupport.class) {
                if (contextRetrievalRP == null) {
                    contextRetrievalRP = new RequestProcessor("Context Retrieval", 1); // NOI18N
                }
                contextRetrievalRP.post(new Runnable() {
                    public void run() {
                        final Context c = retrieveContext();
                        if (c != null) {
                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    setupContext(editorPane, c.fileObject, c.line);
                                    if (contextSetUp != null) {
                                        contextSetUp.actionPerformed(null);
                                    }
                                }
                            });
                        }
                    }
                });
            }
            setupUI(editorPane);
        } else {
            Context c = retrieveContext();
            if (c != null) {
                setupContext(editorPane, c.fileObject, c.line);
            } else {
                setupUI(editorPane);
            }
            if (contextSetUp != null) {
                contextSetUp.actionPerformed(null);
            }
        }
    }

    protected Context retrieveContext() {
        List<ContextProvider> providers  = new ArrayList<ContextProvider>(
                Lookup.getDefault().lookupAll(ContextProvider.class));

        for (ContextProvider provider : providers) {
            Context result = provider.retrieveContext();
            if (result != null) {
                return result;
            }
        }

        // Use default implementation
        FileObject fo = EditorContextDispatcher.getDefault().getCurrentFile();
        if (fo != null) {
            return new Context(fo, EditorContextDispatcher.getDefault().getCurrentLineNumber());
        } else {
            fo = EditorContextDispatcher.getDefault().getMostRecentFile();
            if (fo != null) {
                return new Context(fo, EditorContextDispatcher.getDefault().getMostRecentLineNumber());
            }
        }
        return null;
    }

    private static void setupContext(JEditorPane editorPane, FileObject contextFO, int line) {
        setupUI(editorPane);
        StyledDocument doc;
        if (contextFO == null) {
            return;
        }
        try {
            DataObject dobj = DataObject.find(contextFO);
            EditorCookie ec = dobj.getCookie(EditorCookie.class);
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
        try {
            int offset = NbDocument.findLineOffset(doc, line);
            DialogBinding.bindComponentToDocument(doc, offset, 0, editorPane);
        } catch (IndexOutOfBoundsException ioobex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioobex);
        }
    }

    private static void setupUI(final JEditorPane editorPane) {
//        Runnable runnable = new Runnable() {
//            public void run() {
//                EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(editorPane);
//                eui.removeLayer(ExtCaret.HIGHLIGHT_ROW_LAYER_NAME);
//                // Do not draw text limit line
//                try {
//                    java.lang.reflect.Field textLimitLineField = EditorUI.class.getDeclaredField("textLimitLineVisible"); // NOI18N
//                    textLimitLineField.setAccessible(true);
//                    textLimitLineField.set(eui, false);
//                } catch (Exception ex) {}
//            }
//        };
//        if (SwingUtilities.isEventDispatchThread()) {
//            runnable.run();
//        } else {
//            SwingUtilities.invokeLater(runnable);
//        }
    }

    public static JScrollPane createScrollableLineEditor(JEditorPane editorPane) {
        editorPane.setKeymap(new FilteredKeymap(editorPane));
        final JScrollPane sp = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        editorPane.setBorder(
                new CompoundBorder(editorPane.getBorder(),
                new EmptyBorder(0, 0, 0, 0)));

        JTextField referenceTextField = new JTextField("M"); // NOI18N
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
        if (sp.getPreferredSize().height < preferredHeight) {
            sp.setPreferredSize(referenceTextField.getPreferredSize());
        }
        sp.setMinimumSize(sp.getPreferredSize());

        Set<AWTKeyStroke> tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        editorPane.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, tfkeys);
        tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
        editorPane.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, tfkeys);
        return sp;
    }

    public static final class Context {
        public final FileObject fileObject;
        public final int line;
        
        public Context(FileObject fo, int contextLine) {
            this.fileObject = fo;
            this.line = contextLine <= 0 ? 0 : contextLine;
        }
    }

    public static interface ContextProvider {
        Context retrieveContext();
    }
}
