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
import java.util.logging.Logger;
import org.netbeans.modules.spring.beans.model.ExclusiveAccess.AsyncTask;
import org.netbeans.modules.spring.beans.model.impl.ConfigFileSpringBeanSource;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.text.Document;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 * Handles the lifecycle of a single config file. Can be notified of external changes
 * to the file through the {@link #change} method. Also provides access
 * to the beans for a single file through the {@link #getBeanSource} method.
 *
 * @author Andrei Badea
 */
public class SpringConfigFileModelController {

    private static final Logger LOGGER = Logger.getLogger(SpringConfigFileModelController.class.getName());
    private static final int DELAY = 500;

    private final ConfigFileSpringBeanSource beanSource = new ConfigFileSpringBeanSource();
    private final File file;

    private volatile boolean parsedAtLeastOnce = false;

    private AsyncTask currentUpdateTask;
    private FileObject currentFile;

    public SpringConfigFileModelController(File file) {
        this.file = file;
    }

    /**
     * Returns the {@link BeanSource} for this config file. This method needs to be
     * called under exclusive access and the client should have already
     * called makeUpToDate().
     *
     * @return the bean source for this config file.
     */
    public SpringBeanSource getBeanSource() {
        assert ExclusiveAccess.getInstance().isCurrentThreadAccess();
        return beanSource;
    }

    /**
     * Makes the beans up to date, that is, if there has previously been
     * an external change and the config file hasn't been parsed yet,
     * it is parsed now. This method needs to be called under exclusive
     * access.
     */
    public void makeUpToDate() throws IOException {
        makeUpToDateImpl(false);
    }

    public Document makeUpToDateForWrite() throws IOException {
        return makeUpToDateImpl(true);
    }

    private Document makeUpToDateImpl(boolean force) throws IOException {
        assert ExclusiveAccess.getInstance().isCurrentThreadAccess();
        FileObject fileToParse = null;
        synchronized (this) {
            if (currentUpdateTask == null || currentUpdateTask.isFinished()) {
                // No update scheduled.
                if (parsedAtLeastOnce) {
                    // The file is already parsed. We don't need to parse again...
                    if (force) {
                        // ... unless we are forced to.
                        fileToParse = currentFile;
                    }
                } else {
                    // Not parsed yet, so parse now.
                    fileToParse = FileUtil.toFileObject(file);
                }
            } else {
                // An update is scheduled, so perform it now.
                fileToParse = currentFile;
                // Ensure the updater will not run again.
                LOGGER.log(Level.FINE, "Canceling update task for " + currentFile);
                currentUpdateTask.cancel();
            }
        }
        if (fileToParse != null) {
            return parse(fileToParse, force);
        }
        return null;
    }

    public void notifyChange(FileObject configFO) {
        assert configFO != null;
        LOGGER.log(Level.FINE, "Scheduling update for {0}", configFO);
        synchronized (this) {
            if (configFO != currentFile) {
                // We are going to parse another FileObject (for example, because the
                // original one has been renamed).
                if (currentUpdateTask != null) {
                    currentUpdateTask.cancel();
                }
                currentFile = configFO;
                currentUpdateTask = ExclusiveAccess.getInstance().createAsyncTask(new Updater(configFO));
            }
            currentUpdateTask.schedule(DELAY);
        }
    }

    private Document parse(FileObject configFO, boolean keepDocument) throws IOException {
        assert ExclusiveAccess.getInstance().isCurrentThreadAccess();
        if (!parsedAtLeastOnce) {
            parsedAtLeastOnce = true;
        }
        beanSource.parse(configFO);
        return null;
    }

    private final class Updater implements Runnable {

        private final FileObject configFile;

        public Updater(FileObject configFile) {
            this.configFile = configFile;
        }

        public void run() {
            assert ExclusiveAccess.getInstance().isCurrentThreadAccess();
            try {
                parse(configFile, false);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }
}
