/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.esb.management.api.notification.EventNotification;
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
import javax.management.Notification;
import javax.management.openmbean.CompositeDataSupport;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.sun.manager.jbi.GenericConstants;
import org.netbeans.modules.sun.manager.jbi.actions.RefreshAction;
import org.netbeans.modules.sun.manager.jbi.management.JBIMBeanTaskResultHandler;

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
import org.openide.util.actions.SystemAction;

import static org.netbeans.modules.sun.manager.jbi.NotificationConstants.*;

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

    // Current state of the SA
    private String currentState;
    
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

        registerNotificationListener();
    }
    
    protected boolean shouldProcessNotification(String sourceName, 
            String sourceType, String eventType) {
        return "ServiceAssembly".equals(sourceType) 
                && sourceName.equals(getName()) 
                && (NOTIFICATION_EVENT_TYPE_STARTED.equals(eventType) 
                || NOTIFICATION_EVENT_TYPE_STOPPED.equals(eventType) 
                || NOTIFICATION_EVENT_TYPE_SHUTDOWN.equals(eventType)); 
    }
    
    protected void processNotificationData(CompositeDataSupport data) {
        clearServiceAssemblyStatusCache();
        currentState = (String) data.get("EventType"); // NOI18N
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                fireIconChange();
                updatePropertySheet();
            }
        });
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

    private Map<Attribute, MBeanAttributeInfo> getServiceAssemblyStatisticsSheetSetProperties()
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
        if (currentState == null) {
            ServiceAssemblyInfo assembly = getAssemblyInfo();
            currentState = assembly == null ? 
                JBIComponentInfo.UNKNOWN_STATE : assembly.getState();
        }
        return currentState;
    }

    private void clearServiceAssemblyStatusCache() {
        currentState = null;
        getRuntimeManagementServiceWrapper().clearServiceAssemblyStatusCache();
    }

    @Override
    public Image getIcon(int type) {

        String baseIconName = IconConstants.SERVICE_ASSEMBLY_ICON;

        String status = getAssemblyState();

        String externalBadgeIconName = null;
        if (busy) {
            externalBadgeIconName = IconConstants.BUSY_ICON;
        } else {
            if (JBIComponentInfo.SHUTDOWN_STATE.equalsIgnoreCase(status)) {
                externalBadgeIconName = getInstalledIconBadgeName();
            } else if (JBIComponentInfo.STOPPED_STATE.equalsIgnoreCase(status)) {
                externalBadgeIconName = getStoppedIconBadgeName();
            } else if (!JBIComponentInfo.STARTED_STATE.equalsIgnoreCase(status)) {
                externalBadgeIconName = getUnknownIconBadgeName();
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
        
        if (SwingUtilities.isEventDispatchThread()) {
            fireIconChange();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    fireIconChange();
                }
            });
        }
    }

    private void updatePropertySheet() {
        Sheet sheet = createSheet();
        setSheet(sheet);
        firePropertySetsChange(null, null);
    }

    //========================== Startable =====================================
    public boolean canStart() {

        boolean ret = false;

//        if (!busy) {
            String assemblyState = getAssemblyState(); 

            if (ServiceAssemblyInfo.STOPPED_STATE.equalsIgnoreCase(assemblyState)) {
                ret = true;
            } else if (ServiceAssemblyInfo.SHUTDOWN_STATE.equalsIgnoreCase(assemblyState)) {
                ret = true;
            } else if (ServiceAssemblyInfo.STARTED_STATE.equalsIgnoreCase(assemblyState)) {
                ServiceAssemblyInfo saInfo = getAssemblyInfo();
                List<ServiceUnitInfo> units = saInfo.getServiceUnitInfoList();
                if (units != null) {
                    for (ServiceUnitInfo unit : units) {
                        String unitState = unit.getState();
                        if (ServiceAssemblyInfo.STOPPED_STATE.equalsIgnoreCase(unitState) ||
                                ServiceAssemblyInfo.SHUTDOWN_STATE.equalsIgnoreCase(unitState)) {
                            ret = true;
                            break;
                        }
                    }
                }
            }
//        }

        return ret;
    }

    public void start() {
        final RuntimeManagementServiceWrapper mgmtService =
                getRuntimeManagementServiceWrapper();
        if (mgmtService == null) {
            return;
        }

        setBusy(true);

        postToRequestProcessorThread(new Runnable() {
            public void run() {
                String assemblyName = getName();
                String result = null;
                try {
                    result = mgmtService.startServiceAssembly(assemblyName, SERVER_TARGET);
                } catch (ManagementRemoteException e) {
                    result = e.getMessage();
                } finally {
                    JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                            GenericConstants.START_SERVICE_ASSEMBLY_OPERATION_NAME,
                            assemblyName, result);
                    setBusy(false);
                }
            }
        });
    }

    //========================== Stoppable =====================================
    public boolean canStop() {

        boolean ret = false;

//        if (!busy) {
            String assemblyStatus = getAssemblyState(); 

            if (ServiceAssemblyInfo.STARTED_STATE.equalsIgnoreCase(assemblyStatus)) {
                ret = true;
            } else if (ServiceAssemblyInfo.STOPPED_STATE.equalsIgnoreCase(assemblyStatus)) {
                ServiceAssemblyInfo saInfo = getAssemblyInfo();
                List<ServiceUnitInfo> units = saInfo.getServiceUnitInfoList();
                if (units != null) {
                    for (ServiceUnitInfo unit : units) {
                        String unitState = unit.getState();
                        if (ServiceAssemblyInfo.STARTED_STATE.equalsIgnoreCase(unitState)) {
                            ret = true;
                            break;
                        }
                    }
                }
            }
//        }

        return ret;
    }

    public void stop() {
        final RuntimeManagementServiceWrapper mgmtService =
                getRuntimeManagementServiceWrapper();
        if (mgmtService == null) {
            return;
        }

        setBusy(true);
        
        postToRequestProcessorThread(new Runnable() {
            public void run() {
                String assemblyName = getName();
                String result = null;
                try {
                    result = mgmtService.stopServiceAssembly(assemblyName, SERVER_TARGET);
                } catch (ManagementRemoteException e) {
                    result = e.getMessage();
                } finally {
                    JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                            GenericConstants.STOP_SERVICE_ASSEMBLY_OPERATION_NAME,
                            assemblyName, result);
                    setBusy(false);
                }
            }
        });
    }

    //========================== Shutdownable ==================================
    public boolean canShutdown(boolean force) {

        boolean ret = canStop();

        if (!ret /*&& !busy*/) {
            String assemblyStatus = getAssemblyState();

            if (ServiceAssemblyInfo.STOPPED_STATE.equalsIgnoreCase(assemblyStatus)) {
                ret = true;
            } else if (ServiceAssemblyInfo.SHUTDOWN_STATE.equalsIgnoreCase(assemblyStatus)) {
                ServiceAssemblyInfo saInfo = getAssemblyInfo();
                List<ServiceUnitInfo> units = saInfo.getServiceUnitInfoList();
                if (units != null) {
                    for (ServiceUnitInfo unit : units) {
                        String unitState = unit.getState();
                        if (!ServiceUnitInfo.SHUTDOWN_STATE.equalsIgnoreCase(unitState)) {
                            ret = true;
                            break;
                        }
                    }
                }
            }
        }

        return ret;
    }

    public void shutdown(final boolean force) {

        final RuntimeManagementServiceWrapper mgmtService =
                getRuntimeManagementServiceWrapper();
        if (mgmtService == null) {
            return;
        }

        setBusy(true);

        postToRequestProcessorThread(new Runnable() {
            public void run() {
                String assemblyName = getName();
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
                    setBusy(false);
                }
            }
        });
    }

    //========================== Undeployable =================================
    public boolean canUndeploy(boolean force) {
        String assemblyStatus = getAssemblyState();
        return force || canShutdown(force) ||
                /*!busy &&*/ ServiceAssemblyInfo.SHUTDOWN_STATE.equalsIgnoreCase(assemblyStatus);
    }

    public void undeploy(final boolean force) {

        // The cached SA status is for performance purpose. The tradeoff is that
        // the actual SA status is not always correct. 
        // The following cache clearing is not really necessary, but is added 
        // anyway for #111623.
        clearServiceAssemblyStatusCache();

        setBusy(true);
        
        postToRequestProcessorThread(new Runnable()  {
            public void run() {
                final DeploymentService deploymentService = getDeploymentService();
                if (deploymentService == null) {
                    return;
                }

                String assemblyName = getName();
                String result = null;
                try {
                    if (canShutdown(force)) {
                        RuntimeManagementServiceWrapper mgmtService =
                                getRuntimeManagementServiceWrapper();
                        result = mgmtService.shutdownServiceAssembly(
                                assemblyName, force, SERVER_TARGET);
                    }
                    result = deploymentService.undeployServiceAssembly(
                            assemblyName, force, SERVER_TARGET);
                } catch (ManagementRemoteException e) {
                    result = e.getMessage();
                } finally {
                    JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                            GenericConstants.UNDEPLOY_SERVICE_ASSEMBLY_OPERATION_NAME,
                            assemblyName, result);
                    setBusy(false);
                }
            }
        });
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
