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

package org.netbeans.modules.web.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.provider.PersistenceProviderSupplier;

/**
 * An implementation of PersistenceProviderSupplier for web project.
 *
 * @author Erno Mononen
 */
public class WebPersistenceProviderSupplier implements PersistenceProviderSupplier{
    
    private final WebProject project;
    
    /** Creates a new instance of WebPersistenceProviderSupplier */
    public WebPersistenceProviderSupplier(WebProject project) {
        this.project = project;
    }
    
    public List<Provider> getSupportedProviders() {
        // TODO: the implementation of the this method (and whole PersistenceProviderSupplier)
        // is pretty much identical with the EJB implementation,
        // should be refactored to some common class.
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
    

}
