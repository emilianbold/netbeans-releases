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
package org.netbeans.modules.dlight.indicators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.dlight.api.indicator.IndicatorConfiguration;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.indicators.graph.TimeSeriesIndicatorConfigurationAccessor;
import org.netbeans.modules.dlight.indicators.impl.IndicatorConfigurationIDs;
import org.netbeans.modules.dlight.indicators.impl.TimeSeriesIndicator;

/**
 * Configuration for {@link TimeSeriesIndicator}.
 *
 * @author Alexey Vladykin
 */
public final class TimeSeriesIndicatorConfiguration extends IndicatorConfiguration {

    private String title;
    private int scale;
    private final List<TimeSeriesDescriptor> seriesDescriptors;
    private final List<DetailDescriptor> detailDescriptors;
    private DataRowToTimeSeries dataRowHandler;
    private ValueFormatter formatter;

    public TimeSeriesIndicatorConfiguration(IndicatorMetadata metadata, int position) {
        super(metadata, position, true);
        this.title = ""; // NOI18N
        this.scale = 100;
        this.seriesDescriptors = new ArrayList<TimeSeriesDescriptor>();
        this.detailDescriptors = new ArrayList<DetailDescriptor>();
    }

    @Override
    public String getID() {
        return IndicatorConfigurationIDs.TIMESERIES_ID;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private String getTitle() {
        return title;
    }

    public void setGraphScale(int scale) {
        this.scale = scale;
    }

    private int getGraphScale() {
        return scale;
    }

    public void addTimeSeriesDescriptors(TimeSeriesDescriptor... seriesDescriptors) {
        this.seriesDescriptors.addAll(Arrays.asList(seriesDescriptors));
    }

    private List<TimeSeriesDescriptor> getTimeSeriesDescriptors() {
        return Collections.unmodifiableList(seriesDescriptors);
    }

    public void addDetailDescriptors(DetailDescriptor... detailDescriptors) {
        this.detailDescriptors.addAll(Arrays.asList(detailDescriptors));
    }

    private List<DetailDescriptor> getDetailDescriptors() {
        return Collections.unmodifiableList(detailDescriptors);
    }

    public void setDataRowHandler(DataRowToTimeSeries dataRowHandler) {
        this.dataRowHandler = dataRowHandler;
    }

    private DataRowToTimeSeries getDataRowHandler() {
        return dataRowHandler;
    }

    public void setLabelFormatter(ValueFormatter formatter) {
        this.formatter = formatter;
    }

    private ValueFormatter getLabelRenderer() {
        return formatter;
    }

    private static class TimeSeriesIndicatorConfigurationAccessorImpl extends TimeSeriesIndicatorConfigurationAccessor {

        @Override
        public String getTitle(TimeSeriesIndicatorConfiguration conf) {
            return conf.getTitle();
        }

        @Override
        public int getGraphScale(TimeSeriesIndicatorConfiguration conf) {
            return conf.getGraphScale();
        }

        @Override
        public List<TimeSeriesDescriptor> getTimeSeriesDescriptors(TimeSeriesIndicatorConfiguration conf) {
            return conf.getTimeSeriesDescriptors();
        }

        @Override
        public List<DetailDescriptor> getDetailDescriptors(TimeSeriesIndicatorConfiguration conf) {
            return conf.getDetailDescriptors();
        }

        @Override
        public DataRowToTimeSeries getDataRowHandler(TimeSeriesIndicatorConfiguration conf) {
            return conf.getDataRowHandler();
        }

        @Override
        public ValueFormatter getLabelRenderer(TimeSeriesIndicatorConfiguration conf) {
            return conf.getLabelRenderer();
        }
    }

    static {
        TimeSeriesIndicatorConfigurationAccessor.setDefault(new TimeSeriesIndicatorConfigurationAccessorImpl());
    }
}
