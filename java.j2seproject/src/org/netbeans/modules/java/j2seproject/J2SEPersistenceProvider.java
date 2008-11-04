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

package org.netbeans.modules.java.j2seproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
//retouche:
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
//import org.netbeans.modules.j2ee.metadata.ClassPathSupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScopes;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeFactory;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeImplementation;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeProvider;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceLocationProvider;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeFactory;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeImplementation;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeProvider;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopesProvider;
import org.netbeans.modules.j2ee.persistence.spi.support.EntityMappingsMetadataModelHelper;
import org.netbeans.modules.j2ee.persistence.spi.support.PersistenceScopesHelper;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Andrei Badea
 */
public class J2SEPersistenceProvider implements PersistenceLocationProvider, PersistenceScopeProvider, PersistenceScopesProvider, EntityClassScopeProvider, PropertyChangeListener {

    private final J2SEProject project;
    private final ClassPathProviderImpl cpProvider;

    private final ScopeImpl scopeImpl = new ScopeImpl();
    private final PersistenceScope persistenceScope = PersistenceScopeFactory.createPersistenceScope(scopeImpl);
    private final EntityClassScope entityClassScope = EntityClassScopeFactory.createEntityClassScope(scopeImpl);

    private final PersistenceScopesHelper scopesHelper = new PersistenceScopesHelper();
    private final EntityMappingsMetadataModelHelper modelHelper;

    private ClassPath projectSourcesClassPath;

    public J2SEPersistenceProvider(J2SEProject project, ClassPathProviderImpl cpProvider) {
        this.project = project;
        this.cpProvider = cpProvider;
        modelHelper = createEntityMappingsHelper();
        project.getSourceRoots().addPropertyChangeListener(this);
        sourcesChanged();
    }

    public FileObject getLocation() {
        return getMetaInfFolder();
    }

    public FileObject createLocation() throws IOException {
        FileObject root = getFirstSourceRoot();
        if (root == null) {
            throw new IOException("There are no source roots in the project or the first source root does not exist."); // NOI18N
        }
        FileObject metaInf = root.getFileObject("META-INF"); // NOI18N
        if (metaInf != null) {
            if (!metaInf.isFolder()) {
                throw new IOException("The META-INF directory exists, but is not a folder."); // NOI18N
            }
        } else {
            metaInf = root.createFolder("META-INF"); // NOI18N
        }
        return metaInf;
    }

    public PersistenceScope findPersistenceScope(FileObject fo) {
        Project project = FileOwnerQuery.getOwner(fo);
        if (project != null) {
            J2SEPersistenceProvider provider = (J2SEPersistenceProvider)project.getLookup().lookup(J2SEPersistenceProvider.class);
            return provider.getPersistenceScope();
        }
        return null;
    }
    
    public EntityClassScope findEntityClassScope(FileObject fo) {
        Project project = FileOwnerQuery.getOwner(fo);
        if (project != null) {
            J2SEPersistenceProvider provider = (J2SEPersistenceProvider)project.getLookup().lookup(J2SEPersistenceProvider.class);
            return provider.getEntityClassScope();
        }
        return null;
    }

    public PersistenceScopes getPersistenceScopes() {
        return scopesHelper.getPersistenceScopes();
    }

    private FileObject getFirstSourceRoot() {
        for (URL url : project.getSourceRoots().getRootURLs()) {
            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                return fo;
            }
        }
        return null;
    }

    private FileObject getMetaInfFolder() {
        for (URL url : project.getSourceRoots().getRootURLs()) {
            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                FileObject metaInf = fo.getFileObject("META-INF"); // NOI18N
                if (metaInf != null && metaInf.isFolder()) {
                    return metaInf;
                }
            }
        }
        return null;
    }

    private PersistenceScope getPersistenceScope() {
        FileObject persistenceXml = persistenceScope.getPersistenceXml();
        if (persistenceXml != null && persistenceXml.isValid()) {
            return persistenceScope;
        }
        return null;
    }

    private EntityClassScope getEntityClassScope() {
        return entityClassScope;
    }

    private ClassPath getProjectSourcesClassPath() {
        synchronized (this) {
            if (projectSourcesClassPath == null) {
                projectSourcesClassPath = ClassPathSupport.createProxyClassPath(new ClassPath[] {
                    cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
                    cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
                });
            }
            return projectSourcesClassPath;
        }
    }

    private EntityMappingsMetadataModelHelper createEntityMappingsHelper() {
        return EntityMappingsMetadataModelHelper.create(
            cpProvider.getProjectSourcesClassPath(ClassPath.BOOT),
            cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
            cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE));
    }

    public void propertyChange(PropertyChangeEvent event) {
        sourcesChanged();
    }

    private void sourcesChanged() {
        File persistenceXmlFile = null;
        FileObject metaInf = getMetaInfFolder();
        if (metaInf != null) {
            // if there is a META-INF folder, expect persistence.xml to be created there
            File metaInfFile = FileUtil.toFile(metaInf);
            if (metaInfFile != null) {
                 persistenceXmlFile = new File(metaInfFile, "persistence.xml"); //NOI18N
            }
        } else {
            FileObject firstSourceRoot = getFirstSourceRoot();
            if (firstSourceRoot != null) {
                File sourceRootFile = FileUtil.toFile(firstSourceRoot);
                persistenceXmlFile = new File(sourceRootFile, "META-INF/persistence.xml"); // NOI18N
            }
        }
        synchronized (this) {
            if (persistenceXmlFile != null) {
                scopesHelper.changePersistenceScope(persistenceScope, persistenceXmlFile);
                modelHelper.changePersistenceXml(persistenceXmlFile);
            } else {
                scopesHelper.changePersistenceScope(null, null);
                modelHelper.changePersistenceXml(null);
            }
        }
    }

    /**
     * Implementation of PersistenceScopeImplementation and EntityClassScopeImplementation.
     */
    private final class ScopeImpl implements PersistenceScopeImplementation, EntityClassScopeImplementation {

        public FileObject getPersistenceXml() {
            FileObject location = getLocation();
            if (location == null) {
                return null;
            }
            return location.getFileObject("persistence.xml"); // NOI18N
        }

        public ClassPath getClassPath() {
            return getProjectSourcesClassPath();
        }

        public MetadataModel<EntityMappingsMetadata> getEntityMappingsModel(String persistenceUnitName) {
            return modelHelper.getEntityMappingsModel(persistenceUnitName);
        }
        
        public MetadataModel<EntityMappingsMetadata> getEntityMappingsModel(boolean withDeps) {
            return modelHelper.getDefaultEntityMappingsModel(withDeps);
        }
    }
}
