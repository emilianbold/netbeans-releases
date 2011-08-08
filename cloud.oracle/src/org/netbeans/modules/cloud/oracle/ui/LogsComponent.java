/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

/*
 * LogsComponent.java
 *
 * Created on 23/06/2011, 3:15:14 PM
 */
package org.netbeans.modules.cloud.oracle.ui;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import oracle.nuviaq.api.ApplicationManager;
import oracle.nuviaq.model.xml.Job;
import oracle.nuviaq.model.xml.Log;
import org.netbeans.modules.cloud.oracle.OracleInstance;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

@TopComponent.Description(preferredID = "LogsTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
//@ActionID(category = "Window", id = "org.netbeans.modules.cloud.oracle.ui.LogsTopComponent")
//@ActionReference(path = "Menu/Window" /*, position = 333 */)
//@TopComponent.OpenActionRegistration(displayName = "#CTL_LogsAction",
//preferredID = "LogsTopComponent")
public class LogsComponent extends TopComponent {

    private List<Job> jobs;
    private ApplicationManager am;
    
    /** Creates new form LogsComponent */
    public LogsComponent(OracleInstance oi) {
        initComponents();
        setName(NbBundle.getMessage(LogsComponent.class, "CTL_LogsTopComponent", oi.getName()));
        setToolTipText(NbBundle.getMessage(LogsComponent.class, "HINT_LogsTopComponent"));
        this.am = oi.getApplicationManager();
//        jobs = pm.listJobs();
//        Collections.reverse(jobs);
        jobs = new ArrayList<Job>();
        Job jt = new Job();
        jt.setOperation("loading...");
        jobs.add(jt);
        jobsTable.setModel(new JobsModel(jobs));
        jobsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                fetchLogs();
            }
        });
        loadJobs();
    }
    
    private void loadJobs() {
        OracleInstance.runAsynchronously(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final List<Job> jobs_ = am.listJobs();
                Collections.reverse(jobs_);
                final List<Job> jobs = new ArrayList<Job>();
                int i = 0;
                for (Job jt: jobs_) {
                    jobs.add(jt);
                    i++;
                    if (i > 50) {
                        break;
                    }
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        LogsComponent.this.jobs = jobs;
                        jobsTable.setModel(new JobsModel(jobs));
                        if (jobs.size() > 0) {
                            jobsTable.getSelectionModel().setSelectionInterval(0, 0);
                        }
                    }
                });
                return null;
            }
        });
    }
    
    private void fetchLogs() {
        if (jobsTable.getSelectedRow() < 0) {
            logs.setText("");
            return;
        }
        final Job jt = jobs.get(jobsTable.getSelectedRow());
        if (jt.getJobId() == null) {
            // in "loading..." state
            logs.setText("");
            return;
        }
        if (jt.getLogs().size() == 0) {
            logs.setText("no logs was generated");
            return;
        }
        
        logs.setText("loading...");
        
        OracleInstance.runAsynchronously(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final StringBuffer sb = new StringBuffer();
                List<Log> logs = jt.getLogs();
                sb.append(""+logs.size()+ " log file(s) found:\n\n");
                for (Log lt : logs) {
                    sb.append("==================== Log file: "+lt.getName()+"==========================\n\n");
                    ByteArrayOutputStream os = new ByteArrayOutputStream(8000);
                    try {
                        am.fetchJobLog(jt.getJobId(), lt.getName(), os);
                    } catch (Throwable t) {
                        sb.append("Exception occured while retrieving the log:\n"+t.toString());
                        continue;
                    }
                    try {
                        sb.append(os.toString(Charset.defaultCharset().name()));
                    } catch (UnsupportedEncodingException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        LogsComponent.this.logs.setText(sb.toString());
                    }
                });
                return null;
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jobsTable = new javax.swing.JTable();
        refreshButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        logs = new javax.swing.JTextArea();
        closeButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(LogsComponent.class, "LogsComponent.jLabel1.text")); // NOI18N

        jScrollPane1.setViewportView(jobsTable);

        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, org.openide.util.NbBundle.getMessage(LogsComponent.class, "LogsComponent.refreshButton.text")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(LogsComponent.class, "LogsComponent.jLabel2.text")); // NOI18N

        logs.setColumns(20);
        logs.setRows(5);
        jScrollPane2.setViewportView(logs);

        org.openide.awt.Mnemonics.setLocalizedText(closeButton, org.openide.util.NbBundle.getMessage(LogsComponent.class, "LogsComponent.closeButton.text")); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 603, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 603, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 515, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(refreshButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addComponent(closeButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeButton)
                    .addComponent(refreshButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    public static void showJobsDialog(OracleInstance oi) {
        LogsComponent p = new LogsComponent(oi);
        p.open();
        p.requestActive();
    }
    
    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        loadJobs();
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_closeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jobsTable;
    private javax.swing.JTextArea logs;
    private javax.swing.JButton refreshButton;
    // End of variables declaration//GEN-END:variables

//    private static class JobDesc {
//        private String jobId;
//        private String jobOperation;
//        private String jobStatus;
//        private String duration;
//    }
    
    private static class JobsModel implements TableModel {

        private List<Job> jobs;

        public JobsModel(List<Job> jobs) {
            this.jobs = new ArrayList(jobs);
        }
        
        @Override
        public int getRowCount() {
            return jobs.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        private String[] header = new String[]{"ID", "Operation", "Status", "Duration"};
        private Class[] headerClass = new Class[]{String.class, String.class, String.class, String.class};
        
        @Override
        public String getColumnName(int columnIndex) {
            return header[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return headerClass[columnIndex];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Job jt = jobs.get(rowIndex);
            if (columnIndex == 0) {
                return jt.getJobId();
            } else if (columnIndex == 1) {
                return jt.getOperation();
            } else if (columnIndex == 2) {
                return jt.getStatus();
            } else {
                if (jt.getStartTime() != null && jt.getEndTime() != null) {
                    return getDuration(jt.getStartTime(), jt.getEndTime());
                } else {
                    return "";
                }
            }
        }

        
        private String getDuration(String start, String end) {
            // <StartTime>2011-06-24 11:16:47.034</StartTime>
            // <EndTime>2011-06-24 11:18:03.421</EndTime>            
            DateFormat formatter;
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            try {
                Date startDate = formatter.parse(start);
                Date endDate = formatter.parse(end);
                long startTime = startDate.getTime();
                long endTime = endDate.getTime();
                long diff = endTime - startTime;

                long secondInMillis = 1000;
                long minuteInMillis = secondInMillis * 60;

                long elapsedMinutes = diff / minuteInMillis;
                diff = diff % minuteInMillis;
                long elapsedSeconds = diff / secondInMillis;
                
                return ""+elapsedMinutes+":"+elapsedSeconds;
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
                return "?";
            }
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
        }
        
    }


}
