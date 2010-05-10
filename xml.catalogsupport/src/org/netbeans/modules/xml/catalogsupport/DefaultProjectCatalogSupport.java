/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

/*
 * DefaultProjectCatalogSupport.java
 *
 * Created on December 18, 2006, 3:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.catalogsupport;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.xml.retriever.catalog.ProjectCatalogSupport;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Ajit
 */
public class DefaultProjectCatalogSupport extends ProjectCatalogSupport {
        
    public Project project;
    public AntProjectHelper helper;
    public ReferenceHelper refHelper;
    
    /**
     * Creates a new instance of DefaultProjectCatalogSupport
     */
    public DefaultProjectCatalogSupport(Project project) {
        this(project,project.getLookup().lookup(AntProjectHelper.class),project.getLookup().lookup(ReferenceHelper.class));
    }
    
    public DefaultProjectCatalogSupport(Project project, AntProjectHelper helper,
            ReferenceHelper refHelper) {
        this.project = project;
        this.helper = helper;
        this.refHelper = refHelper;
    }

    /**
     * Be aware that the method can return null. It can happen, for example,
     * because of the specified file object doesn't relate to a project.
     * @param source
     * @return
     */
    public static DefaultProjectCatalogSupport getInstance(FileObject source) {
        Project owner = FileOwnerQuery.getOwner(source);

        if(owner == null) {
            return null;
        }
        return (DefaultProjectCatalogSupport) owner.getLookup().lookup(DefaultProjectCatalogSupport.class);
    }
    
    public boolean supportsCrossProject() {
        return helper != null;
    }
    
    public URI constructProjectProtocol(FileObject foTobeAddedInCat) {
        Project owner = FileOwnerQuery.getOwner(foTobeAddedInCat);
        if(owner!=null) {
            String ssp = getUsableProjectName(owner);
            String fragment = getRelativePath(owner.getProjectDirectory(),foTobeAddedInCat);
            try {
                return new URI(ProjectConstants.NBURI_SCHEME,ssp,fragment);
            } catch (URISyntaxException ex) {
            }
        }
        return null;
    }
    
    public boolean isProjectProtocol(URI uriStoredInCatFile) {
        return ProjectConstants.NBURI_SCHEME.equals(uriStoredInCatFile.getScheme());
    }
    
    public FileObject resolveProjectProtocol(URI uriToBeResolved) {
        if(supportsCrossProject() && isProjectProtocol(uriToBeResolved)) {
            String ssp = uriToBeResolved.getSchemeSpecificPart();
            String targetPrjRelativeRoot = helper.getProperties(
                    AntProjectHelper.PROJECT_PROPERTIES_PATH).
                    getProperty("project.".concat(ssp));
            if(targetPrjRelativeRoot!=null){
                File myPrjRoot = FileUtil.toFile(project.getProjectDirectory());
                File tgtPrjRoot = new File(myPrjRoot.toURI().resolve(targetPrjRelativeRoot));
                FileObject tgtPrjFobj = FileUtil.toFileObject(FileUtil.normalizeFile(tgtPrjRoot));

                if (tgtPrjFobj == null) {
                  return null;
                }
                return tgtPrjFobj.getFileObject(uriToBeResolved.getFragment());
            }
        }
        return null;
    }
    
    public boolean needsCatalogEntry(FileObject source, FileObject target) {
        assert source !=null && target !=null;
        // check if target belongs to different project or different source root
        if(project!=FileOwnerQuery.getOwner(target)) {
            return true;
        }

        FileObject folder = getSourceFolderByContentFile(source);
        if (folder != null && !FileUtil.isParentOf(folder,target)) {
            return true;
        }
        
        return false;
    }
    
    public URI createCatalogEntry(FileObject source, FileObject target) 
           throws IOException, CatalogModelException {
        assert source !=null && target !=null;
        CatalogWriteModel cwm = CatalogWriteModelFactory.getInstance().
                getCatalogWriteModelForProject(project.getProjectDirectory());
        assert cwm!= null;
        Project targetProject = FileOwnerQuery.getOwner(target);
        URI targetURI = null;
        URI sourceURI = null;

        try {
            sourceURI = getReferenceURI(source, target);
        } catch (URISyntaxException ex) {
            return null;
        }
        if(project != targetProject && targetProject != null) {
            if(!getProjectReferences().contains(targetProject) &&
                    supportsCrossProject()) {
                ProjectReferenceUtility.addProjectReference(project, refHelper, targetProject);
            }
            targetURI = constructProjectProtocol(target);
        } else {
            try {
                targetURI = new URI(FileUtil.toFile(target).toURI().toString());
            }
            catch (URISyntaxException ex) {
                return null;
            }
            sourceURI = targetURI;
        }
        
        try {
            if (sourceURI != null) {
                sourceURI = new URI(sourceURI.toASCIIString());
            }
            if (targetURI != null) {
                targetURI = new URI(targetURI.toASCIIString());
            }
        } catch(Exception e) {
            Exceptions.printStackTrace(e);
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "", e);
        }

        cwm.addURI(sourceURI, targetURI);

        return sourceURI;
    }
    
    public URI getReferenceURI(FileObject source, FileObject target) throws URISyntaxException {
        Project targetProject = FileOwnerQuery.getOwner(target);
        FileObject sourceFolder = getSourceFolderByContentFile(source);

        if (sourceFolder == null) {
            sourceFolder = source;
        }
        String relPathToSrcGroup = getRelativePath(source.getParent(), sourceFolder);
        String relPathToSrcGroupWithSlash = relPathToSrcGroup.trim().equals("") ? "" : 
            relPathToSrcGroup.concat("/");

        if (project != targetProject && targetProject != null) {
            FileObject folder = getSourceFolderByContentFile(targetProject, target);
        
            if (folder == null) {
                String relPathFromTgtGroup = getRelativePath(targetProject.getProjectDirectory(), target);
                return new URI(getUsableProjectName(targetProject).concat("/").concat(relPathFromTgtGroup));
            }
            String relPathFromTgtGroup = getRelativePath(folder, target);
            return new URI(getUsableProjectName(targetProject) + "/" + relPathFromTgtGroup);
        } else {
            FileObject targetSourceFolderByContentFile = getSourceFolderByContentFile(target);
        
            if (targetSourceFolderByContentFile == null) {
                targetSourceFolderByContentFile = target;
            }
            String relativePath;
            if (targetProject == null) {
                relativePath = getRelativePath(sourceFolder, target);
                relPathToSrcGroupWithSlash = "../";
            }
            else {
                relativePath = getRelativePath(targetSourceFolderByContentFile, target);
            }
            return new URI(relPathToSrcGroupWithSlash.concat(relativePath));
        }
    }
    
    public Set getProjectReferences() {
        SubprojectProvider provider = (SubprojectProvider)project.getLookup().
                lookup(SubprojectProvider.class);
        return provider.getSubprojects();
    }
    
    private FileObject getSourceFolderByContentFile(FileObject source) {
        return getSourceFolderByContentFile(project, source);
    }
    
    private static String[] sourceTypes = new String[] {
        ProjectConstants.SOURCES_TYPE_XML,
        ProjectConstants.SOURCES_TYPE_JAVA,
        ProjectConstants.TYPE_DOC_ROOT,
        ProjectConstants.TYPE_WEB_INF,
        ProjectConstants.SOURCES_TYPE_PHP,
        ProjectConstants.SOURCES_TYPE_RUBY
    };
    
    private static FileObject getSourceFolderByContentFile(Project project, FileObject source) {
        Sources sources = ProjectUtils.getSources(project);
        assert sources !=null;
        List<SourceGroup> sourceGroups = new ArrayList<SourceGroup>();
        for (String type : sourceTypes) {
            SourceGroup[] groups = sources.getSourceGroups(type);
            if (groups != null) {
                sourceGroups.addAll(Arrays.asList(groups));
            }
        }
            
        assert sourceGroups.size()>0;
        for(SourceGroup sourceGroup:sourceGroups) {
            if(FileUtil.isParentOf(sourceGroup.getRootFolder(),source))
                return sourceGroup.getRootFolder();
        }
        
        FileObject metaInf = project.getProjectDirectory().getFileObject("src/conf"); //NOI18N
        if (metaInf != null) {
            if (FileUtil.isParentOf(metaInf, source)) {
                return metaInf;
            }
        }
        return null;
    }
    
    private static String getRelativePath(FileObject source, FileObject target) {
        File sourceLocationFile = FileUtil.toFile(source);
        File targetLocationFile = FileUtil.toFile(target);
        String sourceLocation = sourceLocationFile.toURI().toString();
        String targetLocation = targetLocationFile.toURI().toString();
        StringTokenizer st1 = new StringTokenizer(sourceLocation,"/");
        StringTokenizer st2 = new StringTokenizer(targetLocation,"/");
        String relativeLoc = "";

        while (st1.hasMoreTokens() && st2.hasMoreTokens()) {
            relativeLoc = st2.nextToken();
            if (!st1.nextToken().equals(relativeLoc)) {
                break;
            }
            if(!st1.hasMoreTokens() || !st2.hasMoreTokens()) {
                // seems like one of the file is parent directory of other file
                if(st1.hasMoreElements()) {
                    // seems like target is parent of source
                    relativeLoc = "..";
                    st1.nextToken();
                } else if(st2.hasMoreTokens()) {
                    // seems like source is parent of target
                    relativeLoc = st2.nextToken();
                } else {
                    // both represent same file
                    relativeLoc = "";
                }
            }
        }
        while (st1.hasMoreTokens()) {
            relativeLoc = "../".concat(relativeLoc);
            st1.nextToken();
        }
        while(st2.hasMoreTokens()) {
            relativeLoc = relativeLoc.concat("/").concat(st2.nextToken());
        }
        return relativeLoc;
    }

    private static String getUsableProjectName(Project project) {
        return  PropertyUtils.getUsablePropertyName(ProjectUtils.getInformation
                (project).getName()).replace('.','_');
    }
    
    public boolean removeCatalogEntry(URI uri) throws IOException {
        CatalogWriteModel cwm  = null;
        try {
            cwm = CatalogWriteModelFactory.getInstance().
                    getCatalogWriteModelForProject(project.getProjectDirectory());
        } catch (CatalogModelException ex) {
            return false;
        }
        boolean entryFound = false;
        for(CatalogEntry ce : cwm.getCatalogEntries()){
            URI src = null;
            try {
                src = new URI(ce.getSource());
            } catch (URISyntaxException ex) {
                continue;
            }
            if(src.equals(uri)){
                entryFound = true;
                break;
            }
        }
        if(entryFound){
            cwm.removeURI(uri);
            return true;
        }
        return false;
    }
}
