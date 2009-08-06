/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.threadmap.support.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.dlight.core.stack.dataprovider.ThreadMapDataProvider;
import org.netbeans.modules.dlight.api.dataprovider.DataModelScheme;
import org.netbeans.modules.dlight.api.support.DataModelSchemeProvider;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerDataProviderFactory;
import org.netbeans.modules.dlight.threadmap.dataprovider.ThreadMapDataProviderImpl;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author Alexander Simon
 */
@ServiceProviders({@ServiceProvider(service = VisualizerDataProviderFactory.class)})
public class ThreadMapDataProviderFactory implements VisualizerDataProviderFactory<ThreadMapDataProvider> {

    private final List<DataStorageType> supportedStorageTypes;
    private final List<DataModelScheme> providedSchemas;

    public ThreadMapDataProviderFactory() {
        supportedStorageTypes = new ArrayList<DataStorageType>(1);
        supportedStorageTypes.add(DataStorageTypeFactory.getInstance().getDataStorageType("ThreadMapDataStorage")); //NOI18N
        providedSchemas = new ArrayList<DataModelScheme>(1);
        providedSchemas.add(DataModelSchemeProvider.getInstance().getScheme("model:threadmap")); //NOI18N
    }

    public ThreadMapDataProvider create() {
        return new ThreadMapDataProviderImpl();
    }

    public Collection<DataModelScheme> getProvidedDataModelScheme() {
        return providedSchemas;
    }

    public boolean provides(DataModelScheme dataModel) {
        return getProvidedDataModelScheme().contains(dataModel);
    }

    public Collection<DataStorageType> getSupportedDataStorageTypes() {
        return supportedStorageTypes;
    }
}
