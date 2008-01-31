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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.util.fcs.FileChangeSupport;
import org.netbeans.modules.spring.util.fcs.FileChangeSupportEvent;
import org.netbeans.modules.spring.util.fcs.FileChangeSupportListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * The implementation of the config model. Listens on the config files
 * and manages the controllers for each of them. Provides access to the model.
 * This class is thread-safe.
 *
 * @author Andrei Badea
 */
public class SpringConfigModelController {

    // XXX probably make lazy. First runReadAccess() will be slower, but
    // at least we won't be eating up unnecessary memory.

    private final ConfigFileGroup configFileGroup;
    private final Map<File, SpringConfigFileModelController> file2Controller = Collections.synchronizedMap(new HashMap<File, SpringConfigFileModelController>());

    private Listener listener;
    private EditorRegistryListener erListener;

    // Encapsulates the current access to the model.
    private Access currentAccess;

    /**
     * Creates a new instance. A factory method is needed in order to avoid
     * escaping {@code this} from the constructor.
     *
     * @param  configFileGroup the config file group to create a model for.
     * @return a new instance; never null.
     */
    public static SpringConfigModelController create(ConfigFileGroup configFileGroup) {
        SpringConfigModelController result = new SpringConfigModelController(configFileGroup);
        result.initialize();
        return result;
    }

    private SpringConfigModelController(ConfigFileGroup configFileGroup) {
        this.configFileGroup = configFileGroup;
    }

    private void initialize() {
        listener = new Listener();
        synchronized (file2Controller) {
            for (File file : configFileGroup.getFiles()) {
                // FileObject fo = FileUtil.toFileObject(file);
                file2Controller.put(file, new SpringConfigFileModelController(file));
                FileChangeSupport.DEFAULT.addListener(listener, file);
            }
        }
        erListener = new EditorRegistryListener();
        erListener.initialize();
    }

    /**
     * Provides access to the model by running the passed
     * action under exclusive access.
     *
     * @param  action the action to run.
     */
    public void runReadAction(Action<SpringBeans> action) throws IOException {
        try {
            runReadAction0(action);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else if (e instanceof IOException) {
                throw (IOException)e;
            } else {
                IOException ioe = new IOException(e.getMessage());
                throw (IOException)ioe.initCause(e);
            }
        }
    }

    private void runReadAction0(final Action<SpringBeans> action) throws Exception {
        ExclusiveAccess.getInstance().runPriorityTask(new Callable<Void>() {
            public Void call() throws IOException {
                // Handle reentrant access.
                boolean firstEntry = (currentAccess == null);
                if (firstEntry) {
                    currentAccess = new Access();
                }
                action.run(new ConfigModelSpringBeans(currentAccess));
                if (firstEntry) {
                    currentAccess = null;
                }
                return null;
            }
        });
    }

    private void notifyFileChanged(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) {
            return;
        }
        SpringConfigFileModelController fileController = file2Controller.get(file);
        if (fileController != null) {
            fileController.notifyChange(fo, null);
        }
    }

    private void notifyFileDeleted(File file) {
        // XXX probably in order to support repeatable read, we should not remove
        // the controller under exclusive access
    }

    private void notifyFileChanged(FileObject fo, Document document) {
        File file = FileUtil.toFile(fo);
        if (file == null) {
            return;
        }
        SpringConfigFileModelController fileController = file2Controller.get(file);
        if (file2Controller != null) {
            fileController.notifyChange(fo, document);
        }
    }

    /**
     * Encapsulates one access to the model. Makes sure the config files are up to date.
     * All methods should be called run under exclusive access.
     */
    public final class Access {

        public Access() throws IOException {
            ensureUpToDate();
        }

        private void ensureUpToDate() throws IOException {
            synchronized (file2Controller) {
                for (SpringConfigFileModelController controller : file2Controller.values()) {
                    controller.makeUpToDate();
                }
            }
        }

        public SpringBeanSource getBeanSource(File file) {
            SpringConfigFileModelController fileModelController = file2Controller.get(file);
            if (fileModelController != null) {
                return fileModelController.getBeanSource();
            }
            return null;
        }

        public List<SpringBeanSource> getBeanSources() {
            List<SpringBeanSource> result = new ArrayList<SpringBeanSource>();
            synchronized (file2Controller) {
                for (SpringConfigFileModelController fileModelController : file2Controller.values()) {
                    result.add(fileModelController.getBeanSource());
                }
            }
            return result;
        }

        public boolean isValid() {
            return ExclusiveAccess.getInstance().isCurrentThreadAccess();
        }
    }

    /**
     * Listens on changes to the config files.
     */
    private final class Listener implements FileChangeSupportListener {

        public void fileCreated(FileChangeSupportEvent event) {
            notifyFileChanged(event.getPath());
        }

        public void fileModified(FileChangeSupportEvent event) {
            notifyFileChanged(event.getPath());
        }

        public void fileDeleted(FileChangeSupportEvent event) {
            notifyFileDeleted(event.getPath());
        }
    }

    // XXX this leaks. Need to create some kind of support to which Controllers register.
    private final class EditorRegistryListener implements PropertyChangeListener, DocumentListener {

        private volatile Document currentDocument;

        public EditorRegistryListener() {
        }

        public void initialize() {
            EditorRegistry.addPropertyChangeListener(this);
            JTextComponent newComponent = EditorRegistry.lastFocusedComponent();
            currentDocument = newComponent != null ? newComponent.getDocument() : null;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            JTextComponent newComponent = EditorRegistry.lastFocusedComponent();
            Document newDocument = newComponent != null ? newComponent.getDocument() : null;
            if (currentDocument == newDocument) {
                return;
            }
            currentDocument.removeDocumentListener(this);
            currentDocument = newDocument;
            currentDocument.addDocumentListener(this);
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
            // XXX sending events for all files, perhaps should check MIME type.
            // Or perhaps use a sliding task.
            notifyFileChanged(fo, document);
        }
    }
}
