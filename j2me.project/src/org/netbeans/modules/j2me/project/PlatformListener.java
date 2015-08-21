/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2me.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2me.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Zezula
 */
final class PlatformListener implements Runnable, PropertyChangeListener {

    private static final Logger LOG = Logger.getLogger(PlatformListener.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(PlatformListener.class);
    
    private final J2MEProject project;

    private PlatformListener(@NonNull final J2MEProject project) {
        Parameters.notNull("project", project); //NOI18N
        this.project = project;
    }

    @Override
    public void propertyChange(@NonNull final PropertyChangeEvent evt) {
        final String propName = evt.getPropertyName();
        if (propName == null || ProjectProperties.PLATFORM_ACTIVE.equals(propName)) {
            run();
        }
    }

    @Override
    public void run() {
        if (J2MEProjectProperties.isPropertiesSave()) {
            J2MEProjectProperties.postSave(new Runnable() {
                @Override
                public void run() {
                    updateIfNeeded();
                }
            });
        } else if (ProjectManager.mutex().isReadAccess()) {
            RP.execute(this);
        } else if (ProjectManager.mutex().isWriteAccess()) {
            updateIfNeeded();
        } else {
            ProjectManager.mutex().writeAccess(this);
        }
    }


    void start() {
        final PropertyEvaluator eval = project.evaluator();
        eval.addPropertyChangeListener(this);
        run();
    }

    void stop() {
        final PropertyEvaluator eval = project.evaluator();
        eval.removePropertyChangeListener(this);
    }

    private void updateIfNeeded() {
        final PropertyEvaluator eval = project.evaluator();
        final AntProjectHelper helper = project.getHelper();
        final String activePlatformId = eval.getProperty(ProjectProperties.PLATFORM_ACTIVE);
        final JavaPlatform activePlatform = CommonProjectUtils.getActivePlatform(
            activePlatformId,
            J2MEProjectProperties.PLATFORM_TYPE_J2ME);
        if (activePlatform == null) {
            return;
        }
        final Collection<? extends FileObject> installFolders = activePlatform.getInstallFolders();
        if (installFolders.isEmpty()) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(
                    Level.FINE,
                    "Project {0} has broken platform.",
                    ProjectUtils.getInformation(project).getDisplayName());
            }
            return;
        }
        final FileObject installFolder = installFolders.iterator().next();
        final File installDir = FileUtil.toFile(installFolder);
        if (installDir == null) {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.log(
                    Level.WARNING,
                    "Project {0} platfrom is on non local file system.",    //NOI18N
                    ProjectUtils.getInformation(project).getDisplayName());
            }
            return;
        }
        final EditableProperties ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        final String currentPlatformHome = ep.getProperty(J2MEProjectProperties.PLATFORM_HOME);
        if (!installDir.getAbsolutePath().equals(currentPlatformHome)) {
            ep.setProperty(J2MEProjectProperties.PLATFORM_HOME, installDir.getAbsolutePath());
            helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
            try {
                ProjectManager.getDefault().saveProject(project);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @NonNull
    static PlatformListener create(@NonNull final J2MEProject project) {
        return new PlatformListener(project);
    }

}
