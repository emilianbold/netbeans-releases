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

import org.netbeans.modules.dlight.indicators.graph.DataRowToPlot;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.dlight.api.indicator.IndicatorConfiguration;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.indicators.graph.DetailDescriptor;
import org.netbeans.modules.dlight.indicators.graph.Graph.LabelRenderer;
import org.netbeans.modules.dlight.indicators.graph.GraphDescriptor;
import org.netbeans.modules.dlight.indicators.support.IndicatorConfigurationIDs;

/**
 *
 * @author Alexey Vladykin
 */
public final class PlotIndicatorConfiguration extends IndicatorConfiguration {

    private final String title;
    private final int scale;
    private final List<GraphDescriptor> graphDescriptors;
    private final DataRowToPlot dataRowHandler;
    private List<DetailDescriptor> detailDescriptors;
    private LabelRenderer renderer;

    public PlotIndicatorConfiguration(IndicatorMetadata metadata, int position, String title, int scale, List<GraphDescriptor> graphDescriptors, DataRowToPlot dataRowHandler) {
        super(metadata, position, true);
        this.title = title;
        this.scale = scale;
        this.graphDescriptors = graphDescriptors;
        this.dataRowHandler = dataRowHandler;
    }

    @Override
    public String getID() {
        return IndicatorConfigurationIDs.PLOT_ID;
    }

    public String getTitle() {
        return title;
    }

    public int getGraphScale() {
        return scale;
    }

    public List<GraphDescriptor> getGraphDescriptors() {
        return Collections.unmodifiableList(graphDescriptors);
    }

    public void setLabelRenderer(LabelRenderer renderer) {
        this.renderer = renderer;
    }

    public LabelRenderer getLabelRenderer() {
        return renderer;
    }

    public List<DetailDescriptor> getDetailDescriptors() {
        return detailDescriptors;
    }

    public void setDetailDescriptors(List<DetailDescriptor> detailDescriptors) {
        this.detailDescriptors = detailDescriptors;
    }

    public DataRowToPlot getDataRowHandler() {
        return dataRowHandler;
    }
}
