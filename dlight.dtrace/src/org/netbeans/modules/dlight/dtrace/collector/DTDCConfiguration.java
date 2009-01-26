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
package org.netbeans.modules.dlight.dtrace.collector;

import java.util.List;
import org.netbeans.modules.dlight.dtrace.collector.impl.DTDCConfigurationAccessor;
import org.netbeans.modules.dlight.dtrace.collector.support.DtraceParser;
import org.netbeans.modules.dlight.collector.api.DataCollectorConfiguration;
import org.netbeans.modules.dlight.indicator.api.IndicatorDataProviderConfiguration;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata;

/**
 * Configuration object for creating
 * {@link org.netbeans.modules.dlight.dtrace.collector.support.DtraceDataCollector}.
 */
public final class DTDCConfiguration implements
        DataCollectorConfiguration, IndicatorDataProviderConfiguration {

    /**
     * String constant that identifies dtrace_kernel privilege (to be used with
     * dtrace(1M))
     */
    public static final String DTRACE_KERNEL = "dtrace_kernel"; // NOI18N
    /**
     * String constant that identifies dtrace_user privilege (to be used with
     * dtrace(1M))
     */
    public static final String DTRACE_USER = "dtrace_user"; // NOI18N
    /**
     * String constant that identifies dtrace_proc privilege (to be used with
     * dtrace(1M))
     */
    public static final String DTRACE_PROC = "dtrace_proc"; // NOI18N
    static final String DTDC_CONFIGURATION_ID = "DtraceDataCollectorConfigurationId"; // NOI18N
    private String scriptPath;
    private String args;
    private List<DataTableMetadata> datatableMetadata;
    private DtraceParser parser;
    private List<String> requiredPrivileges;
    private boolean stackSupportEnabled = false;
    private int indicatorFiringFactor;


    static {
        DTDCConfigurationAccessor.setDefault(new DTDCConfigurationAccessorImpl());
    }

    /**
     * Constructs DTDCConfiguration object initialized with params.
     * @param scriptPath path to d-trace script (on the localhost).
     * @param dataTableMetadata metadats description of provided data.
     */
    public DTDCConfiguration(String scriptPath, List<DataTableMetadata> dataTableMetadata) {
        this.scriptPath = scriptPath;
        this.datatableMetadata = dataTableMetadata;
        this.args = null;
        this.parser = null;
        this.requiredPrivileges = null;
    }

    /**
     * Sets arguments that d-tarce script should be invoked with.
     *
     * @param args params string.
     */
    public void setScriptArgs(String args) {
        this.args = args;
    }

    /**
     * Sets parser to be used for script's output parsing.
     * @param parser parser to use for script's output parsing.
     */
    public void setDtraceParser(DtraceParser parser) {
        this.parser = parser;
    }

    DTDCConfiguration setParser(DtraceParser parser) {
        this.parser = parser;
        return this;
    }

    /**
     * Sets size of buffer that accumulates script's output before notify
     * indicators about new data arrival.
     * @param indicatorFiringFactor number of events that should arrive before
     * next indicators notification event is issued.
     */
    public void setIndicatorFiringFactor(int indicatorFiringFactor) {
        this.indicatorFiringFactor = indicatorFiringFactor;
    }

    /**
     * Returns list of dtrace privileges needed to successfully run the script
     * @param requiredPrivileges list of dtrace privileges needed to
     * successfully run the script
     */
    public void setRequiredDTracePrivileges(List<String> requiredPrivileges) {
        this.requiredPrivileges = requiredPrivileges;
    }

    /**
     * With setting stackSupportEnabled to <tt>true</tt> using this method,
     * <tt>ConfigurationProvider</tt> indicates that collector is able to
     * provide stack information.
     * @param stackSupportEnabled <tt>true</tt> means that stack data is
     * provided; <tt>false</tt> otherwise.
     */
    public void setStackSupportEnabled(boolean stackSupportEnabled) {
        this.stackSupportEnabled = stackSupportEnabled;
    }

    int getIndicatorFiringFactor() {
        return indicatorFiringFactor;
    }

    String getArgs() {
        return args;
    }

    List<DataTableMetadata> getDatatableMetadata() {
        return datatableMetadata;
    }

    DtraceParser getParser() {
        return parser;
    }

    List<String> getRequiredPrivileges() {
        return requiredPrivileges;
    }

    String getScriptPath() {
        return scriptPath;
    }

    boolean isStackSupportEnabled() {
        return stackSupportEnabled;
    }

    /**
     * Returns unique ID to be used to identify configuration
     * @return unique id
     */
    public String getID() {
        return DTDC_CONFIGURATION_ID;
    }

    private static final class DTDCConfigurationAccessorImpl extends DTDCConfigurationAccessor {

        @Override
        public String getArgs(DTDCConfiguration conf) {
            return conf.getArgs();
        }

        @Override
        public List<DataTableMetadata> getDatatableMetadata(DTDCConfiguration conf) {
            return conf.getDatatableMetadata();
        }

        @Override
        public DtraceParser getParser(DTDCConfiguration conf) {
            return conf.getParser();
        }

        @Override
        public List<String> getRequiredPrivileges(DTDCConfiguration conf) {
            return conf.getRequiredPrivileges();
        }

        @Override
        public String getScriptPath(DTDCConfiguration conf) {
            return conf.getScriptPath();
        }

        @Override
        public String getID() {
            return DTDC_CONFIGURATION_ID;
        }

        @Override
        public boolean isStackSupportEnabled(DTDCConfiguration conf) {
            return conf.isStackSupportEnabled();
        }

        @Override
        public int getIndicatorFiringFactor(DTDCConfiguration conf) {
            return conf.getIndicatorFiringFactor();
        }
    }
}
