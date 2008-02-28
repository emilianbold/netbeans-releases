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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel.DocumentAccess;
import org.netbeans.modules.spring.beans.model.SpringConfigFileModelController.LockedDocument;
import org.netbeans.modules.spring.beans.model.impl.ConfigFileSpringBeanSource;
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

    private FileListener fileListener;

    // Encapsulates the current read access to the model.
    private ConfigModelSpringBeans readAccess;
    // Encapsulates the current read access to the model.
    private boolean writeAccess;

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
        fileListener = new FileListener();
        synchronized (file2Controller) {
            for (File file : configFileGroup.getFiles()) {
                file2Controller.put(file, new SpringConfigFileModelController(file, new ConfigFileSpringBeanSource()));
                FileChangeSupport.DEFAULT.addListener(fileListener, file);
            }
        }
        EditorListener.getInstance().register(this);
    }

    ConfigFileGroup getConfigFileGroup() {
        return configFileGroup;
    }

    private void notifyFileChanged(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) {
            return;
        }
        notifyFileChanged(fo, file);
    }

    private void notifyFileDeleted(File file) {
        // XXX probably in order to support repeatable read, we should remove
        // the controller under exclusive access
    }

    void notifyFileChanged(FileObject fo, File file) {
        SpringConfigFileModelController fileController = file2Controller.get(file);
        if (fileController != null) {
            fileController.notifyChange(fo);
        }
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
        ExclusiveAccess.getInstance().runSyncTask(new Callable<Void>() {
            public Void call() throws IOException {
                if (writeAccess) {
                    throw new IllegalStateException("Already in write access.");
                }
                // Handle reentrant access.
                boolean firstEntry = (readAccess == null);
                try {
                    if (firstEntry) {
                        readAccess = new ConfigModelSpringBeans(computeSpringBeanSources(null));
                    }
                    action.run(readAccess);
                } finally {
                    if (firstEntry) {
                        readAccess = null;
                    }
                }
                return null;
            }
        });
    }

    public void runDocumentAction(Action<DocumentAccess> action) throws IOException {
        try {
            runDocumentAction0(action);
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

    private void runDocumentAction0(final Action<DocumentAccess> action) throws Exception {
        ExclusiveAccess.getInstance().runSyncTask(new Callable<Void>() {
            public Void call() throws IOException {
                if (readAccess != null) {
                    throw new IllegalStateException("Already in read access.");
                }
                if (writeAccess) {
                    throw new IllegalStateException("Reentrant write access not supported");
                }
                writeAccess = true;
                try {
                    synchronized (file2Controller) {
                        for (File currentFile : file2Controller.keySet()) {
                            Map<File, SpringBeanSource> beanSources = computeSpringBeanSources(currentFile);
                            SpringConfigFileModelController controller = file2Controller.get(currentFile);
                            LockedDocument lockedDoc = controller.getLockedDocument();
                            if (lockedDoc != null) {
                                lockedDoc.lock();
                                try {
                                    beanSources.put(currentFile, lockedDoc.getBeanSource());
                                    ConfigModelSpringBeans springBeans = new ConfigModelSpringBeans(beanSources);
                                    DocumentAccess docAccess = new DocumentAccess(springBeans, currentFile, lockedDoc);
                                    action.run(docAccess);
                                } finally {
                                    lockedDoc.unlock();
                                }
                            }
                        }
                    }
                } finally {
                    writeAccess = false;
                }
                return null;
            }
        });
    }

    private Map<File, SpringBeanSource> computeSpringBeanSources(File skip) throws IOException {
        Map<File, SpringBeanSource> result = new HashMap<File, SpringBeanSource>();
        synchronized (file2Controller) {
            for (Map.Entry<File, SpringConfigFileModelController> entry : file2Controller.entrySet()) {
                File currentFile = entry.getKey();
                if (!currentFile.equals(skip)) {
                    result.put(entry.getKey(), entry.getValue().getUpToDateBeanSource());
                }
            }
        }
        return result;
    }

    /**
     * Listens on changes to the config files.
     */
    private final class FileListener implements FileChangeSupportListener {

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
}
