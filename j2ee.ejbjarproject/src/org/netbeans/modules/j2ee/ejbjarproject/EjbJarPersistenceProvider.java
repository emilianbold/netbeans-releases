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

package org.netbeans.modules.j2ee.ejbjarproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.ejbjarproject.classpath.ClassPathProviderImpl;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
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
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;

/**
 * Provides persistence location and scope delegating to this project's EjbJar
 * module.
 *
 * @author Andrei Badea
 */
public class EjbJarPersistenceProvider implements PersistenceLocationProvider, PersistenceScopeProvider, PersistenceScopesProvider, PersistenceClassPathProvider, PropertyChangeListener {

    private final EjbJarProject project;
    private final PropertyEvaluator evaluator;

    private final PersistenceScopeImplementation persistenceScopeImpl = new PersistenceScopeImpl();
    private final PersistenceScope persistenceScope = PersistenceScopeFactory.createPersistenceScope(persistenceScopeImpl);

    private final PersistenceScopesHelper scopesHelper = new PersistenceScopesHelper();

    private ClassPath projectSourcesClassPath;

    public EjbJarPersistenceProvider(EjbJarProject project, PropertyEvaluator evaluator) {
        this.project = project;
        this.evaluator = evaluator;
        evaluator.addPropertyChangeListener(this);
        locationChanged();
    }

    public FileObject getLocation() {
        return project.getEjbModule().getMetaInf();
    }

    public FileObject createLocation() throws IOException {
        // the folder should have be created when the project was generated
        return project.getAPIEjbJar().getMetaInf();
    }

    public PersistenceScope findPersistenceScope(FileObject fo) {
        Project project = FileOwnerQuery.getOwner(fo);
        if (project != null) {
            EjbJarPersistenceProvider provider = (EjbJarPersistenceProvider)project.getLookup().lookup(EjbJarPersistenceProvider.class);
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
        String propName = event.getPropertyName();
        if (propName == null || propName.equals(EjbJarProjectProperties.META_INF)) {
            locationChanged();
        }
    }

    private void locationChanged() {
        File metaInfFile = project.getEjbModule().getMetaInfAsFile();
        if (metaInfFile != null) {
            File persistenceXmlFile = new File(metaInfFile, "persistence.xml"); // NOI18N
            scopesHelper.changePersistenceScope(persistenceScope, persistenceXmlFile);
        } else {
            scopesHelper.changePersistenceScope(null, null);
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
