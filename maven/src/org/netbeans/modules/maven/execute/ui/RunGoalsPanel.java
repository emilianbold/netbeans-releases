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
package org.netbeans.modules.maven.execute.ui;

import org.netbeans.modules.maven.api.execute.RunConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.netbeans.modules.maven.spi.grammar.GoalsProvider;
import org.netbeans.modules.maven.TextValueCompleter;
import org.netbeans.modules.maven.api.ProjectProfileHandler;
import org.netbeans.modules.maven.execute.BeanRunConfig;
import org.netbeans.modules.maven.customizer.ActionMappings;
import org.netbeans.modules.maven.customizer.PropertySplitter;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import hidden.org.codehaus.plexus.util.StringUtils;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;

/**
 *
 * @author  mkleint
 */
public class RunGoalsPanel extends javax.swing.JPanel {

    private List<NetbeansActionMapping> historyMappings;
    private int historyIndex = 0;
    private TextValueCompleter goalcompleter;
    private TextValueCompleter profilecompleter;

    /** Creates new form RunGoalsPanel */
    public RunGoalsPanel() {
        initComponents();
        historyMappings = new ArrayList<NetbeansActionMapping>();
        btnPrev.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/maven/execute/back.png", false)); //NOI18N
        btnNext.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/maven/execute/forward.png", false)); //NOI18N

        goalcompleter = new TextValueCompleter(new ArrayList<String>(0), txtGoals, " "); //NOI18N
        goalcompleter.setLoading(true);
        // doing lazy.. 
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                GoalsProvider provider = Lookup.getDefault().lookup(GoalsProvider.class);
                if (provider != null) {
                    final Set<String> strs = provider.getAvailableGoals();
                    try {
                        @SuppressWarnings("unchecked")
                        List<String> phases = EmbedderFactory.getProjectEmbedder().getLifecyclePhases();
                        strs.addAll(phases);
                    } catch (Exception e) {
                        // oh wel just ignore..
                        e.printStackTrace();
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            goalcompleter.setValueList(strs);
                        }
                    });
                }
            }
        });

        profilecompleter = new TextValueCompleter(new ArrayList<String>(0), txtProfiles, " ");
    }

    @Override
    public void addNotify() {
        super.addNotify();
        txtGoals.requestFocus();

    }

    private void readProfiles(final Project mavenProject) {
        profilecompleter.setLoading(true);
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                ProjectProfileHandler profileHandler = mavenProject.getLookup().lookup(ProjectProfileHandler.class);
                final List<String> ret = profileHandler.getAllProfiles();
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        profilecompleter.setValueList(ret);
                    }
                });
            }
        });
    }

    
    
    private String createSpaceSeparatedList(List list) {
        String str = ""; //NOI18N
        if (list != null) {
            Iterator it = list.iterator();
            while (it.hasNext()) {
                String elem = (String) it.next();
                str = str + elem + " "; //NOI18N
            }
        }
        return str;
    }

    public void readMapping(NetbeansActionMapping mapp, NbMavenProjectImpl project, ActionToGoalMapping historyMappings) {
        this.historyMappings.clear();
        this.historyMappings.addAll(historyMappings.getActions());
        this.historyMappings.add(mapp);
        historyIndex = this.historyMappings.size();
        readProfiles(project);
        moveHistory(-1);
    }

    public void readConfig(final RunConfig config) {
        historyMappings.clear();
        btnNext.setVisible(false);
        btnPrev.setVisible(false);
        txtGoals.setText(createSpaceSeparatedList(config.getGoals()));
        if (config.getProperties() != null) {
            StringBuffer buf = new StringBuffer();
            Iterator it = config.getProperties().keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                buf.append(key).append("=").append("\"").append(config.getProperties().getProperty(key)).append("\"").append("\n"); //NOI18N
            }
            taProperties.setText(buf.toString());
            if (buf.toString().matches(".*maven\\.test\\.skip\\s*=\\s*true\\s*.*")) { //NOI18N
                cbSkipTests.setSelected(true);
            }
        } else {
            taProperties.setText(""); //NOI18N
        }
        List<String> activatedProfiles = config.getActivatedProfiles();
        if (config.getProject() != null) {
            ProjectProfileHandler profileHandler=config.getProject().getLookup().lookup(ProjectProfileHandler.class);
            List<String> retrieveMergedActiveProfiles =
                    profileHandler.getMergedActiveProfiles(false);
            txtProfiles.setText(createSpaceSeparatedList(retrieveMergedActiveProfiles));
        } else {
            txtProfiles.setText(createSpaceSeparatedList(activatedProfiles));
        }
        
        setUpdateSnapshots(config.isUpdateSnapshots());
        setOffline(config.isOffline() != null ? config.isOffline().booleanValue() : false);
        setRecursive(config.isRecursive());
        setShowDebug(config.isShowDebug());
        if(config.getProject()!=null){
            readProfiles(config.getProject());
        }
    }

    private void readMapping(NetbeansActionMapping mapp) {
        txtGoals.setText(createSpaceSeparatedList(mapp.getGoals()));
        if (mapp.getProperties() != null) {
            StringBuffer buf = new StringBuffer();
            Iterator it = mapp.getProperties().keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                buf.append(key).append("=").append(mapp.getProperties().getProperty(key)).append("\n"); //NOI18N
            }
            taProperties.setText(buf.toString());
            if (buf.toString().matches(".*maven\\.test\\.skip\\s*=\\s*true\\s*.*")) { //NOI18N
                cbSkipTests.setSelected(true);
            }
        } else {
            taProperties.setText(""); //NOI18N
        }
        txtProfiles.setText(createSpaceSeparatedList(mapp.getActivatedProfiles()));
    }

    public void applyValues(NetbeansActionMapping mapp) {
        StringTokenizer tok = new StringTokenizer(txtGoals.getText().trim());
        List<String> lst = new ArrayList<String>();
        while (tok.hasMoreTokens()) {
            lst.add(tok.nextToken());
        }
        mapp.setGoals(lst.size() > 0 ? lst : null);

        PropertySplitter split = new PropertySplitter(taProperties.getText());
        String token = split.nextPair();
        Properties props = new Properties();
        while (token != null) {
            String[] prp = StringUtils.split(token, "=", 2); //NOI18N
            if (prp.length == 2) {
                String key = prp[0];
                //in case the user adds -D by mistake, remove it to get a parsable xml file.
                if (key.startsWith("-D")) { //NOI18N
                    key = key.substring("-D".length()); //NOI18N
                }
                if (key.startsWith("-")) { //NOI18N
                    key = key.substring(1);
                }
                props.setProperty(key, prp[1]);
            }
            token = split.nextPair();
        }
        if (cbSkipTests.isSelected()) {
            props.setProperty(ActionMappings.PROP_SKIP_TEST, "true"); //NOI18N
        }
        mapp.setProperties(props);

        tok = new StringTokenizer(txtProfiles.getText().trim());
        lst = new ArrayList<String>();
        while (tok.hasMoreTokens()) {
            lst.add(tok.nextToken());
        }
        mapp.setActivatedProfiles(lst);
        mapp.setRecursive(cbRecursive.isSelected());

    }

    public void applyValues(BeanRunConfig rc) {
        StringTokenizer tok = new StringTokenizer(txtGoals.getText().trim());
        List<String> lst = new ArrayList<String>();
        while (tok.hasMoreTokens()) {
            lst.add(tok.nextToken());
        }
        rc.setGoals(lst.size() > 0 ? lst : Collections.singletonList("install")); //NOI18N
        tok = new StringTokenizer(txtProfiles.getText().trim());
        lst = new ArrayList<String>();
        while (tok.hasMoreTokens()) {
            lst.add(tok.nextToken());
        }
        rc.setActivatedProfiles(lst);

        PropertySplitter split = new PropertySplitter(taProperties.getText());
        String token = split.nextPair();
        Properties props = new Properties();
        while (token != null) {
            String[] prp = StringUtils.split(token, "=", 2); //NOI18N
            if (prp.length == 2) {
                props.setProperty(prp[0], prp[1]);
            }
            token = split.nextPair();
        }
        if (cbSkipTests.isSelected()) {
            props.setProperty(ActionMappings.PROP_SKIP_TEST, "true"); //NOI18N
        }
        rc.setProperties(props);
        rc.setRecursive(isRecursive());
        rc.setShowDebug(isShowDebug());
        rc.setUpdateSnapshots(isUpdateSnapshots());
        rc.setOffline(Boolean.valueOf(isOffline()));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblGoals = new javax.swing.JLabel();
        txtGoals = new javax.swing.JTextField();
        lblProfiles = new javax.swing.JLabel();
        txtProfiles = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taProperties = new javax.swing.JTextArea();
        cbRecursive = new javax.swing.JCheckBox();
        cbOffline = new javax.swing.JCheckBox();
        cbDebug = new javax.swing.JCheckBox();
        cbUpdateSnapshots = new javax.swing.JCheckBox();
        cbSkipTests = new javax.swing.JCheckBox();
        btnNext = new javax.swing.JButton();
        btnPrev = new javax.swing.JButton();
        cbRemember = new javax.swing.JCheckBox();
        txtRemember = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();

        org.openide.awt.Mnemonics.setLocalizedText(lblGoals, org.openide.util.NbBundle.getMessage(RunGoalsPanel.class, "LBL_Goals")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblProfiles, org.openide.util.NbBundle.getMessage(RunGoalsPanel.class, "LBL_Profiles")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(RunGoalsPanel.class, "LBL_Properties")); // NOI18N

        taProperties.setColumns(20);
        taProperties.setRows(5);
        jScrollPane1.setViewportView(taProperties);

        org.openide.awt.Mnemonics.setLocalizedText(cbRecursive, org.openide.util.NbBundle.getMessage(RunGoalsPanel.class, "LBL_Recursive")); // NOI18N
        cbRecursive.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbRecursive.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(cbOffline, org.openide.util.NbBundle.getMessage(RunGoalsPanel.class, "LBL_Offline")); // NOI18N
        cbOffline.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbOffline.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(cbDebug, org.openide.util.NbBundle.getMessage(RunGoalsPanel.class, "LBL_Debug")); // NOI18N
        cbDebug.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbDebug.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(cbUpdateSnapshots, org.openide.util.NbBundle.getMessage(RunGoalsPanel.class, "LBL_Update_Snapshots")); // NOI18N
        cbUpdateSnapshots.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbUpdateSnapshots.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(cbSkipTests, org.openide.util.NbBundle.getMessage(RunGoalsPanel.class, "LBL_Skip_Tests")); // NOI18N
        cbSkipTests.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbSkipTests.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbSkipTests.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbSkipTestsActionPerformed(evt);
            }
        });

        btnNext.setToolTipText(org.openide.util.NbBundle.getMessage(RunGoalsPanel.class, "TIP_Next")); // NOI18N
        btnNext.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });

        btnPrev.setToolTipText(org.openide.util.NbBundle.getMessage(RunGoalsPanel.class, "TIP_Prev")); // NOI18N
        btnPrev.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrevActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbRemember, org.openide.util.NbBundle.getMessage(RunGoalsPanel.class, "LBL_Remember")); // NOI18N
        cbRemember.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbRemember.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(cbRecursive)
                            .add(cbOffline))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(cbDebug)
                            .add(cbUpdateSnapshots)))
                    .add(cbSkipTests)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblGoals)
                            .add(lblProfiles)
                            .add(jLabel2))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
                            .add(txtGoals, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
                            .add(txtProfiles, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(btnPrev)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnNext)
                        .add(52, 52, 52)
                        .add(cbRemember)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(txtRemember, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE))
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblGoals)
                    .add(txtGoals, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblProfiles)
                    .add(txtProfiles, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cbRecursive)
                    .add(cbUpdateSnapshots))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cbOffline)
                    .add(cbDebug))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbSkipTests)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnPrev)
                    .add(btnNext)
                    .add(cbRemember)
                    .add(txtRemember, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    private void cbSkipTestsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbSkipTestsActionPerformed
        String current = taProperties.getText();
        if (current.contains(ActionMappings.PROP_SKIP_TEST)) {
            taProperties.setText(current.replaceAll(".*maven\\.test\\.skip\\s*=\\s*[a-z]*\\s*.*", "maven.test.skip=" + (cbSkipTests.isSelected() ? "true" : "false"))); //NOI18N
        } else if (cbSkipTests.isSelected()) {
            taProperties.setText(taProperties.getText() + "\nmaven.test.skip=true"); //NOI18N
        }
        
    }//GEN-LAST:event_cbSkipTestsActionPerformed

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        moveHistory(1);
    }//GEN-LAST:event_btnNextActionPerformed

    private void moveHistory(int step) {
        historyIndex = historyIndex + step;
        readMapping(historyMappings.get(historyIndex));
        btnPrev.setEnabled(historyIndex != 0);
        btnNext.setEnabled(historyIndex != (historyMappings.size() - 1));
    }

    private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevActionPerformed
        moveHistory(-1);
    }//GEN-LAST:event_btnPrevActionPerformed

    public boolean isOffline() {
        return cbOffline.isSelected();
    }

    public boolean isShowDebug() {
        return cbDebug.isSelected();
    }

    public void setOffline(boolean b) {
        cbOffline.setSelected(b);
    }

    public void setShowDebug(boolean b) {
        cbDebug.setSelected(b);
    }

    public void setUpdateSnapshots(boolean b) {
        cbUpdateSnapshots.setSelected(b);
    }

    public void setSkipTests(boolean b) {
        cbSkipTests.setSelected(b);
    }

    public void setRecursive(boolean b) {
        cbRecursive.setSelected(b);
    }

    public boolean isSkipTests() {
        return cbSkipTests.isSelected();
    }

    public boolean isRecursive() {
        return cbRecursive.isSelected();
    }

    public boolean isUpdateSnapshots() {
        return cbUpdateSnapshots.isSelected();
    }

    public String isRememberedAs() {
        if (cbRemember.isSelected()) {
            String txt = txtRemember.getText().trim();
            if (txt.length() > 0) {
                return txt;
            }
        }
        return null;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrev;
    private javax.swing.JCheckBox cbDebug;
    private javax.swing.JCheckBox cbOffline;
    private javax.swing.JCheckBox cbRecursive;
    private javax.swing.JCheckBox cbRemember;
    private javax.swing.JCheckBox cbSkipTests;
    private javax.swing.JCheckBox cbUpdateSnapshots;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblGoals;
    private javax.swing.JLabel lblProfiles;
    private javax.swing.JTextArea taProperties;
    private javax.swing.JTextField txtGoals;
    private javax.swing.JTextField txtProfiles;
    private javax.swing.JTextField txtRemember;
    // End of variables declaration//GEN-END:variables
}
