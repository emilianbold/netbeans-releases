/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.catalogsupport.util;

import java.net.URI;
import java.net.URISyntaxException;

import org.openide.filesystems.FileObject;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;

import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;

/**
 * Utility Class to provide project reference support.
 * @author Ajit
 */
public class ProjectReferenceUtility {
    
    /** Creates a new instance of ProjectReferenceUtility */
    private ProjectReferenceUtility() {
    }
    
    /**
     * This api adds a project reference to another project.
     * The api looks up AntArtifactProvider in referenced project's lookup,
     * and find all the build artifacts to which reference is to be created,
     * in the referencing project.
     * @param refHelper The Reference Helper of the referencing project.
     * @param refProject The referenced project.
     * @see org.netbeans.spi.project.support.ant.ReferenceHelper
     * @see org.netbeans.api.project.Project#getLookup
     * @see org.netbeans.spi.project.ant.AntArtifactProvider
     */
    public static void addProjectReference(ReferenceHelper refHelper, Project refProject) {
        AntArtifactProvider prov = (AntArtifactProvider)refProject.
                getLookup().lookup(AntArtifactProvider.class);
        if(prov!=null) {
            AntArtifact[] antArtifacts = prov.getBuildArtifacts();
            for(AntArtifact artifact:antArtifacts) {
                if(artifact.getType().equals(WebProjectConstants.ARTIFACT_TYPE_WAR)
                || artifact.getType().equals(JavaProjectConstants.ARTIFACT_TYPE_JAR)){
                    for(URI uri:artifact.getArtifactLocations()) {
                        refHelper.addReference(artifact,uri);
                    }
                }
            }
        }
    }

    /**
     * This api removes a project reference from another project.
     * The api looks up AntArtifactProvider in referenced project's lookup,
     * and removes all the build artifacts's references from the referencing project.
     * @param refHelper The Reference Helper of the referencing project.
     * @param refProject The referenced project.
     * @see org.netbeans.spi.project.support.ant.ReferenceHelper
     * @see org.netbeans.api.project.Project#getLookup
     * @see org.netbeans.spi.project.ant.AntArtifactProvider
     */
    public static void removeProjectReference(ReferenceHelper refHelper, Project refProject) {
        AntArtifactProvider prov = (AntArtifactProvider)refProject.
                getLookup().lookup(AntArtifactProvider.class);
        if(prov!=null) {
            ProjectInformation pInfo = ProjectUtils.getInformation(refProject);
            String refPrefix = "${reference."+PropertyUtils.
                    getUsablePropertyName(pInfo.getName()).replace('.', '_')+".";
            AntArtifact[] antArtifacts = prov.getBuildArtifacts();
            for(AntArtifact artifact:antArtifacts) {
                refHelper.destroyReference(refPrefix+PropertyUtils.
                        getUsablePropertyName(artifact.getID()).replace('.', '_')+"}");
            }
        }
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
            CatalogWriteModel cwm = CatalogWriteModelFactory.getInstance().
                    getCatalogWriteModelForProject(projectDirectory);
            for(CatalogEntry ce:cwm.getCatalogEntries()) {
                URI uri = new URI(ce.getTarget());
                if(catalogSupport.isProjectProtocol(uri) &&
                        refProjectName.equals(uri.getSchemeSpecificPart())) {
                    return true;
                }
            }
        } catch (URISyntaxException ex) {
        } catch (CatalogModelException ex) {
        }
        return false;
    }
}
