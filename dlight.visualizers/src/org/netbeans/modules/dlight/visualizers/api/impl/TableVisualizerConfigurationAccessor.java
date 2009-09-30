/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.visualizers.api.impl;

import org.netbeans.modules.dlight.visualizers.api.TableVisualizerConfiguration;

/**
 *
 * @author mt154047
 */
public abstract class TableVisualizerConfigurationAccessor {
      private static volatile TableVisualizerConfigurationAccessor DEFAULT;

    public static TableVisualizerConfigurationAccessor getDefault() {
        TableVisualizerConfigurationAccessor a = DEFAULT;
        if (a != null) {
            return a;
        }

        try {
            Class.forName(TableVisualizerConfigurationAccessor.class.getName(), true, TableVisualizerConfigurationAccessor.class.getClassLoader());//
        } catch (Exception e) {
        }
        return DEFAULT;
    }

    public static void setDefault(TableVisualizerConfigurationAccessor accessor) {
        if (DEFAULT != null) {
            throw new IllegalStateException();
        }
        DEFAULT = accessor;
    }

    public TableVisualizerConfigurationAccessor() {
    }

    public abstract String getEmptyRunningMessage(TableVisualizerConfiguration configuration);

    public abstract String getEmptyAnalyzeMessage(TableVisualizerConfiguration configuration);
    

}
