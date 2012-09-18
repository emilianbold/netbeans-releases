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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.web.clientproject.ClientSideConfigurationProvider;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.api.WebClientLibraryManager;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectConfigurationImplementation;
import org.netbeans.modules.web.clientproject.ui.JavaScriptLibrarySelection;
import org.netbeans.modules.web.clientproject.ui.JavaScriptLibrarySelection.SelectedLibrary;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;

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

    @NbBundle.Messages({
        "ClientSideProjectProperties.jsLibs.downloading=Downloading selected JavaScript libraries...",
        "# {0} - names of JS libraries",
        "ClientSideProjectProperties.error.jsLibs=<html><b>These JavaScript libraries failed to download:</b><br><br>{0}<br><br>"
            + "<i>More information can be found in IDE log.</i>"
    })
    void addNewJsLibraries() throws IOException {
        if (jsLibFolder != null && !newJsLibraries.isEmpty()) {
            ProgressHandle progressHandle = ProgressHandleFactory.createHandle(Bundle.ClientSideProjectProperties_jsLibs_downloading());
            progressHandle.start();
            try {
                List<SelectedLibrary> failedLibs = ClientSideProjectUtilities.applyJsLibraries(newJsLibraries, jsLibFolder, project.getSiteRootFolder(), progressHandle);
                LOGGER.log(Level.INFO, "Failed download of JS libraries: {0}", failedLibs);
                if (!failedLibs.isEmpty()) {
                    errorOccured(Bundle.ClientSideProjectProperties_error_jsLibs(joinStrings(getLibraryNames(failedLibs), "<br>"))); // NOI18N
                }
            } finally {
                progressHandle.finish();
            }
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

    private static void errorOccured(String message) {
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
    }

    private List<String> getLibraryNames(List<SelectedLibrary> libraries) {
        List<String> names = new ArrayList<String>(libraries.size());
        for (JavaScriptLibrarySelection.SelectedLibrary selectedLibrary : libraries) {
            JavaScriptLibrarySelection.LibraryVersion libraryVersion = selectedLibrary.getLibraryVersion();
            Library library = libraryVersion.getLibrary();
            String name = library.getProperties().get(WebClientLibraryManager.PROPERTY_REAL_DISPLAY_NAME);
            names.add(name);
        }
        return names;
    }

    private static String joinStrings(Collection<String> strings, String glue) {
        StringBuilder sb = new StringBuilder(200);
        for (String string : strings) {
            if (sb.length() > 0) {
                sb.append(glue);
            }
            sb.append(string);
        }
        return sb.toString();
    }

}
