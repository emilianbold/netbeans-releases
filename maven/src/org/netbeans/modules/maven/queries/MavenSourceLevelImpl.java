/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import javax.swing.event.ChangeListener;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * maven implementation of SourceLevelQueryImplementation.
 * checks a property of maven-compiler-plugin
 * @author Milos Kleint
 */
@ProjectServiceProvider(service=SourceLevelQueryImplementation2.class, projectType="org-netbeans-modules-maven")
public class MavenSourceLevelImpl implements SourceLevelQueryImplementation2 {

    private final Project project;

    public MavenSourceLevelImpl(Project proj) {
        project = proj;
    }
    
    private String getSourceLevelString(FileObject javaFile) {
        File file = FileUtil.toFile(javaFile);
        if (file == null) {
            //#128609 something in jar?
            return null;
        }
        URI uri = Utilities.toURI(file);
        assert "file".equals(uri.getScheme());
        String goal = "compile"; //NOI18N
        NbMavenProjectImpl nbprj = project.getLookup().lookup(NbMavenProjectImpl.class);
        for (URI testuri : nbprj.getSourceRoots(true)) {
            if (uri.getPath().startsWith(testuri.getPath())) {
                goal = "testCompile"; //NOI18N
            }
        }
        for (URI testuri : nbprj.getGeneratedSourceRoots(true)) {
            if (uri.getPath().startsWith(testuri.getPath())) {
                goal = "testCompile"; //NOI18N
            }
        }
        String sourceLevel = PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS,  //NOI18N
                                                              Constants.PLUGIN_COMPILER,  //NOI18N
                                                              "source",  //NOI18N
                                                              goal,
                                                              "maven.compiler.source");
        if (sourceLevel != null) {
            return sourceLevel;
        }
        String version = PluginPropertyUtils.getPluginVersion(
                nbprj.getOriginalMavenProject(),
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER);
        if (version == null || new DefaultArtifactVersion(version).compareTo(new DefaultArtifactVersion("2.3")) >= 0) {
            return "1.5";
        } else {
            return "1.3";
        }
    }

    @Override public Result getSourceLevel(final FileObject javaFile) {
        return new ResultImpl(javaFile);
    }
    
    private class ResultImpl implements SourceLevelQueryImplementation2.Result, PropertyChangeListener {
        
        private final FileObject javaFile;
        private final ChangeSupport cs = new ChangeSupport(this);
        private final PropertyChangeListener pcl = WeakListeners.propertyChange(this, project.getLookup().lookup(NbMavenProject.class));
        
        ResultImpl(FileObject javaFile) {
            this.javaFile = javaFile;
            project.getLookup().lookup(NbMavenProject.class).addPropertyChangeListener(pcl);
        }

        @Override public String getSourceLevel() {
            return getSourceLevelString(javaFile);
        }

        @Override public void addChangeListener(ChangeListener listener) {
            cs.addChangeListener(listener);
        }

        @Override public void removeChangeListener(ChangeListener listener) {
            cs.removeChangeListener(listener);
        }

        @Override public void propertyChange(PropertyChangeEvent evt) {
            if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                cs.fireChange();
            }
        }

    }
    
}
