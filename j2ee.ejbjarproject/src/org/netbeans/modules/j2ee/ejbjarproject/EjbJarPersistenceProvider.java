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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbjarproject.classpath.ClassPathProviderImpl;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScopes;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeFactory;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeImplementation;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeProvider;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceLocationProvider;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeFactory;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeImplementation;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeProvider;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopesProvider;
import org.netbeans.modules.j2ee.persistence.spi.provider.PersistenceProviderSupplier;
import org.netbeans.modules.j2ee.persistence.spi.support.EntityMappingsMetadataModelHelper;
import org.netbeans.modules.j2ee.persistence.spi.support.PersistenceScopesHelper;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;

/**
 * Provides persistence location and scope delegating to this project's EjbJar
 * module.
 *
 * @author Andrei Badea
 */
public class EjbJarPersistenceProvider implements PersistenceLocationProvider, PersistenceScopeProvider, PersistenceScopesProvider, EntityClassScopeProvider, PropertyChangeListener, PersistenceProviderSupplier {
    
    private final EjbJarProject project;
    private final PropertyEvaluator evaluator;
    private final ClassPathProviderImpl cpProvider;
    
    private final ScopeImpl scopeImpl = new ScopeImpl();
    private final PersistenceScope persistenceScope = PersistenceScopeFactory.createPersistenceScope(scopeImpl);
    private final EntityClassScope entityClassScope = EntityClassScopeFactory.createEntityClassScope(scopeImpl);
    
    private final PersistenceScopesHelper scopesHelper = new PersistenceScopesHelper();
    private final EntityMappingsMetadataModelHelper modelHelper;
    
    private ClassPath projectSourcesClassPath;
    
    public EjbJarPersistenceProvider(EjbJarProject project, PropertyEvaluator evaluator, ClassPathProviderImpl cpProvider) {
        this.project = project;
        this.evaluator = evaluator;
        this.cpProvider = cpProvider;
        modelHelper = createEntityMappingsHelper();
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
    
    public EntityClassScope findEntityClassScope(FileObject fo) {
        Project project = FileOwnerQuery.getOwner(fo);
        if (project != null) {
            EjbJarPersistenceProvider provider = (EjbJarPersistenceProvider)project.getLookup().lookup(EjbJarPersistenceProvider.class);
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
                ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)project.getLookup().lookup(ClassPathProviderImpl.class);
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
        if (propName == null || propName.equals(EjbJarProjectProperties.META_INF)) {
            locationChanged();
        }
    }
    
    private void locationChanged() {
        File metaInfFile = project.getEjbModule().getMetaInfAsFile();
        if (metaInfFile != null) {
            File persistenceXmlFile = new File(metaInfFile, "persistence.xml"); // NOI18N
            scopesHelper.changePersistenceScope(persistenceScope, persistenceXmlFile);
            modelHelper.changePersistenceXml(persistenceXmlFile);
        } else {
            scopesHelper.changePersistenceScope(null, null);
            modelHelper.changePersistenceXml(null);
        }
    }
    
    public List<Provider> getSupportedProviders() {
        J2eeModuleProvider j2eeModuleProvider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        J2eePlatform platform  = Deployment.getDefault().getJ2eePlatform(j2eeModuleProvider.getServerInstanceID());
        
        if (platform == null){
            return Collections.<Provider>emptyList();
        }
        List<Provider> result = new ArrayList<Provider>();
        
        addPersistenceProvider(ProviderUtil.HIBERNATE_PROVIDER, "hibernatePersistenceProviderIsDefault", platform, result); // NOI18N
        addPersistenceProvider(ProviderUtil.TOPLINK_PROVIDER, "toplinkPersistenceProviderIsDefault", platform, result);// NOI18N
        addPersistenceProvider(ProviderUtil.KODO_PROVIDER, "kodoPersistenceProviderIsDefault", platform, result); // NOI18N
        
        return result;
    }
    
    private void addPersistenceProvider(Provider provider, String defaultProvider, J2eePlatform platform, List<Provider> providers){
        // would need an api for this..
        if (platform.isToolSupported(provider.getProviderClass())){
            if (platform.isToolSupported(defaultProvider)){
                providers.add(0, provider);
            } else {
                providers.add(provider);
            }
        }
    }
    
    public boolean supportsDefaultProvider() {
        
        J2eeModuleProvider j2eeModuleProvider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        J2eePlatform platform  = Deployment.getDefault().getJ2eePlatform(j2eeModuleProvider.getServerInstanceID());
        
        if (platform == null){
            // server probably not registered, can't resolve whether default provider is supported (see #79856)
            return false;
        }
        
        Set<String> supportedVersions = platform.getSupportedSpecVersions(j2eeModuleProvider.getJ2eeModule().getModuleType());
        
        return supportedVersions.contains(J2eeModule.JAVA_EE_5);
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
