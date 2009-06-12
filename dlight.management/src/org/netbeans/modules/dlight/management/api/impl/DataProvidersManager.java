/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.dlight.management.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.dataprovider.DataModelScheme;
import org.netbeans.modules.dlight.spi.dataprovider.DataProvider;
import org.netbeans.modules.dlight.spi.dataprovider.DataProviderFactory;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerDataProvider;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerDataProviderFactory;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.openide.util.Lookup;

/** 
 * DataProvider links two things:
 *   - DataStorage (so, it should know how to talk to it (i.e. support DataStorageScheme))
 *   - Visualizer  (so, it should be able to talk to visualizer i.e. support the same
 *                 DataProviderScheme as visualizer does)
 *
 * Scope: DLight Context
 *
 * 
 */
public final class DataProvidersManager {

    private static DataProvidersManager instance = null;
    private Collection<? extends VisualizerDataProviderFactory> allVisualizerDataProviders;
    private Collection<VisualizerDataProvider> activeVisualizerDataProviders;
    private static final Logger log = DLightLogger.getLogger(DataProvidersManager.class);

    private DataProvidersManager() {
        allVisualizerDataProviders = Lookup.getDefault().lookupAll(VisualizerDataProviderFactory.class);
        activeVisualizerDataProviders = new ArrayList<VisualizerDataProvider>();
        log.fine(allVisualizerDataProviders.size() + " data provider(s) found!"); // NOI18N
    }

    public static DataProvidersManager getInstance() {
        if (instance == null) {
            instance = new DataProvidersManager();
        }

        return instance;
    }

    public VisualizerDataProvider getDataProviderFor(DataModelScheme dataModel){
        for (VisualizerDataProviderFactory providerFactory : allVisualizerDataProviders) {
            if (providerFactory.provides(dataModel)) {
                VisualizerDataProvider newProvider = providerFactory.create();
                activeVisualizerDataProviders.add(newProvider);
                return newProvider;
            }
        }
        return null;
    }

    public DataProvider getDataProviderFor(DataStorageType dst, DataModelScheme dataModel) {
        Collection<? extends DataProviderFactory> factories = Lookup.getDefault().lookupAll(DataProviderFactory.class);
        for (DataProviderFactory providerFactory : factories) {
            if (providerFactory.provides(dataModel) && providerFactory.getSupportedDataStorageTypes().contains(dst)) {
                DataProvider newProvider = providerFactory.create();
                activeVisualizerDataProviders.add(newProvider);
                return newProvider;
            }
        }
        return null;
    }
}
