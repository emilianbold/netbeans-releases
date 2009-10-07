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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.compapp.projects.common;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.compapp.projects.common.CatalogWSDL.Entry;
import org.netbeans.modules.compapp.projects.common.CatalogWSDL.EntryType;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.modules.xml.catalogsupport.ProjectConstants;
import org.netbeans.modules.xml.catalogsupport.util.ProjectReferenceUtility;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory;
import org.netbeans.modules.xml.retriever.catalog.ProjectCatalogSupport;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * This class represents the main interface to support the implicit namespace 
 * reference resolver in SOA projects based on composite application project
 * infrastructure. 
 * <p>
 * This class implements ProjectCatalogSupport to support cross project wsdl/xsd
 * import references as well as the implicit namespace references corresponding
 * to the wsdl/xsd files.
 * <p>
 * Each SOA project(Service Unit deployment project for SE or BC), would register
 * this implemenation in the Project's lookup in addition to the DefaultProjectCatalogSupport
 * from the catalog support module to resolve the implicit references as well as the 
 * explicit references of the wsdl/xsd files.
 * <p>
 * SU project should use the ImplicitCatalogSupport(Project, AntProjectHelper,ReferenceHelper)
 * constructor to create the instance to register it in the project's lookup and use 
 * ImplicitCatalogSupport.getInstance(Project) to get the instance of this class which looks up
 * the instance from the project's lookup or create a new one to the user. 
 * 
 * <p><blockquote><pre>
 * // registering the instance in project's lookup
 * Lookups.fixed(new Object[] {
 *  ...,
 *  new DefaultProjectCatalogSupport(prj, antHelper, refHelper),  
 *  ...,  
 *  new ImplicitCatalogSupport(prj, antHelper, refHelper)
 * });
 * </blockquote></pre></p>
 * <p><blockquote><pre>
 * // get the instance of ImplicitCatalogSupport instance
 * // using project object
 * ImplicitCatalogSupport catSupport = ImplicitCatalogSupport.getInstance(prj);
 * // or source file object
 * ImplicitCatalogSupport catSupport = ImplicitCatalogSupport.getInstance(myXmlFO);
 * // add the implicit reference to the catalog support
 * URI uriRef = catSupport.createImplicitCatalogEntry(...);
 * 
 * // resolve the implicit reference to the file object 
 * FileObject xsdFO = 
 * catSupport.resolveImplicitReference("http://ns/mynamespace", EntryType.XSD);
 *                     
 * </blockquote></pre></p>
 * <p>
 * @see CatalogWSDL
 * @author chikkala
 */
public class ImplicitCatalogSupport extends ProjectCatalogSupport {

    /** logger */
    private static final Logger sLogger = Logger.getLogger(ImplicitCatalogSupport.class.getName());
    /** project that registered this instance in its lookup*/
    private Project mProject;
    /** ant project helper of the project */
    private AntProjectHelper mAntPrjHelper;
    /** project reference helper of the project */
    private ReferenceHelper mPrjRefHelper;

    /**
     * Constructor that will be used in adding this class instance in the project's lookup
     * to support the Implicit wsdl/xsd references in this project's sources. 
     * @param project
     * @param helper
     * @param refHelper
     */
    public ImplicitCatalogSupport(Project project, AntProjectHelper helper,
            ReferenceHelper refHelper) {
        assert project != null;
        this.mProject = project;
        this.mAntPrjHelper = helper;
        this.mPrjRefHelper = refHelper;
    }

    /**
     * Constructor that will be used to create a default instance in #getInstance
     * implemenation if user did not register the instance of this class in project 
     * lookup support on a particular project.
     * @see #getInstance 
     * @param project
     */
    protected ImplicitCatalogSupport(Project project) {
        this(project, null, null);
    }

    /**
     * this will try to lookup the instance from the project's lookup. if not
     * existing, will create a new instance. 
     * @param prj
     * @return ImplicitCatalogSupport
     */
    public static ImplicitCatalogSupport getInstance(Project prj) {
        Project owner = prj;
        if (owner != null) {
            ImplicitCatalogSupport support =
                    (ImplicitCatalogSupport) owner.getLookup().lookup(ImplicitCatalogSupport.class);
            if (support != null) {
                return support;
            }
        }
        return new ImplicitCatalogSupport(owner);
    }

    /**
     * DefaultProjectCatalogSupport equivalent creation method.
     * @param source
     * @return ImplicitCatalogSupport
     */
    public static ImplicitCatalogSupport getInstance(FileObject source) {
        return getInstance(FileOwnerQuery.getOwner(source));
    }

    /**
     * used to delegate most of the implementation to the DefaultProjectCatalog.
     * @return DefaultProjectCatalogSupport
     */
    protected DefaultProjectCatalogSupport getDefaultProjectCatalogSupport() {
        Project owner = this.mProject;
        if (owner != null) {
            DefaultProjectCatalogSupport support = (DefaultProjectCatalogSupport) owner.getLookup().lookup(DefaultProjectCatalogSupport.class);
            if (support != null) {
                return support;
            }
        }
        if (this.mAntPrjHelper != null) {
            return new DefaultProjectCatalogSupport(owner, this.mAntPrjHelper, this.mPrjRefHelper);
        } else {
            return new DefaultProjectCatalogSupport(owner);
        }
    }

    /**
     * delegates the method call to the default project catalog support
     * @see org.netbeans.modules.xml.catalogsupport.ProjectCatalogSupport
     * @param foTobeAddedInCat
     * @return
     */
    @Override
    public URI constructProjectProtocol(FileObject foTobeAddedInCat) {
        return getDefaultProjectCatalogSupport().constructProjectProtocol(foTobeAddedInCat);
    }

    /**
     * delegates the method call to the default project catalog support
     * @see org.netbeans.modules.xml.catalogsupport.ProjectCatalogSupport
     * @param uriStoredInCatFile
     * @return
     */
    @Override
    public boolean isProjectProtocol(URI uriStoredInCatFile) {
        return getDefaultProjectCatalogSupport().isProjectProtocol(uriStoredInCatFile);
    }

    /**
     * delegates the method call to the default project catalog support
     * @see org.netbeans.modules.xml.catalogsupport.ProjectCatalogSupport
     * @param uriToBeResolved
     * @return
     */
    @Override
    public FileObject resolveProjectProtocol(URI uriToBeResolved) {
        return getDefaultProjectCatalogSupport().resolveProjectProtocol(uriToBeResolved);
    }

    /**
     * This generates the catalog entry relative to the source file or relative
     * to the project. @see #createCatalogEntry(FileObject) for more details on
     * project relative catalog entry.
     * @param source. If non null, creates a source relative entry. If null,
     * creates a project relative entry.
     * @param target target file object to which the reference is created.
     * @return uri reference of the entry created.
     * @throws org.netbeans.modules.xml.xam.locator.CatalogModelException
     * @throws java.io.IOException
     */
    @Override
    public URI createCatalogEntry(FileObject source, FileObject target) throws CatalogModelException, IOException {
        if (source == null) {
            return createCatalogEntry(target);
        } else {
            return getDefaultProjectCatalogSupport().createCatalogEntry(source, target);
        }
    }

    /**
     * removes the specified systemid uri entry
     * @see org.netbeans.modules.xml.catalogsupport.ProjectCatalogSupport
     * @param uri system id uri entry in the catalog.
     * @return true if removed, false if it is not remove
     * @throws java.io.IOException
     */
    @Override
    public boolean removeCatalogEntry(URI uri) throws IOException {
        return getDefaultProjectCatalogSupport().removeCatalogEntry(uri);
    }

    /**
     * creates a target entry into the catalog.xml. The target entry generated 
     * will be project relative either external or internal reference.
     * @param target
     * @return the systemId uri that was entered in the catalog for target.
     * @throws org.netbeans.modules.xml.xam.locator.CatalogModelException
     * @throws java.io.IOException
     */
    public URI createCatalogEntry(FileObject target) throws CatalogModelException, IOException {
        assert target != null;
        CatalogWriteModel cwm = CatalogWriteModelFactory.getInstance().
                getCatalogWriteModelForProject(this.mProject.getProjectDirectory());
        assert cwm != null;
        Project targetProject = FileOwnerQuery.getOwner(target);
        URI systemId = null;
        URI uriReference = null;

        systemId = generateSystemID(targetProject, target);
        uriReference = generateURIReference(targetProject, target);

        if (systemId == null || uriReference == null) {
            //TODO log info. why they are null.
            return null;
        }
        cwm.addURI(systemId, uriReference);
        return systemId;
    }

    /**
     * adds implicit catalog entry to the catalog support. An entry for the
     * target with the namespace specified will be added to the catalog.xml and 
     * catalog.wsdl to resolve this implicit catalog entry.
     * 
     * @param namespace
     * @param target
     * @param type
     * @return
     * @throws org.netbeans.modules.xml.xam.locator.CatalogModelException
     * @throws java.io.IOException
     */
    public URI createImplicitCatalogEntry(String namespace, FileObject target, CatalogWSDL.EntryType type) throws CatalogModelException, IOException {
        // update catalog.xml
        URI systemId = createCatalogEntry(target);
        // update catalog.wsdl        
        String location = systemId.toASCIIString();
        Entry entry = null;
        // create catalog wsdl entry object
        if (EntryType.WSDL.equals(type)) {
            entry = Entry.createWSDLEntry(namespace, location);
        } else if (EntryType.XSD.equals(type)) {
            entry = Entry.createXSDEntry(namespace, location);
        } else {
            entry = null;
        }
        // add the entry to catalog wsdl and save.
        if (entry != null) {
            CatalogWSDL catWSDL = CatalogWSDL.loadCatalogWSDL(this.mProject);
            catWSDL.addEntry(entry);
            CatalogWSDL.saveCatalogWSDL(catWSDL, this.mProject);
        } else {
            //Log
            sLogger.fine("#### NO ENTRY WAS ADDED IN CatalogWSDL for namespace " + namespace);
        }
        return systemId;
    }

    /**
     * removes implicit catalog entry from the catalog support. This only removes
     * the catalog entry correpsonds to the namespace from the catalog.wsdl. It
     * will not remove the entry in the catalog.xml corresponds to this namespace
     * as that entry might be used by some other explicit refernce. To cleanup the
     * unused catalog.xml entries users can use the project customizer.
     * 
     * @param namespace
     * @param type
     * @return
     * @throws org.netbeans.modules.xml.xam.locator.CatalogModelException
     * @throws java.io.IOException
     */
    public boolean removeImplicitCatalogEntry(String namespace, CatalogWSDL.EntryType type) throws CatalogModelException, IOException {
        CatalogWSDL catWSDL = CatalogWSDL.loadCatalogWSDL(this.mProject);
        Entry entry = catWSDL.getEntry(type, namespace, null);
        boolean removed = catWSDL.removeEntry(entry);
        CatalogWSDL.saveCatalogWSDL(catWSDL, this.mProject);
        return removed;
    }

    /**
     * resolves the implicit reference to the file object using the implicit
     * catalog support. It uses both the catalog.wsdl and the catalog.xml to 
     * resolve the reference. If the type is null, it returns the fileobject
     * corresponding to the first entry with the namespace (wsdl or xsd).
     * 
     * @param namespace  namespace to lookup
     * @param type wsdl or xsd file type correpsonding to the namespace
     * @return fileobject correpsonding to the namespace or null
     * @throws org.netbeans.modules.xml.xam.locator.CatalogModelException
     * @throws java.io.IOException
     */
    public FileObject resolveImplicitReference(String namespace, CatalogWSDL.EntryType type) throws CatalogModelException, IOException {
        FileObject refFO = null;
        CatalogWSDL catWSDL = CatalogWSDL.loadCatalogWSDL(this.mProject);
        Entry entry = null;
        if ( type == null ) {
            List<Entry> entries = catWSDL.getEntries(namespace);
            if (entries.size() > 0 ) {
                entry = entries.get(0);
            }
        } else {
            entry = catWSDL.getEntry(type, namespace, null);
        }
        if ( entry != null ) {
            refFO = resolveImplicitReference(entry.getLocation());
        }
        return refFO;
    }

    /**
     * resolves the implicit reference to the file object using the implicit
     * catalog support. It uses both the catalog.wsdl and the catalog.xml to 
     * resolve the reference.
     * 
     * @param location location in the catalog entry
     * @return fileobject correpsonding to the namespace or null.
     * @throws org.netbeans.modules.xml.xam.locator.CatalogModelException
     * @throws java.io.IOException
     */
    public FileObject resolveImplicitReference(String location) throws CatalogModelException, IOException {
        FileObject refFO = null;
        if ( location == null ) {
            return refFO;
        }
        try {            
            URI systemIdUri = new URI(location);
            CatalogWriteModel cwm =
                    CatalogWriteModelFactory.getInstance().getCatalogWriteModelForProject(
                    this.mProject.getProjectDirectory());
            URI uriReference = cwm.searchURI(systemIdUri);

            if (uriReference != null) {
                refFO = resolveProjectProtocol(uriReference);
                if (refFO == null) {
                    // it is not project protocol. so resolve it as project relative uri.
                    // uriReference.re
                    refFO = resolveRelativeReference(uriReference);
                }
                if (refFO == null) {
                    sLogger.fine("Can not find file object for SystemId: " + systemIdUri + " URIReference: " + uriReference + "in catalog.xml");
                }
            } else {
                sLogger.fine("Can not find the URI reference for SystemId" + systemIdUri + "in catalog.xml");
            }
        } catch (URISyntaxException ex) {
            sLogger.log(Level.FINE, ex.getMessage(), ex);
        }
        return refFO;
    }

    /**
     * checks if this instance supports the cross project references
     * @return
     */
    public boolean supportsCrossProject() {
        return this.mAntPrjHelper != null;
    }

    /**
     * returns the sub project references 
     * @return
     */
    public Set getProjectReferences() {
        SubprojectProvider provider = (SubprojectProvider) this.mProject.getLookup().
                lookup(SubprojectProvider.class);
        return provider.getSubprojects();
    }

    /**
     * returns the file object in the same project represented by the relative
     * uriReference from the project directory.
     * @param uriReference
     * @return
     */
    protected FileObject resolveRelativeReference(URI uriReference) {
        File myPrjRoot = FileUtil.toFile(this.mProject.getProjectDirectory());
        File refFile = new File(myPrjRoot.toURI().resolve(uriReference));
        FileObject refFO = FileUtil.toFileObject(FileUtil.normalizeFile(refFile));
        return refFO;
    }

    /**
     * generates the URI references w.r.t the target project for both internal
     * or external file targets.
     * 
     * @param targetProject
     * @param target
     * @return
     */
    protected URI generateURIReference(Project targetProject, FileObject target) {
        assert targetProject != null && target != null;

        if (this.mProject != targetProject) {
            // external reference. generate uri reference with project protocol
            if (!getProjectReferences().contains(targetProject) &&
                    supportsCrossProject()) {
                ProjectReferenceUtility.addProjectReference(this.mPrjRefHelper, targetProject);
            }
            return constructProjectProtocol(target);
        } else {
            try {
                // internal reference. generate the relative uri w.r.t. project root
                String relativePath = FileUtil.getRelativePath(this.mProject.getProjectDirectory(), target);
                return new URI(relativePath);
            } catch (URISyntaxException ex) {
                //TODO: log the exception
                sLogger.log(Level.FINE, ex.getMessage(), ex);
                return null;
            }
        }
    }

    /**
     * generates the system id for the target file within or outside the target
     * project w.r.t. the target project.
     * 
     * @param targetProject
     * @param target
     * @return
     */
    protected URI generateSystemID(Project targetProject, FileObject target) {
        assert targetProject != null && target != null;
        try {
            if (this.mProject != targetProject) {

                // external reference. generate w.r.t. external project source root.
                FileObject targetSourceFolder = getSourceFolder(targetProject, target);
                String projectName = getUsableProjectName(targetProject);
                String relativePath = FileUtil.getRelativePath(targetSourceFolder, target);
                return new URI(projectName + "/" + relativePath);
            } else {
                // internal reference. generate w.r.t. source root
                FileObject targetSourceFolder = getSourceFolder(target);
                if (targetSourceFolder == null) {
                    throw new IllegalArgumentException(target.getPath() + " is not in project source"); //NOI18N
                }
                String relativePath = FileUtil.getRelativePath(targetSourceFolder, target);
                return new URI(relativePath);
            }
        } catch (URISyntaxException ex) {
            //TODO: log ex
            sLogger.log(Level.FINE, ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * usable project name
     * @param project
     * @return
     */
    private static String getUsableProjectName(Project project) {
        return PropertyUtils.getUsablePropertyName(ProjectUtils.getInformation(project).getName()).replace('.', '_');
    }

    /**
     * return the source folder of the source file.
     * @param source
     * @return
     */
    private FileObject getSourceFolder(FileObject source) {
        return getSourceFolder(this.mProject, source);
    }
    /** supported sources from which a file reference can be resolved */
    private static String[] sourceTypes = new String[]{
        ProjectConstants.SOURCES_TYPE_XML,
        ProjectConstants.SOURCES_TYPE_JAVA,
        ProjectConstants.TYPE_DOC_ROOT,
        ProjectConstants.TYPE_WEB_INF
    };

    /**
     * 
     * @param project
     * @param source
     * @return
     */
    private static FileObject getSourceFolder(Project project, FileObject source) {
        Sources sources = ProjectUtils.getSources(project);
        assert sources != null;
        ArrayList<SourceGroup> sourceGroups = new ArrayList<SourceGroup>();
        for (String type : sourceTypes) {
            SourceGroup[] groups = sources.getSourceGroups(type);
            if (groups != null) {
                sourceGroups.addAll(Arrays.asList(groups));
            }
        }

        assert sourceGroups.size() > 0;
        for (SourceGroup sourceGroup : sourceGroups) {
            if (FileUtil.isParentOf(sourceGroup.getRootFolder(), source)) {
                return sourceGroup.getRootFolder();
            }
        }

        FileObject metaInf = project.getProjectDirectory().getFileObject("src/conf"); //NOI18N
        if (metaInf != null) {
            if (FileUtil.isParentOf(metaInf, source)) {
                return metaInf;
            }
        }
        return null;
    }
}
