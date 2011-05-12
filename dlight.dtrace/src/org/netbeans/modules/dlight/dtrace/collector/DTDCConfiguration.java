/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.dtrace.collector;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.dlight.api.collector.DataCollectorConfiguration;
import org.netbeans.modules.dlight.api.indicator.IndicatorDataProviderConfiguration;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.core.stack.datacollector.CpuSamplingSupport;
import org.netbeans.modules.dlight.dtrace.collector.impl.DTDCConfigurationAccessor;

/**
 * DTDCConfiguration is used for  
 * {@link org.netbeans.modules.dlight.dtrace.collector.support.DtraceDataCollector}
 * creation.
 * <p>
 * Several DTDCConfiguration can be provided by active/selected tool(s).
 * <p>
 * <code>DtraceDataCollector</code> deals with attaching dtrace(1M) to a process 
 * and parsing script's output based on the provided DataTableMetadata. 
 * <p>
 * <b>Implementation Restriction</b> <br>
 *  Only the first DataTableMetadata from the provided list is used as a 
 *  basis for default output processing. However it is possible to provide 
 *  own DTraceOutputParser.
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
    /**
     * The property which can be used when tool is created to set path to the script
     */
    public static final String DSCRIPT_TOOL_PROPERTY = "dtrace.script";//NOI18N
    static final String DTDC_CONFIGURATION_ID = "DtraceDataCollectorConfigurationId"; // NOI18N
    private static DTDCConfiguration CPU_SAMPLING;
    private URL scriptUrl;
    private String args;
    private List<DataTableMetadata> datatableMetadata;
    private DTraceOutputParser parser;
    private List<String> requiredPrivileges;
    private boolean stackSupportEnabled = false;
    private int indicatorFiringFactor;
    private boolean standalone;
    private String prefix;

    static {
        DTDCConfigurationAccessor.setDefault(new DTDCConfigurationAccessorImpl());
    }

    /**
     * Constructs DTDCConfiguration object initialized with params.
     * @param scriptUrl path to d-trace script (on the localhost).
     * @param dataTableMetadata metadats description of provided data.
     */
    public DTDCConfiguration(URL scriptUrl, List<DataTableMetadata> dataTableMetadata) {
        this.scriptUrl = scriptUrl;
        this.datatableMetadata = dataTableMetadata;
        this.args = null;
        this.parser = null;
        this.requiredPrivileges = null;
        this.indicatorFiringFactor = 1;
    }

    /**
     * Sets arguments that d-trace script should be invoked with.
     *
     * @param args params string.
     */
    public void setScriptArgs(String args) {
        this.args = args;
    }

    /**
     * Sets parser to be used for script's output parsing. <p>
     * <b>Note</b><br>
     * If stackSupport is enabled with {@see setStackSupportEnabled(boolean)} 
     * then this method has no effect and <b>default</b> parser is used!
     * 
     * @param parser parser to use for script's output parsing.
     */
    public void setDtraceParser(DTraceOutputParser parser) {
        this.parser = parser;
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
     * Sets a list of dtrace(1M) privileges needed to successfully run the script
     * @param requiredPrivileges list of dtrace(1M) privileges needed to
     * successfully run the script
     */
    public void setRequiredDTracePrivileges(List<String> requiredPrivileges) {
        this.requiredPrivileges = new ArrayList<String>(requiredPrivileges);
    }

    /**
     * With setting stackSupportEnabled to <tt>true</tt> using this method,
     * <tt>ConfigurationProvider</tt> indicates that collector is able to
     * provide stack information.
     * <p>
     * <b>Note:</b> <br>
     * In this case the <b>DataAndStackParser</b> is used for output parsing
     * even if setDtraceParser was called. Hence setting this to 
     * <code>true</code> forces dtrace script to follow special output format!
     * <p>
     * @see org.netbeans.modules.dlight.dtrace.collector.support.DataAndStackParser
     * 
     * @param stackSupportEnabled <tt>true</tt> means that stack data is
     * provided; <tt>false</tt> otherwise.
     */
    public void setStackSupportEnabled(boolean stackSupportEnabled) {
        this.stackSupportEnabled = stackSupportEnabled;
    }

    /**
     * If <code>standalone = true</code>, the DTrace script will be executed
     * in a separate <code>dtrace</code> instance. If <code>standalone = false</code>,
     * the DTrace script is merged with scripts from other @{link DTDConfiguration}s
     * and executed in a single <code>dtrace</code>. Latter mode (merged)
     * is the default, because attaching one <code>dtrace</code> instance to
     * a process gives less overhead than attaching many instances.
     *
     * @param standalone  pass <code>true</code> for standalone mode,
     *      <code>false</code> for merged mode.
     */
    public void setStandalone(boolean standalone) {
        this.standalone = standalone;
    }

    /**
     * Prefix is used in merged scripts mode (see {@link #setStandalone(boolean)})
     * to distinguish output from individual DTrace scripts. Format strings of
     * all <code>printf</code> and <code>printa</code> calls in this script
     * are prepended with this prefix before merging with other scripts.
     *
     * @param prefix  prefix to distinguish this script's output after merge
     */
    public void setOutputPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Returns unique ID to be used to identify configuration
     * @return unique id
     */
    @Override
    public String getID() {
        return DTDC_CONFIGURATION_ID;
    }

    public static synchronized DTDCConfiguration createCpuSamplingConfiguration() {
        if (CPU_SAMPLING == null) {
            CPU_SAMPLING = new DTDCConfiguration(CpuSamplingSupport.CPU_SAMPLING_SCRIPT_URL, Arrays.asList(CpuSamplingSupport.CPU_SAMPLE_TABLE));
            CPU_SAMPLING.setStackSupportEnabled(true);
            CPU_SAMPLING.setOutputPrefix("cpu:"); // NOI18N
        }
        return CPU_SAMPLING;
    }

    private static final class DTDCConfigurationAccessorImpl extends DTDCConfigurationAccessor {

        @Override
        public String getArgs(DTDCConfiguration conf) {
            return conf.args;
        }

        @Override
        public List<DataTableMetadata> getDatatableMetadata(DTDCConfiguration conf) {
            return conf.datatableMetadata == null ? null
                    : Collections.unmodifiableList(conf.datatableMetadata);
        }

        @Override
        public DTraceOutputParser getParser(DTDCConfiguration conf) {
            return conf.parser;
        }

        @Override
        public List<String> getRequiredPrivileges(DTDCConfiguration conf) {
            return conf.requiredPrivileges == null ? null
                    : Collections.unmodifiableList(conf.requiredPrivileges);
        }

        @Override
        public URL getScriptUrl(DTDCConfiguration conf) {
            return conf.scriptUrl;
        }

        @Override
        public String getID() {
            return DTDC_CONFIGURATION_ID;
        }

        @Override
        public boolean isStackSupportEnabled(DTDCConfiguration conf) {
            return conf.stackSupportEnabled;
        }

        @Override
        public int getIndicatorFiringFactor(DTDCConfiguration conf) {
            return conf.indicatorFiringFactor;
        }

        @Override
        public boolean isStandalone(DTDCConfiguration conf) {
            return conf.standalone;
        }

        @Override
        public String getOutputPrefix(DTDCConfiguration conf) {
            return conf.prefix;
        }
    }
}
