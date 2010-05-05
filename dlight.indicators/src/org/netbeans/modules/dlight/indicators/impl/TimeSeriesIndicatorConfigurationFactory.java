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
import java.util.List;
import java.util.Map;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.indicators.Aggregation;
import org.netbeans.modules.dlight.indicators.DataRowToTimeSeries;
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

    /*package*/ static TimeSeriesIndicatorConfiguration create(Map<?, ?> map) {
        int position = map.get("position") == null ? 1 : (Integer) map.get("position");//NOI18N
        FileObject rootFolder = FileUtil.getConfigRoot();
        String metadataPath = (String) map.get("metadata");//NOI18N
        FileObject columnsFolder = rootFolder.getFileObject(metadataPath);
        IndicatorMetadata metadata = (IndicatorMetadata) columnsFolder.getAttribute("instanceCreate");//NOI18N
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
        if (getString(map, "row.handler") != null) {//NOI18N
            String pathToRowHandler = getString(map, "row.handler");//NOI18N
            FileObject rowHandler = rootFolder.getFileObject(pathToRowHandler);
            DataRowToTimeSeries dataRowToTimeSeries = (DataRowToTimeSeries) rowHandler.getAttribute("instanceCreate");//NOI18N
            indicatorConfiguration.setDataRowHandler(dataRowToTimeSeries);
        } else {
            //set default
            List<Column> columns = metadata.getColumns();
            Column[][] rowHandlerColumns = new Column[columns.size()][1];
            for (int i = 0, size = columns.size(); i < size; i++) {
                rowHandlerColumns[i][0] = columns.get(i);
            }
            indicatorConfiguration.setDataRowHandler(new DefaultDataRowToTimeSeries(rowHandlerColumns));
        }
        if (map.get("label.formatter") != null && map.get("label.formatter") instanceof ValueFormatter) {//NOI18N
            indicatorConfiguration.setLabelFormatter((ValueFormatter) map.get("label.formatter"));//NOI18N
        }
        indicatorConfiguration.setLastNonNull((Boolean) map.get("LastNonNull"));//NOI18N
        if (getString(map, "action.displayName") != null) {//NOI18N
            indicatorConfiguration.setActionDisplayName(getString(map, "action.displayName"));//NOI18N
        }
        if (getString(map, "action.tooltip") != null) {//NOI18N
            indicatorConfiguration.setActionTooltip(getString(map, "action.tooltip"));//NOI18N
        }

        return indicatorConfiguration;
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
}
