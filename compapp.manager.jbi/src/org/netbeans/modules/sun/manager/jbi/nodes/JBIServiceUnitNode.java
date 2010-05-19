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
import com.sun.esb.management.api.notification.EventNotification;
import com.sun.esb.management.common.ManagementRemoteException;
import com.sun.esb.management.common.data.ServiceAssemblyStatisticsData;
import com.sun.esb.management.common.data.ServiceUnitStatisticsData;
import com.sun.jbi.ui.common.JBIComponentInfo;
import com.sun.jbi.ui.common.ServiceUnitInfo;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.Notification;
import javax.management.openmbean.CompositeDataSupport;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.modules.sun.manager.jbi.management.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.PerformanceMeasurementServiceWrapper;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.RuntimeManagementServiceWrapper;
import org.netbeans.modules.sun.manager.jbi.util.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Sheet;
import org.openide.util.datatransfer.ExTransferable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import static org.netbeans.modules.sun.manager.jbi.NotificationConstants.*;

/**
 * Node for one JBI Service Unit.
 *
 * @author jqian
 */
public class JBIServiceUnitNode extends AppserverJBIMgmtLeafNode {

    private static final String SERVICE_UNIT_STATISTICS_SHEET_SET_NAME =
            "SERVICE_UNIT_STATISTICS"; // NOI18N
    
    private String componentName;

    // Current state of the SU
    private String currentState;
    
    public JBIServiceUnitNode(final AppserverJBIMgmtController controller,
            final String name,
            final String displayName,
            final String description) {
        super(controller, NodeType.SERVICE_UNIT);
        
        setName(name);
        
        setDisplayName(displayName);
        
        // Use HTML version for tooltip.
        setShortDescription(Utils.getTooltip(description));         
        // Use non-HTML version in the property sheet's description area.
        setValue("nodeDescription", description); // NOI18N 
        
        registerNotificationListener();
    }
        
    protected boolean shouldProcessNotification(String sourceName, 
            String sourceType, String eventType) {
        return NOTIFICATION_SOURCE_TYPE_SERVICE_UNIT.equals(sourceType) 
                && sourceName.equals(getName());
    }
    
    protected void processNotificationData(CompositeDataSupport data) {
        //clearServiceAssemblyStatusCache();
        currentState = (String) data.get(NOTIFICATION_EVENT_TYPE);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                fireIconChange();
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

        // Augment the general property sheet by adding SU statistics sheet
        try {
            addSheetSet(sheet,
                    SERVICE_UNIT_STATISTICS_SHEET_SET_NAME,
                    "LBL_SERVICE_UNIT_STATISTICS_PROPERTIES", // NOI18N
                    "DSC_SERVICE_UNIT_STATISTICS_PROPERTIES", // NOI18N
                    getServiceUnitStatisticsSheetSetProperties());
        } catch (ManagementRemoteException e) {
            e.printStackTrace();
        }
        return sheet;
    }

    private Map<Attribute, MBeanAttributeInfo> 
            getServiceUnitStatisticsSheetSetProperties()
            throws ManagementRemoteException {
        AppserverJBIMgmtController controller = getAppserverJBIMgmtController();
        
        PerformanceMeasurementServiceWrapper perfService =
                controller.getPerformanceMeasurementServiceWrapper();
        
        String saName = getParentNode().getName();
        ServiceAssemblyStatisticsData saStatistics =
                perfService.getServiceAssemblyStatistics(
                saName, SERVER_TARGET);

        String suName = getName();
        for (ServiceUnitStatisticsData suStatistics : 
            saStatistics.getServiceUnitStatisticsList()) {
            if (suStatistics.getName().equals(suName)) {
                return Utils.getIntrospectedPropertyMap(suStatistics, true);
            }
        }

        return null;
    }

    private Map<Attribute, MBeanAttributeInfo> getGeneralSheetSetProperties() {
        ServiceUnitInfo suInfo = getServiceUnitInfo();
        return Utils.getIntrospectedPropertyMap(suInfo, true,
                MODEL_BEAN_INFO_PACKAGE_NAME);
    }
  
    public Attribute setSheetProperty(String attrName, Object value) {
        return null;
    }

    /**
     *
     * @return
     */
    private ServiceUnitInfo getServiceUnitInfo() {
        try {
            String assemblyName = getParentNode().getName();
            RuntimeManagementServiceWrapper mgmtService =
                    getRuntimeManagementServiceWrapper();
            return mgmtService.getServiceUnit(assemblyName, getName(), SERVER_TARGET);
        } catch (ManagementRemoteException e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }

        return null;
    }
    
    private String getState() {
        if (currentState == null) {
            ServiceUnitInfo unit = getServiceUnitInfo();
            currentState = unit == null ? JBIComponentInfo.UNKNOWN_STATE : unit.getState();
        }
        return currentState;
    }

    /**
     *
     */
    public Image getIcon(int type) {

        String baseIconName = IconConstants.SERVICE_UNIT_ICON;

        String status = getState();

        String externalBadgeIconName = null;
        if (JBIComponentInfo.SHUTDOWN_STATE.equalsIgnoreCase(status)) {
            externalBadgeIconName = IconConstants.INSTALLED_ICON;
        } else if (JBIComponentInfo.STOPPED_STATE.equalsIgnoreCase(status)) {
            externalBadgeIconName = IconConstants.STOPPED_ICON;
        } else if (!JBIComponentInfo.STARTED_STATE.equalsIgnoreCase(status)) {
            externalBadgeIconName = IconConstants.UNKNOWN_ICON;
        }

        return Utils.getBadgedIcon(
                getClass(), baseIconName, null, externalBadgeIconName);
    }
    
    // DnD Support for CASA
    public static final DataFlavor ServiceUnitDataFlavor =
            new DataFlavor(Object.class, "JBIServiceUnitDataFlavor") {  // NOI18N
            };

    public Transferable drag() throws IOException {
        ExTransferable retValue = ExTransferable.create(super.drag());
        //add the 'data' into the Transferable
        final String suDD = getDeploymentDescriptor();

        retValue.put(new ExTransferable.Single(ServiceUnitDataFlavor) {

            protected Object getData() throws IOException, UnsupportedFlavorException {
                List<String> ret = new ArrayList<String>();
                ret.add("JBIMGR_SU_TRANSFER"); // NOI18N
                ret.add(getName()); // service unit name
                ret.add(getComponentName());
                ret.add(getShortDescription());
                ret.add(suDD);
                return ret;
            }
        });

        /*
        return new Transferable() {
        public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {
        JBIServiceUnitTransferObject.ServiceUnitDataFlavor};
        }
        public boolean isDataFlavorSupported(DataFlavor flavor) {
        return JBIServiceUnitTransferObject.ServiceUnitDataFlavor.equals(flavor);
        }
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return suTransfer;
        }
        };*/

        return retValue;
    }

    public String getDeploymentDescriptor() {

        String suDD = null;

        AdministrationService adminService = getAdministrationService();

        String assemblyName = getParentNode().getName();
        try {
            suDD = adminService.getServiceUnitDeploymentDescriptor(
                    assemblyName, getName());
        } catch (ManagementRemoteException e) {
            e.printStackTrace();
        }

        return suDD;
    }

    private String getComponentName() {
        if (componentName == null) {
            JBIServiceAssemblyNode saNode = (JBIServiceAssemblyNode) getParentNode();
            String saDD = saNode.getDeploymentDescriptor();
            String myName = getName();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            try {
                DocumentBuilder builder = factory.newDocumentBuilder();

                // parse SA DD
                Document saDoc = builder.parse(new InputSource(new StringReader(saDD)));
                NodeList sus = saDoc.getElementsByTagName("service-unit"); // NOI18N
                for (int i = 0; i < sus.getLength(); i++) {
                    Element su = (Element) sus.item(i);
                    String name = ((Element) su.getElementsByTagName("name").item(0)).getFirstChild().getNodeValue(); // identification/name
                    if (name.equals(myName)) {
                        componentName = ((Element) su.getElementsByTagName("component-name").item(0)).getFirstChild().getNodeValue(); // target/component-name
                        break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (componentName == null) {
                componentName = "?"; // NOI18N
            }
        }
        return componentName;
    }
}
