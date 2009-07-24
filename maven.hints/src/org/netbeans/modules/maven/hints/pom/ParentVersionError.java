/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.hints.pom;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.hints.pom.spi.Configuration;
import org.netbeans.modules.maven.hints.pom.spi.POMErrorFixProvider;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Parent;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileUtil;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class ParentVersionError implements POMErrorFixProvider {
    private Configuration configuration;
    static final String PROP_SOURCES = "sources";//NOI18N
    static final String PROP_SNAPSHOT = "snapshot";//NOI18N

    public ParentVersionError() {
        configuration = new Configuration("ParentVersionError", //NOI18N
                NbBundle.getMessage(ParentVersionError.class, "TIT_ParentVersionError"),
                NbBundle.getMessage(ParentVersionError.class, "DESC_ParentVersionError"),
                true, Configuration.HintSeverity.WARNING);
    }


    public List<ErrorDescription> getErrorsForDocument(POMModel model, Project prj) {
        assert model != null;
        List<ErrorDescription> toRet = new ArrayList<ErrorDescription>();
        if (prj == null) {
            return null;
        }
        Parent par = model.getProject().getPomParent();
        if (par == null) {
            return toRet;
        }
        boolean useSources = getConfiguration().getPreferences().getBoolean(PROP_SOURCES, true);
        boolean useSnapshot = getConfiguration().getPreferences().getBoolean(PROP_SNAPSHOT, false);
        String declaredVersion = par.getVersion();
        String relPath = par.getRelativePath();
        if (relPath == null) {
            relPath = ".." + File.separator; //NOI18N
        }
        if (relPath.endsWith("pom.xml")) {
            relPath = relPath.substring(0, relPath.length() - "pom.xml".length());
        }
        File resolvedDir = FileUtilities.resolveFilePath(FileUtil.toFile(prj.getProjectDirectory()), relPath);
        String currentVersion = null;
        boolean usedSources = false;
        if (useSources && resolvedDir.exists()) {
            try {
                Project parentPrj = ProjectManager.getDefault().findProject(FileUtil.toFileObject(resolvedDir));
                if (parentPrj != null) {
                    NbMavenProject nbprj = parentPrj.getLookup().lookup(NbMavenProject.class);
                    if (nbprj != null) { //do we have some non-maven project maybe?
                        MavenProject mav = nbprj.getMavenProject();
                        //#167711 check the coordinates to filter out parents in non-default location without relative-path elemnt
                        if (par.getGroupId().equals(mav.getGroupId()) &&
                            par.getArtifactId().equals(mav.getArtifactId())) {
                            currentVersion = mav.getVersion();
                            usedSources = true;
                        }
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if ((!useSources || currentVersion == null) && declaredVersion != null) {
            List<NBVersionInfo> infos = RepositoryQueries.getVersions(par.getGroupId(), par.getArtifactId());
            ArtifactVersion currentAV = new DefaultArtifactVersion(declaredVersion);
            for (NBVersionInfo info : infos) {
                ArtifactVersion av = new DefaultArtifactVersion(info.getVersion());
                if (!useSnapshot && info.getVersion().contains("SNAPSHOT")) { //NOI18N
                    continue;
                }
                if (currentAV.compareTo(av) < 0) {
                    currentAV = av;
                }
            }
            currentVersion = currentAV.toString();
        }

        if (currentVersion != null && !currentVersion.equals(declaredVersion)) {
            int position = par.findChildElementPosition(model.getPOMQNames().VERSION.getQName());
            Line line = NbEditorUtilities.getLine(model.getBaseDocument(), position, false);
            String message = usedSources ?
                NbBundle.getMessage(ParentVersionError.class, "TXT_ParentVersionError", currentVersion) :
                NbBundle.getMessage(ParentVersionError.class, "TXT_ParentVersionError2", currentVersion);

            toRet.add(ErrorDescriptionFactory.createErrorDescription(
                    configuration.getSeverity(configuration.getPreferences()).toEditorSeverity(),
                    message,
                    Collections.<Fix>singletonList(new SynchronizeFix(par, currentVersion, usedSources)),
                    model.getBaseDocument(), line.getLineNumber() + 1));

        }
        return toRet;

    }

    public JComponent getCustomizer(Preferences preferences) {
        return new ParentVersionErrorCustomizer(preferences);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    private static class SynchronizeFix implements Fix {
        private Parent parent;
        private String version;
        private String message;

        SynchronizeFix(Parent par, String version, boolean usedSources) {
            parent = par;
            this.version = version;
            message = usedSources ?
                NbBundle.getMessage(ParentVersionError.class, "TEXT_ParentVersionFix", version) :
                NbBundle.getMessage(ParentVersionError.class, "TEXT_ParentVersionFix2", version);
        }

        public String getText() {
            return message;
        }

        public ChangeInfo implement() throws Exception {
            ChangeInfo info = new ChangeInfo();
            POMModel mdl = parent.getModel();
            if (!mdl.getState().equals(Model.State.VALID)) {
                return info;
            }
            mdl.startTransaction();
            try {
                parent.setVersion(version);
            } finally {
                mdl.endTransaction();
            }
            return info;
        }
    }

}
