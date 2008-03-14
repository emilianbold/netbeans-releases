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

package org.netbeans.modules.db.sample;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.db.sample.providers.MySqlSampleDbProvider;
import org.netbeans.spi.db.sample.SampleDbProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;


/**
 * This class manages the list of registered sample database providers. These
 * providers know how to create sample databases for the given database vendor.
 *
 * @see org.netbeans.spi.db.explorer.SampleDbProvider
 *
 * @author David Van Couvering
 */
public final class SampleDbProviderManager { 
    /**
     * The path where the sampe providers are registered in the SystemFileSystem.
     */
    private static final String SAMPLES_PATH = "Databases/Sample/Providers"; // NOI18N
    
    /**
     * The singleton provider manager instance.
     */
    private static SampleDbProviderManager DEFAULT = null;
    
    /**
     * The Lookup.Result instance containing all the SampleDbProvider instances.
     */
    private Lookup.Result result = getLookupResult();
    
    /**
     * Returns the singleton provider manager instance.
     */
    public static synchronized SampleDbProviderManager getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new SampleDbProviderManager();
        }
        return DEFAULT;
    }
    
    public SampleDbProvider[] getProviders() {
        Collection providers = result.allInstances();
        return (SampleDbProvider[])providers.toArray(new SampleDbProvider[providers.size()]);
    }
    
    /**
     * Returns the runtimes registered for the specified JDBC driver.
     *
     * @param jdbcDriverClassName the JDBC driver to search for; must not be null.
     *
     * @return the provider registered for the specified JDBC driver or null
     *         if no provider is registered for this driver.
     *
     * @throws NullPointerException if the specified JDBC driver is null.
     */
    public SampleDbProvider[] getProviders(String jdbcDriverClassName) {
        if (jdbcDriverClassName == null) {
            throw new NullPointerException();
        }
        List<SampleDbProvider> providerList = new LinkedList();
        for (SampleDbProvider provider : providerList ) {
            if (jdbcDriverClassName.equals(provider.getJDBCDriverClass())) {
                providerList.add(provider);
            }
        }
        
        // TODO - get this to work in unit tests with Lookup - currently not
        // working, so I'm injecting the MySQL provider by hand
        SampleDbProvider[] providers;
        if ( providerList.size() == 0 ) {
            providers = new SampleDbProvider[] { 
                MySqlSampleDbProvider.getDefault() 
            };
        } else {
            providers = (SampleDbProvider[])providerList.toArray(
                    new SampleDbProvider[providerList.size()]);
        }
        
        return providers;
    }
    
    private synchronized Lookup.Result getLookupResult() {
        return Lookups.forPath(SAMPLES_PATH).lookupResult(SampleDbProvider.class);
    }
}
