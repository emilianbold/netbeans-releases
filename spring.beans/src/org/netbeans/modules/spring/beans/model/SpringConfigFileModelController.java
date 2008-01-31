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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.spring.beans.model.impl.ConfigFileSpringBeanSource;
import java.io.IOException;
import javax.swing.text.Document;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Handles the lifecycle of a single config file. Can be notified of external changes
 * to the file through the {@link #change} method. Also provides access
 * to the beans for a single file through the {@link #getBeanSource} method.
 *
 * @author Andrei Badea
 */
public class SpringConfigFileModelController {

    private static final Logger LOGGER = Logger.getLogger(SpringConfigFileModelController.class.getName());

    private final ConfigFileSpringBeanSource beanSource = new ConfigFileSpringBeanSource();
    private final File file;

    // XXX better to use a sliding task to avoid creating an instance on every
    // notifyChange().
    private Updater updater;
    private volatile boolean parsedAtLeastOnce = false;

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
        assert ExclusiveAccess.getInstance().isCurrentThreadAccess();
        synchronized (this) {
            if (updater == null && parsedAtLeastOnce) {
                // Already up to date.
                return;
            }
            // XXX we need to use the data from the update, since it could
            // have been a document-based updater.
            updater = null;
        }
        FileObject configFO = FileUtil.toFileObject(file);
        if (configFO != null) {
            parse(configFO, null);
        }
    }

    public void notifyChange(FileObject configFO, Document document) {
        if (document != null) {
            LOGGER.log(Level.FINE, "Notified of document change to {0}", configFO);
        } else {
            LOGGER.log(Level.FINE, "Notified of document to {0}", configFO);
        }
        synchronized (this) {
            updater = new Updater(configFO, document);
            ExclusiveAccess.getInstance().postTask(updater);
        }
    }

    private void parse(FileObject configFO, Document document) throws IOException {
        assert ExclusiveAccess.getInstance().isCurrentThreadAccess();
        if (!parsedAtLeastOnce) {
            parsedAtLeastOnce = true;
        }
        beanSource.parse(configFO, document);
    }

    private final class Updater implements Runnable {

        private final FileObject configFile;
        private final Document document;

        public Updater(FileObject configFile, Document document) {
            this.configFile = configFile;
            this.document = document;
        }

        public void run() {
            assert ExclusiveAccess.getInstance().isCurrentThreadAccess();
            synchronized (SpringConfigFileModelController.this) {
                // Back off if we are not the current updater -- there should
                // be another one later in the queue.
                if (updater != this) {
                    return;
                }
                // Signal that we are updating
                // This way, if a change event is fired while we are updating,
                // we will update again later.
                updater = null;
            }
            try {
                parse(configFile, document);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }
}
