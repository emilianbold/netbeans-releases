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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xml.catalogsupport;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.openide.filesystems.FileObject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.modules.xml.catalogsupport.ProjectConstants;

public final class ProjectReferenceUtility {
    
    private ProjectReferenceUtility() {}
    
    /**
     * This api adds a project reference to another project.
     * The api looks up AntArtifactProvider in referenced project's lookup,
     * and find all the build artifacts to which reference is to be created,
     * in the referencing project.
     * @param project The source project.
     * @param refHelper The Reference Helper of the referencing (source) project.
     * @param refProject The referenced project.
     * @see org.netbeans.spi.project.support.ant.ReferenceHelper
     * @see org.netbeans.api.project.Project#getLookup
     * @see org.netbeans.spi.project.ant.AntArtifactProvider
     */
    public static void addProjectReference(Project project, ReferenceHelper refHelper, Project refProject) {
//System.out.println();
//System.out.println("ADD ProjectReference: " + refProject);
//System.out.println();
        AntArtifactProvider prov = (AntArtifactProvider)refProject.getLookup().lookup(AntArtifactProvider.class);
//System.out.println();
//System.out.println("AntArtifactProvider: " + prov);
//System.out.println();
        if (prov == null) {
            return;
        }
        AntArtifact[] antArtifacts = prov.getBuildArtifacts();

        for(AntArtifact artifact : antArtifacts) {
//System.out.println("  see: '" + artifact.getType() + "'");
            if (artifact.getType().equals(ProjectConstants.ARTIFACT_TYPE_WAR) ||
                artifact.getType().equals(ProjectConstants.ARTIFACT_TYPE_JAR) ||
                artifact.getType().startsWith("CAPS.") // NOI18N
            ) {
                for (URI uri : artifact.getArtifactLocations()) {
                    refHelper.addReference(artifact, uri);
                }
            }
        }
        saveProject(project);
    }

    /**
     * This api removes a project reference from another project.
     * The api looks up AntArtifactProvider in referenced project's lookup,
     * and removes all the build artifacts's references from the referencing project.
     * @param project The source project.
     * @param refHelper The Reference Helper of the referencing (source) project.
     * @param refProject The referenced project.
     * @see org.netbeans.spi.project.support.ant.ReferenceHelper
     * @see org.netbeans.api.project.Project#getLookup
     * @see org.netbeans.spi.project.ant.AntArtifactProvider
     */
    public static void removeProjectReference(Project project, ReferenceHelper refHelper, Project refProject) {
        AntArtifactProvider prov = (AntArtifactProvider)refProject.getLookup().lookup(AntArtifactProvider.class);

        if (prov == null) {
            return;
        }
        ProjectInformation pInfo = ProjectUtils.getInformation(refProject);
        String refPrefix = "${reference."+PropertyUtils.getUsablePropertyName(pInfo.getName()).replace('.', '_')+".";
        AntArtifact[] antArtifacts = prov.getBuildArtifacts();

        for(AntArtifact artifact:antArtifacts) {
            refHelper.destroyReference(refPrefix+PropertyUtils.
                    getUsablePropertyName(artifact.getID()).replace('.', '_')+"}");
        }
        saveProject(project);
    }
    
    /**
     * This api checks if  a project is referenced in the catalog of another project.
     * The api looks up the catalog entries in target project and returns true,
     * if any of the entry points to the refProject
     * @param targetProject The target project which may have references,
     *        to refProject, in its catalog.
     * @param refProject The referenced project.
     */
    public static boolean hasProjectReferenceInCatalog(Project targetProject, Project refProject) {
        FileObject projectDirectory = targetProject.getProjectDirectory();
        DefaultProjectCatalogSupport catalogSupport =
                DefaultProjectCatalogSupport.getInstance(projectDirectory);
        ProjectInformation pInfo = ProjectUtils.getInformation(refProject);
        String refProjectName = PropertyUtils.getUsablePropertyName(
                pInfo.getName()).replace('.', '_');
        try {
            CatalogWriteModel cwm = CatalogWriteModelFactory.getInstance().getCatalogWriteModelForProject(projectDirectory);

            for(CatalogEntry ce : cwm.getCatalogEntries()) {
                if (ce == null) {
                    continue;
                }
                if (ce.getTarget() == null) {
                    continue;
                }
                URI uri = new URI(ce.getTarget());

                if( catalogSupport.isProjectProtocol(uri) && refProjectName.equals(uri.getSchemeSpecificPart())) {
                    return true;
                }
            }
        } catch (URISyntaxException ex) {
        } catch (CatalogModelException ex) {
        }
        return false;
    }

    private static void saveProject(Project project) {
        try {
            ProjectManager.getDefault().saveProject(project);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
