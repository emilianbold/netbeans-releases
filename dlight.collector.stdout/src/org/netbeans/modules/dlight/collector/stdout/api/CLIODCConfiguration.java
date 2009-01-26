/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.collector.stdout.api;


import java.util.List;
import org.netbeans.modules.dlight.api.collector.DataCollectorConfiguration;
import org.netbeans.modules.dlight.api.indicator.IndicatorDataProviderConfiguration;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.collector.stdout.api.impl.CLIODCConfigurationAccessor;

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
