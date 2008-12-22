/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.gsf.codecoverage;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
//import javax.swing.table.TableRowSorter;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Window which displays a code coverage report
 */
final class CoverageReportTopComponent extends TopComponent {
    /** path to the icon used by the component and its open action */
    //static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private CoverageTableModel model;
    private Project project;
    private static final String PREFERRED_ID = "CoverageReportTopComponent";

    CoverageReportTopComponent(Project project, List<FileCoverageSummary> results) {
        model = new CoverageTableModel(results);
        this.project = project;
        initComponents();
        //Color color = table.getBackground();
        //table.setGridColor(color.darker());

        //table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setMaxWidth(1000);
        columnModel.getColumn(1).setMaxWidth(150);
        columnModel.getColumn(2).setMaxWidth(150);
        columnModel.getColumn(3).setMaxWidth(300);

        String projectName = ProjectUtils.getInformation(project).getDisplayName();
        setName(NbBundle.getMessage(CoverageReportTopComponent.class, "CTL_CoverageReportTopComponent", projectName));
        setToolTipText(NbBundle.getMessage(CoverageReportTopComponent.class, "HINT_CoverageReportTopComponent"));
        //setIcon(Utilities.loadImage(ICON_PATH, true));

        // Make the Total row bigger
        //if (results != null && results.size() > 0) {
        //    int rowHeight = table.getRowHeight();
        //    table.setRowHeight(model.getRowCount()-1, 2*rowHeight);
        //}

        table.setDefaultRenderer(Float.class, new CoverageRenderer());
        //JDK6 only - row sorting
        /*
        table.setAutoCreateRowSorter(true);
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel());
        table.setRowSorter(sorter);
        Comparator comparableComparator = new Comparator() {
            @SuppressWarnings("unchecked")
            public int compare(Object o1, Object o2) {
                return ((Comparable) o1).compareTo(o2);
            }
        };
        for (int i = 0; i < 4; i++) {
            sorter.setComparator(i, comparableComparator);
        }
        */

        ((CoverageBar) totalCoverage).setCoverage(model.getTotalCoverage());
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new JScrollPane();
        table = new JTable();
        clearResultsButton = new JButton();
        jLabel1 = new JLabel();
        totalCoverage = new CoverageBar();
        includeAllFilesCb = new JCheckBox();

        table.setModel(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                clicked(evt);
            }
        });
        jScrollPane1.setViewportView(table);

        Mnemonics.setLocalizedText(clearResultsButton, NbBundle.getMessage(CoverageReportTopComponent.class, "CoverageReportTopComponent.clearResultsButton.text")); // NOI18N
        clearResultsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                clearResultsButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(CoverageReportTopComponent.class, "CoverageReportTopComponent.jLabel1.text"));
        Mnemonics.setLocalizedText(includeAllFilesCb, NbBundle.getMessage(CoverageReportTopComponent.class, "CoverageReportTopComponent.includeAllFilesCb.text"));
        includeAllFilesCb.setEnabled(false);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(jScrollPane1, GroupLayout.DEFAULT_SIZE, 522, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(clearResultsButton)
                        .addPreferredGap(LayoutStyle.RELATED, 97, Short.MAX_VALUE)
                        .add(includeAllFilesCb))
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(totalCoverage, GroupLayout.PREFERRED_SIZE, 231, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.TRAILING)
                    .add(jLabel1)
                    .add(totalCoverage, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jScrollPane1, GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(clearResultsButton)
                    .add(includeAllFilesCb))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void clearResultsButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_clearResultsButtonActionPerformed
        CoverageManagerImpl.getInstance().clear(project);
}//GEN-LAST:event_clearResultsButtonActionPerformed

    private void clicked(MouseEvent evt) {//GEN-FIRST:event_clicked
        if (evt.getClickCount() == 2) {
            int row = table.getSelectedRow();
            if (row != -1) {
                FileCoverageSummary result = (FileCoverageSummary) model.getValueAt(row, -1);
                CoverageManagerImpl.getInstance().showFile(project, result);
            }
        }
    }//GEN-LAST:event_clicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton clearResultsButton;
    private JCheckBox includeAllFilesCb;
    private JLabel jLabel1;
    private JScrollPane jScrollPane1;
    private JTable table;
    private JProgressBar totalCoverage;
    // End of variables declaration//GEN-END:variables

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        CoverageManagerImpl.getInstance().closedReport(project);
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    void updateData(List<FileCoverageSummary> results) {
        model = new CoverageTableModel(results);
        table.setModel(model);
        ((CoverageBar) totalCoverage).setCoverage(model.getTotalCoverage());
    }

    private static class CoverageTableModel implements TableModel {
        List<FileCoverageSummary> results;
        //List<TableModelListener> listeners = new ArrayList<TableModelListener>();
        float totalCoverage = 0.0f;

        public CoverageTableModel(List<FileCoverageSummary> results) {
            if (results == null || results.size() == 0) {
                results = new ArrayList<FileCoverageSummary>();
            } else {
                Collections.sort(results);
            }

            int lineCount = 0;
            int executedLineCount = 0;
            for (FileCoverageSummary result : results) {
                lineCount += result.getLineCount();
                executedLineCount += result.getExecutedLineCount();
            }

            if (results.size() == 0) {
                results.add(new FileCoverageSummary(null, NbBundle.getMessage(CoverageReportTopComponent.class, "NoData"), lineCount, executedLineCount));
            } else {
                FileCoverageSummary total = new FileCoverageSummary(null, "<html><b>" + // NOI18N
                        NbBundle.getMessage(CoverageReportTopComponent.class, "Total") +
                        "</b></html>", lineCount, executedLineCount); // NOI18N
                totalCoverage = total.getCoveragePercentage();
                results.add(total);
            }
            this.results = results;
        }

        float getTotalCoverage() {
            return totalCoverage;
        }

        public int getRowCount() {
            return results.size();
        }

        public int getColumnCount() {
            return 4;
        }

        public String getColumnName(int col) {
            switch (col) {
                case 0:
                    return NbBundle.getMessage(CoverageReportTopComponent.class, "Filename");
                case 1:
                    return NbBundle.getMessage(CoverageReportTopComponent.class, "TotalStatements");
                case 2:
                    return NbBundle.getMessage(CoverageReportTopComponent.class, "ExecutedStatements");
                case 3:
                default:
                    return NbBundle.getMessage(CoverageReportTopComponent.class, "Coverage");
            }
        }

        public Class<?> getColumnClass(int col) {
            switch (col) {
                case 1:
                    return Integer.class;
                case 2:
                    return Integer.class;
                case 3:
                    return Float.class;
                case 0:
                default:
                    return String.class;
            }
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }

        public Object getValueAt(int row, int col) {
            FileCoverageSummary result = results.get(row);
            switch (col) {
                case -1: // Special contract with table selection handler
                    return result;
                case 0:
                    return result.getDisplayName();
                case 1:
                    return result.getLineCount();
                case 2:
                    return result.getExecutedLineCount();
                case 3:
                    return result.getCoveragePercentage();
                default:
                    return null;
            }
        }

        public void setValueAt(Object arg0, int arg1, int arg2) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addTableModelListener(TableModelListener listener) {
        }

        public void removeTableModelListener(TableModelListener listener) {
        }
    }

    private static class CoverageBar extends JProgressBar {
        protected static Border noFocusBorder = new EmptyBorder(0, 0, 0, 0);

        CoverageBar() {
            setBorder(noFocusBorder);
            setBorderPainted(true);
            //setBorderPaintedFlat(true);
            setOpaque(true);
            setIndeterminate(false);
            setMaximum(100);
            setBackground(Color.RED);
            setForeground(Color.GREEN);
            setStringPainted(true);
            setUI(new BasicProgressBarUI() {
                @Override
                protected Color getSelectionForeground() {
                    return Color.BLACK;
                }

                @Override
                protected Color getSelectionBackground() {
                    return Color.WHITE;
                }
            });
        }

        public void setCoverage(float coveragePercent) {
            String percent = String.format("%.1f %%", coveragePercent); // NOI18N
            setString(percent);
            setValue((int) coveragePercent);
        }
    }

    private static class CoverageRenderer extends CoverageBar implements TableCellRenderer {
        public CoverageRenderer() {
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (value == null) {
                return new DefaultTableCellRenderer().getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
            }

            float coverage = (Float) value;
            setCoverage(coverage);

            return this;
        }
    }
}
