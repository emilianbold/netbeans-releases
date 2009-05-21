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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.SideBarFactory;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProvider;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageDetails;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Editor footer for files while in code coverage mode: Show file coverage rate,
 * warnings about files being out of date, and quick buttons for enabling/disabling
 * highlights and clearing results.
 * <p>
 * <b>NOTE</b>: You must compile this module before attempting to open this form
 * in the GUI builder! The design depends on the CoverageBar class and Matisse can
 * only load the form if the .class, not just the .java file, is available!
 *
 * @author Tor Norbye
 */
public class CoverageSideBar extends javax.swing.JPanel {
    private static final String COVERAGE_SIDEBAR_PROP = "coverageSideBar"; // NOI18N

    private Document document;
    private boolean enabled;

    /** Creates new form CoverageSideBar */
    public CoverageSideBar(JTextComponent target) {
        document = target.getDocument();

        String mimeType = (String) document.getProperty("mimeType"); // NOI18N
        boolean on = false;
        if (mimeType != null) {
            CoverageManagerImpl manager = CoverageManagerImpl.getInstance();
            on = manager.isEnabled(mimeType);
            if (on) {
                CoverageProvider provider = getProvider();
                if (provider != null) {
                    on = provider.isEnabled() && manager.getShowEditorBar();
                } else {
                    on = false;
                }
            }
        }

        if (on) {
            showCoveragePanel(true);
        } else {
            updatePreferredSize();
        }

        document.putProperty(COVERAGE_SIDEBAR_PROP, this);
    }

    public static CoverageSideBar getSideBar(Document document) {
        return (CoverageSideBar)document.getProperty(COVERAGE_SIDEBAR_PROP);
    }

    public void setCoverage(FileCoverageDetails details) {
        if (details != null) {
            FileCoverageSummary summary = details.getSummary();
            float coverage = summary.getCoveragePercentage();

            if (coverage >= 0.0) {
                coverageBar.setCoveragePercentage(coverage);
            }
            //coverageBar.setStats(summary.getLineCount(), summary.getExecutedLineCount(),
            //        summary.getInferredCount(), summary.getPartialCount());

            long dataModified = details.lastUpdated();
            FileObject fo = details.getFile();
            boolean tooOld = false;
            if (fo != null && dataModified > 0 && dataModified < fo.lastModified().getTime()) {
                tooOld = true;
            } else if (fo != null && fo.isValid()) {
                try {
                    DataObject dobj = DataObject.find(fo);
                    tooOld = dobj.isModified();
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (tooOld) {
                warningsLabel.setText(NbBundle.getMessage(CoverageSideBar.class, "DataTooOld"));
            } else {
                warningsLabel.setText("");
            }
        } else {
            coverageBar.setCoveragePercentage(0.0f);
            warningsLabel.setText("");
        }
    }

    public boolean isShowingCoverage() {
        return enabled;
    }

    public void showCoveragePanel(boolean on) {
        if (on == enabled) {
            return;
        }
        this.enabled = on;
        if (on) {
            initComponents();
            setCoverage(null); // hide until we know
        } else {
            removeAll();
        }

        updatePreferredSize();
        revalidate();
        repaint();
    }

    private void updatePreferredSize() {
        if (enabled) {
            // Recompute
            setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            setPreferredSize(null);
            Dimension preferred = getPreferredSize();
            setPreferredSize(preferred);
        } else {
            setPreferredSize(new Dimension(0, 0));
            setMaximumSize(new Dimension(0, 0));
        }
        revalidate();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {


        label = new JLabel();
        coverageBar = new CoverageBar();
        warningsLabel = new JLabel();
        testButton = new JButton();
        allTestsButton = new JButton();
        clearButton = new JButton();
        reportButton = new JButton();
        jButton1 = new JButton();

        label.setText(NbBundle.getMessage(CoverageSideBar.class, "CoverageSideBar.label.text")); // NOI18N
        coverageBar.setMinimumSize(new Dimension(40, 10));

        warningsLabel.setForeground(UIManager.getDefaults().getColor("nb.errorForeground"));

        testButton.setText(NbBundle.getMessage(CoverageSideBar.class, "CoverageSideBar.testButton.text")); // NOI18N
        testButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                testOne(evt);
            }
        });

        allTestsButton.setText(NbBundle.getMessage(CoverageSideBar.class, "CoverageSideBar.allTestsButton.text")); // NOI18N
        allTestsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                allTests(evt);
            }
        });

        clearButton.setText(NbBundle.getMessage(CoverageSideBar.class, "CoverageSideBar.clearButton.text")); // NOI18N
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                clearResults(evt);
            }
        });

        reportButton.setText(NbBundle.getMessage(CoverageSideBar.class, "CoverageSideBar.reportButton.text")); // NOI18N
        reportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                report(evt);
            }
        });

        jButton1.setText(NbBundle.getMessage(CoverageSideBar.class, "CoverageSideBar.jButton1.text")); // NOI18N
        jButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                done(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(label)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(coverageBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED, 86, Short.MAX_VALUE)
                .add(warningsLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(testButton)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(allTestsButton)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(clearButton)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(reportButton)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jButton1))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createParallelGroup(GroupLayout.BASELINE)
                .add(label)
                .add(coverageBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .add(jButton1)
                .add(reportButton)
                .add(clearButton)
                .add(allTestsButton)
                .add(testButton)
                .add(warningsLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void clearResults(ActionEvent evt) {//GEN-FIRST:event_clearResults
        Project project = getProject();
        if (project != null) {
            CoverageManagerImpl.getInstance().clear(project);
        }
}//GEN-LAST:event_clearResults

    private void allTests(ActionEvent evt) {//GEN-FIRST:event_allTests
        String action = ActionProvider.COMMAND_TEST;
        CoverageProvider provider = getProvider();
        if (provider != null && provider.getTestAllAction() != null) {
            action = provider.getTestAllAction();
        }
        runAction(action);
    }//GEN-LAST:event_allTests

    private void testOne(ActionEvent evt) {//GEN-FIRST:event_testOne
        runAction(ActionProvider.COMMAND_TEST_SINGLE);
    }//GEN-LAST:event_testOne

    private void report(ActionEvent evt) {//GEN-FIRST:event_report
        Project project = getProject();
        if (project != null) {
            CoverageManagerImpl.getInstance().showReport(project);
        }
    }//GEN-LAST:event_report

    private void done(ActionEvent evt) {//GEN-FIRST:event_done
        Project project = getProject();
        if (project != null) {
            CoverageManagerImpl.getInstance().setEnabled(project, false);
        }
        showCoveragePanel(false);
    }//GEN-LAST:event_done

    private void runAction(String action) {
        Project project = getProject();
        if (project != null) {
            Lookup lookup = project.getLookup();
            FileObject fo = GsfUtilities.findFileObject(document);
            if (fo != null) {
                try {
                    DataObject dobj = DataObject.find(fo);
                    lookup = dobj.getLookup();
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            ActionProvider provider = project.getLookup().lookup(ActionProvider.class);
            if (provider != null) {
                if (provider.isActionEnabled(action, lookup)) {
                    provider.invokeAction(action, lookup);
                }
            }
        }
    }

    private Project getProject() {
        FileObject fo = GsfUtilities.findFileObject(document);
        if (fo != null) {
            Project project = FileOwnerQuery.getOwner(fo);
            return project;
        }

        return null;
    }

    private CoverageProvider getProvider() {
        Project project = getProject();
        if (project != null) {
            return CoverageManagerImpl.getProvider(project);
        }

        return null;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton allTestsButton;
    private JButton clearButton;
    private CoverageBar coverageBar;
    private JButton jButton1;
    private JLabel label;
    private JButton reportButton;
    private JButton testButton;
    private JLabel warningsLabel;
    // End of variables declaration//GEN-END:variables

    public static final class Factory implements SideBarFactory {
        public JComponent createSideBar(JTextComponent target) {
            return new CoverageSideBar(target);
        }
    }
}
