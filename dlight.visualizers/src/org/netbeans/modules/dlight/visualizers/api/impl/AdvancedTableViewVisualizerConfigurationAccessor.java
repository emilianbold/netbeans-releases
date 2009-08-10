/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.visualizers.api.impl;

import java.util.List;
import org.netbeans.modules.dlight.visualizers.api.AdvancedTableViewVisualizerConfiguration;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TableModel;

/**
 *
 * @author mt154047
 */
public abstract class AdvancedTableViewVisualizerConfigurationAccessor {

    private static volatile AdvancedTableViewVisualizerConfigurationAccessor DEFAULT;

    public static AdvancedTableViewVisualizerConfigurationAccessor getDefault() {
        AdvancedTableViewVisualizerConfigurationAccessor a = DEFAULT;
        if (a != null) {
            return a;
        }

        try {
            Class.forName(AdvancedTableViewVisualizerConfiguration.class.getName(), true,
                AdvancedTableViewVisualizerConfiguration.class.getClassLoader());//
        } catch (Exception e) {
        }
        return DEFAULT;
    }

    public static void setDefault(AdvancedTableViewVisualizerConfigurationAccessor accessor) {
        if (DEFAULT != null) {
            throw new IllegalStateException();
        }
        DEFAULT = accessor;
    }

    public AdvancedTableViewVisualizerConfigurationAccessor() {
    }

    public abstract NodeActionsProvider getNodeActionProvider(AdvancedTableViewVisualizerConfiguration configuration);

    public abstract String getNodeColumnName(AdvancedTableViewVisualizerConfiguration configuration);

    public abstract TableModel getTableModel(AdvancedTableViewVisualizerConfiguration configuration);

    public abstract String getEmptyRunningMessage(AdvancedTableViewVisualizerConfiguration configuration);

    public abstract String getEmptyAnalyzeMessage(AdvancedTableViewVisualizerConfiguration configuration);

    public abstract String getRowNodeColumnName(AdvancedTableViewVisualizerConfiguration configuration);

    public abstract List<String> getHiddenColumnNames(AdvancedTableViewVisualizerConfiguration configuration);
}
