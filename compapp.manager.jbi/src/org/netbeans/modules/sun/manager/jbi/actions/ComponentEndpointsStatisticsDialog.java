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

package org.netbeans.modules.sun.manager.jbi.actions;

import com.sun.esb.management.api.administration.AdministrationService;
import com.sun.esb.management.common.ManagementRemoteException;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.PerformanceMeasurementServiceWrapper;
import com.sun.esb.management.common.data.IEndpointStatisticsData;
import com.sun.esb.management.common.data.ProvisioningEndpointStatisticsData;
import com.sun.esb.management.common.data.helper.EndpointStatisticsDataReader;
import java.awt.Frame;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.sun.manager.jbi.management.AppserverJBIMgmtController;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;

/**
 *
 * @author  jqian
 */
public class ComponentEndpointsStatisticsDialog extends javax.swing.JDialog {

    private TableModel tableModel;
    
    /** Creates new form EndpointStatisticsDialog */
    public ComponentEndpointsStatisticsDialog(AppserverJBIMgmtController controller,
            String compName) throws ManagementRemoteException {
        super((Frame) null, true);
        setTitle(NbBundle.getMessage(ComponentEndpointsStatisticsDialog.class, 
                "TTL_ENDPOINT_STATISTICS", compName)); // NOI18N        
        
        tableModel = getStatisticsTableModel(controller, compName);
        
        initComponents();
    }

    private TableModel getStatisticsTableModel(
            AppserverJBIMgmtController controller,
            String compName) 
            throws ManagementRemoteException {

        PerformanceMeasurementServiceWrapper perfService =
                controller.getPerformanceMeasurementServiceWrapper();

        Map<Endpoint, IEndpointStatisticsData> endpoint2Statistics =
                new HashMap<Endpoint, IEndpointStatisticsData>();
        
        List<Endpoint> endpoints = new ArrayList<Endpoint>();

        TabularData provisioningEndpointsTable =
                perfService.getProvidingEndpointsForComponentAsTabularData(compName,
                AppserverJBIMgmtController.SERVER_TARGET);
        addEndpoints(provisioningEndpointsTable, endpoint2Statistics,
                endpoints, false, perfService);

        TabularData consumingEndpointsTable =
                perfService.getConsumingEndpointsForComponentAsTabularData(compName,
                AppserverJBIMgmtController.SERVER_TARGET);
        addEndpoints(consumingEndpointsTable, endpoint2Statistics,
                endpoints, true, perfService);

        return new EndpointStatisticsTableModel(endpoints, endpoint2Statistics);
    }

    private void addEndpoints(TabularData endpointsTabularData,
            Map<Endpoint, IEndpointStatisticsData> endpoint2Statistics,
            List<Endpoint> endpoints, boolean isConsumes,
            PerformanceMeasurementServiceWrapper perfService)
            throws ManagementRemoteException {

        String[] targetNames = {AppserverJBIMgmtController.SERVER_TARGET};

        CompositeData compositeData = endpointsTabularData.get(targetNames);

        if (compositeData.containsKey("Endpoints")) {
            String[] provisioningEndpoints = (String[]) compositeData.get("Endpoints");
            if (provisioningEndpoints != null) {
                for (String endpointName : provisioningEndpoints) {
                    System.out.println("Endpoint Name: " + endpointName);
                    String[] parts = endpointName.split(","); // NOI18N
                    Endpoint endpoint = new Endpoint(parts[0], parts[1], parts[2], isConsumes);
                    endpoints.add(endpoint);

                    IEndpointStatisticsData statistics =
                            perfService.getEndpointStatistics(endpointName,
                            AppserverJBIMgmtController.SERVER_TARGET);

                    endpoint2Statistics.put(endpoint, statistics);
                    //System.out.println(data.getDisplayString());
                }
            }
        }
    }
    
//    private void addEndpoints(String[] endpointStrings,
//            Map<Endpoint, IEndpointStatisticsData> endpoint2Statistics,
//            List<Endpoint> endpoints, boolean isConsumes,
//            PerformanceMeasurementServiceWrapper perfService)
//            throws ManagementRemoteException {
//
//        for (String endpointString : endpointStrings) {
//
//            // The endpoint string is in the following format:
//            //     ${namespaceURI},${service-name},${endpoint-name},[Provider|Consumer]
//
//            assert (isConsumes && endpointString.endsWith(",Consumer")) || // NOI18N
//                   (!isConsumes && endpointString.endsWith(",Provider")); // NOI18N
//
//            endpointString = endpointString.substring(0, endpointString.lastIndexOf(",")); // NOI18N
//            IEndpointStatisticsData statistics =
//                    perfService.getEndpointStatistics(endpointString,
//                    AppserverJBIMgmtController.SERVER_TARGET);
//
//            String[] parts = endpointString.split(","); // NOI18N
//            Endpoint endpoint = new Endpoint(parts[0], parts[1], parts[2], isConsumes);
//            endpoints.add(endpoint);
//
//            endpoint2Statistics.put(endpoint, statistics);
//        }
//    }
        
    class Endpoint {

        private String namespaceURI;
        private String serviceName;
        private String endpointName;
        private boolean isConsumes;

        Endpoint(String namespaceURI, String serviceName, String endpointName, 
                boolean isConsumes) {
            this.namespaceURI = namespaceURI;
            this.serviceName = serviceName;
            this.endpointName = endpointName;
            this.isConsumes = isConsumes;
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
        
        public boolean isConsumes() {
            return isConsumes;
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
            return 12;
        }

        @Override
        public String getColumnName(int columnIndex) {
            String ret = null;

            if (columnIndex == 0) {
                ret = NbBundle.getMessage(ComponentEndpointsStatisticsDialog.class,
                        "LBL_SERVICE_NAME_MULTILINE"); // NOI18N
                 
            } else if (columnIndex == 1) {
                ret = NbBundle.getMessage(ComponentEndpointsStatisticsDialog.class,
                        "LBL_ENDPOINT_NAME_MULTILINE"); // NOI18N

            } else if (columnIndex == 2) {
                ret = NbBundle.getMessage(ComponentEndpointsStatisticsDialog.class,
                        "LBL_IS_CONSUMES_MULTILINE"); // NOI18N

            } else if (columnIndex == 3) {
                ret = NbBundle.getMessage(ComponentEndpointsStatisticsDialog.class,
                        "LBL_NUM_OF_RECEIVED_DONES_MULTILINE"); // NOI18N

            } else if (columnIndex == 4) {
                ret = NbBundle.getMessage(ComponentEndpointsStatisticsDialog.class,
                        "LBL_NUM_OF_RECEIVED_ERRORS_MULTILINE"); // NOI18N

            } else if (columnIndex == 5) {
                ret = NbBundle.getMessage(ComponentEndpointsStatisticsDialog.class,
                        "LBL_NUM_OF_RECEIVED_FAULTS_MULTILINE"); // NOI18N

            } else if (columnIndex == 6) {
                ret = NbBundle.getMessage(ComponentEndpointsStatisticsDialog.class,
                        "LBL_NUM_OF_SENT_DONES_MULTILINE"); // NOI18N

            } else if (columnIndex == 7) {
                ret = NbBundle.getMessage(ComponentEndpointsStatisticsDialog.class,
                        "LBL_NUM_OF_SENT_ERRORS_MULTILINE"); // NOI18N

            } else if (columnIndex == 8) {
                ret = NbBundle.getMessage(ComponentEndpointsStatisticsDialog.class,
                        "LBL_NUM_OF_SENT_FAULTS_MULTILINE"); // NOI18N

            } else if (columnIndex == 9) {
                ret = NbBundle.getMessage(ComponentEndpointsStatisticsDialog.class,
                        "LBL_MSG_EXCHANGE_COMPONENT_TIME_AVERAGE_MULTILINE"); // NOI18N
                
            } else if (columnIndex == 10) {
                ret = NbBundle.getMessage(ComponentEndpointsStatisticsDialog.class,
                        "LBL_MSG_EXCHANGE_DELIVERY_CHANNEL_TIME_AVERAGE_MULTILINE"); // NOI18N

            } else if (columnIndex == 11) {
                ret = NbBundle.getMessage(ComponentEndpointsStatisticsDialog.class,
                        "LBL_MSG_EXCHANGE_MESSAGE_SERVICE_TIME_AVERAGE_MULTILINE"); // NOI18N
                
            }

            return "<HTML><B>" + ret + "</B></HTML>"; // NOI18N

        }
        
        @Override
        public Class getColumnClass(int columnIndex) {
            if (columnIndex == 2) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }

        public Object getValueAt(int rowIndex, int columnIndex) {

            Endpoint endpoint = endpoints.get(rowIndex);

            if (columnIndex == 0) {
                return "{" + endpoint.getNamespaceURI() + "}" + endpoint.getServiceName(); // NOI18N
            } else if (columnIndex == 1) {
                return endpoint.getEndpointName(); // NOI18N
            } else if (columnIndex == 2) {
                return endpoint.isConsumes();
            } else if (columnIndex == 3) {
                return statisticsMap.get(endpoint).getNumberOfReceivedDones();
            } else if (columnIndex == 4) {
                return statisticsMap.get(endpoint).getNumberOfReceivedErrors();
            } else if (columnIndex == 5) {
                return statisticsMap.get(endpoint).getNumberOfReceivedFaults();
            } else if (columnIndex == 6) {
                return statisticsMap.get(endpoint).getNumberOfSentDones();
            } else if (columnIndex == 7) {
                return statisticsMap.get(endpoint).getNumberOfSentErrors();
            } else if (columnIndex == 8) {
                return statisticsMap.get(endpoint).getNumberOfSentFaults();
            } else if (columnIndex == 9) {
                return statisticsMap.get(endpoint).getMessageExchangeComponentTimeAverage();
            } else if (columnIndex == 10) {
                return statisticsMap.get(endpoint).getMessageExchangeDeliveryChannelTimeAverage();
            } else if (columnIndex == 11) {
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
        table = new javax.swing.JTable();
        okButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pLabel.setText(org.openide.util.NbBundle.getMessage(ComponentEndpointsStatisticsDialog.class, "ComponentEndpointsStatisticsDialog.pLabel.text")); // NOI18N

        table.setModel(tableModel);
        pTableScrollPane.setViewportView(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getTableHeader().setReorderingAllowed(false);
        TableColumnModel tcm = table.getColumnModel();
        TableColumn tableFirstTC = tcm.getColumn(0);
        TableColumn tableSecondTC = tcm.getColumn(1);
        tableFirstTC.setPreferredWidth(150);
        tableSecondTC.setPreferredWidth(150);

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
                .add(pLabel)
                .addContainerGap(920, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(okButton))
            .add(pTableScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 971, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(pLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pTableScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
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
            .add(layout.createSequentialGroup()
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel pLabel;
    private javax.swing.JScrollPane pTableScrollPane;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
