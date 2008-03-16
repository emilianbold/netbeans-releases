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
import com.sun.esb.management.api.deployment.DeploymentService;
import com.sun.esb.management.common.ManagementRemoteException;
import com.sun.esb.management.common.data.ServiceAssemblyStatisticsData;
import com.sun.jbi.ui.common.JBIComponentInfo;
import com.sun.jbi.ui.common.ServiceAssemblyInfo;
import com.sun.jbi.ui.common.ServiceUnitInfo;
import java.awt.Image;
import java.util.List;
import java.util.Map;

import java.util.logging.Logger;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.sun.manager.jbi.GenericConstants;
import org.netbeans.modules.sun.manager.jbi.actions.RefreshAction;
import org.netbeans.modules.sun.manager.jbi.management.JBIMBeanTaskResultHandler;

import org.netbeans.modules.sun.manager.jbi.util.ProgressUI;
import org.netbeans.modules.sun.manager.jbi.actions.ShutdownAction;
import org.netbeans.modules.sun.manager.jbi.actions.StartAction;
import org.netbeans.modules.sun.manager.jbi.actions.StopAction;
import org.netbeans.modules.sun.manager.jbi.actions.UndeployAction;
import org.netbeans.modules.sun.manager.jbi.management.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.PerformanceMeasurementServiceWrapper;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.RuntimeManagementServiceWrapper;
import org.netbeans.modules.sun.manager.jbi.util.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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

    private static final String SERVICE_ASSEMBLY_STATISTICS_SHEET_SET_NAME =
            "SERVICE_ASSEMBLY_STATISTICS"; // NOI18N
    private boolean busy;

    private static Logger logger = Logger.getLogger("org.netbeans.modules.sun.manager.jbi.nodes.JBIServiceAssemblyNode"); // NOI18N

    /** Creates a new instance of ServiceAssemblyNode */
    public JBIServiceAssemblyNode(final AppserverJBIMgmtController controller,
            String name, String description) {
        super(controller, NodeType.SERVICE_ASSEMBLY);

        setName(name);

        setDisplayName(name);

        // Use HTML version for tooltip.
        setShortDescription(Utils.getTooltip(description));
        // Use non-HTML version in the property sheet's description area.
        setValue("nodeDescription", description); // NOI18N 
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = new Sheet();

        addSheetSet(sheet,
                GENERAL_SHEET_SET_NAME,
                "LBL_GENERAL_PROPERTIES", // NOI18N
                "DSC_GENERAL_PROPERTIES", // NOI18N
                getGeneralSheetSetProperties());

        // Augment the general property sheet by adding SA statistics sheet
        try {
            addSheetSet(sheet,
                    SERVICE_ASSEMBLY_STATISTICS_SHEET_SET_NAME,
                    "LBL_SERVICE_ASSEMBLY_STATISTICS_PROPERTIES", // NOI18N
                    "DSC_SERVICE_ASSEMBLY_STATISTICS_PROPERTIES", // NOI18N
                    getServiceAssemblyStatisticsSheetSetProperties());
        } catch (ManagementRemoteException e) {
            logger.warning(e.getMessage());
        }
        return sheet;
    }

    private Map<Attribute, MBeanAttributeInfo> 
            getServiceAssemblyStatisticsSheetSetProperties()
            throws ManagementRemoteException {
        AppserverJBIMgmtController controller = getAppserverJBIMgmtController();
        PerformanceMeasurementServiceWrapper perfService =
                controller.getPerformanceMeasurementServiceWrapper();
        ServiceAssemblyStatisticsData statistics =
                perfService.getServiceAssemblyStatistics(
                getName(), SERVER_TARGET);
        return Utils.getIntrospectedPropertyMap(statistics, true);
    }

    public ServiceAssemblyInfo getAssemblyInfo() {
        try {
            return getRuntimeManagementServiceWrapper().getServiceAssembly(
                    getName(), SERVER_TARGET);
        } catch (ManagementRemoteException e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }

        return null;
    }

    private String getAssemblyState() {
        ServiceAssemblyInfo assembly = getAssemblyInfo();
        return assembly != null ? assembly.getState() : null;
    }

    private void clearServiceAssemblyStatusCache() {
        getRuntimeManagementServiceWrapper().clearServiceAssemblyStatusCache();
    }

    /**
     *
     */
    @Override
    public Image getIcon(int type) {

        String baseIconName = IconConstants.SERVICE_ASSEMBLY_ICON;

        String status = getAssemblyState();

        String externalBadgeIconName = null;
        if (busy) {
            externalBadgeIconName = IconConstants.BUSY_ICON;
        } else {
            if (JBIComponentInfo.SHUTDOWN_STATE.equals(status)) {
                externalBadgeIconName = IconConstants.INSTALLED_ICON;
            } else if (JBIComponentInfo.STOPPED_STATE.equals(status)) {
                externalBadgeIconName = IconConstants.STOPPED_ICON;
            } else if (!JBIComponentInfo.STARTED_STATE.equals(status)) {
                externalBadgeIconName = IconConstants.UNKNOWN_ICON;
            }
        }

        return Utils.getBadgedIcon(getClass(), baseIconName, null,
                externalBadgeIconName);
    }

    // For now, use the same open for open/closed state
    @Override
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
    @Override
    public Action[] getActions(boolean flag) {
        return new SystemAction[]{
            SystemAction.get(StartAction.class),
            SystemAction.get(StopAction.class),
            SystemAction.get(ShutdownAction.Normal.class),
            SystemAction.get(UndeployAction.Normal.class),
            null,
            //SystemAction.get(ShutdownAction.Force.class),
            SystemAction.get(UndeployAction.Force.class),
            null,
            SystemAction.get(PropertiesAction.class),
            SystemAction.get(RefreshAction.class)
        };
    }
    
    @Override
    public void refresh() {
        // clear the cache first
        RuntimeManagementServiceWrapper service = 
                getRuntimeManagementServiceWrapper();
        service.clearServiceAssemblyStatusCache();
        
        super.refresh();
        
        fireIconChange(); // necessary for the SA node to refresh
        
        // Explicitly reset the property sheet (since the property sheet in 
        // AbstractNode is "sticky").
        setSheet(createSheet());
    }
     
    /**
     * Return the SheetProperties to be displayed for this JVM.
     *
     * @return A java.util.Map containing all JVM properties.
     */
    private Map<Attribute, MBeanAttributeInfo> getGeneralSheetSetProperties() {
        ServiceAssemblyInfo assemblyInfo = getAssemblyInfo();
        return Utils.getIntrospectedPropertyMap(assemblyInfo, true,
                MODEL_BEAN_INFO_PACKAGE_NAME);
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

    private void updatePropertySheet() {
        Sheet sheet = createSheet();
        setSheet(sheet);
        firePropertySetsChange(null, null);
    }
    
    protected boolean needRefresh(String notificationSourceType) {
        return false;
    }

    //========================== Startable =====================================
    public boolean canStart() {

        boolean ret = false;

        if (!busy) {
            ServiceAssemblyInfo saInfo = getAssemblyInfo();
            String assemblyState = (saInfo != null) ? saInfo.getState() : null;

            if (ServiceAssemblyInfo.STOPPED_STATE.equals(assemblyState)) {
                ret = true;
            } else if (ServiceAssemblyInfo.SHUTDOWN_STATE.equals(assemblyState)) {
                ret = true;
            } else if (ServiceAssemblyInfo.STARTED_STATE.equals(assemblyState)) {
                List<ServiceUnitInfo> units = saInfo.getServiceUnitInfoList();
                if (units != null) {
                    for (ServiceUnitInfo unit : units) {
                        String unitState = unit.getState();
                        if (ServiceAssemblyInfo.STOPPED_STATE.equals(unitState) ||
                                ServiceAssemblyInfo.SHUTDOWN_STATE.equals(unitState)) {
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
        RuntimeManagementServiceWrapper mgmtService =
                getRuntimeManagementServiceWrapper();

        if (mgmtService == null) {
            return;
        }

        final String assemblyName = getName();

        String title =
                NbBundle.getMessage(JBIServiceAssemblyNode.class,
                "LBL_Starting_Service_Assembly", // NOI18N
                new Object[]{assemblyName});
        final ProgressUI progressUI = new ProgressUI(title, false);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setBusy(true);
                progressUI.start();
            }
        });

        String result = null;
        try {
            result = mgmtService.startServiceAssembly(assemblyName, SERVER_TARGET);
        } catch (ManagementRemoteException e) {
            result = e.getMessage();
        } finally {
            JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                    GenericConstants.START_SERVICE_ASSEMBLY_OPERATION_NAME,
                    assemblyName, result);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                clearServiceAssemblyStatusCache();
                progressUI.finish();
                setBusy(false);
                updatePropertySheet();
            }
        });
    }

    //========================== Stoppable =====================================
    public boolean canStop() {

        boolean ret = false;

        if (!busy) {
            ServiceAssemblyInfo saInfo = getAssemblyInfo();
            String assemblyStatus = (saInfo != null) ? saInfo.getState() : null;

            if (ServiceAssemblyInfo.STARTED_STATE.equals(assemblyStatus)) {
                ret = true;
            } else if (ServiceAssemblyInfo.STOPPED_STATE.equals(assemblyStatus)) {
                List<ServiceUnitInfo> units = saInfo.getServiceUnitInfoList();
                if (units != null) {
                    for (ServiceUnitInfo unit : units) {
                        String unitState = unit.getState();
                        if (ServiceAssemblyInfo.STARTED_STATE.equals(unitState)) {
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
        RuntimeManagementServiceWrapper mgmtService =
                getRuntimeManagementServiceWrapper();

        if (mgmtService == null) {
            return;
        }

        final String assemblyName = getName();

        String title =
                NbBundle.getMessage(JBIServiceAssemblyNode.class,
                "LBL_Stopping_Service_Assembly", // NOI18N
                new Object[]{assemblyName});
        final ProgressUI progressUI = new ProgressUI(title, false);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setBusy(true);
                progressUI.start();
            }
        });

        String result = null;
        try {
            result = mgmtService.stopServiceAssembly(assemblyName, SERVER_TARGET);
        } catch (ManagementRemoteException e) {
            result = e.getMessage();
        } finally {
            JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                    GenericConstants.STOP_SERVICE_ASSEMBLY_OPERATION_NAME,
                    assemblyName, result);
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                clearServiceAssemblyStatusCache();
                progressUI.finish();
                setBusy(false);
                updatePropertySheet();
            }
        });
    }

    //========================== Shutdownable ==================================
    public boolean canShutdown(boolean force) {

        boolean ret = canStop();

        if (!ret && !busy) {
            ServiceAssemblyInfo saInfo = getAssemblyInfo();
            String assemblyStatus = (saInfo != null) ? saInfo.getState() : null;

            if (ServiceAssemblyInfo.STOPPED_STATE.equals(assemblyStatus)) {
                ret = true;
            } else if (ServiceAssemblyInfo.SHUTDOWN_STATE.equals(assemblyStatus)) {
                List<ServiceUnitInfo> units = saInfo.getServiceUnitInfoList();
                if (units != null) {
                    for (ServiceUnitInfo unit : units) {
                        String unitState = unit.getState();
                        if (!ServiceUnitInfo.SHUTDOWN_STATE.equals(unitState)) {
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

        RuntimeManagementServiceWrapper mgmtService =
                getRuntimeManagementServiceWrapper();

        if (mgmtService == null) {
            return;
        }

        final String assemblyName = getName();

        String title =
                NbBundle.getMessage(JBIServiceAssemblyNode.class,
                "LBL_Shutting_Down_Service_Assembly", // NOI18N
                new Object[]{assemblyName});
        final ProgressUI progressUI = new ProgressUI(title, false);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setBusy(true);
                progressUI.start();
            }
        });

        String result = null;
        try {
            result = mgmtService.shutdownServiceAssembly(
                    assemblyName, force, SERVER_TARGET);
        } catch (ManagementRemoteException e) {            
            result = e.getMessage();
        } finally {
            JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                    GenericConstants.SHUTDOWN_SERVICE_ASSEMBLY_OPERATION_NAME,
                    assemblyName, result);
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                clearServiceAssemblyStatusCache();
                progressUI.finish();
                setBusy(false);
                updatePropertySheet();
            }
        });
    }

    //========================== Undeployable =================================
    public boolean canUndeploy(boolean force) {
        String assemblyStatus = getAssemblyState();
        return force || canShutdown(force) ||
                !busy && ServiceAssemblyInfo.SHUTDOWN_STATE.equals(assemblyStatus);
    }

    public boolean undeploy(boolean force) {

        // The cached SA status is for performance purpose. The tradeoff is that
        // the actual SA status is not always correct. 
        // The following cache clearing is not really necessary, but is added 
        // anyway for #111623.
        clearServiceAssemblyStatusCache();

        if (canShutdown(force)) {
            shutdown(force);
        }

        DeploymentService deploymentService = getDeploymentService();

        if (deploymentService == null) {
            return false;
        }

        final String assemblyName = getName();

        String title =
                NbBundle.getMessage(JBIServiceAssemblyNode.class,
                "LBL_Undeploying_Service_Assembly", // NOI18N
                new Object[]{assemblyName});
        final ProgressUI progressUI = new ProgressUI(title, false);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setBusy(true);
                progressUI.start();
            }
        });

        boolean success = true;
        String result = null;
        try {
            result = deploymentService.undeployServiceAssembly(
                    assemblyName, force, SERVER_TARGET);
        } catch (ManagementRemoteException e) {
            result = e.getMessage();
        } finally {
            success = JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                    GenericConstants.UNDEPLOY_SERVICE_ASSEMBLY_OPERATION_NAME,
                    assemblyName, result);
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                clearServiceAssemblyStatusCache();
                progressUI.finish();
                setBusy(false);
            }
        });
        
        return success;
    }

    // DnD Support for CASA

    /*
    public static final DataFlavor ServiceAssemblyDataFlavor =
    new DataFlavor(Object.class, "JBIServiceAssemblyDataFlavor" ) {  // NOI18N
    };
    public Transferable drag() throws IOException {
    ExTransferable retValue = ExTransferable.create( super.drag() );
    //add the 'data' into the Transferable
    try {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    // parse SA DD
    String saDD = getDeploymentDescriptor();
    final Document saDoc = builder.parse(new InputSource(new StringReader(saDD)));
    final List<Document> bcsuDocList = new ArrayList<Document>();
    final List<Document> sesuDocList = new ArrayList<Document>();
    for (Node child : getChildren().getNodes()) {        
    final String suDD = ((JBIServiceUnitNode) child).getDeploymentDescriptor();
    // parse SU DD
    Document suDoc = builder.parse(new InputSource(new StringReader(suDD)));
    Element services = (Element) suDoc.getElementsByTagName("services").item(0);
    boolean isBC = services.getAttribute("binding-component").equals("true");
    if (isBC) {
    bcsuDocList.add(suDoc);
    } else {
    sesuDocList.add(suDoc);
    }
    }
    retValue.put( new ExTransferable.Single(ServiceAssemblyDataFlavor) {
    protected Object getData() throws IOException, UnsupportedFlavorException {
    List<Object> ret = new ArrayList<Object>();
    ret.add("JBIMGR_SA_TRANSFER"); // NOI18N
    ret.add(saDoc); 
    ret.add(bcsuDocList); 
    ret.add(sesuDocList); 
    return ret;
    }
    });
    } catch (Exception e) {
    }
    return retValue;
    }
     */
    public String getDeploymentDescriptor() {
        String saDD = null;

        AdministrationService adminService = getAdministrationService();

        String assemblyName = getName();
        try {
            saDD = adminService.getServiceAssemblyDeploymentDescriptor(assemblyName);
        } catch (ManagementRemoteException e) {
            e.printStackTrace();
        }

        return saDD;
    }
}
