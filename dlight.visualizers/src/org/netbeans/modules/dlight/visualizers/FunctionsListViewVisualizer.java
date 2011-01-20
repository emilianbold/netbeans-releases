/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.dlight.visualizers;

import java.awt.Component;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;
import org.netbeans.modules.dlight.core.stack.api.support.FunctionDatatableDescription;
import org.netbeans.modules.dlight.core.stack.dataprovider.FunctionsListDataProvider;
import org.netbeans.modules.dlight.core.stack.spi.AnnotatedSourceSupport;
import org.netbeans.modules.dlight.spi.SourceSupportProvider;
import org.netbeans.modules.dlight.visualizers.api.ColumnsUIMapping;
import org.netbeans.modules.dlight.visualizers.api.FunctionsListViewVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.impl.FunctionsListViewVisualizerConfigurationAccessor;
import org.netbeans.modules.dlight.visualizers.ui.FunctionCallNodeChildren;
import org.netbeans.modules.dlight.visualizers.ui.FunctionsListViewTable;
import org.netbeans.modules.dlight.visualizers.ui.TableViewNodeChildren;
import org.openide.util.Lookup;

/**
 *
 * @author mt154047
 */
public final class FunctionsListViewVisualizer extends TableViewVisualizer<FunctionsListViewVisualizerConfiguration, FunctionCallWithMetric> {

    private final DataTableMetadata metadata;
    private final FunctionDatatableDescription functionDatatableDescription;
    private final List<Column> metrics;
    private final FunctionsListDataProvider dataProvider;

    FunctionsListViewVisualizer(final FunctionsListDataProvider dataProvider, final FunctionsListViewVisualizerConfiguration cfg) {
        super(dataProvider, cfg);

        this.dataProvider = dataProvider;
        this.metadata = cfg.getMetadata();

        FunctionsListViewVisualizerConfigurationAccessor cfgAccess =
                FunctionsListViewVisualizerConfigurationAccessor.getDefault();

        this.metrics = cfgAccess.getMetricsList(cfg);
        this.functionDatatableDescription = cfgAccess.getFunctionDatatableDescription(cfg);
    }

    @Override
    protected Component initTableView() {
        FunctionsListViewVisualizerConfigurationAccessor cfgAccess =
                FunctionsListViewVisualizerConfigurationAccessor.getDefault();

        // Create table view
        final ColumnsUIMapping columnsUIMapping = cfgAccess.getColumnsUIMapping(getVisualizerConfiguration());

        String nodeLabel = columnsUIMapping == null
                || columnsUIMapping.getDisplayedName(functionDatatableDescription.getNameColumn()) == null
                ? metadata.getColumnByName(functionDatatableDescription.getNameColumn()).getColumnUName()
                : columnsUIMapping.getDisplayedName(functionDatatableDescription.getNameColumn());

        return new FunctionsListViewTable(getExplorerManager(), nodeLabel, columnsUIMapping, metrics);
    }

    @Override
    protected List<FunctionCallWithMetric> getUpdatedData() {
        List<FunctionCallWithMetric> newData = dataProvider.getFunctionsList(metadata, functionDatatableDescription, metrics);
        List<FunctionCallWithMetric> details = dataProvider.getDetailedFunctionsList(metadata, functionDatatableDescription, metrics);

        Collection<? extends AnnotatedSourceSupport> supports = Lookup.getDefault().lookupAll(AnnotatedSourceSupport.class);

        if (supports != null) {
            for (AnnotatedSourceSupport sourceSupport : supports) {
                sourceSupport.updateSource(dataProvider, metrics, newData, details);
            }
        }

        return newData;
    }

    @Override
    protected TableViewNodeChildren<FunctionCallWithMetric> initChildren() {
        GotoSourceActionProvider gotoSourceActionsProvider =
                new GotoSourceActionProvider(Lookup.getDefault().lookup(SourceSupportProvider.class), dataProvider);
        return new FunctionCallNodeChildren(gotoSourceActionsProvider, metrics);
    }

    @Override
    protected boolean matchesFilter(String filter, FunctionCallWithMetric function) {
        return filter == null || filter.isEmpty() || function.getFunction().getName().contains(filter);
    }
}
