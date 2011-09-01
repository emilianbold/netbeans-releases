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

package org.netbeans.modules.maven;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.ArtifactFixer;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.queries.MavenFileOwnerQueryImpl;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.sonatype.aether.artifact.Artifact;

/**
 * #189442: tries to associate (usually snapshot) artifacts with their owners.
 */
@ServiceProvider(service=ArtifactFixer.class)
public class NbArtifactFixer implements ArtifactFixer {

    public @Override File resolve(Artifact artifact) {
        if (!artifact.getExtension().equals(NbMavenProject.TYPE_POM)) {
            return null;
        }
        if (!artifact.getClassifier().isEmpty()) {
            return null;
        }
        ArtifactRepository local = EmbedderFactory.getProjectEmbedder().getLocalRepository();
        if (local.getLayout() != null) { // #189807: for unknown reasons, there is no layout when running inside MavenCommandLineExecutor.run
            if (new File(local.getBasedir(), local.pathOf(new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), null, artifact.getExtension(), artifact.getClassifier(), new DefaultArtifactHandler(artifact.getExtension())))).exists()) {
                return null; // for now, we prefer the repository version when available
            }
        }
        // MavenFileOwnerQueryImpl could give us the dir location quickly, but we would still need to verify that the version matches, so loading the project is necessary.
        Project owner = MavenFileOwnerQueryImpl.getInstance().getOwner(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
        if (owner != null) {
            NbMavenProjectImpl mavenProject = owner.getLookup().lookup(NbMavenProjectImpl.class);
            if (mavenProject != null) {
                return mavenProject.getPOMFile();
            }
        }
        try {
            return createFallbackPOM(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
        } catch (IOException x) {
            Exceptions.printStackTrace(x);
            return null;
        }
    }

    public static final String FALLBACK_NAME = "F@LLB@CK";
    private static Map<String,File> fallbackPOMs = new HashMap<String,File>();
    private static synchronized File createFallbackPOM(String groupId, String artifactId, String version) throws IOException {
        String k = groupId + ':' + artifactId + ':' + version;
        File fallbackPOM = fallbackPOMs.get(k);
        if (fallbackPOM == null) {
            fallbackPOM = File.createTempFile("fallback", ".pom");
            fallbackPOM.deleteOnExit();
            PrintWriter w = new PrintWriter(fallbackPOM);
            try {
                w.println("<project>");
                w.println("<modelVersion>4.0.0</modelVersion>");
                w.println("<groupId>" + groupId + "</groupId>");
                w.println("<artifactId>" + artifactId + "</artifactId>");
                w.println("<packaging>pom</packaging>");
                w.println("<version>" + version + "</version>");
                w.println("<name>" + FALLBACK_NAME + "</name>");
                w.println("</project>");
                w.flush();
            } finally {
                w.close();
            }
            fallbackPOMs.put(k, fallbackPOM);
        }
        return fallbackPOM;
    }

}
