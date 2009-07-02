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

package org.netbeans.modules.web.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
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
        addPersistenceProvider(ProviderUtil.DATANUCLEUS_PROVIDER, "dataNucleusPersistenceProviderIsDefault", platform, result); // NOI18N
        addPersistenceProvider(ProviderUtil.OPENJPA_PROVIDER, "openJpaPersistenceProviderIsDefault", platform, result); // NOI18N
        addPersistenceProvider(ProviderUtil.ECLIPSELINK_PROVIDER, "eclipseLinkPersistenceProviderIsDefault", platform, result); // NOI18N
        
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
        J2eeProjectCapabilities capabilities = J2eeProjectCapabilities.forProject(project);
        return capabilities != null && capabilities.hasDefaultPersistenceProvider();
    }
    

}
