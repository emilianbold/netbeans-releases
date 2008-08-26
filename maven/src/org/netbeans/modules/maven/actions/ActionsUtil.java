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
package org.netbeans.modules.maven.actions;

import java.io.File;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.extension.ExtensionScanningException;
import org.apache.maven.project.InvalidProjectModelException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.reactor.MavenExecutionException;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.openide.util.Exceptions;

/**
 *
 * @author Anuradha G (anuradha@codehaus.org)
 */
public class ActionsUtil {

    
    
    public static MavenProject readMavenProject(Artifact artifact) {
        MavenProject mavenProject = null;

        String absolutePath = artifact.getFile().getAbsolutePath();
        String extension = artifact.getArtifactHandler().getExtension();

        String pomPath = absolutePath.substring(0, absolutePath.length() - extension.length());
        pomPath += "pom";//NOI18N
        File file = new File(pomPath);
        if (file.exists()) {
            try {

                mavenProject = EmbedderFactory.getProjectEmbedder().
                        readProject(file);

            } catch (InvalidProjectModelException ex) {
                //ignore nexus is falling ???
            } catch (ProjectBuildingException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExtensionScanningException ex) {
                Exceptions.printStackTrace(ex);
            } catch (MavenExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }

        }

        return mavenProject;
    }
}
