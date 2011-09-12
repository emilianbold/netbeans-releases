/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.spi.impl;

import java.util.List;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;
import org.netbeans.modules.dlight.spi.indicator.IndicatorRepairActionProvider;

/**
 *
 * @author masha
 */
public abstract class IndicatorAccessor {

    private static volatile IndicatorAccessor DEFAULT;

    public static IndicatorAccessor getDefault() {
        IndicatorAccessor a = DEFAULT;
        if (a != null) {
            return a;
        }

        try {
            Class.forName(Indicator.class.getName(), true, Indicator.class.getClassLoader());//
        } catch (Exception e) {
        }
        return DEFAULT;
    }

    public static void setDefault(IndicatorAccessor accessor) {
        if (DEFAULT != null) {
            throw new IllegalStateException();
        }
        DEFAULT = accessor;
    }

    public IndicatorAccessor() {
    }

    public abstract void setToolID(Indicator<?> ind, String toolID);
    
    public abstract void setDetailsEnabled(Indicator<?> ind, boolean isDetailsEnabled);

    public abstract void setToolDescription(Indicator<?> ind, String toolDescription);

    public abstract String getToolID(Indicator<?> ind);

    public abstract List<Column> getMetadataColumns(Indicator<?> indicator);

    public abstract String getMetadataColumnName(Indicator<?> indicator, int idx);

    public abstract List<VisualizerConfiguration> getVisualizerConfigurations(Indicator<?> indicator);

    public abstract void addIndicatorActionListener(Indicator<?> indicator, IndicatorActionListener l);

    public abstract void removeIndicatorActionListener(Indicator<?> indicator, IndicatorActionListener l);

    public abstract void initMouseListener(Indicator<?> indicator);

    public abstract void setRepairActionProviderFor(Indicator<?> indicator, IndicatorRepairActionProvider repairActionProvider);

}
