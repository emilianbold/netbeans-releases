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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.ClassPathSupport;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScopes;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceClassPathProvider;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceLocationProvider;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeFactory;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeImplementation;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeProvider;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopesProvider;
import org.netbeans.modules.j2ee.persistence.spi.support.PersistenceScopesHelper;
import org.netbeans.modules.java.j2seproject.classpath.ClassPathProviderImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Andrei Badea
 */
public class J2SEPersistenceProvider implements PersistenceLocationProvider, PersistenceScopeProvider, PersistenceScopesProvider, PersistenceClassPathProvider, PropertyChangeListener {

    private final J2SEProject project;

    private final PersistenceScopeImplementation persistenceScopeImpl = new PersistenceScopeImpl();
    private final PersistenceScope persistenceScope = PersistenceScopeFactory.createPersistenceScope(persistenceScopeImpl);

    private final PersistenceScopesHelper scopesHelper = new PersistenceScopesHelper();

    private ClassPath projectSourcesClassPath;

    public J2SEPersistenceProvider(J2SEProject project) {
        this.project = project;
        project.getSourceRoots().addPropertyChangeListener(this);
        sourcesChanged();
    }

    public FileObject getLocation() {
        FileObject root = getLocationSourceRoot();
        if (root == null) {
            return null;
        }
        FileObject metaInf = root.getFileObject("META-INF"); // NOI18N
        if (metaInf == null || !metaInf.isFolder()) {
            return null;
        }
        return metaInf;
    }

    public FileObject createLocation() throws IOException {
        FileObject root = getLocationSourceRoot();
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

    public PersistenceScopes getPersistenceScopes() {
        return scopesHelper.getPersistenceScopes();
    }

    public ClassPath getClassPath() {
        return getProjectSourcesClassPath();
    }

    private File getFirstSourceRoot() {
        URL[] urls = project.getSourceRoots().getRootURLs();
        if (urls.length == 0) {
            return null;
        }
        return new File(URI.create(urls[0].toExternalForm()));
    }

    private FileObject getLocationSourceRoot() {
        File sourceRoot = getFirstSourceRoot();
        if (sourceRoot != null) {
            return FileUtil.toFileObject(sourceRoot);
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

    private ClassPath getProjectSourcesClassPath() {
        synchronized (this) {
            if (projectSourcesClassPath == null) {
                ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)project.getLookup().lookup(ClassPathProviderImpl.class);
                projectSourcesClassPath = ClassPathSupport.createWeakProxyClassPath(new ClassPath[] {
                    cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
                    cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
                });
            }
            return projectSourcesClassPath;
        }
    }

    public void propertyChange(PropertyChangeEvent event) {
        sourcesChanged();
    }

    private void sourcesChanged() {
        synchronized (this) {
            File sourceRootFile = getFirstSourceRoot();
            if (sourceRootFile != null) {
                File persistenceXmlFile = new File(sourceRootFile, "META-INF/persistence.xml"); //NOI18N
                scopesHelper.changePersistenceScope(persistenceScope, persistenceXmlFile);
            } else {
                scopesHelper.changePersistenceScope(null, null);
            }
        }
    }

    /**
     * Implementation of PersistenceScopeImplementation.
     */
    private final class PersistenceScopeImpl implements PersistenceScopeImplementation {

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
    }
}
