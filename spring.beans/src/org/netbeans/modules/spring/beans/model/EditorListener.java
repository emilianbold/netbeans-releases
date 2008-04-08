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

package org.netbeans.modules.spring.beans.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.netbeans.modules.spring.api.beans.SpringConstants;
import org.netbeans.modules.spring.beans.loader.SpringXMLConfigDataLoader;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakSet;

/**
 *
 * @author Andrei Badea
 */
public class EditorListener {

    static final int EXPUNGE_EVERY = 50;

    private static EditorListener INSTANCE;

    // @GuardedBy("this")
    final Map<File, WeakSet<SpringConfigModelController>> file2Controllers = new HashMap<File, WeakSet<SpringConfigModelController>>();
    // @GuardedBy("this")
    private int expungeCountDown = EXPUNGE_EVERY;
    private EditorRegistryListener listener;

    private EditorListener() {}

    public static synchronized EditorListener getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EditorListener();
            INSTANCE.initialize();
        }
        return INSTANCE;
    }

    private void initialize() {
        listener = new EditorRegistryListener();
        listener.initialize();
    }

    public void register(SpringConfigModelController controller) {
        ConfigFileGroup group = controller.getConfigFileGroup();
        synchronized (this) {
            for (File file : group.getFiles()) {
                register(file, controller);
            }
            expungeStaleFiles();
        }
    }

    private void register(File file, SpringConfigModelController controller) {
        assert Thread.holdsLock(this);
        WeakSet<SpringConfigModelController> controllers = file2Controllers.get(file);
        if (controllers == null) {
            controllers = new WeakSet<SpringConfigModelController>();
            file2Controllers.put(file, controllers);
        }
        controllers.add(controller);
    }

    private void notifyFileChanged(FileObject fo, File file) {
        synchronized (this) {
            WeakSet<SpringConfigModelController> controllers = file2Controllers.get(file);
            if (controllers != null) {
                if (controllers.size() == 0) {
                // This is a stale entry, expunge it.
                    file2Controllers.remove(file);
                } else {
                    for (SpringConfigModelController controller : controllers) {
                        controller.notifyFileChanged(fo, file);
                    }
                }
            }
            expungeStaleFiles();
        }
    }

    private void expungeStaleFiles() {
        assert Thread.holdsLock(this);
        if (expungeCountDown > 0) {
            expungeCountDown--;
            return;
        }
        expungeCountDown = EXPUNGE_EVERY;
        Iterator<Map.Entry<File, WeakSet<SpringConfigModelController>>> iterator = file2Controllers.entrySet().iterator();
        while (iterator.hasNext()) {
            WeakSet<SpringConfigModelController> controllers = iterator.next().getValue();
            if (controllers.size() == 0) {
                iterator.remove();
            }
        }
    }

    private final class EditorRegistryListener implements PropertyChangeListener, DocumentListener {

        private Document currentDocument;

        public EditorRegistryListener() {
        }

        public synchronized void initialize() {
            EditorRegistry.addPropertyChangeListener(this);
            JTextComponent newComponent = EditorRegistry.lastFocusedComponent();
            currentDocument = newComponent != null ? newComponent.getDocument() : null;
            if (currentDocument != null) {
                currentDocument.addDocumentListener(listener);
            }
        }

        public synchronized void propertyChange(PropertyChangeEvent evt) {
            assert SwingUtilities.isEventDispatchThread();
            JTextComponent newComponent = EditorRegistry.lastFocusedComponent();
            Document newDocument = newComponent != null ? newComponent.getDocument() : null;
            if (currentDocument == newDocument) {
                return;
            }
            if (currentDocument != null) {
                currentDocument.removeDocumentListener(this);
            }
            currentDocument = newDocument;
            if (currentDocument != null) {
                currentDocument.addDocumentListener(this);
            }
        }

        public void changedUpdate(DocumentEvent e) {
            notify(e.getDocument());
        }

        public void insertUpdate(DocumentEvent e) {
            notify(e.getDocument());
        }

        public void removeUpdate(DocumentEvent e) {
            notify(e.getDocument());
        }

        private void notify(Document document) {
            FileObject fo = NbEditorUtilities.getFileObject(document);
            if (fo == null){
                return;
            }
            if (!SpringConstants.CONFIG_MIME_TYPE.equals(fo.getMIMEType())) {
                return;
            }
            File file = FileUtil.toFile(fo);
            if (file == null) {
                return;
            }
            notifyFileChanged(fo, file);
        }
    }
}
