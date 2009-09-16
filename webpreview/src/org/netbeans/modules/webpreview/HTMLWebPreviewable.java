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
package org.netbeans.modules.webpreview;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.core.browser.api.WebBrowser;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.webpreview.api.WebPreviewable;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;

/**
 *
 * @author Milan Kubec
 */
public class HTMLWebPreviewable {

    private static WebPreviewable instance = WebPreviewable.create(new HTMLWebPreviewableImpl());

    private HTMLWebPreviewable() { }

    public static WebPreviewable instanceCreate() {
        return instance;
    }

    public static class HTMLWebPreviewableImpl implements WebPreviewable.Impl {

        private Map<FileObject,Boolean> enabledComponents = new WeakHashMap<FileObject,Boolean>();

        private PropertyChangeSupport pcs;

        private WebBrowser attachedBrowser;

        public HTMLWebPreviewableImpl() {
            pcs = new PropertyChangeSupport(this);
        }

        public void setUserPreviewFile(String url) {
            // XXX
        }
        
        public void setPreviewEnabled(boolean b) {
            FileObject docFO = getFocusedFile();
            if (docFO != null) {
                enabledComponents.put(docFO, b);
            }
            pcs.firePropertyChange(WebPreviewable.PROP_PREVIEW_ENABLED, null, null);
        }

        public boolean isPreviewEnabled() {
            boolean toReturn = false;
            if (enabledComponents.isEmpty()) {
                return false;
            } else {
                FileObject docFO = getFocusedFile();
                if (docFO != null) {
                    Boolean bool = enabledComponents.get(docFO);
                    toReturn = bool == null ? false : bool;
                }
            }
            return toReturn;
        }

        public void onAttach(final WebBrowser browser) {
            attachedBrowser = browser;
            FileObject docFO = getFocusedFile();
            if (docFO != null) {
                docFO.removeFileChangeListener(fileChangeListener);
                docFO.addFileChangeListener(fileChangeListener);
                String file2Open = null;
                try {
                    file2Open = docFO.getURL().toExternalForm();
                } catch (FileStateInvalidException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (file2Open != null) {
                    browser.setURL(file2Open);
                }
            }
        }

        // listening on file save
        private FileChangeListener fileChangeListener = new FileChangeListener() {
            public void fileFolderCreated(FileEvent fe) {
            }
            public void fileDataCreated(FileEvent fe) {
            }
            public void fileChanged(FileEvent fe) {
                WebBrowser browser = getAttachedBrowser();
                if (browser != null) {
                    browser.reloadDocument();
                }
            }
            public void fileDeleted(FileEvent fe) {
            }
            public void fileRenamed(FileRenameEvent fe) {
            }
            public void fileAttributeChanged(FileAttributeEvent fe) {
            }
        };

        private WebBrowser getAttachedBrowser() {
            return attachedBrowser;
        }

        private FileObject getFocusedFile() {
            FileObject docFO = null;
            JTextComponent comp = EditorRegistry.lastFocusedComponent();
            if (comp != null) {
                Document doc = comp.getDocument();
                docFO = NbEditorUtilities.getFileObject(doc);
            }
            return docFO;
        }

        public void onDettach(WebBrowser browser) {
        }

        public void onClose(WebBrowser browser) {
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }

    }

}
