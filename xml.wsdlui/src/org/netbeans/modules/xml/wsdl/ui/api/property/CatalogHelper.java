/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.xml.wsdl.ui.api.property;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.catalogsupport.ProjectConstants;
import org.netbeans.modules.xml.retriever.catalog.CatalogElement;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory;
import org.netbeans.modules.xml.retriever.catalog.ProjectCatalogSupport;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 */
public class CatalogHelper {

    private Project project;
    private CatalogWriteModel catalogWriteModelForProject;
    private Map<CatalogEntry, CatalogWriteModel> map;

    public CatalogHelper(Project project) {
        this.project = project;
        map = new HashMap<CatalogEntry, CatalogWriteModel>();
        try {
            if (project != null) {
                catalogWriteModelForProject = CatalogWriteModelFactory.getInstance().getCatalogWriteModelForProject(project.getProjectDirectory());
            }
        } catch (CatalogModelException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    public List<CatalogEntry> getReferencedResources(String extension) {
        if (catalogWriteModelForProject == null) return null;
        List<CatalogEntry> list = new ArrayList<CatalogEntry>();
        try {
            catalogWriteModelForProject = CatalogWriteModelFactory.getInstance().getCatalogWriteModelForProject(project.getProjectDirectory());
            if (catalogWriteModelForProject != null) {
                findSystems(catalogWriteModelForProject, list, extension);
            }
        } catch (CatalogModelException ex) {
            Exceptions.printStackTrace(ex);
        }
        return list.isEmpty() ? null : list;
    }

    public String getReferencePath(FileObject fo, String extension) {
        if (catalogWriteModelForProject == null) return null;
        List<CatalogEntry> systems = new ArrayList<CatalogEntry>();
        findSystems(catalogWriteModelForProject, systems, extension);

        for (CatalogEntry entry : systems) {
            FileObject fileObject = getFileObject(entry);
            if (fileObject != null && fileObject.equals(fo)) {
                return entry.getSource();
            }
        }
        return null;
    }

    private void findSystems(CatalogWriteModel catalog, List<CatalogEntry> systems, String extension) {
        if (catalog == null) {
            return;
        }
        extension = extension.toLowerCase();
        Collection<CatalogEntry> entries = catalog.getCatalogEntries();
        for (CatalogEntry entry : entries) {
            if (entry.getEntryType() == CatalogElement.nextCatalog) {
                findSystems(getCatalog(entry.getSource()), systems, extension);
            } else if (entry.getEntryType() == CatalogElement.system) {
                if ((entry.getSource().endsWith("." + extension) || entry.getTarget().endsWith("." + extension)) &&
                        !entry.getTarget().startsWith(ProjectConstants.NBURI_SCHEME)) {
                    systems.add(entry);
                    map.put(entry, catalog);
                }
            }
        }
    }

    public CatalogWriteModel getCatalog(String name) {
        if (name == null) {
            return null;
        }
        try {
            FileObject catalog = project.getProjectDirectory().getFileObject(name);
            if (catalog == null) {
                return null;
            }
            return CatalogWriteModelFactory.getInstance().getCatalogWriteModelForCatalogFile(catalog);
        } catch (CatalogModelException e) {
            return null;
        }
    }

    public static String getFileName(String file) {
        file = file.replaceAll("%20", " "); // NOI18N

        if (file.startsWith("file:")) { // NOI18N
            file = file.substring(5);
        }
        if (file.startsWith("/") && Utilities.isWindows()) {
            file = file.substring(1);
        }
        return file.replace("\\", "/"); // NOI18N
    }

    public FileObject getFileObject(CatalogEntry entry) {
        FileObject file = getFileFromProject(entry, entry.getTarget());
        return file;
    }

    public static FileObject getFromProject(Project project, String source) {
        ProjectCatalogSupport support = (ProjectCatalogSupport) project.getLookup().lookup(ProjectCatalogSupport.class);
        if (support == null) return null;
        try {
            URI uri = new URI(source);
            if (support.isProjectProtocol(uri)) {
                return support.resolveProjectProtocol(uri);
            }
        } catch (URISyntaxException e) {
            return null;
        }
        return null;
    }

    private FileObject getFileFromProject(CatalogEntry entry, String name) {
        //1. Absolute path
        FileObject file = FileUtil.toFileObject(FileUtil.normalizeFile(new File(name)));
        String target = entry.getTarget();

        //2. Relative path
        if (file == null) {
            CatalogWriteModel catalog = map.get(entry);
            file = catalog.getCatalogFileObject().getParent().getFileObject(name);
        }

        if (file == null && target.startsWith("src/")) { // NOI18N
            if (catalogWriteModelForProject != null) {
                file = catalogWriteModelForProject.getCatalogFileObject().getParent().getFileObject(target);
            }
        }
        if (file == null) {
            file = getFromProject(project, target);
        }
        if (file == null) {
            file = getFromRemote(entry);
        }
        return file;
    }

    private FileObject getFromRemote(CatalogEntry entry) {
        CatalogWriteModel model = map.get(entry);
        FileObject folder = model.getCatalogFileObject().getParent();

        if (!isRemoteResource(entry)) {
            return null;
        }
        if (entry.getTarget().startsWith(ProjectConstants.NBURI_SCHEME)) { // NOI18N
            return null;
        }
        String target = entry.getTarget();
        FileObject file = folder.getFileObject(target);

        if (file == null) {
            return null;
        }
        return file;
    }

    public boolean isRemoteResource(CatalogEntry entry) {
        return getFromProject(project, entry.getTarget()) == null && entry.getSource().contains("://"); // NOI18N
    }
}
