/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.ui.customizer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.web.clientproject.ClientSideConfigurationProvider;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectConfigurationImplementation;
import org.netbeans.modules.web.clientproject.ui.JavaScriptLibrarySelection;
import org.netbeans.modules.web.clientproject.ui.JavaScriptLibrarySelection.SelectedLibrary;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 *
 * @author Jan Becicka
 */
final class ClientSideProjectProperties {

    private static final Logger LOGGER = Logger.getLogger(ClientSideProjectProperties.class.getName());

    private final ClientSideProject project;
    private final List<JavaScriptLibrarySelection.SelectedLibrary> newJsLibraries = new CopyOnWriteArrayList<JavaScriptLibrarySelection.SelectedLibrary>();

    private volatile String jsLibFolder = null;


    public ClientSideProjectProperties(ClientSideProject project) {
        this.project = project;
    }

    public void save() {
        try {
            // store properties
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws IOException {
                    saveConfig();
                    addNewJsLibraries();
                    ProjectManager.getDefault().saveProject(project);
                    return null;
                }
            });
        } catch (MutexException e) {
            LOGGER.log(Level.WARNING, null, e.getException());
        }
    }

    void saveConfig() {
        assert ProjectManager.mutex().isWriteAccess() : "Write mutex required"; //NOI18N
        for (ClientProjectConfigurationImplementation config : project.getLookup().lookup(ClientSideConfigurationProvider.class).getConfigurations()) {
            config.save();
        }
    }

    void addNewJsLibraries() throws IOException {
        if (jsLibFolder != null && !newJsLibraries.isEmpty()) {
            ClientSideProjectUtilities.applyJsLibraries(newJsLibraries, jsLibFolder, project.getSiteRootFolder(), null);
        }
    }

    public ClientSideProject getProject() {
        return project;
    }

    public void setNewJsLibraries(List<SelectedLibrary> newJsLibraries) {
        assert newJsLibraries != null;
        // not needed to be locked, called always by just one caller
        this.newJsLibraries.clear();
        this.newJsLibraries.addAll(newJsLibraries);
    }

    public void setJsLibFolder(String jsLibFolder) {
        assert jsLibFolder != null;
        this.jsLibFolder = jsLibFolder;
    }

}
