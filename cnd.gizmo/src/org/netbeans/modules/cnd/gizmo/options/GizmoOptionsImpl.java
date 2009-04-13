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
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObject;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.gizmo.spi.GizmoOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.IntConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightConfigurationManager;
import org.netbeans.modules.dlight.api.tool.DLightTool;
import org.openide.util.NbBundle;

public class GizmoOptionsImpl implements ConfigurationAuxObject, GizmoOptions {
    public static final String PROFILE_ID = "gizmo_options"; // NOI18N

    private PropertyChangeSupport pcs = null;
    private boolean needSave = false;
    private String baseDir;

//    // Profile on Run
    private BooleanConfiguration profileOnRun;
    public static String PROFILE_ON_RUN_PROP = "profileOnRun"; // NOI18N
    private final Map<String, BooleanConfiguration> toolConfigurations;
//    // Cpu
//    private BooleanConfiguration cpu;
//    public static String CPU_PROP = "cpu"; // NOI18N
//    // Memory
//    private BooleanConfiguration memory;
//    public static String MEMORY_PROP = "memory"; // NOI18N
//    // Synchronization
//    private BooleanConfiguration synchronization;
//    public static String SYNCHRONIZATION_PROP = "synchronization"; // NOI18N
    // Data Provider
    public static final int SUN_STUDIO = 0;
    public static final int DTRACE = 1;
    private static final String[] DATA_PROVIDER_NAMES = {
	  getString("SunStudio"),
	  getString("DTrace"),
    };
    private IntConfiguration dataProvider;
    public static String DATA_PROVIDER_PROP = "dataProvider"; // NOI18N
    
    public GizmoOptionsImpl(String baseDir, PropertyChangeSupport pcs) {
        this.baseDir = baseDir;
        this.pcs = pcs;
        DLightConfiguration gizmoConfiguration = DLightConfigurationManager.getInstance().getConfigurationByName("Gizmo");//NOI18N
        toolConfigurations = new HashMap<String, BooleanConfiguration>();
        List<DLightTool> tools = gizmoConfiguration.getToolsSet();
        for (DLightTool tool : tools){
            toolConfigurations.put(tool.getName(), new BooleanConfiguration(null, true, tool.getName(), tool.getName()));
        }
        profileOnRun = new BooleanConfiguration(null, true, null, null);
//        cpu = new BooleanConfiguration(null, true, null, null);
//        memory = new BooleanConfiguration(null, true, null, null);
//        synchronization = new BooleanConfiguration(null, true, null, null);
        dataProvider = new IntConfiguration(null, SUN_STUDIO, DATA_PROVIDER_NAMES, null);
    }

    public Collection<String> getNames() {
        return toolConfigurations.keySet();
    }



    public void initialize() {
        clearChanged();
    }

    public static GizmoOptionsImpl getOptions(Configuration conf) {
        GizmoOptionsImpl gizmoOptions = (GizmoOptionsImpl) conf.getAuxObject(GizmoOptionsImpl.PROFILE_ID);
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


    public BooleanConfiguration getByName(String toolName){
        return toolConfigurations.get(toolName);
    }

    public boolean getValueByName(String toolName){
        return toolConfigurations.get(toolName).getValue();
    }

    public void setByName(String toolName, BooleanConfiguration value){
        toolConfigurations.put(toolName, value);
    }

    public void setValueByName(String toolName, boolean value){
        BooleanConfiguration confguration = toolConfigurations.get(toolName);
        boolean oldValue = confguration == null ? false : confguration.getValue();
        if (confguration == null){
            confguration = new BooleanConfiguration(null, true, toolName, toolName);
            toolConfigurations.put(toolName, confguration);
        }
        confguration.setValue(value);
        checkPropertyChange(toolName, oldValue, value);
    }
//
//    /**
//     * CPU
//     */
//    public BooleanConfiguration getCpu() {
//        return cpu;
//    }
//
//    public boolean getCpuValue() {
//        return getCpu().getValue();
//    }
//
//    public void setCpu(BooleanConfiguration cpu) {
//        this.cpu = cpu;
//    }
//
//    public void setCpuValue(boolean cpu) {
//        boolean oldValue = getCpuValue();
//        getCpu().setValue(cpu);
//        checkPropertyChange(CPU_PROP, oldValue, getCpuValue());
//    }
//
//    /**
//     * Memory
//     */
//    public BooleanConfiguration getMemory() {
//        return memory;
//    }
//
//    public boolean getMemoryValue() {
//        return getMemory().getValue();
//    }
//
//    public void setMemory(BooleanConfiguration memory) {
//        this.memory = memory;
//    }
//
//    public void setMemoryValue(boolean memory) {
//        boolean oldValue = getMemoryValue();
//        getMemory().setValue(memory);
//        checkPropertyChange(MEMORY_PROP, oldValue, getMemoryValue());
//    }
//
//    /**
//     * Synchronization
//     */
//    public BooleanConfiguration getSynchronization() {
//        return synchronization;
//    }
//
//    public boolean getSynchronizationValue() {
//        return getSynchronization().getValue();
//    }
//
//    public void setSynchronization(BooleanConfiguration synchronization) {
//        this.synchronization = synchronization;
//    }
//
//    public void setSynchronizationValue(boolean synchronization) {
//        boolean oldValue = getSynchronizationValue();
//        getSynchronization().setValue(synchronization);
//        checkPropertyChange(MEMORY_PROP, oldValue, getSynchronizationValue());
//    }

    /**
     * Data Provider
     */
    public IntConfiguration getDataProvider() {
        return dataProvider;
    }

    public DataProvider getDataProviderValue() {
        if (getDataProvider().getValue() == SUN_STUDIO) {
            return DataProvider.SUN_STUDIO;
        }
        else if (getDataProvider().getValue() == DTRACE) {
            return DataProvider.DTRACE;
        }
        assert true;
        return GizmoOptions.DataProvider.DTRACE;
    }

    public void setDataProvider(IntConfiguration dataProvider) {
        this.dataProvider = dataProvider;
    }

    public void setDataProviderValue(DataProvider dataProvider) {
        int value = DTRACE;
        if (dataProvider == DataProvider.SUN_STUDIO) {
            value = SUN_STUDIO;
        }
        else if (dataProvider == DataProvider.DTRACE) {
            value = DTRACE;
        }
        else {
            assert true;
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
        GizmoOptionsImpl gizmoOptions = (GizmoOptionsImpl)auxObject;

        oldBoolValue = getProfileOnRun().getValue();
        getProfileOnRun().assign(gizmoOptions.getProfileOnRun());
        checkPropertyChange(PROFILE_ON_RUN_PROP, oldBoolValue, getProfileOnRunValue());
        Set<String> keys = toolConfigurations.keySet();
        for (String key: keys){
            BooleanConfiguration conf = toolConfigurations.get(key);
            oldBoolValue = conf.getValue();
            conf.assign(gizmoOptions.getByName(key));
            checkPropertyChange(key, oldBoolValue, getValueByName(key));
        }
//        oldBoolValue = getCpu().getValue();
//        getCpu().assign(gizmoOptions.getCpu());
//        checkPropertyChange(CPU_PROP, oldBoolValue, getCpu().getValue());
//
//        oldBoolValue = getMemory().getValue();
//        getMemory().assign(gizmoOptions.getMemory());
//        checkPropertyChange(MEMORY_PROP, oldBoolValue, getMemory().getValue());
//
//        oldBoolValue = getSynchronization().getValue();
//        getSynchronization().assign(gizmoOptions.getSynchronization());
//        checkPropertyChange(SYNCHRONIZATION_PROP, oldBoolValue, getSynchronization().getValue());

        oldDValue = getDataProviderValue();
        getDataProvider().assign(gizmoOptions.getDataProvider());
        checkPropertyChange(DATA_PROVIDER_PROP, oldDValue, getDataProviderValue());
    }

    
    @Override
    public GizmoOptionsImpl clone() {
        GizmoOptionsImpl clone = new GizmoOptionsImpl(getBaseDir(), null);

        clone.setProfileOnRun(getProfileOnRun().clone());
        Set<String> keys = toolConfigurations.keySet();
        for (String key: keys){
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
