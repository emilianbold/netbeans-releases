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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.util.fcs.FileChangeSupport;
import org.netbeans.modules.spring.util.fcs.FileChangeSupportEvent;
import org.netbeans.modules.spring.util.fcs.FileChangeSupportListener;

/**
 * The implementation of the config model. Listens on the config files
 * and manages the controllers for each of them. Provides access to the model.
 * This class is thread-safe.
 *
 * @author Andrei Badea
 */
public class SpringConfigModelController {

    private final ConfigFileGroup configFileGroup;
    private final Map<File, SpringConfigFileModelController> file2Controller = Collections.synchronizedMap(new HashMap<File, SpringConfigFileModelController>());
    private final Listener listener = new Listener();

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
        // XXX need to listen on the context, the list of files may change
        for (File configFile : configFileGroup.getConfigFiles()) {
            file2Controller.put(configFile, new SpringConfigFileModelController(configFile));
            FileChangeSupport.DEFAULT.addListener(listener, configFile);
        }
    }

    /**
     * Provides access to the model by running the passed
     * action under exclusive access.
     *
     * @param  action the action to run.
     */
    public void runReadAction(final Action<SpringBeans> action) {
        ExclusiveAccess.getInstance().runPriorityTask(new Runnable() {
            public void run() {
                // Handle reentrant access.
                boolean firstEntry = (currentAccess == null);
                if (firstEntry) {
                    currentAccess = new Access();
                }
                action.run(new SpringConfigModelBeansImpl(currentAccess));
                if (firstEntry) {
                    currentAccess = null;
                }
            }
        });
    }

    private void notifyFileChanged(File file) {
        SpringConfigFileModelController fileController = file2Controller.get(file);
        if (fileController != null) {
            fileController.notifyChange();
        }
    }

    private void notifyFileDeleted(File file) {
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * Encapsulates one access to the model. Makes sure the config files are up to date.
     * All methods should be called run under exclusive access.
     */
    public final class Access {

        private final Set<SpringConfigFileModelController> upToDateFileModels = new HashSet<SpringConfigFileModelController>();

        public SpringBeanSource getBeanSource(File file) {
            SpringConfigFileModelController fileModelController = file2Controller.get(file);
            if (fileModelController != null) {
                ensureUpToDate(fileModelController);
            }
            return fileModelController.getBeanSource();
        }

        public List<SpringBeanSource> getBeanSources() {
            List<SpringBeanSource> result = new ArrayList<SpringBeanSource>();
            for (SpringConfigFileModelController fileModelController : file2Controller.values()) {
                ensureUpToDate(fileModelController);
                result.add(fileModelController.getBeanSource());
            }
            return result;
        }

        private void ensureUpToDate(SpringConfigFileModelController fileModelController) {
            if (!upToDateFileModels.contains(fileModelController)) {
                fileModelController.makeUpToDate();
                upToDateFileModels.add(fileModelController);
            }
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
}
