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

import java.awt.Image;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.sun.manager.jbi.GenericConstants;
import org.netbeans.modules.sun.manager.jbi.management.JBIMBeanTaskResultHandler;

import org.netbeans.modules.sun.manager.jbi.util.ProgressUI;
import org.netbeans.modules.sun.manager.jbi.actions.ShutdownAction;
import org.netbeans.modules.sun.manager.jbi.actions.StartAction;
import org.netbeans.modules.sun.manager.jbi.actions.StopAction;
import org.netbeans.modules.sun.manager.jbi.actions.UndeployAction;
import org.netbeans.modules.sun.manager.jbi.management.AdministrationService;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentStatus;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIServiceAssemblyStatus;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIServiceUnitStatus;
import org.netbeans.modules.sun.manager.jbi.util.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.util.NodeTypes;
import org.netbeans.modules.sun.manager.jbi.util.Utils;
import org.openide.nodes.Sheet;
import org.openide.actions.PropertiesAction;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Node for one JBI Service Assembly.
 *
 * @author jqian
 */
public class JBIServiceAssemblyNode extends AppserverJBIMgmtContainerNode
        implements Startable, Stoppable, Shutdownable, Undeployable {
    
    private boolean busy;
    
    private JBIServiceAssemblyStatus cachedAssemblyStatus;
    
    /** Creates a new instance of ServiceAssemblyNode */
    public JBIServiceAssemblyNode(final AppserverJBIMgmtController controller,
            String name,
            String description) {
        super(controller, NodeTypes.SERVICE_ASSEMBLY);
        setName(name);
        setDisplayName(name);
        setShortDescription(description);
    }
    
    public JBIServiceAssemblyStatus getAssembly() {
        return getAssembly(false);  // non-cached by default
    }
    
    private JBIServiceAssemblyStatus getAssembly(boolean cached) {
        if (cachedAssemblyStatus == null || !cached) {
            cachedAssemblyStatus =
                    getAdminService().getServiceAssemblyStatus(getName());
        }
        
        return cachedAssemblyStatus;
    }
    
    /**
     *
     */
    public Image getIcon(int type) {
        
        String baseIconName = IconConstants.SERVICE_ASSEMBLY_ICON;
        
        String status = getAssemblyStatus(false);
        
        String externalBadgeIconName = null;
        if (busy) {
            externalBadgeIconName = IconConstants.BUSY_ICON;
        } else {
            if (JBIComponentStatus.INSTALLED_STATE.equals(status)) {
                externalBadgeIconName = IconConstants.INSTALLED_ICON;
            } else if (JBIComponentStatus.STOPPED_STATE.equals(status)) {
                externalBadgeIconName = IconConstants.STOPPED_ICON;
            } else if (!JBIComponentStatus.STARTED_STATE.equals(status)) {
                externalBadgeIconName = IconConstants.UNKNOWN_ICON;
            }
        }
        
        return Utils.getBadgedIcon(getClass(), baseIconName, null, externalBadgeIconName);
    }
    
    // For now, use the same open for open/closed state
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    /**
     * Return the actions associated with the menu drop down seen when
     * a user right-clicks on a node in the plugin.
     *
     * @param boolean true/false
     * @return An array of Action objects.
     */
    public Action[] getActions(boolean flag) {
        return new SystemAction[] {
            SystemAction.get(StartAction.class),
            SystemAction.get(StopAction.class),
            SystemAction.get(ShutdownAction.Normal.class),
            SystemAction.get(UndeployAction.Normal.class),
            null,
            //SystemAction.get(ShutdownAction.Force.class),
            SystemAction.get(UndeployAction.Force.class),
            null,
            SystemAction.get(PropertiesAction.class),
        };
    }
    
    /**
     * Return the SheetProperties to be displayed for this JVM.
     *
     * @return A java.util.Map containing all JVM properties.
     */
    protected Map<Attribute, MBeanAttributeInfo> getSheetProperties() {
        JBIServiceAssemblyStatus assemblyStatus =
                getAdminService().getServiceAssemblyStatus(getName());
        return Utils.getIntrospectedPropertyMap(assemblyStatus, true);
    }
    
    public Attribute setSheetProperty(String attrName, Object value) {
        return null;
    }
    
    /**
     *
     * @param busy
     */
    private void setBusy(boolean busy) {
        this.busy = busy;
        fireIconChange();
    }
    
    private String getAssemblyStatus(boolean cached) {
        JBIServiceAssemblyStatus assembly = getAssembly(cached);
        if (assembly != null) {
            return assembly.getStatus();
        } else {
            return null;
        }
    }
    
    private void updatePropertySheet() {
        Sheet sheet = createSheet();
        setSheet(sheet);
        firePropertySetsChange(null, null);
    }
    
    private AdministrationService getAdminService() {
        return getAppserverJBIMgmtController().getJBIAdministrationService();
    }
    
    //========================== Startable =====================================
    
    public boolean canStart() {
        
        boolean ret = false;
        
        if (!busy) {
            JBIServiceAssemblyStatus assembly = getAssembly(false);
            String assemblyStatus = (assembly != null) ? assembly.getStatus() : null;
            
            if (JBIServiceAssemblyStatus.STOP_STATUS.equals(assemblyStatus)) {
                ret = true;
            } else if (JBIServiceAssemblyStatus.SHUTDOWN_STATUS.equals(assemblyStatus)) {
                ret = true;
            } else if (JBIServiceAssemblyStatus.START_STATUS.equals(assemblyStatus)) {
                List<JBIServiceUnitStatus> units = assembly.getJbiServiceUnitStatusList();
                if (units != null) {
                    for (JBIServiceUnitStatus unit : units) {
                        String unitStatus = unit.getStatus();
                        if (JBIServiceAssemblyStatus.STOP_STATUS.equals(unitStatus) ||
                                JBIServiceAssemblyStatus.SHUTDOWN_STATUS.equals(unitStatus)) {
                            ret = true;
                            break;
                        }
                    }
                }
            }
        }
        
        return ret;
    }
    
    
    public void start() {
        AdministrationService adminService = getAdminService();
        
        if (adminService != null) {
            final String assemblyName = getName();
            
            String title =
                    NbBundle.getMessage(JBIServiceAssemblyNode.class,
                    "LBL_Starting_Service_Assembly",    // NOI18N
                    new Object[] {assemblyName});
            final ProgressUI progressUI = new ProgressUI(title, false);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setBusy(true);
                    progressUI.start();
                }
            });
            
            final String result = adminService.startServiceAssembly(assemblyName);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressUI.finish();
                    JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                            GenericConstants.START_SERVICE_ASSEMBLY_OPERATION_NAME,
                            assemblyName, result);
                    setBusy(false);
                    updatePropertySheet();
                }
            });            
        }
    }
    
    //========================== Stoppable =====================================
    
    public boolean canStop() {
        
        boolean ret = false;
        
        if (!busy) {
            JBIServiceAssemblyStatus assembly = getAssembly(true);  // cached
            String assemblyStatus = (assembly != null) ? assembly.getStatus() : null;
            
            if (JBIServiceAssemblyStatus.START_STATUS.equals(assemblyStatus)) {
                ret = true;
            } else if (JBIServiceAssemblyStatus.STOP_STATUS.equals(assemblyStatus)) {
                List units = assembly.getJbiServiceUnitStatusList();
                if (units != null) {
                    for (Iterator it = units.iterator(); it.hasNext();) {
                        JBIServiceUnitStatus unit = (JBIServiceUnitStatus) it.next();
                        String unitStatus = unit.getStatus();
                        if (JBIServiceAssemblyStatus.START_STATUS.equals(unitStatus)) {
                            ret = true;
                            break;
                        }
                    }
                }
            }
        }
        
        return ret;
    }
    
    public void stop() {
        AdministrationService adminService = getAdminService();
        
        if (adminService != null) {
            final String assemblyName = getName();
            
            String title =
                    NbBundle.getMessage(JBIServiceAssemblyNode.class,
                    "LBL_Stopping_Service_Assembly",    // NOI18N
                    new Object[] {assemblyName});
            final ProgressUI progressUI = new ProgressUI(title, false);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setBusy(true);
                    progressUI.start();
                }
            });
            
            final String result = adminService.stopServiceAssembly(assemblyName);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressUI.finish();
                    JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                            GenericConstants.STOP_SERVICE_ASSEMBLY_OPERATION_NAME,
                            assemblyName, result);
                    setBusy(false);
                    updatePropertySheet();
                }
            });            
        }
    }
    
    //========================== Shutdownable ==================================
    
    public boolean canShutdown() {
        
        boolean ret = canStop();
        
        if (!ret && !busy) {
            JBIServiceAssemblyStatus assembly = getAssembly(true);  // cached
            String assemblyStatus = (assembly != null) ? assembly.getStatus() : null;
            
            if (JBIServiceAssemblyStatus.STOP_STATUS.equals(assemblyStatus)) {
                ret = true;
            } else if (JBIServiceAssemblyStatus.SHUTDOWN_STATUS.equals(assemblyStatus)) {
                List units = assembly.getJbiServiceUnitStatusList();
                if (units != null) {
                    for (Iterator it = units.iterator(); it.hasNext();) {
                        JBIServiceUnitStatus unit = (JBIServiceUnitStatus) it.next();
                        String unitStatus = unit.getStatus();
                        if (JBIServiceAssemblyStatus.START_STATUS.equals(unitStatus) ||
                                JBIServiceAssemblyStatus.STOP_STATUS.equals(unitStatus)) {
                            ret = true;
                            break;
                        }
                    }
                }
            }
        }
        
        return ret;
    }
    
    public void shutdown(boolean force) {
        
        if (canStop()) {
            stop();
        }
        
        AdministrationService adminService = getAdminService();
        
        if (adminService != null) {
            final String assemblyName = getName();
            
            String title =
                    NbBundle.getMessage(JBIServiceAssemblyNode.class,
                    "LBL_Shutting_Down_Service_Assembly",   // NOI18N
                    new Object[] {assemblyName});
            final ProgressUI progressUI = new ProgressUI(title, false);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setBusy(true);
                    progressUI.start();
                }
            });
            
            final String result =
                    adminService.shutdownServiceAssembly(assemblyName, force);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressUI.finish();
                    JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                            GenericConstants.SHUTDOWN_SERVICE_ASSEMBLY_OPERATION_NAME,
                            assemblyName, result);
                    setBusy(false);
                    updatePropertySheet();
                }
            });            
        }
    }
    
    //========================== Undeployable =================================
    
    public boolean canUndeploy() {
        String assemblyStatus = getAssemblyStatus(true); // cached
        return canShutdown() ||
                !busy && JBIServiceAssemblyStatus.SHUTDOWN_STATUS.equals(assemblyStatus);
    }
    
    public void undeploy(boolean force) {
                 
        if (canShutdown()) {
            shutdown(force);
        }
        
        AdministrationService adminService = getAdminService();
        
        if (adminService != null) {
            final String assemblyName = getName();
            
            String title =
                    NbBundle.getMessage(JBIServiceAssemblyNode.class,
                    "LBL_Undeploying_Service_Assembly",     // NOI18N
                    new Object[] {assemblyName});
            final ProgressUI progressUI = new ProgressUI(title, false);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setBusy(true);
                    progressUI.start();
                }
            });
            
            final String result =
                    adminService.undeployServiceAssembly(assemblyName, force);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressUI.finish();
                    JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                            GenericConstants.UNDEPLOY_SERVICE_ASSEMBLY_OPERATION_NAME,
                            assemblyName, result);
                    setBusy(false);
                    //updatePropertySheet();
                }
            });            
        }
    }
}
