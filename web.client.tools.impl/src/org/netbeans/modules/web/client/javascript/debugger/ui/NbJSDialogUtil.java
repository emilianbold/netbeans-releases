/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.web.client.javascript.debugger.ui;

import java.awt.AWTKeyStroke;
import java.awt.EventQueue;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.StyledDocument;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * A GUI panel for customizing a Watch.

 * @author Joelle Lam
 */
public class NbJSDialogUtil {

    /**
     * Setup Context for code completion.
     * @param editorPane in which code completion will be done.
     * @param url of the file context
     * @param line number
     */
    public static void setupContext(JEditorPane editorPane, String url, int line) {
        FileObject file;
        StyledDocument doc;
        DataObject dobj;
        try {
            file = URLMapper.findFileObject(new URL(url));
            if (file == null) {
                return;
            }
            try {
                dobj = DataObject.find(file);
                EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
                if (ec == null) {
                    return;
                }
                try {
                    doc = ec.openDocument();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
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
            int offset = NbDocument.findLineOffset(doc, line);
            editorPane.getDocument().putProperty(javax.swing.text.Document.StreamDescriptionProperty, dobj);
        /* Joelle: TODO - DialogBinding needs to be completed for code completion in the dialog box. */
        // JavaSource js = DialogBinding.bindComponentToFile(file, offset, 0, editorPane);
        } catch (IndexOutOfBoundsException ioobex) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioobex);
        }
    }

    public static JScrollPane createScrollableLineEditor(JEditorPane editorPane) {
        editorPane.setKeymap(new FilteredKeymap(editorPane));
        JScrollPane sp = new JScrollPane(editorPane, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        editorPane.setBorder(
                new CompoundBorder(editorPane.getBorder(),
                new EmptyBorder(0, 0, 0, 0)));

        JTextField referenceTextField = new JTextField();

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

    /**
     * A keymap that filters ENTER, ESC and TAB, which have special meaning in dialogs
     *
     * @author Joelle Lam
     */
    private static class FilteredKeymap implements Keymap {

        private final javax.swing.KeyStroke enter = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0);
        private final javax.swing.KeyStroke esc = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0);
        private final javax.swing.KeyStroke tab = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB, 0);
        private final Keymap keyMap; // The original keymap

        /** Creates a new instance of FilteredKeymap */
        public FilteredKeymap(final JTextComponent component) {

            class KeymapUpdater implements Runnable {

                public void run() {
                    component.setKeymap(new FilteredKeymap(component));
                }
            }

            keyMap = component.getKeymap();
            component.addPropertyChangeListener("keymap", new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    if (!(evt.getNewValue() instanceof FilteredKeymap)) {
                        // We have to do that lazily, because the property change
                        // is fired *before* the keymap is actually changed!
                        component.removePropertyChangeListener("keymap", this);
                        if (EventQueue.isDispatchThread()) {
                            EventQueue.invokeLater(new KeymapUpdater());
                        } else {
                            RequestProcessor.getDefault().post(new KeymapUpdater(), 100);
                        }
                    }
                }
            });
        }

        public void addActionForKeyStroke(KeyStroke key, Action a) {
            keyMap.addActionForKeyStroke(key, a);
        }

        public Action getAction(KeyStroke key) {
            if (enter.equals(key) ||
                    esc.equals(key) ||
                    tab.equals(key)) {

                return null;
            } else {
                return keyMap.getAction(key);
            }
        }

        public Action[] getBoundActions() {
            return keyMap.getBoundActions();
        }

        public KeyStroke[] getBoundKeyStrokes() {
            return keyMap.getBoundKeyStrokes();
        }

        public Action getDefaultAction() {
            return keyMap.getDefaultAction();
        }

        public KeyStroke[] getKeyStrokesForAction(Action a) {
            return keyMap.getKeyStrokesForAction(a);
        }

        public String getName() {
            return keyMap.getName() + "_Filtered"; //NOI18N
        }

        public javax.swing.text.Keymap getResolveParent() {
            return keyMap.getResolveParent();
        }

        public boolean isLocallyDefined(KeyStroke key) {
            if (enter.equals(key) ||
                    esc.equals(key) ||
                    tab.equals(key)) {

                return false;
            } else {
                return keyMap.isLocallyDefined(key);
            }
        }

        public void removeBindings() {
            keyMap.removeBindings();
        }

        public void removeKeyStrokeBinding(KeyStroke keys) {
            keyMap.removeKeyStrokeBinding(keys);
        }

        public void setDefaultAction(Action a) {
            keyMap.setDefaultAction(a);
        }

        public void setResolveParent(javax.swing.text.Keymap parent) {
            keyMap.setResolveParent(parent);
        }
    }
}
