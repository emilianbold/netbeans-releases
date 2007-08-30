/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.sun.manager.jbi.nodes;

import org.netbeans.modules.sun.manager.jbi.management.JBIComponentType;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.sun.manager.jbi.management.JBIMBeanTaskResultHandler;
import org.netbeans.modules.sun.manager.jbi.util.ProgressUI;
import org.netbeans.modules.sun.manager.jbi.GenericConstants;
import org.netbeans.modules.sun.manager.jbi.actions.AdvancedAction;
import org.netbeans.modules.sun.manager.jbi.actions.RefreshAction;
import org.netbeans.modules.sun.manager.jbi.actions.ShutdownAction;
import org.netbeans.modules.sun.manager.jbi.actions.StartAction;
import org.netbeans.modules.sun.manager.jbi.actions.StopAction;
import org.netbeans.modules.sun.manager.jbi.actions.UninstallAction;
import org.netbeans.modules.sun.manager.jbi.nodes.property.SchemaBasedConfigPropertySupportFactory;
import org.netbeans.modules.sun.manager.jbi.management.AdministrationService;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentStatus;
import org.netbeans.modules.sun.manager.jbi.management.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.management.JBIComponentConfigurator;
import org.netbeans.modules.sun.manager.jbi.nodes.property.JBIPropertySupportFactory;
import org.netbeans.modules.sun.manager.jbi.util.Utils;
import org.openide.nodes.Sheet;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Abstract Node class for a JBI Component.
 *
 * @author jqian
 */
public abstract class JBIComponentNode extends AppserverJBIMgmtLeafNode
        implements Refreshable, Startable, Stoppable, Shutdownable, Uninstallable {
    
    private boolean busy;
    
    private JBIComponentType compType;    
    
    // Cached component configuration schema
    private String configSchema;
    
    private boolean hasConfigSchema = true;
    
        
    public JBIComponentNode(final AppserverJBIMgmtController controller,
            JBIComponentType compType, NodeType nodeType, String name, String description) {
        super(controller, nodeType);
        setName(name);
        setDisplayName(name);
        setShortDescription(description);
        this.compType = compType;
    }
    
    private JBIComponentConfigurator getComponentConfigurator() {
        AppserverJBIMgmtController controller = getAppserverJBIMgmtController();
        String containerType = getContainerType();
        String compName = getName();
        JBIComponentConfigurator configurator =
                controller.getComponentConfigurator(containerType, compName);
        return configurator;
    }
    
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        
        // Augment the general property sheet by adding Identification sheet
        try {
            Map<Attribute, MBeanAttributeInfo> identificationPropertyMap = 
                        getIdentificationProperties();
            Sheet.Set sheetSet = createSheetSet("Identification", // NOI18N
                    "LBL_IDENTIFICATION_PROPERTIES", // NOI18N
                    "DSC_IDENTIFICATION_PROPERTIES", // NOI18N
                    identificationPropertyMap);
            if (sheetSet != null) {
                sheet.put(sheetSet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Augment the general property sheet by adding Configuration sheet
        try {
            if (hasConfigSchema && configSchema == null) {
                JBIComponentConfigurator configurator = getComponentConfigurator();
                configSchema = configurator.getConfigurationSchema();                
                if (configSchema == null) {
                    hasConfigSchema = false;
                }
            }
                        
            Map<Attribute, ? extends MBeanAttributeInfo> configPropertyMap = 
                        getConfigurationProperties();
            
            Sheet.Set sheetSet = null;
            if (hasConfigSchema) {
                PropertySupport[] propertySupports = 
                        createPropertySupportArrayWithSchema(configPropertyMap);
                sheetSet = createSheetSet("Configuration", // NOI18N
                        "LBL_CONFIG_PROPERTIES", // NOI18N
                        "DSC_CONFIG_PROPERTIES", // NOI18N
                        propertySupports);    
            } else {                
                sheetSet = createSheetSet("Configuration", // NOI18N            
                    "LBL_CONFIG_PROPERTIES", // NOI18N
                    "DSC_CONFIG_PROPERTIES", // NOI18N
                    configPropertyMap);                
            }
                    
            if (sheetSet != null) {
                sheet.put(sheetSet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return sheet;
    }
    
    protected PropertySupport[] createPropertySupportArrayWithSchema(
            final Map<Attribute, ? extends MBeanAttributeInfo> attrMap) {
        
        assert configSchema != null;
        
        List<PropertySupport> supports = new ArrayList<PropertySupport>();
        
        try {
            String compName = getName();
            
            for (Attribute attr : attrMap.keySet()) {                
                String attrName = attr.getName();
                MBeanAttributeInfo info = attrMap.get(attr); 
                
                if (attrName.equals("EnvironmentVariables")) { // NOI18N
                    // There is no schema support for environment variables
                    PropertySupport support = JBIPropertySupportFactory.
                            getPropertySupport(this, attr, info);
                    supports.add(support);
                    continue;
                }
                
                PropertySupport support = SchemaBasedConfigPropertySupportFactory.
                        getPropertySupport(configSchema, compName, this, attr, info);
                
                if (support == null) {
                    String msg = "Fail to get property support for " + 
                            compName + ":" + attrName;
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
    
    protected Map<Attribute, MBeanAttributeInfo> getSheetProperties() {
        JBIComponentStatus jbiComponent = getJBIComponentStatus();
        return Utils.getIntrospectedPropertyMap(jbiComponent, true);
    }
    
    /**
     * Return the identification properties to be displayed for this JBIComponent.
     *
     * @return A java.util.Map containing all identification properties.
     */
    private Map<Attribute, MBeanAttributeInfo> getIdentificationProperties()
    throws Exception { 
        AppserverJBIMgmtController controller = getAppserverJBIMgmtController();
        return getIdentificationProperties(controller, true);
    }
    
    /**
     * Return the configuration properties to be displayed for this JBIComponent.
     *
     * @return A java.util.Map containing all configuration properties.
     */
    private Map<Attribute, ? extends MBeanAttributeInfo> getConfigurationProperties()
    throws Exception {
        AppserverJBIMgmtController controller = getAppserverJBIMgmtController();
        String containerType = getContainerType();
        String name = getName();
        return controller.getJBIComponentConfigProperties(
                containerType, name, true);
    }
    
    /**
     * Return the logger properties to be displayed for this JBIComponent.
     *
     * @return A java.util.Map containing all logger properties.
     */
    protected Map<Attribute, MBeanAttributeInfo> getLoggerProperties()
    throws Exception {
        AppserverJBIMgmtController controller = getAppserverJBIMgmtController();
        return controller.getJBIComponentLoggerProperties(getName(), true);
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
        
        try {
            String containerType = getContainerType();
            AppserverJBIMgmtController controller = getAppserverJBIMgmtController();
            controller.setJBIComponentConfigProperty(
                    containerType, getName(), attrName, value);
            
            // Get the new value
            Object newValue = controller.getJBIComponentConfigPropertyValue(
                    containerType, getName(), attrName);
            
//            updatePropertySheet();
            
            return new Attribute(attrName, newValue);
        } catch (Exception e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
        
        return null;
    }
    
    public Attribute setLoggerSheetProperty(String attrName, Object value) {
        
        try {
            AppserverJBIMgmtController controller = getAppserverJBIMgmtController();
            controller.setJBIComponentLoggerProperty(getName(), attrName, value);            
            
//            // Get the new value
//            Object newValue = controller.getJBIComponentConfigPropertyValue(
//                    containerType, getName(), attrName);
            
            updatePropertySheet();
            
            return new Attribute(attrName, value);
        } catch (Exception e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
        
        return null;
    }
       
    /**
     * Explicitly reset the property sheet (since the property sheet in 
     * AbstractNode is "sticky").
     */
    public void refresh() {        
        setSheet(createSheet());
    }
    
    /**
     *
     */
    public Image getIcon(int type) {
        String state = getState();
        String iconName = getIconName(state);
        
        String externalBadgeIconName = null;
        if (busy) {
            externalBadgeIconName = IconConstants.BUSY_ICON;
        } else {
            if (JBIComponentStatus.SHUTDOWN_STATE.equals(state)) {
                externalBadgeIconName = getInstalledIconBadgeName();
            } else if (JBIComponentStatus.STOPPED_STATE.equals(state)) {
                externalBadgeIconName = getStoppedIconBadgeName();
            } else if (!JBIComponentStatus.STARTED_STATE.equals(state)) {
                externalBadgeIconName = getUnknownIconBadgeName();
            }
        }
        
        return Utils.getBadgedIcon(getClass(), iconName, null, externalBadgeIconName);
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
    private JBIComponentStatus getJBIComponentStatus() {
        return getAdminService().getJBIComponentStatus(compType, getName());
    }
        
    private void clearJBIComponentStatusCache(JBIComponentType compType) {
        getAdminService().clearJBIComponentStatusCache(compType);
    }
    
    /**
     *
     * @return
     */
    private String getState() {
        JBIComponentStatus status = getJBIComponentStatus();
        if (status != null) {
            return status.getState();
        } else {
            return null;
        }
    }
    
    private void updatePropertySheet() {
        Sheet sheet = createSheet();
        setSheet(sheet);
        firePropertySetsChange(null, null);
    }
    
    //========================== Startable =====================================
    
    /**
     *
     */
    public boolean canStart() {
        String state = getState();
        return !busy &&
                (JBIComponentStatus.STOPPED_STATE.equals(state) ||
                JBIComponentStatus.SHUTDOWN_STATE.equals(state));
    }
    
    /**
     *
     */
    public void start() {
        final AdministrationService adminService = getAdminService();
        
        if (adminService != null) {
            
            String progressLabel = getStartProgressLabel();
            final String componentName = getName();
            String title =
                    NbBundle.getMessage(JBIComponentNode.class, progressLabel,
                    new Object[] {componentName});
            final ProgressUI progressUI = new ProgressUI(title, false);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setBusy(true);
                    progressUI.start();
                }
            });
            
            final String result = adminService.startComponent(componentName);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    clearJBIComponentStatusCache(compType);
                    progressUI.finish();
                    setBusy(false);
                    JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                            GenericConstants.START_COMPONENT_OPERATION_NAME,
                            componentName, result);
                    updatePropertySheet();                    
                }
            });
        }
    }
    
    //========================== Stoppable =====================================
    
    /**
     *
     */
    public boolean canStop() {
        return !busy && JBIComponentStatus.STARTED_STATE.equals(getState()); 
    }
    
    /**
     *
     */
    public void stop() {
        AdministrationService adminService = getAdminService();
        
        if (adminService != null) {
            
            String progressLabel = getStopProgressLabel();
            final String componentName = getName();
            String title =
                    NbBundle.getMessage(JBIComponentNode.class, progressLabel,
                    new Object[] {componentName});
            final ProgressUI progressUI = new ProgressUI(title, false);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setBusy(true);
                    progressUI.start();
                }
            });
            
            final String result = adminService.stopComponent(componentName);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    clearJBIComponentStatusCache(compType);
                    progressUI.finish();
                    setBusy(false);
                    JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                            GenericConstants.STOP_COMPONENT_OPERATION_NAME,
                            componentName, result);
                    updatePropertySheet();
                }
            });
        }
    }
    
    //========================== Shutdownable ==================================
    
    /**
     *
     */
    public boolean canShutdown() {
        return canStop() ||
                !busy && JBIComponentStatus.STOPPED_STATE.equals(getState()); 
    }
    
    /**
     *
     */
    public void shutdown(boolean force) {
        if (canStop()) {
            stop();
        }
        
        AdministrationService adminService = getAdminService();
        
        if (adminService != null) {
            
            String progressLabel = getShutdownProgressLabel();
            final String componentName = getName();
            String title =
                    NbBundle.getMessage(JBIComponentNode.class, progressLabel,
                    new Object[] {componentName});
            final ProgressUI progressUI = new ProgressUI(title, false);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setBusy(true);
                    progressUI.start();
                }
            });
            
            final String result = adminService.shutdownComponent(componentName, force);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    clearJBIComponentStatusCache(compType);
                    progressUI.finish();
                    setBusy(false);
                    JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                            GenericConstants.SHUTDOWN_COMPONENT_OPERATION_NAME,
                            componentName, result);
                    updatePropertySheet();
                }
            });
        }
    }
    
    //========================== Uninstallable =================================
    
    /**
     *
     */
    public boolean canUninstall() {
        return canShutdown() ||
                !busy && JBIComponentStatus.SHUTDOWN_STATE.equals(getState()); 
    }
    
    /**
     *
     */
    public void uninstall(boolean force) {
        if (canShutdown()) {
            shutdown(force);
        }
        
        AdministrationService adminService = getAdminService();
        
        if (adminService != null) {
            
            final String componentName = getName();
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(JBIComponentNode.class, "MSG_UNINSTALL_CONFIRMATION", componentName), // NOI18N
                    NbBundle.getMessage(JBIComponentNode.class, "TTL_UNINSTALL_CONFIRMATION"), // NOI18N
                    NotifyDescriptor.OK_CANCEL_OPTION);
            if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.OK_OPTION) {
                return;
            }
            
            String progressLabel = getUninstallProgressLabel();
            String title =
                    NbBundle.getMessage(JBIComponentNode.class, progressLabel,
                    new Object[] {componentName});
            final ProgressUI progressUI = new ProgressUI(title, false);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressUI.start();
                }
            });
            
            final String result = uninstallComponent(adminService, componentName, force);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    clearJBIComponentStatusCache(compType);
                    progressUI.finish();
                    JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                            GenericConstants.UNINSTALL_COMPONENT_OPERATION_NAME,
                            componentName, result);
                    //updatePropertySheet();
                }
            });
        }
    }
    
    //===================== Abstract Methods ===================================
    
    protected abstract String getContainerType();
    
    protected abstract String getIconName(String state);
    
    protected abstract String getStartProgressLabel();
    
    protected abstract String getStopProgressLabel();
    
    protected abstract String getShutdownProgressLabel();
    
    protected abstract String getUninstallProgressLabel();
    
    protected abstract String uninstallComponent(
            AdministrationService adminService, String componentName, boolean force);
    
    protected abstract Map<Attribute, MBeanAttributeInfo> getIdentificationProperties(
            AppserverJBIMgmtController controller, boolean sort) throws Exception;
    
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
        
        public Action[] getActions(boolean flag) {
            return new SystemAction[] {
                SystemAction.get(StartAction.class),
                SystemAction.get(StopAction.class),
                SystemAction.get(ShutdownAction.Normal.class),
                SystemAction.get(UninstallAction.Normal.class),
                null,
                SystemAction.get(AdvancedAction.class),
                null,
                SystemAction.get(PropertiesAction.class),
                SystemAction.get(RefreshAction.class),
            };
        }
        
        protected Sheet createSheet() {
            Sheet sheet = super.createSheet();
            
            try {
                Sheet.Set sheetSet = createSheetSet("Loggers", // NOI18N
                        "LBL_LOGGERS_PROPERTIES", // NOI18N
                        "DSC_LOGGERS_PROPERTIES", // NOI18N
                        getLoggerProperties());
                if (sheetSet != null) {
                    sheet.put(sheetSet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return sheet;
        }
        
        protected String uninstallComponent(
                AdministrationService adminService, String componentName,
                boolean force) {
            return adminService.uninstallComponent(componentName, force);
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
        
        protected Map<Attribute, MBeanAttributeInfo> getIdentificationProperties(
                AppserverJBIMgmtController controller, boolean sort) throws Exception {
            return controller.getJBIComponentIdentificationProperties(getName(), sort);
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
        
        public Action[] getActions(boolean flag) {
            return new SystemAction[] {
                SystemAction.get(StartAction.class),
                SystemAction.get(StopAction.class),
                SystemAction.get(ShutdownAction.Normal.class),
                SystemAction.get(UninstallAction.Normal.class),
                null,
                SystemAction.get(AdvancedAction.class),
                null,
                SystemAction.get(PropertiesAction.class),
                SystemAction.get(RefreshAction.class),
            };
        }        
        
        protected Sheet createSheet() {
            Sheet sheet = super.createSheet();
            
            try {
                Sheet.Set sheetSet = createSheetSet("Loggers", // NOI18N
                        "LBL_LOGGERS_PROPERTIES", // NOI18N
                        "DSC_LOGGERS_PROPERTIES", // NOI18N
                        getLoggerProperties());
                if (sheetSet != null) {
                    sheet.put(sheetSet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return sheet;
        }
        
        protected String uninstallComponent(
                AdministrationService adminService, String componentName,
                boolean force) {
            return adminService.uninstallComponent(componentName, force);
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
        
        protected Map<Attribute, MBeanAttributeInfo> getIdentificationProperties(
                AppserverJBIMgmtController controller, boolean sort) throws Exception {
            return controller.getJBIComponentIdentificationProperties(getName(), sort);
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
        
        public Action[] getActions(boolean flag) {
            return new SystemAction[] {
                SystemAction.get(UninstallAction.Normal.class),
                null,
                SystemAction.get(PropertiesAction.class),
            };
        }
        
        protected String uninstallComponent(
                AdministrationService adminService, String componentName,
                boolean force) { // ignore force
            return adminService.uninstallSharedLibrary(componentName);
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
        
        protected String getInstalledIconBadgeName() {
            return null;
        }
        
        protected String getStoppedIconBadgeName() {
            return null;
        }
        
        protected String getUnknownIconBadgeName() {
            return null;
        }
        
        protected Map<Attribute, MBeanAttributeInfo> getIdentificationProperties(
                AppserverJBIMgmtController controller, boolean sort) throws Exception {
            return controller.getSharedLibraryIdentificationProperties(getName(), sort);
        }
    }
}
