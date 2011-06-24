/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.provider.PersistenceProviderSupplier;
import org.netbeans.modules.javaee.specs.support.api.JpaProvider;
import org.netbeans.modules.javaee.specs.support.api.JpaSupport;

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
        
        Set<Provider> candidates = new HashSet<Provider>();
        // TODO why we are selecting only some of them  ?
        // can't we just use ProviderUtil.getAllProviders() ?
        candidates.add(ProviderUtil.HIBERNATE_PROVIDER);
        candidates.add(ProviderUtil.HIBERNATE_PROVIDER2_0);
        candidates.add(ProviderUtil.TOPLINK_PROVIDER1_0);
        candidates.add(ProviderUtil.KODO_PROVIDER);
        candidates.add(ProviderUtil.DATANUCLEUS_PROVIDER);
        candidates.add(ProviderUtil.OPENJPA_PROVIDER);
        candidates.add(ProviderUtil.OPENJPA_PROVIDER1_0);
        candidates.add(ProviderUtil.ECLIPSELINK_PROVIDER);
        addPersistenceProviders(candidates, platform, result);
        
        return result;
    }
    
    private void addPersistenceProviders(Set<Provider> providers, J2eePlatform platform, List<Provider> result){
        JpaSupport jpaSupport = JpaSupport.getInstance(platform);
        Map<String, JpaProvider> map = new HashMap<String, JpaProvider>();
        for (JpaProvider provider : jpaSupport.getProviders()) {
            map.put(provider.getClassName(), provider);
        }
        for (Provider provider : providers) {
            JpaProvider jpa = map.get(provider.getProviderClass());
            if (jpa != null) {
                String version = ProviderUtil.getVersion(provider);
                if (version == null
                        || ((version.equals(Persistence.VERSION_2_0) && jpa.isJpa2Supported())
                        || (version.equals(Persistence.VERSION_1_0) && jpa.isJpa1Supported()))) {

                    if (jpa.isDefault()) {
                        result.add(0, provider);
                    } else {
                        result.add(provider);
                    }
                }
            }
        }
        return;
    }
    
    public boolean supportsDefaultProvider() {
        J2eeProjectCapabilities capabilities = J2eeProjectCapabilities.forProject(project);
        return capabilities != null && capabilities.hasDefaultPersistenceProvider();
    }
    

}
