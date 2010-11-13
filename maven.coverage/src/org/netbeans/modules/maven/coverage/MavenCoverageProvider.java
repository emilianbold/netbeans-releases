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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.coverage;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.text.Document;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.ReportPlugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProvider;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageDetails;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.spi.project.ProjectServiceProvider;

@ProjectServiceProvider(service=CoverageProvider.class, projectType="org-netbeans-modules-maven") // not limited to a packaging
public class MavenCoverageProvider implements CoverageProvider {

    private static final String GROUP_COBERTURA = "org.codehaus.mojo"; // NOI18N
    private static final String ARTIFACT_COBERTURA = "cobertura-maven-plugin"; // NOI18N
    private static final String GROUP_SITE = "org.apache.maven.plugins"; // NOI18N
    private static final String ARTIFACT_SITE = "maven-site-plugin"; // NOI18N

    private final Project p;

    public MavenCoverageProvider(Project p) {
        this.p = p;
    }

    public @Override boolean supportsHitCounts() {
        return true;
    }

    public @Override boolean supportsAggregation() {
        return false;
    }

    @SuppressWarnings("deprecation")
    public @Override boolean isEnabled() {
        NbMavenProject prj = p.getLookup().lookup(NbMavenProject.class);
        if (prj == null) {
            return false;
        }
        if (PluginPropertyUtils.getPluginVersion(prj.getMavenProject(), GROUP_COBERTURA, ARTIFACT_COBERTURA) != null) {
            // For whatever reason, was configured as a direct build plugin... fine.
            return true;
        }
        // Maven 3.x configuration:
        for (Plugin plug : prj.getMavenProject().getBuildPlugins()) {
            if (GROUP_SITE.equals(plug.getGroupId()) && ARTIFACT_SITE.equals(plug.getArtifactId())) {
                Xpp3Dom cfg = (Xpp3Dom) plug.getConfiguration(); // MNG-4862
                if (cfg == null) {
                    continue;
                }
                Xpp3Dom reportPlugins = cfg.getChild("reportPlugins"); // NOI18N
                if (reportPlugins == null) {
                    continue;
                }
                for (Xpp3Dom plugin : reportPlugins.getChildren("plugin")) { // NOI18N
                    Xpp3Dom groupId = plugin.getChild("groupId"); // NOI18N
                    if (groupId == null) {
                        continue;
                    }
                    Xpp3Dom artifactId = plugin.getChild("artifactId"); // NOI18N
                    if (artifactId == null) {
                        continue;
                    }
                    if (GROUP_COBERTURA.equals(groupId.getValue()) && ARTIFACT_COBERTURA.equals(artifactId.getValue())) {
                        return true;
                    }
                }
            }
        }
        // Maven 2.x configuration:
        for (ReportPlugin plug : prj.getMavenProject().getReportPlugins()) {
            if (GROUP_COBERTURA.equals(plug.getGroupId()) && ARTIFACT_COBERTURA.equals(plug.getArtifactId())) {
                return true;
            }
        }
        // In fact you _could_ just run the plugin directly here... but perhaps the user did not want to do so.
        return false;
    }

    public @Override boolean isAggregating() {
        throw new UnsupportedOperationException();
    }

    public @Override void setAggregating(boolean aggregating) {
        throw new UnsupportedOperationException();
    }

    public @Override Set<String> getMimeTypes() {
        return Collections.singleton("text/x-java");
    }

    public @Override void setEnabled(boolean enabled) {
        // XXX add plugin configuration here if not already present
    }

    public @Override void clear() {
        // XXX run clean goal
    }
    
    public @Override FileCoverageDetails getDetails(org.openide.filesystems.FileObject fo, Document doc) {
        // XXX
        throw new UnsupportedOperationException();
    }
    
    public @Override List<FileCoverageSummary> getResults() {
        // XXX
        throw new UnsupportedOperationException();
    }
    
    public @Override String getTestAllAction() {
        // XXX mvn -DskipTests=false -Dcobertura.report.format=xml cobertura:cobertura
        throw new UnsupportedOperationException();
        // XXX and Test button runs COMMAND_TEST_SINGLE on file, which is not good here
    }

}
