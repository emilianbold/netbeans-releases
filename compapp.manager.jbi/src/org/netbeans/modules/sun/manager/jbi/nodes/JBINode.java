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

import com.sun.esb.management.api.configuration.ConfigurationService;
import com.sun.esb.management.common.ManagementRemoteException;
import com.sun.esb.management.common.data.FrameworkStatisticsData;
import com.sun.esb.management.common.data.NMRStatisticsData;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.CompositeDataSupport;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.netbeans.modules.sun.manager.jbi.actions.RefreshAction;
import org.netbeans.modules.sun.manager.jbi.management.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.management.JBIComponentType;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.PerformanceMeasurementServiceWrapper;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.RuntimeManagementServiceWrapper;
import org.netbeans.modules.sun.manager.jbi.nodes.property.JBIPropertySupportFactory;
import org.netbeans.modules.sun.manager.jbi.util.ComparableAttribute;
import org.netbeans.modules.sun.manager.jbi.util.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;

/**
 * Top node for the JBI lifecycle module.
 *
 * @author jqian
 */
public class JBINode extends AppserverJBIMgmtContainerNode {

    private static final String FRAMEWORK_STATISTICS_SHEET_SET_NAME =
            "FRAMEWORK_STATISTICS"; // NOI18N
    private static final String NMR_STATISTICS_SHEET_SET_NAME =
            "NMR_STATISTICS"; // NOI18N
    private static final String LOGGERS_SHEET_SET_NAME = "LOGGERS"; // NOI18N
    private static Logger logger = Logger.getLogger("org.netbeans.modules.sun.manager.jbi.nodes.JBINode"); // NOI18N   
    
    // Runtime configuration meta data
    private static final String RUNTIME_CONFIG_DISPLAY_NAME = "displayName"; // NOI18N
    private static final String RUNTIME_CONFIG_TOOLTIP = "toolTip"; // NOI18N
    private static final String RUNTIME_CONFIG_IS_STATIC = "isStatic"; // NOI18N
//    private static final String RUNTIME_CONFIG_IS_PASSWORD = "isPassword"; // NOI18N
//    private static final String RUNTIME_CONFIG_MIN_VALUE = "minValue"; // NOI18N
//    private static final String RUNTIME_CONFIG_MAX_VALUE = "maxValue"; // NOI18N
//    private static final String RUNTIME_CONFIG_ENUM_VALUE = "enumValue"; // NOI18N
    
    // A list of runtime configurations that are not very useful for IDE users.
    private static final List<String> hiddenRuntimeConfigNames;

    static {
        hiddenRuntimeConfigNames = new ArrayList<String>();
        hiddenRuntimeConfigNames.add("jbiHome"); // NOI18N
        hiddenRuntimeConfigNames.add("autoInstallDir"); // NOI18N
        hiddenRuntimeConfigNames.add("autoDeployDir"); // NOI18N
    }

    /** Creates a new instance of JBINode */
    public JBINode(final AppserverJBIMgmtController controller) {
        super(controller, NodeType.JBI);
    }

    @Override
    public Action[] getActions(boolean flag) {
        return new SystemAction[]{
            SystemAction.get(PropertiesAction.class),
            SystemAction.get(RefreshAction.class)
        };
    }

    @Override
    public Image getIcon(int type) {
        return new ImageIcon(
                getClass().getResource(IconConstants.JBI_ICON)).getImage();
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public void refresh() {
        // clear the cache first
        RuntimeManagementServiceWrapper service =
                getRuntimeManagementServiceWrapper();
        service.clearJBIComponentStatusCache(JBIComponentType.SERVICE_ENGINE);
        service.clearJBIComponentStatusCache(JBIComponentType.BINDING_COMPONENT);
        service.clearJBIComponentStatusCache(JBIComponentType.SHARED_LIBRARY);
        service.clearServiceAssemblyStatusCache();

        super.refresh();

        // Explicitly reset the property sheet (since the property sheet in 
        // AbstractNode is "sticky").
        setSheet(createSheet());
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = new Sheet();
        
        // Add the general property sheet the non-standard way
        try {
            PropertySupport[] generalPropertySupports = 
                    getGeneralSheetSetPropertySupports();

            Sheet.Set sheetSet = createSheetSet(GENERAL_SHEET_SET_NAME,
                            "LBL_GENERAL_PROPERTIES", // NOI18N
                            "DSC_GENERAL_PROPERTIES", // NOI18N
                            generalPropertySupports);
            if (sheetSet != null) {
                sheet.put(sheetSet);
            }
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }

        // Augment the general property sheet by adding loggers sheet
        try {
            addSheetSet(sheet,
                    LOGGERS_SHEET_SET_NAME,
                    "LBL_LOGGERS_PROPERTIES", // NOI18N
                    "DSC_LOGGERS_PROPERTIES", // NOI18N
                    getLoggerSheetSetProperties());
        } catch (ManagementRemoteException e) {
            logger.warning(e.getMessage());
        }

        // Augment the general property sheet by adding framework statistics sheet
        try {
            addSheetSet(sheet,
                    FRAMEWORK_STATISTICS_SHEET_SET_NAME,
                    "LBL_FRAMEWORK_STATISTICS_PROPERTIES", // NOI18N
                    "DSC_FRAMEWORK_STATISTICS_PROPERTIES", // NOI18N
                    getFrameworkStatisticsSheetSetProperties());
        } catch (ManagementRemoteException e) {
            logger.warning(e.getMessage());
        }

        // Augment the general property sheet by adding NMR statistics sheet
        try {
            addSheetSet(sheet,
                    NMR_STATISTICS_SHEET_SET_NAME,
                    "LBL_NMR_STATISTICS_PROPERTIES", // NOI18N
                    "DSC_NMR_STATISTICS_PROPERTIES", // NOI18N
                    getNMRStatisticsSheetSetProperties());
        } catch (ManagementRemoteException e) {
            logger.warning(e.getMessage());
        }

        return sheet;
    }

    private PropertySupport[] getGeneralSheetSetPropertySupports() 
        throws ManagementRemoteException {
        
        List<PropertySupport> properties = new ArrayList<PropertySupport>();

        AppserverJBIMgmtController controller = getAppserverJBIMgmtController();
        ConfigurationService configService =
                controller.getConfigurationService();
        Map<String, Object> configMap =
                configService.getRuntimeConfigurationAsMap(SERVER_TARGET);

        List<String> keys = new ArrayList<String>();
        keys.addAll(configMap.keySet());
        Collections.sort(keys);

        for (String key : keys) {
            if (hiddenRuntimeConfigNames.contains(key)) {
                continue;
            }

            Object value = configMap.get(key);
            Attribute attr = new Attribute(key, value);

            Properties metaData = configService.getRuntimeConfigurationMetaData(key);
            String displayName = metaData.getProperty(RUNTIME_CONFIG_DISPLAY_NAME);
            String toolTip = metaData.getProperty(RUNTIME_CONFIG_TOOLTIP);
            String isStatic = metaData.getProperty(RUNTIME_CONFIG_IS_STATIC, "false"); // NOI18N
//            String isPassword = metaData.getProperty(RUNTIME_CONFIG_IS_PASSWORD, "false"); // NOI18N
//            String minValue = metaData.getProperty(RUNTIME_CONFIG_MIN_VALUE);
//            String maxValue = metaData.getProperty(RUNTIME_CONFIG_MAX_VALUE);
//            String enumValue = metaData.getProperty(RUNTIME_CONFIG_ENUM_VALUE);

            MBeanAttributeInfo attrInfo = new MBeanAttributeInfo(
                    displayName,
                    value.getClass().getName(),
                    toolTip,
                    true, !Boolean.parseBoolean(isStatic), false);

            PropertySupport property = JBIPropertySupportFactory.getPropertySupport(
                        this, attr, attrInfo);
            properties.add(property);
        }

        return properties.toArray(new PropertySupport[0]);    
    }

    /**
     * Gets the logger properties to be displayed for the framework.
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
        Map<String, Level> loggerMap = configService.getRuntimeLoggerLevels(
                SERVER_TARGET);

        for (String loggerCustomName : loggerMap.keySet()) {
            Level logLevel = loggerMap.get(loggerCustomName);
            String displayName =
                    configService.getRuntimeLoggerDisplayName(
                    loggerCustomName, SERVER_TARGET);

            Attribute attr = new Attribute(loggerCustomName, logLevel);
            MBeanAttributeInfo info = new MBeanAttributeInfo(
                    displayName,
                    "java.util.logging.Level", // NOI18N
                    displayName + " (" + loggerCustomName + ")", // NOI18N
                    true, true, false);
            ret.put(new ComparableAttribute(attr), info);
        }

        return ret;
    }

    private Map<Attribute, MBeanAttributeInfo> getFrameworkStatisticsSheetSetProperties()
            throws ManagementRemoteException {
        AppserverJBIMgmtController controller = getAppserverJBIMgmtController();
        PerformanceMeasurementServiceWrapper perfService =
                controller.getPerformanceMeasurementServiceWrapper();
        FrameworkStatisticsData statistics =
                perfService.getFrameworkStatistics(SERVER_TARGET);
        return Utils.getIntrospectedPropertyMap(
                statistics, true, MODEL_BEAN_INFO_PACKAGE_NAME);
    }

    private Map<Attribute, MBeanAttributeInfo> getNMRStatisticsSheetSetProperties()
            throws ManagementRemoteException {
        AppserverJBIMgmtController controller = getAppserverJBIMgmtController();
        PerformanceMeasurementServiceWrapper perfService =
                controller.getPerformanceMeasurementServiceWrapper();
        NMRStatisticsData statistics =
                perfService.getNMRStatistics(SERVER_TARGET);
        return Utils.getIntrospectedPropertyMap(statistics, true);
    }

    public Attribute setSheetProperty(String attrName, Object value) {
        try {
            ConfigurationService configService = getConfigurationService();
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(attrName, value);
            configService.setRuntimeConfiguration(map, SERVER_TARGET);

            // Get the new value
            value = configService.getRuntimeConfigurationAsMap(
                    SERVER_TARGET).get(attrName);
        } catch (ManagementRemoteException e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }

        return new Attribute(attrName, value);
    }

    /**
     * Sets the logger property of the framework.
     * 
     * @param loggerName
     * @param value
     * @return
     */
    public Attribute setLoggerSheetProperty(String loggerName, Level value) {
        try {
            ConfigurationService configService = getConfigurationService();
            configService.setRuntimeLoggerLevel(loggerName, value, SERVER_TARGET);
        } catch (Exception e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            value = null;
        }

        return new Attribute(loggerName, value);
    }
    
    protected boolean shouldProcessNotification(String sourceName, 
            String sourceType, String eventType) {
        return false;
    }
    
    protected void processNotificationData(CompositeDataSupport data) {
        // no-op
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(JBINode.class);
    }
}
