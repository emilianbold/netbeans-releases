/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.visualizers.api.impl;

import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.visualizers.api.ColumnsUIMapping;
import org.netbeans.modules.dlight.visualizers.api.TreeTableVisualizerConfiguration;
import org.netbeans.spi.viewmodel.NodeActionsProvider;

/**
 *
 * @author masha
 */
public abstract class TreeTableVisualizerConfigurationAccessor {

    private static volatile TreeTableVisualizerConfigurationAccessor DEFAULT;

    public static TreeTableVisualizerConfigurationAccessor getDefault() {
        TreeTableVisualizerConfigurationAccessor a = DEFAULT;
        if (a != null) {
            return a;
        }

        try {
            Class.forName(TreeTableVisualizerConfiguration.class.getName(), true, TreeTableVisualizerConfiguration.class.getClassLoader());//
        } catch (Exception e) {
        }
        return DEFAULT;
    }

    public static void setDefault(TreeTableVisualizerConfigurationAccessor accessor) {
        if (DEFAULT != null) {
            throw new IllegalStateException();
        }
        DEFAULT = accessor;
    }

    public TreeTableVisualizerConfigurationAccessor() {
    }

    public abstract Column[] getTableColumns(TreeTableVisualizerConfiguration configuration);

    public abstract Column getTreeColumn(TreeTableVisualizerConfiguration configuration);

    public abstract boolean isTableView(TreeTableVisualizerConfiguration configuration);

    public abstract NodeActionsProvider getNodesActionProvider(TreeTableVisualizerConfiguration configuration);

    public abstract ColumnsUIMapping getColumnsUIMapping(TreeTableVisualizerConfiguration configuration);
}
