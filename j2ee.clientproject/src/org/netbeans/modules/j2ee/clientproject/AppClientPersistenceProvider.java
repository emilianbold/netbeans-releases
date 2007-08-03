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

package org.netbeans.modules.j2ee.clientproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.clientproject.classpath.ClassPathProviderImpl;
import org.netbeans.modules.j2ee.clientproject.ui.customizer.AppClientProjectProperties;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
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
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;

/**
 * Provides persistence location and scope delegating to this project's Car.
 *
 * @author Andrei Badea
 */
public class AppClientPersistenceProvider implements PersistenceLocationProvider, PersistenceScopeProvider, PersistenceScopesProvider, EntityClassScopeProvider, PropertyChangeListener {

    private final AppClientProject project;
    private final PropertyEvaluator evaluator;
    private final ClassPathProviderImpl cpProvider;

    private final ScopeImpl scopeImpl = new ScopeImpl();
    private final PersistenceScope persistenceScope = PersistenceScopeFactory.createPersistenceScope(scopeImpl);
    private final EntityClassScope entityClassScope = EntityClassScopeFactory.createEntityClassScope(scopeImpl);

    private final PersistenceScopesHelper scopesHelper = new PersistenceScopesHelper();
    private final EntityMappingsMetadataModelHelper modelHelper;

    private ClassPath projectSourcesClassPath;

    public AppClientPersistenceProvider(AppClientProject project, PropertyEvaluator evaluator, ClassPathProviderImpl cpProvider) {
        this.project = project;
        this.evaluator = evaluator;
        this.cpProvider = cpProvider;
        modelHelper = createEntityMappingsHelper();
        evaluator.addPropertyChangeListener(this);
        locationChanged();
    }

    public FileObject getLocation() {
        return project.getAPICar().getMetaInf();
    }

    public FileObject createLocation() throws IOException {
        // the folder should have been created when the project was generated
        return project.getAPICar().getMetaInf();
    }


    public PersistenceScope findPersistenceScope(FileObject fo) {
        Project project = FileOwnerQuery.getOwner(fo);
        if (project != null) {
            AppClientPersistenceProvider provider = project.getLookup().lookup(AppClientPersistenceProvider.class);
            return provider.getPersistenceScope();
        }
        return null;
    }

    public EntityClassScope findEntityClassScope(FileObject fo) {
        Project project = FileOwnerQuery.getOwner(fo);
        if (project != null) {
            AppClientPersistenceProvider provider = project.getLookup().lookup(AppClientPersistenceProvider.class);
            return provider.getEntityClassScope();
        }
        return null;
    }

    public PersistenceScopes getPersistenceScopes() {
        return scopesHelper.getPersistenceScopes();
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
                ClassPathProviderImpl cpProvider = project.getLookup().lookup(ClassPathProviderImpl.class);
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
        String propName = event.getPropertyName();
        if (propName == null || propName.equals(AppClientProjectProperties.META_INF)) {
            locationChanged();
        }
    }

    private void locationChanged() {
        File metaInfFile = project.getCarModule().getMetaInfAsFile();
        if (metaInfFile != null) {
            File persistenceXmlFile = new File(metaInfFile, "persistence.xml"); // NOI18N
            scopesHelper.changePersistenceScope(persistenceScope, persistenceXmlFile);
            modelHelper.changePersistenceXml(persistenceXmlFile);
        } else {
            scopesHelper.changePersistenceScope(null, null);
            modelHelper.changePersistenceXml(null);
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
