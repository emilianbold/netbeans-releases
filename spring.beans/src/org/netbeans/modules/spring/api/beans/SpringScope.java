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
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
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
    // ad-hoc models for Spring config files (that is, models that are created
    // for files not included in the config file group). But, in order to make
    // clients' life easier, they can obtain models through SpringConfigModel
    // (which calls back into this class).

    final Map<FileObject, SpringConfigModel> file2AdHocModel = new HashMap<FileObject, SpringConfigModel>();
    private final Listener listener = new Listener();

    private final ConfigFileGroup configFileGroup = new ConfigFileGroup();
    private SpringConfigModel configModel;

    static {
        SpringScopeAccessor.DEFAULT = new SpringScopeAccessor() {
            @Override
            public SpringScope createSpringScope() {
                return new SpringScope();
            }
            @Override
            public SpringConfigModel getConfigModel(SpringScope scope, FileObject fo) {
                return scope.getConfigModel(fo);
            }
        };
    }

    /**
     * Finds the Spring scope that contains (or could contain) a given file.
     *
     * @param  fo a file; never null.
     * @return the Spring scope or null.
     */
    public static SpringScope getSpringScope(FileObject fo) {
        Parameters.notNull("fo", fo);
        throw new UnsupportedOperationException("Not supported yet");
    }

    private SpringScope() {
    }

    /**
     * Returns the config file group for this Spring scope.
     *
     * @return the config file group; never null.
     */
    public ConfigFileGroup getConfigFileGroup() {
        return configFileGroup;
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
        // If the file is one contained in the config file group, return
        // the model for the config file group.
        if (configFileGroup.getConfigFiles().contains(configFile)) {
            return getConfigModel();
        }
        // Otherwise will need to return an ad-hoc model.
        return getAdHocConfigModel(configFO);
    }

    private synchronized SpringConfigModel getConfigModel() {
        if (configModel == null) {
            configModel = new SpringConfigModel(configFileGroup);
        }
        return configModel;
    }

    private synchronized SpringConfigModel getAdHocConfigModel(FileObject configFO) {
        SpringConfigModel adHocModel = file2AdHocModel.get(configFO);
        if (adHocModel != null) {
            return adHocModel;
        }
        File configFile = FileUtil.toFile(configFO);
        if (configFile == null) {
            // The file is not valid.
            return null;
        }
        ConfigFileGroup adHocFileGroup = new ConfigFileGroup(configFile);
        adHocModel = new SpringConfigModel(adHocFileGroup);
        file2AdHocModel.put(configFO, adHocModel);

        // We need to avoid the race condition between checking if the file is valid
        // and adding the listener.
        configFO.addFileChangeListener(listener);
        if (!configFO.isValid()) {
            file2AdHocModel.remove(configFO);
            configFO.removeFileChangeListener(listener);
            return null;
        }
        return adHocModel;
    }

    private synchronized void notifyFileDeleted(FileObject file) {
        file2AdHocModel.remove(file);
        file.removeFileChangeListener(listener);
    }

    /**
     * Listens on the deletion of config files from which ad-hoc models
     * were created.
     */
    private final class Listener extends FileChangeAdapter {

        @Override
        public void fileDeleted(FileEvent fe) {
            notifyFileDeleted(fe.getFile());
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            notifyFileDeleted(fe.getFile());
        }
    }
}
