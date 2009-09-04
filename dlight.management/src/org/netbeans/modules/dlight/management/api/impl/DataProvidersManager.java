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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.dataprovider.DataModelScheme;
import org.netbeans.modules.dlight.spi.dataprovider.DataProvider;
import org.netbeans.modules.dlight.spi.dataprovider.DataProviderFactory;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerDataProvider;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerDataProviderFactory;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

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

    private final static Logger log = DLightLogger.getLogger(DataProvidersManager.class);
    private final static DataProvidersManager instance = new DataProvidersManager();
    private final LookupListener lookupListener;
    private final Collection<VisualizerDataProviderFactory> allVisualizerDataProviders;
    private final Lookup.Result<VisualizerDataProviderFactory> lookupResult;
    private final Collection<VisualizerDataProvider> activeVisualizerDataProviders;

    private DataProvidersManager() {
        allVisualizerDataProviders = new ArrayList<VisualizerDataProviderFactory>();
        lookupResult = Lookup.getDefault().lookupResult(VisualizerDataProviderFactory.class);

        lookupListener = new LookupListener() {

            public void resultChanged(LookupEvent ev) {
                synchronized (allVisualizerDataProviders) {
                    Collection<? extends VisualizerDataProviderFactory> newSet = lookupResult.allInstances();
                    allVisualizerDataProviders.retainAll(newSet);

                    for (VisualizerDataProviderFactory<?> f : newSet) {
                        if (!allVisualizerDataProviders.contains(f)) {
                            Collection<DataModelScheme> supportedSchemes = f.getProvidedDataModelScheme();
                            if (supportedSchemes != null) {
                                allVisualizerDataProviders.add(f);
                                log.log(Level.FINE, "New VisualizerDataProvider factory registered for the following schemas: {0}", // NOI18N
                                        Arrays.toString(supportedSchemes.toArray(new DataModelScheme[0])));
                            }
                        }
                    }
                }
            }
        };

        lookupResult.addLookupListener(lookupListener);
        lookupListener.resultChanged(null);

        activeVisualizerDataProviders = new ArrayList<VisualizerDataProvider>();
    }

    public static DataProvidersManager getInstance() {
        return instance;
    }

    public VisualizerDataProvider getDataProviderFor(DataModelScheme dataModel) {
        synchronized (allVisualizerDataProviders) {
            for (VisualizerDataProviderFactory providerFactory : allVisualizerDataProviders) {
                try {
                    if (providerFactory.provides(dataModel)) {
                        VisualizerDataProvider newProvider = providerFactory.create();
                        activeVisualizerDataProviders.add(newProvider);
                        return newProvider;
                    }
                } catch (Throwable th) {
                    log.log(Level.FINE, "Exeption in getDataProviderFor " + dataModel, th); // NOI18N
                }
            }
        }
        return null;
    }

    public Collection<DataProviderFactory> getDataProviderFactories(final DataModelScheme visDataModelScheme) {
        Collection<? extends DataProviderFactory> factories = Lookup.getDefault().lookupAll(DataProviderFactory.class);
        List<DataProviderFactory> result = new ArrayList<DataProviderFactory>();

        for (DataProviderFactory factory : factories) {
            if (factory.getProvidedDataModelScheme().contains(visDataModelScheme)) {
                result.add(factory);
            }
        }

        return Collections.unmodifiableCollection(result);
    }

    public DataProvider createProvider(DataProviderFactory providerFactory) {
        DataProvider newProvider = providerFactory.create();
        activeVisualizerDataProviders.add(newProvider);
        return newProvider;
    }
}
