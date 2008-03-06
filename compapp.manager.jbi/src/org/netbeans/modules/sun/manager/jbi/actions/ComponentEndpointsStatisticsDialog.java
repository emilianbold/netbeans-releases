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

package org.netbeans.modules.sun.manager.jbi.actions;

import com.sun.esb.management.api.administration.AdministrationService;
import com.sun.esb.management.common.ManagementRemoteException;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.PerformanceMeasurementServiceWrapper;
import com.sun.esb.management.common.data.IEndpointStatisticsData;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.netbeans.modules.sun.manager.jbi.management.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.management.model.beaninfo.EndpointStatisticsDataBeanInfo;
import org.openide.util.NbBundle;

/**
 *
 * @author  jqian
 */
public class ComponentEndpointsStatisticsDialog extends javax.swing.JDialog {

    private TableModel pTableModel;
    private TableModel cTableModel;
    
    /** Creates new form EndpointStatisticsDialog */
    public ComponentEndpointsStatisticsDialog(AppserverJBIMgmtController controller,
            String compName) throws ManagementRemoteException {
        super((Frame) null, true);
        setTitle(NbBundle.getMessage(ComponentEndpointsStatisticsDialog.class, 
                "TTL_ENDPOINT_STATISTICS", compName)); // NOI18N        
        
        pTableModel = getStatisticsTableModel(controller, compName, false);
        cTableModel = getStatisticsTableModel(controller, compName, true);
        
        initComponents();
    }

    private TableModel getStatisticsTableModel(
            AppserverJBIMgmtController controller,
            String compName, boolean isConsumes) 
            throws ManagementRemoteException {

        AdministrationService adminService =
                controller.getAdministrationService();
        PerformanceMeasurementServiceWrapper perfService =
                controller.getPerformanceMeasurementServiceWrapper();

        Map<Endpoint, IEndpointStatisticsData> endpoint2Statistics =
                new HashMap<Endpoint, IEndpointStatisticsData>();
        
        String[] endpointStrings = isConsumes ? 
            adminService.getConsumingEndpoints(compName,
                AppserverJBIMgmtController.SERVER_TARGET) : 
            adminService.getProvisioningEndpoints(compName,
                AppserverJBIMgmtController.SERVER_TARGET);
        
        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        
        for (String endpointString : endpointStrings) {
            
            // The endpoint string is in the following format:
            //     ${namespaceURI},${service-name},${endpoint-name},[Provider|Consumer]
            
            assert (isConsumes && endpointString.endsWith(",Consumer")) || // NOI18N
                    (!isConsumes && endpointString.endsWith(",Provider")); // NOI18N
            
            endpointString = endpointString.substring(0, endpointString.lastIndexOf(",")); // NOI18N
            IEndpointStatisticsData statistics =
                    perfService.getEndpointStatistics(endpointString,
                    AppserverJBIMgmtController.SERVER_TARGET);
            
            String[] parts = endpointString.split(","); // NOI18N
            Endpoint endpoint = new Endpoint(parts[0], parts[1], parts[2]);
            endpoints.add(endpoint);
            
            endpoint2Statistics.put(endpoint, statistics);
        }

        return new EndpointStatisticsTableModel(endpoints, endpoint2Statistics);
    }
        
    class Endpoint {

        private String namespaceURI;
        private String serviceName;
        private String endpointName;

        Endpoint(String namespaceURI, String serviceName, String endpointName) {
            this.namespaceURI = namespaceURI;
            this.serviceName = serviceName;
            this.endpointName = endpointName;
        }

        public String getEndpointName() {
            return endpointName;
        }

        public String getNamespaceURI() {
            return namespaceURI;
        }

        public String getServiceName() {
            return serviceName;
        }
    }

    class EndpointStatisticsTableModel extends AbstractTableModel {

        private List<Endpoint> endpoints;
        private Map<Endpoint, IEndpointStatisticsData> statisticsMap;

        EndpointStatisticsTableModel(List<Endpoint> endpoints,
                Map<Endpoint, IEndpointStatisticsData> statisticsMap) {
            this.endpoints = endpoints;
            this.statisticsMap = statisticsMap;
        }

        public int getRowCount() {
            return statisticsMap.size();
        }

        public int getColumnCount() {
            return 7; // 10
        }

        @Override
        public String getColumnName(int columnIndex) {
            String ret = null;

            if (columnIndex == 0) {
                ret = "Endpoint";
            } else if (columnIndex == 1) {
                ret = NbBundle.getMessage(EndpointStatisticsDataBeanInfo.class,
                        "LBL_NUM_OF_RECEIVED_DONES"); // NOI18N

            } else if (columnIndex == 2) {
                ret = NbBundle.getMessage(EndpointStatisticsDataBeanInfo.class,
                        "LBL_NUM_OF_RECEIVED_ERRORS"); // NOI18N

            } else if (columnIndex == 3) {
                ret = NbBundle.getMessage(EndpointStatisticsDataBeanInfo.class,
                        "LBL_NUM_OF_RECEIVED_FAULTS"); // NOI18N

            } else if (columnIndex == 4) {
                ret = NbBundle.getMessage(EndpointStatisticsDataBeanInfo.class,
                        "LBL_NUM_OF_SENT_DONES"); // NOI18N

            } else if (columnIndex == 5) {
                ret = NbBundle.getMessage(EndpointStatisticsDataBeanInfo.class,
                        "LBL_NUM_OF_SENT_ERRORS"); // NOI18N

            } else if (columnIndex == 6) {
                ret = NbBundle.getMessage(EndpointStatisticsDataBeanInfo.class,
                        "LBL_NUM_OF_SENT_FAULTS"); // NOI18N

            } else if (columnIndex == 7) {
                ret = NbBundle.getMessage(EndpointStatisticsDataBeanInfo.class,
                        "LBL_MSG_EXCHANGE_COMPONENT_TIME_AVERAGE"); // NOI18N

            } else if (columnIndex == 8) {
                ret = NbBundle.getMessage(EndpointStatisticsDataBeanInfo.class,
                        "LBL_MSG_EXCHANGE_DELIVERY_CHANNEL_TIME_AVERAGE"); // NOI18N

            } else if (columnIndex == 9) {
                ret = NbBundle.getMessage(EndpointStatisticsDataBeanInfo.class,
                        "LBL_MSG_EXCHANGE_MESSAGE_SERVICE_TIME_AVERAGE"); // NOI18N

            }

            return "<HTML><B>" + ret + "</B></HTML>"; // NOI18N

        }

        public Object getValueAt(int rowIndex, int columnIndex) {

            Endpoint endpoint = endpoints.get(rowIndex);

            if (columnIndex == 0) {
                return endpoint.getServiceName() + "," + endpoint.getEndpointName(); // NOI18N

            } else if (columnIndex == 1) {
                return statisticsMap.get(endpoint).getNumberOfReceivedDones();
            } else if (columnIndex == 2) {
                return statisticsMap.get(endpoint).getNumberOfReceivedErrors();
            } else if (columnIndex == 3) {
                return statisticsMap.get(endpoint).getNumberOfReceivedFaults();
            } else if (columnIndex == 4) {
                return statisticsMap.get(endpoint).getNumberOfSentDones();
            } else if (columnIndex == 5) {
                return statisticsMap.get(endpoint).getNumberOfSentErrors();
            } else if (columnIndex == 6) {
                return statisticsMap.get(endpoint).getNumberOfSentFaults();
            } else if (columnIndex == 7) {
                return statisticsMap.get(endpoint).getMessageExchangeComponentTimeAverage();
            } else if (columnIndex == 8) {
                return statisticsMap.get(endpoint).getMessageExchangeDeliveryChannelTimeAverage();
            } else if (columnIndex == 9) {
                return statisticsMap.get(endpoint).getMessageExchangeServiceTimeAverage();
            }

            return null;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        pLabel = new javax.swing.JLabel();
        pTableScrollPane = new javax.swing.JScrollPane();
        pTable = new javax.swing.JTable();
        cLabel = new javax.swing.JLabel();
        cTableScrollPane = new javax.swing.JScrollPane();
        cTable = new javax.swing.JTable();
        okButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pLabel.setText(org.openide.util.NbBundle.getMessage(ComponentEndpointsStatisticsDialog.class, "ComponentEndpointsStatisticsDialog.pLabel.text")); // NOI18N

        pTable.setModel(pTableModel);
        pTableScrollPane.setViewportView(pTable);
        pTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        pTable.getTableHeader().setReorderingAllowed(false);
        TableColumn pTableFirstTC = pTable.getColumnModel().getColumn(0);
        pTableFirstTC.setPreferredWidth(300);

        cLabel.setText(org.openide.util.NbBundle.getMessage(ComponentEndpointsStatisticsDialog.class, "ComponentEndpointsStatisticsDialog.cLabel.text")); // NOI18N

        cTable.setModel(cTableModel);
        cTableScrollPane.setViewportView(cTable);
        cTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        cTable.getTableHeader().setReorderingAllowed(false);
        TableColumn cTableFirstTC = cTable.getColumnModel().getColumn(0);
        cTableFirstTC.setPreferredWidth(300);

        okButton.setText(org.openide.util.NbBundle.getMessage(ComponentEndpointsStatisticsDialog.class, "ComponentEndpointsStatisticsDialog.okButton.text")); // NOI18N
        okButton.setSelected(true);
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pLabel)
                    .add(cLabel))
                .addContainerGap(860, Short.MAX_VALUE))
            .add(pTableScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 971, Short.MAX_VALUE)
            .add(cTableScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 971, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(okButton))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(pLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pTableScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(cLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cTableScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                .add(17, 17, 17)
                .add(okButton))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
    setVisible(false);
}//GEN-LAST:event_okButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel cLabel;
    private javax.swing.JTable cTable;
    private javax.swing.JScrollPane cTableScrollPane;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel pLabel;
    private javax.swing.JTable pTable;
    private javax.swing.JScrollPane pTableScrollPane;
    // End of variables declaration//GEN-END:variables
}
