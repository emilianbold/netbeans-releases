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
 * Contributor(s): theanuradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.indexer.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.InvalidProjectModelException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import hidden.org.codehaus.plexus.util.IOUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.embedder.MavenEmbedder;

/**
 *
 * @author Anuradha G
 */
public final class RepositoryUtil {

    private static Logger LOGGER = Logger.getLogger(RepositoryUtil.class.getName());
    
    private RepositoryUtil() {
    }

    public static Artifact createArtifact(NBVersionInfo info) {
        return createArtifact(info, null);
    }

    public static Artifact createJavadocArtifact(NBVersionInfo info) {
        return createArtifact(info, "javadoc"); //NOI18N
    }

    private static Artifact createArtifact(NBVersionInfo info, String classifier) {
        Artifact art;
        MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
        if (info.getClassifier() != null || classifier != null) {
            art = online.createArtifactWithClassifier(info.getGroupId(),
                    info.getArtifactId(),
                    info.getVersion(),
                    info.getType() != null ? info.getType() : "jar", //NOI18N
                    classifier == null ? info.getClassifier() : classifier);
        } else {
            art = online.createArtifact(info.getGroupId(),
                    info.getArtifactId(),
                    info.getVersion(),
                    null,
                    info.getType() != null ? info.getType() : "jar"); //NOI18N
        }
        ArtifactRepository repo = online.getLocalRepository();
        String localPath = repo.pathOf(art);
        art.setFile(new File(repo.getBasedir(), localPath));

        return art;
    }

    public static String calculateMD5Checksum(File file) throws IOException {
        byte[] buffer = readFile(file);
        String md5sum = DigestUtils.md5Hex(buffer);
        return md5sum;
    }
    
    public static String calculateSHA1Checksum(File file) throws IOException {
        byte[] buffer = readFile(file);
        String sha1sum = DigestUtils.shaHex(buffer);
        return sha1sum;
    }

    static byte[] readFile(File file) throws IOException {

        InputStream is = null; 
        byte[] bytes = new byte[(int) file.length()];
        try {
            is = new FileInputStream(file);


        int offset = 0;
        int numRead = 0;

        while (offset < bytes.length &&
                (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {

            offset += numRead;
        }
        } finally {
            IOUtil.close(is);
        }

        return bytes;
    }

    /**
     *
     * @param grId
     * @param artId
     * @param ver
     * @param repository
     * @return
     * @deprecated don't use, to be moved out of public packages.
     */
    public @Deprecated static MavenProject readMavenProject(String grId, String artId, String ver, ArtifactRepository repository) {
        MavenProject mavenProject = null;
        try {
            // we need to use the online embedder as the project one never
            // puts anything in the local repository, thus not resolving dependencies.
            //mkleint: this is somewhat strange thing to do for indexing remote repositories
            // via the maven-repo-utils CLI tool..
            MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
            ArtifactFactory artifactFactory = (ArtifactFactory) online.getPlexusContainer().lookup(ArtifactFactory.class);
            Artifact projectArtifact = artifactFactory.createProjectArtifact(
                    grId,
                    artId,
                    ver,
                    null);

            MavenProjectBuilder builder = (MavenProjectBuilder) online.getPlexusContainer().lookup(MavenProjectBuilder.class);
            mavenProject = builder.buildFromRepository(projectArtifact, new ArrayList(), repository);

        } catch (InvalidProjectModelException ex) {
            //ignore nexus is falling ???
            LOGGER.log(Level.FINE, "Failed to load project model from repository.", ex);
        } catch (ProjectBuildingException ex) {
            LOGGER.log(Level.FINE, "Failed to load project model from repository.", ex);
        } catch (Exception exception) {
            LOGGER.log(Level.FINE, "Failed to load project model from repository.", exception);
        }
        return mavenProject;
    }
}
