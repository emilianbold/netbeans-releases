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

package org.netbeans.modules.spring.api.beans;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.beans.ProjectSpringScopeProvider;
import org.netbeans.modules.spring.beans.SpringScopeAccessor;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 * Encapsulates the environment of Spring beans configuration files. It
 * comprises a list of related beans configuration files, and a model
 * which provides access to beans definitions in these files.
 *
 * @author Andrei Badea
 */
public final class SpringScope {

    // This class is also responsible for creating and maintaining
    // single-file models for Spring config files (that is, models that are created
    // for files not included in the config file group). But, in order to make
    // clients' life easier, they can obtain models through SpringConfigModel
    // (which calls back into this class).

    private final ConfigFileManager configFileManager;
    private Listener listener;

    final Map<ConfigFileGroup, SpringConfigModel> group2Model = new HashMap<ConfigFileGroup, SpringConfigModel>();
    final Map<FileObject, SpringConfigModel> file2Model = new HashMap<FileObject, SpringConfigModel>();

    static {
        SpringScopeAccessor.DEFAULT = new SpringScopeAccessor() {
            @Override
            public SpringScope createSpringScope(ConfigFileManager configFileManager) {
                SpringScope scope = new SpringScope(configFileManager);
                scope.initialize();
                return scope;
            }
            @Override
            public SpringConfigModel getConfigModel(SpringScope scope, FileObject fo) {
                return scope.getConfigModel(fo);
            }
        };
    }

    private SpringScope(ConfigFileManager configFileManager) {
        this.configFileManager = configFileManager;
    }

    private void initialize() {
        listener = new Listener();
        configFileManager.addChangeListener(listener);
    }

    /**
     * Finds the Spring scope that contains (or could contain) a given file.
     *
     * @param  fo a file; never null.
     * @return the Spring scope or null.
     */
    public static SpringScope getSpringScope(FileObject fo) {
        Parameters.notNull("fo", fo);
        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            return null;
        }
        ProjectSpringScopeProvider provider = project.getLookup().lookup(ProjectSpringScopeProvider.class);
        if (provider == null) {
            return null;
        }
        return provider.getSpringScope();
    }

    /**
     * Returns the config file groups for this Spring scope.
     *
     * @return the config file group; never null.
     */
    public ConfigFileManager getConfigFileManager() {
        return configFileManager;
    }

    /**
     * Returns the a list of all known models for all known configuration
     * files.
     *
     * @return the list of models; never null.
     */
    public List<SpringConfigModel> getAllFilesConfigModels() {
        List<File> files = getConfigFileManager().getConfigFiles();
        List<SpringConfigModel> result = new ArrayList<SpringConfigModel>(files.size());
        for (File file : files) {
            FileObject fo = FileUtil.toFileObject(file);
            if (fo == null) {
                continue;
            }
            SpringConfigModel model = getFileConfigModel(fo);
            if (model == null) {
                continue;
            }
            result.add(model);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the model of the beans configuration files for the given file
     * (and any related files, if the files belongs to a
     * {@link ConfigFileGroup config file group}).
     *
     * @return the beans model; never null.
     */
    private SpringConfigModel getConfigModel(FileObject configFO) {
        File configFile = FileUtil.toFile(configFO);
        if (configFile == null) {
            return null;
        }
        // If the file is one contained in a config file group, return
        // the model for that whole config file group.
        SpringConfigModel model = getGroupConfigModel(configFile);
        if (model != null) {
            return model;
        }
        // Otherwise will need to return a single-file model.
        return getFileConfigModel(configFO);
    }

    private SpringConfigModel getGroupConfigModel(File configFile) {
        for (ConfigFileGroup group : configFileManager.getConfigFileGroups()) {
            if (group.containsFile(configFile)) {
                return getGroupConfigModel(group);
            }
        }
        return null;
    }

    private SpringConfigModel getGroupConfigModel(ConfigFileGroup group) {
        SpringConfigModel model;
        synchronized (this) {
            model = group2Model.get(group);
            if (model == null) {
                model = new SpringConfigModel(group);
                group2Model.put(group, model);
            }
        }
        return model;
    }

    private synchronized SpringConfigModel getFileConfigModel(FileObject configFO) {
        SpringConfigModel model = file2Model.get(configFO);
        if (model != null) {
            return model;
        }
        File configFile = FileUtil.toFile(configFO);
        if (configFile == null) {
            // The file is not valid.
            return null;
        }
        ConfigFileGroup singleFileGroup = ConfigFileGroup.create(Collections.singletonList(configFile));
        model = new SpringConfigModel(singleFileGroup);
        file2Model.put(configFO, model);

        // We need to avoid the race condition between checking if the file is valid
        // and adding the listener.
        configFO.addFileChangeListener(listener);
        if (!configFO.isValid()) {
            file2Model.remove(configFO);
            configFO.removeFileChangeListener(listener);
            return null;
        }
        return model;
    }

    private synchronized void notifyFileDeleted(FileObject file) {
        file2Model.remove(file);
        file.removeFileChangeListener(listener);
    }

    /**
     * Called by ConfigFileManager when the config file groups change.
     */
    synchronized void notifyConfigFileManagerChanged() {
        group2Model.clear();
        file2Model.clear();
    }

    /**
     * Listens on the deletion of config files from which file models
     * were created.
     */
    private final class Listener extends FileChangeAdapter implements ChangeListener {

        @Override
        public void fileDeleted(FileEvent fe) {
            notifyFileDeleted(fe.getFile());
        }

        // XXX perhaps only notify when the file is not a Spring config file
        // anymore (MIME type changed, etc.).
        @Override
        public void fileRenamed(FileRenameEvent fe) {
            notifyFileDeleted(fe.getFile());
        }

        public void stateChanged(ChangeEvent e) {
            notifyConfigFileManagerChanged();
        }
    }
}
