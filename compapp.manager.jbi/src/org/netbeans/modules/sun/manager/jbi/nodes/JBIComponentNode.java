/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.sun.manager.jbi.nodes;

import com.sun.esb.management.api.administration.AdministrationService;
import com.sun.esb.management.api.configuration.ConfigurationService;
import com.sun.esb.management.api.installation.InstallationService;
import com.sun.esb.management.common.ManagementRemoteException;
import com.sun.esb.management.common.data.ComponentStatisticsData;
import com.sun.jbi.ui.common.JBIComponentInfo;
import com.sun.jbi.ui.common.ServiceAssemblyInfo;
import java.io.IOException;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.sun.manager.jbi.management.JBIMBeanTaskResultHandler;
import org.netbeans.modules.sun.manager.jbi.util.ProgressUI;
import org.netbeans.modules.sun.manager.jbi.GenericConstants;
import org.netbeans.modules.sun.manager.jbi.actions.AdvancedAction;
import org.netbeans.modules.sun.manager.jbi.actions.RefreshAction;
import org.netbeans.modules.sun.manager.jbi.actions.ShowComponentEndpointsStatisticsAction;
import org.netbeans.modules.sun.manager.jbi.actions.ShutdownAction;
import org.netbeans.modules.sun.manager.jbi.actions.StartAction;
import org.netbeans.modules.sun.manager.jbi.actions.StopAction;
import org.netbeans.modules.sun.manager.jbi.actions.UndeployAction;
import org.netbeans.modules.sun.manager.jbi.actions.UninstallAction;
import org.netbeans.modules.sun.manager.jbi.actions.UpgradeAction;
import org.netbeans.modules.sun.manager.jbi.nodes.property.SchemaBasedConfigPropertySupportFactory;
import org.netbeans.modules.sun.manager.jbi.management.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.management.model.ComponentActionDescriptor;
import org.netbeans.modules.sun.manager.jbi.management.model.ComponentConfigurationDescriptor;
import org.netbeans.modules.sun.manager.jbi.management.ConfigurationMBeanAttributeInfo;
import org.netbeans.modules.sun.manager.jbi.management.JBIComponentType;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.PerformanceMeasurementServiceWrapper;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.RuntimeManagementServiceWrapper;
import org.netbeans.modules.sun.manager.jbi.nodes.property.JBIPropertySupportFactory;
import org.netbeans.modules.sun.manager.jbi.util.ComparableAttribute;
import org.netbeans.modules.sun.manager.jbi.util.DoNotShowAgainConfirmation;
import org.netbeans.modules.sun.manager.jbi.util.FileFilters;
import org.netbeans.modules.sun.manager.jbi.util.StackTraceUtil;
import org.netbeans.modules.sun.manager.jbi.util.Utils;
import org.openide.nodes.Sheet;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Abstract Node class for a JBI Component (Service Engine, Binding Component,
 * or Shared Library).
 *
 * @author jqian
 */
public abstract class JBIComponentNode extends AppserverJBIMgmtLeafNode
        implements Refreshable, Startable, Stoppable, Shutdownable,
        Uninstallable, Undeployable, Upgradeable {

    public static final String ENVIRONMENT_VARIABLES_NAME = "EnvironmentVariables"; // NOI18N
    public static final String APPLICATION_VARIABLES_NAME = "ApplicationVariables"; // NOI18N
    public static final String APPLICATION_CONFIGURATIONS_NAME = "ApplicationConfigurations"; // NOI18N
    public static final String LAST_JBI_COMPONENT_INSTALLATION_DIRECTORY =
            "lastJBIComponentInstallationDir"; // NOI18N    
    private static final String IDENTIFICATION_SHEET_SET_NAME = "IDENTIFICATION"; // NOI18N
    private static final String LOGGERS_SHEET_SET_NAME = "LOGGERS"; // NOI18N
    private static final String CONFIGURATION_SHEET_SET_NAME = "CONFIGURATION"; // NOI18N
    private static final String COMPONENT_STATISTICS_SHEET_SET_NAME = "COMPONENT_STATISTICS"; // NOI18N
    private static final String ACTIONABLE_MBEAN_NAME = "ManagementActions";
    private static final String ACTIONABLE_MBEAN_GET_ACTIONS_OPERATION_NAME = "getActions";
    private boolean busy;
    private JBIComponentType compType;
    // Cached component configuration schema
    private String configSchema;
    // Whether the component's configuration schema has been checked or not. 
    private boolean hasConfigSchemaBeenChecked;
    // This is not persistent across sessions.
    private static boolean confirmComponentUninstallation = true;
    // This is not persistent across sessions.
    private static boolean confirmComponentShutdownDuringUpgrade = true;
    // This is not persistent across sessions.
    private static boolean confirmForServiceAssembliesUndeployment = true;
    
    private static Logger logger = Logger.getLogger("org.netbeans.modules.sun.manager.jbi.nodes.JBIComponentNode"); // NOI18N


    public JBIComponentNode(final AppserverJBIMgmtController controller,
            JBIComponentType compType, NodeType nodeType,
            String name, String description) {
        super(controller, nodeType);

        setName(name);

        setDisplayName(name);

        // Use HTML version for tooltip.
        setShortDescription(Utils.getTooltip(description));
        // Use non-HTML version in the property sheet's description area.
        setValue("nodeDescription", description); // NOI18N 

        this.compType = compType;
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = new Sheet();

        addSheetSet(sheet,
                GENERAL_SHEET_SET_NAME,
                "LBL_GENERAL_PROPERTIES", // NOI18N
                "DSC_GENERAL_PROPERTIES", // NOI18N
                getGeneralSheetSetProperties());

        // 1. Augment the general property sheet by adding Identification sheet
        try {
            String installationDescriptor = getInstallationDescriptor();
            Map<Attribute, MBeanAttributeInfo> identificationPropertyMap =
                    getIdentificationSheetSetProperties(installationDescriptor);
            addSheetSet(sheet,
                    IDENTIFICATION_SHEET_SET_NAME,
                    "LBL_IDENTIFICATION_PROPERTIES", // NOI18N
                    "DSC_IDENTIFICATION_PROPERTIES", // NOI18N
                    identificationPropertyMap);
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }

        // 2. Augment the general property sheet by adding Configuration sheet
        try {
            // #114173 The configuration schema is only available when  
            // the component is in started state.
            boolean isStarted = false;
            Sheet.Set generalSheetSet = sheet.get(GENERAL_SHEET_SET_NAME);
            if (generalSheetSet != null) {
                Property stateProperty = generalSheetSet.get("State"); // NOI18N
                if (stateProperty != null) {
                    String state = (String) stateProperty.getValue();
                    if (state != null && state.equalsIgnoreCase("started")) { // NOI18N
                        isStarted = true;
                    }
                }
            }
            if (isStarted) {
                if (!hasConfigSchemaBeenChecked) {
                    ConfigurationService configService = getConfigurationService();
                    String compName = getName();
                    configSchema = configService.retrieveConfigurationDisplaySchema(
                            compName, SERVER_TARGET);
                    hasConfigSchemaBeenChecked = true;
                }
            }

            Map<Attribute, ? extends MBeanAttributeInfo> configPropertyMap =
                    getConfigurationSheetSetProperties();

            Sheet.Set sheetSet = null;
            if (configSchema != null && configSchema.trim().length() > 0) {
                PropertySupport[] propertySupports =
                        createPropertySupportArrayWithSchema(configPropertyMap);
                sheetSet = createSheetSet(CONFIGURATION_SHEET_SET_NAME,
                        "LBL_CONFIG_PROPERTIES", // NOI18N
                        "DSC_CONFIG_PROPERTIES", // NOI18N
                        propertySupports);
            } else {
                sheetSet = createSheetSet(CONFIGURATION_SHEET_SET_NAME,
                        "LBL_CONFIG_PROPERTIES", // NOI18N
                        "DSC_CONFIG_PROPERTIES", // NOI18N
                        configPropertyMap);
            }

            if (sheetSet != null) {
                sheet.put(sheetSet);
            }
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }

        // 3. Augment the general property sheet by adding loggers sheet
        try {
            addSheetSet(sheet,
                    LOGGERS_SHEET_SET_NAME,
                    "LBL_LOGGERS_PROPERTIES", // NOI18N
                    "DSC_LOGGERS_PROPERTIES", // NOI18N
                    getLoggerSheetSetProperties());
        } catch (ManagementRemoteException e) {
            logger.warning(e.getMessage());
        }

        // 4. Augment the general property sheet by adding component statistics sheet
        try {
            addSheetSet(sheet,
                    COMPONENT_STATISTICS_SHEET_SET_NAME,
                    "LBL_COMPONENT_STATISTICS_PROPERTIES", // NOI18N
                    "DSC_COMPONENT_STATISTICS_PROPERTIES", // NOI18N
                    geComponentStatisticsSheetSetProperties());
        } catch (ManagementRemoteException e) {
            logger.warning(e.getMessage());
        }

        return sheet;
    }

    private Map<Attribute, MBeanAttributeInfo> geComponentStatisticsSheetSetProperties()
            throws ManagementRemoteException {
        AppserverJBIMgmtController controller = getAppserverJBIMgmtController();
        PerformanceMeasurementServiceWrapper perfService =
                controller.getPerformanceMeasurementServiceWrapper();
        ComponentStatisticsData statistics =
                perfService.getComponentStatistics(getName(), SERVER_TARGET);
        return Utils.getIntrospectedPropertyMap(statistics, true);
    }

    protected PropertySupport[] createPropertySupportArrayWithSchema(
            final Map<Attribute, ? extends MBeanAttributeInfo> attrMap) {

        assert configSchema != null;

        List<PropertySupport> supports = new ArrayList<PropertySupport>();

        try {
            String compName = getName();

            for (Attribute attr : attrMap.keySet()) {
                MBeanAttributeInfo info = attrMap.get(attr);

                PropertySupport support = SchemaBasedConfigPropertySupportFactory.
                        getPropertySupport(configSchema, compName, this, attr, info);

                if (support == null) {

                    if (attr.getValue() instanceof TabularData) {
                        // There is no schema support for tabular data.
                        support = JBIPropertySupportFactory.getPropertySupport(
                                this, attr, info);
                        supports.add(support);
                        continue;
                    }

                    String msg = "Failed to get property support for " +
                            compName + ":" + attr.getName() + ". " +
                            "Missing definition in configuration schema.";
                    NotifyDescriptor d = new NotifyDescriptor.Message(
                            msg, NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                } else {
                    supports.add(support);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return supports.toArray(new PropertySupport[0]);
    }

    private Map<Attribute, MBeanAttributeInfo> getGeneralSheetSetProperties() {
        JBIComponentInfo componentInfo = getJBIComponentInfo();
        return Utils.getIntrospectedPropertyMap(componentInfo, false,
                MODEL_BEAN_INFO_PACKAGE_NAME);
    }

    /**
     * Gets the property map mapping from Attribute to MBeanAttributeInfo.
     * 
     * If there is no schema defined for the component configurations, then
     * all the attributes will be sorted based on their names (not display names).
     * 
     * If there is a schema defined for the component configurations, then
     * all the attributes will be sorted based on the sequence definition
     * in the schema.
     * 
     * @return
     */
    private Map<Attribute, ? extends MBeanAttributeInfo> 
            getConfigurationSheetSetProperties()
            throws ManagementRemoteException {

        Map<Attribute, MBeanAttributeInfo> ret =
                new LinkedHashMap<Attribute, MBeanAttributeInfo>();

        ConfigurationService configService = getConfigurationService();
        String compName = getName();

        Map<String, Object> configMap =
                configService.getComponentConfigurationAsMap(
                compName, SERVER_TARGET);

        try {
            String configXmlData = configService.retrieveConfigurationDisplayData(
                    compName, SERVER_TARGET);

            ComponentConfigurationDescriptor rootDescriptor =
                    ComponentConfigurationDescriptor.parse(configXmlData);

            if (rootDescriptor == null) {
                // Fallback on regular attributes if the component does not have 
                // configuration schema defined yet.
                List<String> keys = new ArrayList<String>();
                keys.addAll(configMap.keySet());
                Collections.sort(keys);

                for (String key : keys) {
                    Object value = configMap.get(key);
                    Attribute attr = new Attribute(key, value);
                    MBeanAttributeInfo attrInfo = new MBeanAttributeInfo(
                            key,
                            value.getClass().getName(),
                            key, // need acess to MBeanAttributeInfo
                            true, true, false);
                    ret.put(attr, attrInfo);
                }

                try {
                    TabularData appVars =
                            configService.getApplicationVariablesAsTabularData(
                            getName(), SERVER_TARGET);
                    Attribute attr =
                            new Attribute(APPLICATION_VARIABLES_NAME, appVars);
                    MBeanAttributeInfo attrInfo =
                            new MBeanAttributeInfo(APPLICATION_VARIABLES_NAME,
                            "javax.management.openmbean.TabularData", // NOI18N
                            "Application variables",
                            true, true, false);
                    ret.put(attr, attrInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    TabularData appConfigs =
                            configService.getApplicationConfigurationsAsTabularData(
                            getName(), SERVER_TARGET);
                    Attribute attr =
                            new Attribute(APPLICATION_CONFIGURATIONS_NAME, appConfigs);
                    MBeanAttributeInfo attrInfo =
                            new MBeanAttributeInfo(APPLICATION_CONFIGURATIONS_NAME,
                            "javax.management.openmbean.TabularData", // NOI18N
                            "Application configurations",
                            true, true, false);
                    ret.put(attr, attrInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                // Attributes are ordered based on schema definition.
                for (String name : rootDescriptor.getChildNames()) {
                    Object value = configMap.get(name);

                    if (value == null) {
                        if (name.equals(APPLICATION_CONFIGURATIONS_NAME)) {
                            value = configService.getApplicationConfigurationsAsTabularData(
                                    getName(), SERVER_TARGET);
                        } else if (name.equals(APPLICATION_VARIABLES_NAME)) {
                            value = configService.getApplicationVariablesAsTabularData(
                                    getName(), SERVER_TARGET);
                        }
                    }

                    Attribute attr = new Attribute(name, value);

                    ComponentConfigurationDescriptor childDescriptor =
                            rootDescriptor.getChild(name);
                    MBeanAttributeInfo attrInfo =
                            new ConfigurationMBeanAttributeInfo(
                            childDescriptor,
                            value.getClass().getName(),
                            true, true, false);

                    ret.put(attr, attrInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * Gets the logger properties to be displayed for this JBI Component.
     *
     * @return A java.util.Map containing all logger properties.
     */
    private Map<Attribute, MBeanAttributeInfo> getLoggerSheetSetProperties()
            throws ManagementRemoteException {

        // Sorted by the fully qualified logger name (loggerCustomName).
        // Only display the short name in the property sheet.
        Map<Attribute, MBeanAttributeInfo> ret =
                new TreeMap<Attribute, MBeanAttributeInfo>();

        ConfigurationService configService = getConfigurationService();
        Map<String, Level> loggerMap = configService.getComponentLoggerLevels(
                getName(), SERVER_TARGET, null); // NULL?    

        for (String loggerCustomName : loggerMap.keySet()) {
            Level logLevel = loggerMap.get(loggerCustomName);
            int lastDotIndex = loggerCustomName.lastIndexOf("."); // NOI18N
            String shortName = lastDotIndex == -1 ? loggerCustomName : 
                loggerCustomName.substring(lastDotIndex + 1);

            Attribute attr = new Attribute(loggerCustomName, logLevel);
            MBeanAttributeInfo info = new MBeanAttributeInfo(
                    shortName,
                    "java.util.logging.Level", // NOI18N
                    loggerCustomName,
                    true, true, false);
            ret.put(new ComparableAttribute(attr), info);
        }

        return ret;
    }

    /**
     * Sets the property as an attribute to the underlying AMX mbeans. It
     * usually will delegate to the controller object which is responsible for
     * finding the correct AMX mbean objectname in order to execute a
     * JMX setAttribute.
     *
     * @param attrName The name of the property to be set.
     * @param value The value retrieved from the property sheet to be set in the
     *        backend.
     * @returns the updated Attribute accessed from the Sheet.
     */
    public Attribute setSheetProperty(String attrName, Object value) {

        if (StackTraceUtil.isCalledBy(
                "org.openide.explorer.propertysheet.PropertyDialogManager", // NOI18N
                "cancelValue")) { // NOI18N
            return new Attribute(attrName, value);
        }
        // Only configuration properties on the component's property sheet is editable.
        try {
            ConfigurationService configService = getConfigurationService();
            String compName = getName();
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(attrName, value);
            configService.setComponentConfiguration(compName, map, SERVER_TARGET);

            // Get the new value
            value = configService.getComponentConfigurationAsMap(
                    compName, SERVER_TARGET).get(attrName);
        } catch (ManagementRemoteException e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }

        return new Attribute(attrName, value);
    }

    public Attribute setLoggerSheetProperty(String loggerName, Level value) {
        try {
            ConfigurationService configService = getConfigurationService();
            String compName = getName();
            configService.setComponentLoggerLevel(
                    compName, loggerName, value, SERVER_TARGET, null); // null?

            // changing top-level logger property has bigger impact
            updatePropertySheet();
        } catch (Exception e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            value = null;
        }

        return new Attribute(loggerName, value);
    }

    /**
     * Updates an existing application variable.
     * 
     * @param attrName  application variable name
     * @param value     new value of the application variable
     */
    public void setApplicationVariable(String attrName, CompositeData value)
            throws ManagementRemoteException {
        ConfigurationService configService = getConfigurationService();
        String compName = getName();
        configService.setApplicationVariable(compName, SERVER_TARGET, attrName, value);
    }

    /**
     * Adds a new application variable.
     * 
     * @param attrName  application variable name
     * @param value     value of composite data
     */
    public void addApplicationVariable(String attrName, CompositeData value)
            throws ManagementRemoteException {
        ConfigurationService configService = getConfigurationService();
        String compName = getName();
        configService.addApplicationVariable(compName, SERVER_TARGET, attrName, value);
    }

    /**
     * Deletes an existing application variable.
     * 
     * @param attrName  application variable name
     */
    public void deleteApplicationVariable(String name)
            throws ManagementRemoteException {
        ConfigurationService configService = getConfigurationService();
        String compName = getName();
        configService.deleteApplicationVariables(compName, SERVER_TARGET,
                new String[]{name}); // TODO: support mass-deletion
    }

    /**
     * Updates an existing application configuration.
     * 
     * @param attrName  application configuration name
     * @param value     new value of composite data
     */
    public void setApplicationConfiguration(String name, CompositeData value)
            throws ManagementRemoteException {
        ConfigurationService configService = getConfigurationService();
        String compName = getName();
        configService.setApplicationConfiguration(
                compName, SERVER_TARGET, name, value);
    }

    /**
     * Adds a new application configuration.
     * 
     * @param name  application configuration name
     * @param value new value of composite data
     */
    public void addApplicationConfiguration(String name, CompositeData value)
            throws ManagementRemoteException {
        ConfigurationService configService = getConfigurationService();
        String compName = getName();
        configService.addApplicationConfiguration(
                compName, SERVER_TARGET, name, value);
    }

    /**
     * Deletes an existing application configuration.
     * 
     * @param attrName  application configuration name
     */
    public void deleteApplicationConfiguration(String name)
            throws ManagementRemoteException {
        ConfigurationService configService = getConfigurationService();
        String compName = getName();
        configService.deleteApplicationConfiguration(
                compName, SERVER_TARGET, name);
    }

    public void refresh() {
        // Explicitly reset the property sheet (since the property sheet in 
        // AbstractNode is "sticky").
        setSheet(createSheet());
    }

    /**
     *
     */
    @Override
    public Image getIcon(int type) {
        String state = getState();
        String iconName = getIconName(state);

        String externalBadgeIconName = null;
        if (busy) {
            externalBadgeIconName = IconConstants.BUSY_ICON;
        } else {
            if (JBIComponentInfo.SHUTDOWN_STATE.equals(state)) {
                externalBadgeIconName = getInstalledIconBadgeName();
            } else if (JBIComponentInfo.STOPPED_STATE.equals(state)) {
                externalBadgeIconName = getStoppedIconBadgeName();
            } else if (!JBIComponentInfo.STARTED_STATE.equals(state)) {
                externalBadgeIconName = getUnknownIconBadgeName();
            }
        }

        return Utils.getBadgedIcon(
                getClass(), iconName, null, externalBadgeIconName);
    }

    protected String getInstalledIconBadgeName() {
        return IconConstants.INSTALLED_ICON;
    }

    protected String getStoppedIconBadgeName() {
        return IconConstants.STOPPED_ICON;
    }

    protected String getUnknownIconBadgeName() {
        return IconConstants.UNKNOWN_ICON;
    }

    /**
     *
     * @param busy
     */
    private void setBusy(boolean busy) {
        this.busy = busy;
        fireIconChange();
    }

    /**
     *
     * @return
     */
    private JBIComponentInfo getJBIComponentInfo() {
        RuntimeManagementServiceWrapper mgmtService =
                getRuntimeManagementServiceWrapper();

        if (mgmtService != null) {
            try {
                return mgmtService.getJBIComponent(
                        compType, getName(), SERVER_TARGET);
            } catch (ManagementRemoteException e) {
                NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(),
                        NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        }

        return null;
    }

    private void clearJBIComponentStatusCache(JBIComponentType compType) {
        getRuntimeManagementServiceWrapper().clearJBIComponentStatusCache(compType);
    }

    /**
     *
     * @return
     */
    private String getState() {
        JBIComponentInfo info = getJBIComponentInfo();
        return info == null ? null : info.getState();
    }

    private void updatePropertySheet() {
        Sheet sheet = createSheet();
        setSheet(sheet);
        firePropertySetsChange(null, null);
    }

    //========================== Startable =====================================
    public boolean canStart() {
        String state = getState();
        return !busy &&
                (JBIComponentInfo.STOPPED_STATE.equals(state) ||
                JBIComponentInfo.SHUTDOWN_STATE.equals(state));
    }

    public void start() {
        RuntimeManagementServiceWrapper mgmtService =
                getRuntimeManagementServiceWrapper();
        if (mgmtService == null) {
            return;
        }

        String progressLabel = getStartProgressLabel();
        final String componentName = getName();
        String title =
                NbBundle.getMessage(JBIComponentNode.class, progressLabel,
                new Object[]{componentName});
        final ProgressUI progressUI = new ProgressUI(title, false);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setBusy(true);
                progressUI.start();
            }
        });

        String result = null;
        try {
            result = mgmtService.startComponent(componentName, SERVER_TARGET);           
        } catch (ManagementRemoteException e) {
            result = e.getMessage();
        } finally {
            JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                GenericConstants.START_COMPONENT_OPERATION_NAME,
                componentName, result);
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                clearJBIComponentStatusCache(compType);
                progressUI.finish();
                setBusy(false);
                updatePropertySheet();
            }
        });
    }

    //========================== Stoppable =====================================
    public boolean canStop() {
        return !busy && JBIComponentInfo.STARTED_STATE.equals(getState());
    }

    public void stop() {
        RuntimeManagementServiceWrapper mgmtService =
                getRuntimeManagementServiceWrapper();
        if (mgmtService == null) {
            return;
        }

        String progressLabel = getStopProgressLabel();
        final String componentName = getName();
        String title =
                NbBundle.getMessage(JBIComponentNode.class, progressLabel,
                new Object[]{componentName});
        final ProgressUI progressUI = new ProgressUI(title, false);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setBusy(true);
                progressUI.start();
            }
        });

        String result = null;
        try {
            result = mgmtService.stopComponent(componentName, SERVER_TARGET);
        } catch (ManagementRemoteException e) {
            result = e.getMessage();
        } finally {
             JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                    GenericConstants.STOP_COMPONENT_OPERATION_NAME,
                    componentName, result);
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                clearJBIComponentStatusCache(compType);
                progressUI.finish();
                setBusy(false);
                updatePropertySheet();
            }
        });
    }

    //========================== Shutdownable ==================================
    public boolean canShutdown() {
        return canStop() ||
                !busy && JBIComponentInfo.STOPPED_STATE.equals(getState());
    }

    public void shutdown(boolean force) {

        RuntimeManagementServiceWrapper mgmtService =
                getRuntimeManagementServiceWrapper();
        if (mgmtService == null) {
            return;
        }

        if (canStop()) {
            stop();
        }

        String progressLabel = getShutdownProgressLabel();
        final String componentName = getName();
        String title =
                NbBundle.getMessage(JBIComponentNode.class, progressLabel,
                new Object[]{componentName});
        final ProgressUI progressUI = new ProgressUI(title, false);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setBusy(true);
                progressUI.start();
            }
        });

        String result = null;
        try {
            result = mgmtService.shutdownComponent(componentName, force, SERVER_TARGET);
        } catch (ManagementRemoteException e) {
            result = e.getMessage();
        } finally {
            JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                    GenericConstants.SHUTDOWN_COMPONENT_OPERATION_NAME,
                    componentName, result);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                clearJBIComponentStatusCache(compType);
                progressUI.finish();
                setBusy(false);
                updatePropertySheet();
            }
        });
    }

    //========================== Uninstallable =================================
    public boolean canUninstall() {
        return canShutdown() ||
                !busy && JBIComponentInfo.SHUTDOWN_STATE.equals(getState());
    }

    public void uninstall(boolean force) {

        InstallationService mgmtService = getInstallationService();
        if (mgmtService == null) {
            return;
        }

        if (canShutdown()) {
            shutdown(force);
        }

        final String componentName = getName();

        if (confirmComponentUninstallation) {
            DoNotShowAgainConfirmation d = new DoNotShowAgainConfirmation(
                    NbBundle.getMessage(JBIComponentNode.class,
                    "MSG_UNINSTALL_CONFIRMATION", componentName), // NOI18N
                    NbBundle.getMessage(JBIComponentNode.class,
                    "TTL_UNINSTALL_CONFIRMATION"), // NOI18N
                    NotifyDescriptor.YES_NO_OPTION);
            if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.YES_OPTION) {
                return;
            }

            if (d.getDoNotShowAgain()) {
                confirmComponentUninstallation = false;
            }
        }

        String progressLabel = getUninstallProgressLabel();
        String title = NbBundle.getMessage(JBIComponentNode.class, progressLabel,
                new Object[]{componentName});
        final ProgressUI progressUI = new ProgressUI(title, false);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressUI.start();
            }
        });

        String result = null;
        try {
            result = uninstallComponent(mgmtService, componentName, force);
        } catch (ManagementRemoteException e) {
            result = e.getMessage();
        } finally {
            JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                    GenericConstants.UNINSTALL_COMPONENT_OPERATION_NAME,
                    componentName, result);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                clearJBIComponentStatusCache(compType);
                progressUI.finish();
            }
        });
    }

    //========================== Upgradeable =================================
    public boolean canUpgrade() {
        return !busy;
    }

    public void upgrade() {

        InstallationService installationService = getInstallationService();
        if (installationService == null) {
            return;
        }

        String componentName = getName();

        JFileChooser chooser = getJFileChooser();
        int returnValue = chooser.showDialog(null,
                NbBundle.getMessage(JBIComponentNode.class,
                "LBL_Upgrade_JBI_Component_Button")); //NOI18N

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = chooser.getSelectedFiles();
            if (selectedFiles.length > 0) {
                System.setProperty(LAST_JBI_COMPONENT_INSTALLATION_DIRECTORY,
                        selectedFiles[0].getParent());
            }

            List<File> files = filterSelectedFiles(selectedFiles);
            if (files.size() == 0) {
                return;
            }

            // Automatic component shutdown before calling upgrade
            String oldState = getState();
            if (JBIComponentInfo.STOPPED_STATE.equals(oldState) ||
                    JBIComponentInfo.STARTED_STATE.equals(oldState)) {
                if (confirmComponentShutdownDuringUpgrade) {
                    DoNotShowAgainConfirmation d = new DoNotShowAgainConfirmation(
                            NbBundle.getMessage(JBIComponentNode.class,
                            "MSG_AUTO_SHUTDOWN_COMPONENT_DURING_UPGRADE", // NOI18N
                            componentName),
                            NbBundle.getMessage(JBIComponentNode.class,
                            "TTL_AUTO_SHUTDOWN_COMPONENT_DURING_UPGRADE"), // NOI18N
                            NotifyDescriptor.YES_NO_OPTION);
                    if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.YES_OPTION) {
                        return;
                    }

                    if (d.getDoNotShowAgain()) {
                        confirmComponentShutdownDuringUpgrade = false;
                    }
                }

                shutdown(false);
            }

            // Make sure the component is really shutdown before calling upgrade
            clearJBIComponentStatusCache(compType);
            String state = getState();
            if (JBIComponentInfo.SHUTDOWN_STATE.equals(state)) {
                String progressLabel = getUpgradeProgressMessageLabel();
                String message = NbBundle.getMessage(JBIComponentContainerNode.class,
                        progressLabel, componentName);
                final ProgressUI progressUI = new ProgressUI(message, false);

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setBusy(true);
                        progressUI.start();
                    }
                });

                String jarFilePath = files.get(0).getAbsolutePath();
                String result = null;
                try {
                    result = installationService.upgradeComponent(
                            componentName, jarFilePath);
                } catch (ManagementRemoteException e) {
                    result = e.getMessage();                    
                    return;
                } finally {                
                    JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                            GenericConstants.UPGRADE_COMPONENT_OPERATION_NAME,
                            jarFilePath, result);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            progressUI.finish();
                            setBusy(false);
                        }
                    });
                }
            }

            // Restore old state
            if (JBIComponentInfo.STARTED_STATE.equals(oldState)) {
                start();
            }
            if (JBIComponentInfo.STOPPED_STATE.equals(oldState)) {
                start();
                stop();
            }
        }
    }

    //========================== Undeployable =================================
    public boolean canUndeploy() {
        RuntimeManagementServiceWrapper mgmtService =
                getRuntimeManagementServiceWrapper();
        if (mgmtService == null) {
            return false;
        }

        String componentName = getName();

        try {
            List<ServiceAssemblyInfo> saInfos = mgmtService.listServiceAssemblies(
                    componentName, SERVER_TARGET);

            return !busy && saInfos.size() > 0;
        } catch (ManagementRemoteException e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }

        return true;
    }

    public void undeploy(boolean force) {
        RuntimeManagementServiceWrapper mgmtService =
                getRuntimeManagementServiceWrapper();

        if (mgmtService == null) {
            return;
        }

        String componentName = getName();
        List<String> saNames = null;
        try {
            saNames = mgmtService.getServiceAssemblyNames(
                    componentName, SERVER_TARGET);
        } catch (ManagementRemoteException e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return;
        }

        if (saNames.size() > 0) {

            if (confirmForServiceAssembliesUndeployment) {
                String wordWrappedSANames = Utils.wordWrapString(
                        saNames.toString(), 80, "<br>");  // NOI18N
                String msg = NbBundle.getMessage(JBIComponentNode.class,
                        "MSG_UNDEPLOY_CONFIRMATION", // NOI18N
                        componentName, wordWrappedSANames);
                String title = NbBundle.getMessage(JBIComponentNode.class,
                        "TTL_UNINSTALL_CONFIRMATION"); // NOI18N
                DoNotShowAgainConfirmation d = new DoNotShowAgainConfirmation(
                        msg, title, NotifyDescriptor.YES_NO_OPTION);

                if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.YES_OPTION) {
                    return;
                }

                if (d.getDoNotShowAgain()) {
                    confirmForServiceAssembliesUndeployment = false;
                }
            }

            JBINode jbiNode = (JBINode) getParentNode().getParentNode();
            JBIServiceAssembliesNode sasNode =
                    (JBIServiceAssembliesNode) jbiNode.getChildren().getNodes()[3];
            sasNode.refresh();

            Node[] saNodes = sasNode.getChildren().getNodes();
            for (String saName : saNames) {
                for (Node saNode : saNodes) {
                    if (saName.equals(saNode.getName())) {
                        ((Undeployable) saNode).undeploy(force);
                        break;
                    }
                }
            }

            sasNode.refresh();
        }
    }

    private List<File> filterSelectedFiles(File[] files) {
        List<File> ret = new ArrayList<File>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        }

        if (docBuilder != null) {
            for (File file : files) {
                if (getValidator().validate(file)) {
                    ret.add(file);
                } else {
                    String msg = NbBundle.getMessage(
                            getClass(),
                            "MSG_INVALID_COMPONENT_SELECTION_FOR_UPGRADE", // NOI18N
                            file.getName(),
                            getName());
                    NotifyDescriptor d = new NotifyDescriptor.Message(
                            msg,
                            NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                }
            }
        }

        return ret;
    }

    private JFileChooser getJFileChooser() {
        JFileChooser chooser = new JFileChooser();

        ResourceBundle bundle = NbBundle.getBundle(JBIComponentNode.class);

        String title = NbBundle.getMessage(JBIComponentNode.class,
                "LBL_Upgrade_Chooser_Name", getName()); // NOI18N
        chooser.setDialogTitle(title);
        chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);

        chooser.setApproveButtonMnemonic(
                bundle.getString("Upgrade_JBI_Component_Button_Mnemonic").charAt(0)); //NOI18N
        chooser.setMultiSelectionEnabled(true);

        chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
        chooser.addChoosableFileFilter(FileFilters.JarFileFilter.getInstance());

        chooser.setApproveButtonToolTipText(
                NbBundle.getMessage(JBIComponentNode.class,
                "LBL_Upgrade_JBI_Component_Button")); //NOI18N

        chooser.getAccessibleContext().setAccessibleName(title);
        chooser.getAccessibleContext().setAccessibleDescription(title);

        String lastInstallDir = System.getProperty(LAST_JBI_COMPONENT_INSTALLATION_DIRECTORY);
        if (lastInstallDir != null) {
            chooser.setCurrentDirectory(new File(lastInstallDir));
        }

        return chooser;
    }

    /**
     * Gets the identification properties of a JBI component or a shared library.
     */
    private static Map<Attribute, MBeanAttributeInfo> 
            getIdentificationSheetSetProperties(
            String descriptor)
            throws ParserConfigurationException, SAXException, IOException {

        Map<Attribute, MBeanAttributeInfo> map =
                new HashMap<Attribute, MBeanAttributeInfo>();

        if (descriptor != null && descriptor.startsWith("<?xml")) { // NOI18N
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(
                    new InputSource(new StringReader(descriptor)));

            NodeList nodeList = doc.getElementsByTagNameNS(
                    "http://www.sun.com/jbi/descriptor/identification", // NOI18N
                    "VersionInfo"); // NOI18N
            if (nodeList.getLength() > 0) {
                Element element = (Element) nodeList.item(0);

                String buildNumberLabel =
                        NbBundle.getMessage(AppserverJBIMgmtController.class,
                        "LBL_BUILD_NUMBER"); // NOI18N
                String buildNumberDesc =
                        NbBundle.getMessage(AppserverJBIMgmtController.class,
                        "DSC_BUILD_NUMBER"); // NOI18N

                String buildNumber = element.getAttribute("build-number"); // NOI18N
                if (buildNumber != null) {
                    Attribute attr = new Attribute(buildNumberLabel, buildNumber);
                    MBeanAttributeInfo info = new MBeanAttributeInfo(
                            buildNumberLabel,
                            "java.lang.String", // NOI18N
                            buildNumberDesc,
                            true, false, false);
                    map.put(attr, info);
                }

                String specVersionLabel =
                        NbBundle.getMessage(AppserverJBIMgmtController.class,
                        "LBL_SPECIFICATION_VERSION"); // NOI18N
                String specVersionDesc =
                        NbBundle.getMessage(AppserverJBIMgmtController.class,
                        "DSC_SPECIFICATION_VERSION"); // NOI18N

                String specVersion =
                        element.getAttribute("specification-version"); // NOI18N
                if (specVersion != null) {
                    Attribute attr = new Attribute(specVersionLabel, specVersion);
                    MBeanAttributeInfo info = new MBeanAttributeInfo(
                            specVersionLabel,
                            "java.lang.String", // NOI18N
                            specVersionDesc,
                            true, false, false);
                    map.put(attr, info);
                }
            }
        }

        return map;
    }

    //===================== Abstract Methods ===================================
    protected abstract String getContainerType();

    protected abstract String getIconName(String state);

    protected abstract String getStartProgressLabel();

    protected abstract String getStopProgressLabel();

    protected abstract String getShutdownProgressLabel();

    protected abstract String getUninstallProgressLabel();

    protected abstract String getUpgradeProgressMessageLabel();

    protected abstract String uninstallComponent(
            InstallationService installationService,
            String componentName, boolean force)
            throws ManagementRemoteException;

    protected abstract JBIArtifactValidator getValidator();

    protected abstract String getInstallationDescriptor()
            throws ManagementRemoteException;

    //==========================================================================
    //========================= Concrete Nodes =================================
    /**
     * Node class for a Service Engine.
     */
    public static class ServiceEngine extends JBIComponentNode {

        public ServiceEngine(final AppserverJBIMgmtController controller,
                String name, String description) {
            super(controller,
                    JBIComponentType.SERVICE_ENGINE,
                    NodeType.SERVICE_ENGINE,
                    name, description);
        }

        @Override
        public Action[] getActions(boolean flag) {
            List<Action> actions = new ArrayList<Action>();

            actions.add(SystemAction.get(StartAction.class));
            actions.add(SystemAction.get(StopAction.class));
            actions.add(SystemAction.get(ShutdownAction.Normal.class));
            actions.add(SystemAction.get(UninstallAction.Normal.class));
            actions.add(SystemAction.get(UpgradeAction.class));
            actions.add(null);
            actions.add(SystemAction.get(AdvancedAction.class));

            List<Action> extraActions = getExtraActions();
            if (extraActions != null && extraActions.size() > 0) {
                actions.add(null);
                actions.addAll(extraActions);
            }

            actions.add(null);
            actions.add(SystemAction.get(UndeployAction.Normal.class));
            actions.add(null);
            actions.add(SystemAction.get(PropertiesAction.class));
            actions.add(SystemAction.get(RefreshAction.class));
            actions.add(null);
            actions.add(SystemAction.get(ShowComponentEndpointsStatisticsAction.class));

            return actions.toArray(new Action[]{});
        }

        /**
         * Gets a non-null list of actions that are specific to this component.
         */
        private List<Action> getExtraActions() {
            List<Action> extraActions = new ArrayList<Action>();

            ConfigurationService configService = getConfigurationService();
            assert configService != null;

            String componentName = getName();

            String actionXML = null;

            try {
                actionXML = (String) configService.invokeExtensionMBeanOperation(
                        componentName, ACTIONABLE_MBEAN_NAME, 
                        ACTIONABLE_MBEAN_GET_ACTIONS_OPERATION_NAME, 
                        new Object[]{}, new String[]{},
                        SERVER_TARGET, null);
            } catch (ManagementRemoteException e) {
                // This is OK. The component is not required to define 
                // its ActionableMBean.   
                e.printStackTrace();
            }

            if (actionXML != null) {
                extraActions.addAll(
                        ComponentActionDescriptor.getActions(actionXML));
            }

            return extraActions;
        }

        private String getString(InputStream is) throws IOException {
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }            
            return buffer.toString();
        }

        protected String uninstallComponent(
                InstallationService installationService,
                String componentName,
                boolean force) throws ManagementRemoteException {
            return installationService.uninstallComponent(componentName,
                    force, SERVER_TARGET);
        }

        protected JBIArtifactValidator getValidator() {
            return JBIArtifactValidator.getServiceEngineValidator(getName());
        }

        protected String getContainerType() {
            return GenericConstants.SERVICE_ENGINES_FOLDER_NAME;
        }

        protected String getIconName(String state) {
            return IconConstants.SERVICE_ENGINE_ICON;
        }

        protected String getStartProgressLabel() {
            return "LBL_Starting_Service_Engine";   // NOI18N
        }

        protected String getStopProgressLabel() {
            return "LBL_Stopping_Service_Engine";   // NOI18N
        }

        protected String getShutdownProgressLabel() {
            return "LBL_Shutting_Down_Service_Engine";  // NOI18N
        }

        protected String getUninstallProgressLabel() {
            return "LBL_Uninstalling_Service_Engine";   // NOI18N
        }

        protected String getUpgradeProgressMessageLabel() {
            return "LBL_Upgrading_Service_Engine";     // NOI18N
        }

        protected String getInstallationDescriptor()
                throws ManagementRemoteException {
            AdministrationService adminService = getAdministrationService();
            return adminService.getComponentInstallationDescriptor(getName());
        }
    }

    //==========================================================================
    /**
     * Node class for a Binding Component.
     */
    public static class BindingComponent extends JBIComponentNode {

        public BindingComponent(final AppserverJBIMgmtController controller,
                String name, String description) {
            super(controller,
                    JBIComponentType.BINDING_COMPONENT,
                    NodeType.BINDING_COMPONENT,
                    name, description);
        }

        @Override
        public Action[] getActions(boolean flag) {
            return new SystemAction[]{
                SystemAction.get(StartAction.class),
                SystemAction.get(StopAction.class),
                SystemAction.get(ShutdownAction.Normal.class),
                SystemAction.get(UninstallAction.Normal.class),
                SystemAction.get(UpgradeAction.class),
                null,
                SystemAction.get(AdvancedAction.class),
                null,
                SystemAction.get(UndeployAction.Normal.class),
                null,
                SystemAction.get(PropertiesAction.class),
                SystemAction.get(RefreshAction.class),
                null,
                SystemAction.get(ShowComponentEndpointsStatisticsAction.class)
            };
        }

        protected String uninstallComponent(
                InstallationService installationService,
                String componentName,
                boolean force) throws ManagementRemoteException {
            return installationService.uninstallComponent(componentName,
                    force, SERVER_TARGET);
        }

        protected JBIArtifactValidator getValidator() {
            return JBIArtifactValidator.getBindingComponentValidator(getName());
        }

        protected String getContainerType() {
            return GenericConstants.BINDING_COMPONENTS_FOLDER_NAME;
        }

        protected String getIconName(String state) {
            return IconConstants.BINDING_COMPONENT_ICON;
        }

        protected String getStartProgressLabel() {
            return "LBL_Starting_Binding_Component";    // NOI18N
        }

        protected String getStopProgressLabel() {
            return "LBL_Stopping_Binding_Component";    // NOI18N
        }

        protected String getShutdownProgressLabel() {
            return "LBL_Shutting_Down_Binding_Component";   // NOI18N
        }

        protected String getUninstallProgressLabel() {
            return "LBL_Uninstalling_Binding_Component";    // NOI18N
        }

        protected String getUpgradeProgressMessageLabel() {
            return "LBL_Upgrading_Binding_Component";     // NOI18N
        }

        @Override
        protected String getInstallationDescriptor()
                throws ManagementRemoteException {
            AdministrationService adminService = getAdministrationService();
            return adminService.getComponentInstallationDescriptor(getName());
        }
    }

    //==========================================================================
    /**
     * Node class for a Shared Library.
     */
    public static class SharedLibrary extends JBIComponentNode {

        public SharedLibrary(final AppserverJBIMgmtController controller,
                String name, String description) {
            super(controller,
                    JBIComponentType.SHARED_LIBRARY,
                    NodeType.SHARED_LIBRARY,
                    name, description);
        }

        @Override
        public Action[] getActions(boolean flag) {
            return new SystemAction[]{
                SystemAction.get(UninstallAction.Normal.class),
                null,
                SystemAction.get(PropertiesAction.class)
            };
        }

        protected String uninstallComponent(
                InstallationService installationService, String componentName,
                boolean force) throws ManagementRemoteException {
            return installationService.uninstallSharedLibrary(componentName,
                    force, SERVER_TARGET);
        }

        protected JBIArtifactValidator getValidator() {
            return null;
        }

        protected String getContainerType() {
            return GenericConstants.SHARED_LIBRARIES_FOLDER_NAME;
        }

        protected String getIconName(String state) {
            return IconConstants.SHARED_LIBRARY_ICON;
        }

        protected String getStartProgressLabel() {
            return null;
        }

        protected String getStopProgressLabel() {
            return null;
        }

        protected String getShutdownProgressLabel() {
            return null;
        }

        protected String getUninstallProgressLabel() {
            return "LBL_Uninstalling_Shared_Library";   // NOI18N
        }

        protected String getUpgradeProgressMessageLabel() {
            return null;
        }

        @Override
        protected String getInstalledIconBadgeName() {
            return null;
        }

        @Override
        protected String getStoppedIconBadgeName() {
            return null;
        }

        @Override
        protected String getUnknownIconBadgeName() {
            return null;
        }

        @Override
        protected String getInstallationDescriptor()
                throws ManagementRemoteException {
            AdministrationService adminService = getAdministrationService();
            return adminService.getSharedLibraryInstallationDescriptor(getName());
        }
    }
}
