/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.gizmo.options;

import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObject;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.gizmo.spi.GizmoOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.IntConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightConfigurationManager;
import org.netbeans.modules.dlight.api.tool.DLightTool;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.NbBundle;

public class GizmoOptionsImpl implements ConfigurationAuxObject, GizmoOptions {

    private static enum DataProvidersCollection {

        DEFAULT,
        LINUX,
        WINDOWS
    }
    public static final String PROFILE_ID = "gizmo_options"; // NOI18N
    private final PropertyChangeSupport pcs;
    private boolean needSave = false;
    private String baseDir;
//    // Profile on Run
    private BooleanConfiguration profileOnRun;
    public static final String PROFILE_ON_RUN_PROP = "profileOnRun"; // NOI18N
    private final Map<String, BooleanConfiguration> toolConfigurations;
    private final Map<String, String> toolDescriptions;
    // Data Provider
//    public static final int SUN_STUDIO = 0;
//    public static final int DTRACE = 1;
//    public static final int SIMPLE = 2;
    private static final String[] DATA_PROVIDER_NAMES = {
        getString("SunStudio"),
        getString("DTrace"),
        getString("Simple")
    };
    private static final String[] LINUX_DATA_PROVIDER_NAMES = {
        getString("Simple"),
        getString("SunStudio")
    };
    private static final String[] WINDOWS_DATA_PROVIDER_NAMES = {
        getString("Simple")
    };
    private IntConfiguration dataProvider;
    private DataProvidersCollection currentDPCollection = DataProvidersCollection.DEFAULT;
    public static final String DATA_PROVIDER_PROP = "dataProvider"; // NOI18N

    public GizmoOptionsImpl(String baseDir, PropertyChangeSupport pcs) {
        this.baseDir = baseDir;
        this.pcs = pcs;
        toolConfigurations = new HashMap<String, BooleanConfiguration>();
        toolDescriptions = new HashMap<String, String>();
        profileOnRun = new BooleanConfiguration(null, true, null, null);
        dataProvider = new IntConfiguration(null, 0, DATA_PROVIDER_NAMES, null);
        currentDPCollection = DataProvidersCollection.DEFAULT;
    }

    public void init(Configuration conf) {
        MakeConfiguration makeConfiguration = (MakeConfiguration) conf;
        ExecutionEnvironment execEnv = makeConfiguration.getDevelopmentHost().getExecutionEnvironment();
        DLightConfiguration gizmoConfiguration = DLightConfigurationManager.getInstance().getConfigurationByName("Gizmo");//NOI18N
        List<DLightTool> tools = gizmoConfiguration.getToolsSet();
        for (DLightTool tool : tools) {
            String toolName = tool.getName();
            boolean oldValue = toolConfigurations.get(toolName) == null ? tool.isEnabled() : toolConfigurations.get(toolName).getValue();
            toolConfigurations.put(toolName, new BooleanConfiguration(null, oldValue, toolName, toolName));
            toolDescriptions.put(toolName, tool.getDetailedName());
        }

        //if we have sun studio compiler along compiler collections presentedCompiler
        CompilerSetManager compilerSetManager = CompilerSetManager.getDefault(execEnv);
        List<CompilerSet> compilers = compilerSetManager.getCompilerSets();
        boolean hasSunStudio = false;
        for (CompilerSet cs : compilers) {
            if (cs.isSunCompiler()) {
                hasSunStudio = true;
                break;
            }
        }
        String platform = makeConfiguration.getDevelopmentHost().getBuildPlatformDisplayName();
        int index = getDataProvider().getValue();
        //if there is no SS in toolchain
        if (platform.indexOf("Linux") != -1 || platform.equals("MacOS")) {//NOI18N
            dataProvider = new IntConfiguration(null, 0, LINUX_DATA_PROVIDER_NAMES, null);
            currentDPCollection = DataProvidersCollection.LINUX;
            switch (index) {
                case 0:
                    setDataProviderValue(DataProvider.SIMPLE);
                    break;
                case 1:
                    setDataProviderValue(DataProvider.SUN_STUDIO);
                    break;
                default:
                    setDataProviderValue(DataProvider.SIMPLE);
                    break;
            }
        } else if (platform.indexOf("Solaris") != -1) {//NOI18N
            dataProvider = new IntConfiguration(null, 0, DATA_PROVIDER_NAMES, null);
            currentDPCollection = DataProvidersCollection.DEFAULT;
            switch (index) {
                case 0:
                    setDataProviderValue(DataProvider.SUN_STUDIO);
                    break;
                case 1:
                    setDataProviderValue(DataProvider.DTRACE);
                    break;
                default:
                    setDataProviderValue(DataProvider.SIMPLE);
                    break;
            }
        } else {//Windows or Whatever else //NOI18N
            dataProvider = new IntConfiguration(null, 0, WINDOWS_DATA_PROVIDER_NAMES, null);
            currentDPCollection = DataProvidersCollection.WINDOWS;
            setDataProviderValue(DataProvider.SIMPLE);

        }
    }

    public Collection<String> getNames() {
        return toolConfigurations.keySet();
    }

    public void initialize() {
        clearChanged();
    }

    public static GizmoOptionsImpl getOptions(Configuration conf) {
        GizmoOptionsImpl gizmoOptions = (GizmoOptionsImpl) conf.getAuxObject(GizmoOptionsImpl.PROFILE_ID);
        gizmoOptions.init(conf);
        return gizmoOptions;
    }

    public boolean isModified() {
        return getProfileOnRun().getModified();
    }

    public String getId() {
        return PROFILE_ID;
    }

    private void checkPropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        if (oldValue != newValue && pcs != null) {
            pcs.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    private void checkPropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (oldValue != newValue && pcs != null) {
            pcs.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * Profile On Run
     */
    public BooleanConfiguration getProfileOnRun() {
        return profileOnRun;
    }

    public boolean getProfileOnRunValue() {
        return getProfileOnRun().getValue();
    }

    public void setProfileOnRun(BooleanConfiguration profileOnRun) {
        this.profileOnRun = profileOnRun;
    }

    public void setProfileOnRunValue(boolean profileOnRunValue) {
        boolean oldValue = getProfileOnRunValue();
        getProfileOnRun().setValue(profileOnRunValue);
        checkPropertyChange(PROFILE_ON_RUN_PROP, oldValue, getProfileOnRunValue());
    }

    public BooleanConfiguration getConfigurationByName(String toolName) {
        return toolConfigurations.get(toolName);
    }

    public boolean isConfigurationModified(String toolName) {
        DLightConfiguration gizmoConfiguration = DLightConfigurationManager.getInstance().getConfigurationByName("Gizmo");//NOI18N
        //in case different from the default
        DLightTool tool = gizmoConfiguration.getToolByName(toolName);
        if (toolConfigurations.get(toolName) != null &&  tool != null && (tool.isEnabled() != toolConfigurations.get(toolName).getValue())){
            return true;
        }
        return false;
    }

    public String getDescriptionByName(String toolName) {
        return toolDescriptions.get(toolName);
    }

    public boolean getValueByName(String toolName) {
        return toolConfigurations.get(toolName).getValue();
    }

    public void setByName(String toolName, BooleanConfiguration value) {
        toolConfigurations.put(toolName, value);
    }

    public void setValueByName(String toolName, boolean value) {
        BooleanConfiguration confguration = toolConfigurations.get(toolName);
        boolean oldValue = confguration == null ? false : confguration.getValue();
        if (confguration == null) {
            confguration = new BooleanConfiguration(null, true, toolName, toolName);
            toolConfigurations.put(toolName, confguration);
        }
        confguration.setValue(value);
        checkPropertyChange(toolName, oldValue, value);
    }

    /**
     * Data Provider
     */
    public IntConfiguration getDataProvider() {
        return dataProvider;
    }

    public DataProvider getDataProviderValue() {
        if (currentDPCollection == DataProvidersCollection.DEFAULT) {
            if (getDataProvider().getValue() == 0) {
                return DataProvider.SUN_STUDIO;
            } else if (getDataProvider().getValue() == 1) {
                return DataProvider.DTRACE;
            } else if (getDataProvider().getValue() == 2) {
                return DataProvider.SIMPLE;
            }
        } else if (currentDPCollection == DataProvidersCollection.LINUX) {
            if (getDataProvider().getValue() == 0) {
                return DataProvider.SIMPLE;
            } else if (getDataProvider().getValue() == 1) {
                return DataProvider.SUN_STUDIO;
            }
        } else {
            return DataProvider.SIMPLE;
        }
        assert true;
        return GizmoOptions.DataProvider.SIMPLE;
    }

    public void setDataProvider(IntConfiguration dataProvider) {
        this.dataProvider = dataProvider;
    }

    public void setDataProviderValue(DataProvider dataProvider) {
        int value = 0;
        if (currentDPCollection == DataProvidersCollection.DEFAULT) {
            if (dataProvider == DataProvider.SUN_STUDIO) {
                value = 0;
            } else if (dataProvider == DataProvider.DTRACE) {
                value = 1;
            } else if (dataProvider == DataProvider.SIMPLE) {
                value = 2;
            }
        } else if (currentDPCollection == DataProvidersCollection.LINUX) {
            if (dataProvider == DataProvider.SUN_STUDIO) {
                value = 1;
            } else if (dataProvider == DataProvider.SIMPLE) {
                value = 0;
            }
        } else {
            value = 0;
        }

        DataProvider oldValue = getDataProviderValue();
        getDataProvider().setValue(value);
        checkPropertyChange(DATA_PROVIDER_PROP, oldValue, getDataProviderValue());
    }

    public boolean shared() {
        return false;
    }

    public XMLDecoder getXMLDecoder() {
        return new GizmoOptionsXMLCodec(this);
    }

    public XMLEncoder getXMLEncoder() {
        return new GizmoOptionsXMLCodec(this);
    }

    // interface ProfileAuxObject
    public boolean hasChanged() {
        return needSave;
    }

    // interface ProfileAuxObject
    public void clearChanged() {
        needSave = false;
    }

    public void assign(ConfigurationAuxObject auxObject) {
        boolean oldBoolValue;
        DataProvider oldDValue;
        GizmoOptionsImpl gizmoOptions = (GizmoOptionsImpl) auxObject;

        oldBoolValue = getProfileOnRun().getValue();
        getProfileOnRun().assign(gizmoOptions.getProfileOnRun());
        checkPropertyChange(PROFILE_ON_RUN_PROP, oldBoolValue, getProfileOnRunValue());
        Set<String> keys = toolConfigurations.keySet();
        for (String key : keys) {
            BooleanConfiguration conf = toolConfigurations.get(key);
            oldBoolValue = conf.getValue();
            conf.assign(gizmoOptions.getConfigurationByName(key));
            checkPropertyChange(key, oldBoolValue, getValueByName(key));
        }
        oldDValue = getDataProviderValue();
        getDataProvider().assign(gizmoOptions.getDataProvider());
        checkPropertyChange(DATA_PROVIDER_PROP, oldDValue, getDataProviderValue());
    }

    @Override
    public GizmoOptionsImpl clone(Configuration c) {
        init(c);
        GizmoOptionsImpl clone = new GizmoOptionsImpl(getBaseDir(), null);
        clone.init(c);
        clone.setProfileOnRun(getProfileOnRun().clone());
        Set<String> keys = toolConfigurations.keySet();
        for (String key : keys) {
            BooleanConfiguration conf = toolConfigurations.get(key);
            clone.setByName(key, conf.clone());
        }

        clone.setDataProvider(getDataProvider().clone());
        return clone;
    }

    public String getBaseDir() {
        return baseDir;
    }
    /** Look up i18n strings here */
    private static ResourceBundle bundle;

    protected static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(GizmoOptionsImpl.class);
        }
        return bundle.getString(s);
    }
}
