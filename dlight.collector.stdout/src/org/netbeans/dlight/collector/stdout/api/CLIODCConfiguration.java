/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.dlight.collector.stdout.api;

import java.util.List;
import org.netbeans.dlight.collector.stdout.api.impl.CLIODCConfigurationAccessor;
import org.netbeans.modules.dlight.collector.api.DataCollectorConfiguration;
import org.netbeans.modules.dlight.indicator.api.IndicatorDataProviderConfiguration;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata;

/**
 * Implementation of the <code>DataCollectorConfiguration</code> interface
 * that uses the output of a command as its data source.
 * It invokes the given command, feds the given parser with its output line by line
 * and stores the data it returns in the tables described in the given tables metadata.
 * 
 * @author masha
 */
public final class CLIODCConfiguration implements DataCollectorConfiguration, IndicatorDataProviderConfiguration {

    static final String ID = "dlight.CLIODataColectorConfiguration";
    private final String command;
    private final String arguments;
    private final CLIOParser parser;
    private final List<DataTableMetadata> dataTablesMetadata;
    private boolean indicatorDataProvider;


    static {
        CLIODCConfigurationAccessor.setDefault(new CLIODCConfigurationAccessorImpl());
    }

    public CLIODCConfiguration(String command, String arguments, CLIOParser parser, List<DataTableMetadata> dataTablesMetadata) {
        this.command = command;
        this.arguments = arguments;
        this.parser = parser;
        this.dataTablesMetadata = dataTablesMetadata;
    }

    public void registerAsIndicatorDataProvider(boolean indicatorDataProvider) {
        this.indicatorDataProvider = indicatorDataProvider;
    }

    /**
     * Gets this configuration unique ID (implements both
     * {@link org.netbeans.modules.dlight.collector.api.DataCollectorConfiguration} and
     * {@link org.netbeans.modules.dlight.indicator.api.IndicatorDataProviderConfiguration}.
     * method <code>getID()</code>).
     *
     * @return this configuration unique ID
     */
    public String getID() {
        return ID;
    }

    /**
     * Get command to run
     *
     * @return command line
     */
    private String getCommand() {
        return command;
    }

    private String getArguments() {
        return arguments;
    }

    private List<DataTableMetadata> getDataTablesMetadata() {
        return dataTablesMetadata;
    }

    private boolean registerAsIndicatorDataProvider() {
        return indicatorDataProvider;
    }

    private CLIOParser getParser() {
        return parser;
    }

    private static final class CLIODCConfigurationAccessorImpl extends CLIODCConfigurationAccessor {

        @Override
        public String getCommand(CLIODCConfiguration configuration) {
            return configuration.getCommand();
        }

        @Override
        public String getArguments(CLIODCConfiguration configuration) {
            return configuration.getArguments();
        }

        @Override
        public List<DataTableMetadata> getDataTablesMetadata(CLIODCConfiguration configuration) {
            return configuration.getDataTablesMetadata();
        }

        @Override
        public CLIOParser getParser(CLIODCConfiguration configuration) {
            return configuration.getParser();
        }

        @Override
        public String getCLIODCConfigurationID() {
            return ID;
        }

        @Override
        public boolean registerAsIndicatorDataProvider(CLIODCConfiguration configuration) {
            return configuration.registerAsIndicatorDataProvider();
        }
    }
}
