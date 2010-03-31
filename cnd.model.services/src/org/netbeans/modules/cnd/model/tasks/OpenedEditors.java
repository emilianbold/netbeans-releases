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
package org.netbeans.modules.cnd.model.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JEditorPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Parameters;

/**
 * @author Jan Lahoda
 * @author Sergey Grinev
 * @author Vladimir Kvashin
 */
public final class OpenedEditors {

    private List<JTextComponent> visibleEditors = new ArrayList<JTextComponent>();
    private Map<JTextComponent, FileObject> visibleEditors2Files = new HashMap<JTextComponent, FileObject>();
    private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private final PropertyChangeListener componentListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            OpenedEditors.this.propertyChange(evt);
        }
    };
    static final boolean SHOW_TIME = Boolean.getBoolean("cnd.model.tasks.time");
    private static final boolean TRACE_FILES = Boolean.getBoolean("cnd.model.tasks.files");
    private final static List<String> mimeTypesList = Arrays.asList(new String[]
                {MIMENames.CPLUSPLUS_MIME_TYPE, MIMENames.C_MIME_TYPE, MIMENames.HEADER_MIME_TYPE});
    private static OpenedEditors DEFAULT = new OpenedEditors();

    private OpenedEditors() {
        EditorRegistry.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                stateChanged(evt);
            }
        });
        stateChanged(null);
    }

    public void fireStateChanged() {
        stateChanged(null);
    }

    public static OpenedEditors getDefault() {
        return DEFAULT;
    }

    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChangeEvent() {
        if (SHOW_TIME) { System.err.println("OpenedEditors.fireChangeEvent()"); }

        ChangeEvent e = new ChangeEvent(this);
        List<ChangeListener> listenersCopy = null;

        synchronized (this) {
            listenersCopy = new ArrayList<ChangeListener>(listeners);
        }

        for (ChangeListener l : listenersCopy) {
            l.stateChanged(e);
        }
    }

    public synchronized List<JTextComponent> getVisibleEditors() {
        return new ArrayList<JTextComponent>(visibleEditors);
    }

    public synchronized Collection<FileObject> getVisibleEditorsFiles() {
        return new ArrayList<FileObject>(visibleEditors2Files.values());
    }

    private synchronized void stateChanged(PropertyChangeEvent evt) {
        if (SHOW_TIME || TRACE_FILES) { 
            System.err.println("OpenedEditors.stateChanged() with event " + (evt == null ? "null" : evt.getPropertyName()));
        }

        for (JTextComponent c : visibleEditors) {
            c.removePropertyChangeListener(componentListener);
            visibleEditors2Files.remove(c);
        }

        visibleEditors.clear();

        for(JTextComponent editor : EditorRegistry.componentList()) {
            FileObject fo = editor != null ? getFileObject(editor) : null;

            if (editor instanceof JEditorPane && fo != null && isSupported(fo)) {
                // FIXUP for #139980 EditorRegistry.componentList() returns editors that are already closed
                // UPDATE it seems that this bug was fixed and there is no need in additional check now
                boolean valid = true;// isOpen((JEditorPane) editor, fo);
                if (TRACE_FILES) { System.err.printf("\tfile: %s valid: %b\n", fo.toString(), valid); }
                if (valid) {
                    visibleEditors.add(editor);
                }
            }
        }

        for (JTextComponent c : visibleEditors) {
            c.addPropertyChangeListener(componentListener);
            visibleEditors2Files.put(c, getFileObject(c));
        }

        fireChangeEvent();
    }

//    private boolean isOpen(JEditorPane editor, FileObject fo) {
//        try {
//            DataObject dao = DataObject.find(fo);
//            if (dao != null) {
//                EditorCookie editorCookie = dao.getCookie(EditorCookie.class);
//                if (editorCookie != null) {
//                    JEditorPane[] panes = editorCookie.getOpenedPanes();
//                    return panes != null && panes.length > 0;
//                }
//            }
//        } catch (DataObjectNotFoundException ex) {
//            // we don't need to report this exception;
//            // probably the file is just removed by user
//        }
//        return false;
//    }

    private synchronized void propertyChange(PropertyChangeEvent evt) {
        if (SHOW_TIME) { System.err.println("OpenedEditors.propertyChange()"); }

        JTextComponent c = (JTextComponent) evt.getSource();
        FileObject originalFile = visibleEditors2Files.get(c);
        FileObject newFile = getFileObject(c);

        if (originalFile != newFile) {
            if (SHOW_TIME) { System.err.println("OpenedEditord: new files found: " + newFile.getNameExt()); }
            visibleEditors2Files.put(c, newFile);
            fireChangeEvent();
        }
    }

    static FileObject getFileObject(JTextComponent pane) {
        Object source = pane.getDocument().getProperty(Document.StreamDescriptionProperty);
        if (!(source instanceof DataObject)) {
            return null;
        }
        return ((DataObject) source).getPrimaryFile();
    }

    /**
     * Checks if the given file is supported.
     */
    private static boolean isSupported(FileObject file) throws NullPointerException {
        Parameters.notNull("files", file); //NOI18N

        return !filterSupportedFiles(Collections.singletonList(file)).isEmpty();
    }

    /**
     * Filter unsupported files from the <code>files</code> parameter.
     */
    static List<FileObject> filterSupportedFiles(Collection<FileObject> files) throws NullPointerException {
        Parameters.notNull("files", files); //NOI18N

        List<FileObject> result = new LinkedList<FileObject>();

        for (FileObject f : files) {
            String fileMimeType = FileUtil.getMIMEType(f);

            if (fileMimeType == null) {
                //unrecognized FileObject
                continue;
            }

            if (mimeTypesList.contains(fileMimeType)) {
                result.add(f);
                continue;
            }

            String shorterMimeType = fileMimeType;

            while (true) {
                int slash = shorterMimeType.indexOf('/');

                if (slash == (-1)) {
                    break;
                }

                int plus = shorterMimeType.indexOf('+', slash);

                if (plus == (-1)) {
                    break;
                }

                shorterMimeType = shorterMimeType.substring(0, slash + 1) + shorterMimeType.substring(plus + 1);

                if (mimeTypesList.contains(shorterMimeType)) {
                    result.add(f);
                    break;
                }
            }
        }

        return result;
    }
}
