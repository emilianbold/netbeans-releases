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
package org.netbeans.modules.dlight.indicators.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;
import org.netbeans.modules.dlight.indicators.Aggregation;
import org.netbeans.modules.dlight.indicators.DataRowToTimeSeries;
import org.netbeans.modules.dlight.indicators.TimeSeriesDescriptor;
import org.netbeans.modules.dlight.indicators.TimeSeriesIndicatorConfiguration;
import org.netbeans.modules.dlight.indicators.support.DefaultDataRowToTimeSeries;
import org.netbeans.modules.dlight.util.ValueFormatter;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 *
 * @author mt154047
 */
public final class TimeSeriesIndicatorConfigurationFactory {

    private TimeSeriesIndicatorConfigurationFactory() {
    }

    static TimeSeriesIndicatorConfiguration createTimeSeriesIndicator(Map<?, ?> map) {
        IndicatorMetadata metadata = getStringAndCreateInstance(map, "metadata", IndicatorMetadata.class); //NOI18N
        int position = getInt(map, "position");//NOI18N
        TimeSeriesIndicatorConfiguration indicatorConfiguration = new TimeSeriesIndicatorConfiguration(metadata, position);
        if (getString(map, "aggregation") != null) {//NOI18N
            Aggregation aggregation = Aggregation.valueOf(getString(map, "aggregation"));//NOI18N
            indicatorConfiguration.setAggregation(aggregation);
        }
        if (map.get("granularity") != null) {//NOI18N
            indicatorConfiguration.setGranularity((Long) map.get("granularity"));//NOI18N
        }
        if (map.get("graph.scale") != null) {//NOI18N
            indicatorConfiguration.setGraphScale((Integer) map.get("graph.scale"));//NOI18N
        }
        if (map.get("title") != null) {//NOI18N
            indicatorConfiguration.setTitle(getString(map, "title"));//NOI18N
        }
        if (map.get("label.formatter") != null && map.get("label.formatter") instanceof ValueFormatter) {//NOI18N
            indicatorConfiguration.setLabelFormatter((ValueFormatter) map.get("label.formatter"));//NOI18N
        }
        indicatorConfiguration.setLastNonNull(getBoolean(map, "LastNonNull"));//NOI18N
        if (getString(map, "action.displayName") != null) {//NOI18N
            indicatorConfiguration.setActionDisplayName(getString(map, "action.displayName"));//NOI18N
        }
        if (getString(map, "action.tooltip") != null) {//NOI18N
            indicatorConfiguration.setActionTooltip(getString(map, "action.tooltip"));//NOI18N
        }
        @SuppressWarnings("unchecked")
        List<TimeSeriesDescriptor> uncheckedDescriptors = getStringAndCreateInstance(map, "timeSeriesDescriptors", List.class); // NOI18N
        indicatorConfiguration.addTimeSeriesDescriptors(uncheckedDescriptors.toArray(new TimeSeriesDescriptor[uncheckedDescriptors.size()]));

        DataRowToTimeSeries dataRowToTimeSeries = getStringAndCreateInstance(map, "row.handler", DataRowToTimeSeries.class); // NOI18N
        if (dataRowToTimeSeries == null) {
            //set default
            Column[][] rowHandlerColumns = new Column[uncheckedDescriptors.size()][];
            for (int i = 0, descriptorCount = uncheckedDescriptors.size(); i < descriptorCount; ++i) {
                Collection<Column> descriptorColumns = TimeSeriesDescriptorAccessor.getDefault().getSourceColumns(uncheckedDescriptors.get(i));
                rowHandlerColumns[i] = descriptorColumns.toArray(new Column[descriptorColumns.size()]);
            }
            dataRowToTimeSeries = new DefaultDataRowToTimeSeries(rowHandlerColumns);
        }
        indicatorConfiguration.setDataRowHandler(dataRowToTimeSeries);

        VisualizerConfiguration visualizer = getStringAndCreateInstance(map, "visualizer", VisualizerConfiguration.class); // NOI18N
        if (visualizer != null) {
            indicatorConfiguration.addVisualizerConfiguration(visualizer);
        }

        return indicatorConfiguration;
    }

    private static <T> T getStringAndCreateInstance(Map<?, ?> map, String key, Class<T> clazz) {
        return createInstance(getString(map, key), clazz);
    }

    /*package*/ static <T> T createInstance(String path, Class<T> clazz) {
        if (path != null) {
            FileObject fileObject = FileUtil.getConfigFile(path);
            return fileObject == null ? null : createInstance(fileObject, clazz);
        }
        return null;
    }

    /*package*/ static <T> T createInstance(FileObject instanceFileObject, Class<T> clazz) {
        if (instanceFileObject != null) {
            try {
                DataObject dataObject = DataObject.find(instanceFileObject);
                InstanceCookie instanceCookie = dataObject.getCookie(InstanceCookie.class);
                if (instanceCookie != null) {
                    return clazz.cast(instanceCookie.instanceCreate());
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ClassCastException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    private static String getString(Map<?, ?> map, String key) {
        return (String) map.get(key);
    }

    private static boolean getBoolean(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value instanceof Boolean ? (Boolean) value : false;
    }

    private static int getInt(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value instanceof Integer ? (Integer) value : 0;
    }
}
