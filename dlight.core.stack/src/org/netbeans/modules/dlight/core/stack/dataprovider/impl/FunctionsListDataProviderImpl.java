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
package org.netbeans.modules.dlight.core.stack.dataprovider.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric;
import org.netbeans.modules.dlight.core.stack.api.support.FunctionDatatableDescription;
import org.netbeans.modules.dlight.core.stack.dataprovider.FunctionCallTreeTableNode;
import org.netbeans.modules.dlight.core.stack.dataprovider.FunctionsListDataProvider;
import org.netbeans.modules.dlight.core.stack.storage.StackDataStorage;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.openide.util.Lookup;

/**
 *
 * @author mt154047
 */
class FunctionsListDataProviderImpl implements FunctionsListDataProvider {

    private StackDataStorage storage;
    private ServiceInfoDataStorage serviceInfoStorage;

    FunctionsListDataProviderImpl() {
    }

    public void attachTo(DataStorage storage) {
        this.storage = (StackDataStorage) storage;
        attachTo((ServiceInfoDataStorage) storage);
    }

    public List<FunctionCall> getFunctionsList(DataTableMetadata metadata, FunctionDatatableDescription functionDescription, List<Column> metricsColumn) {
        if (functionDescription.getOffsetColumn() == null) {
            List<FunctionCall> result = new ArrayList<FunctionCall>();
            List<FunctionCallTreeTableNode> nodes = FunctionCallTreeTableNode.getFunctionCallTreeTableNodes(storage.getHotSpotFunctions(FunctionMetric.CpuTimeInclusiveMetric, Integer.MAX_VALUE));
            for (FunctionCallTreeTableNode node : nodes) {
                FunctionCall call = node.getDeligator();
                result.add(call);
            }

            return result;
        }
        return storage.getFunctionsList(metadata, metricsColumn, functionDescription);
    }

    public SourceFileInfo getSourceFileInfo(FunctionCall functionCall) {
        //we should get here SourceFileInfoProvider
        Collection<? extends SourceFileInfoProvider> sourceInforFileProviders =
                Lookup.getDefault().lookupAll(SourceFileInfoProvider.class);

        if (sourceInforFileProviders.isEmpty()) {
            return null;
        }
        Iterator<? extends SourceFileInfoProvider> iterator = sourceInforFileProviders.iterator();
        while (iterator.hasNext()) {
            SourceFileInfoProvider provider = iterator.next();
            try {
                // TODO: pass meaningful values for offset and executable
                final SourceFileInfo lineInfo = provider.fileName(functionCall.getFunction().getName(), functionCall.getOffset(), serviceInfoStorage.getInfo());
                if (lineInfo != null && lineInfo.isSourceKnown()) {
                    return lineInfo;
                }
            } catch (SourceFileInfoProvider.SourceFileInfoCannotBeProvided e) {
            }
        }
        return null;
    }

    public void attachTo(ServiceInfoDataStorage serviceInfoDataStorage) {
        this.serviceInfoStorage = serviceInfoDataStorage;
    }
}
