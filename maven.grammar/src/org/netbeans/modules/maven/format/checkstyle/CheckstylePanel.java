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



package org.netbeans.modules.maven.format.checkstyle;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.modules.maven.api.customizer.support.CheckBoxUpdater;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.maven.model.pom.ReportPlugin;
import org.netbeans.modules.maven.model.pom.Reporting;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;

/**
http://checkstyle.sourceforge.net/config_blocks.html#LeftCurly
http://checkstyle.sourceforge.net/config_blocks.html#RightCurly
http://checkstyle.sourceforge.net/config_sizes.html#LineLength
http://checkstyle.sourceforge.net/config_blocks.html#NeedBraces
http://checkstyle.sourceforge.net/config_whitespace.html#WhitespaceAfter
http://checkstyle.sourceforge.net/config_whitespace.html#WhitespaceAround
http://checkstyle.sourceforge.net/config_whitespace.html#ParenPad
 *
 * @author mkleint
 */
public class CheckstylePanel extends javax.swing.JPanel {
    private final ModelHandle handle;
    private final ProjectCustomizer.Category category;
    private boolean generated = false;
    private final CheckBoxUpdater checkboxUpdater;


    CheckstylePanel(ModelHandle hndl, ProjectCustomizer.Category cat) {
        initComponents();
        this.handle = hndl;
        category = cat;
        checkboxUpdater = new CheckBoxUpdater(cbEnable) {
            @Override
            public Boolean getValue() {
                org.netbeans.modules.maven.model.profile.Profile prof = handle.getNetbeansPrivateProfile(false);
                String val = null;
                if (prof != null) {
                    org.netbeans.modules.maven.model.profile.Properties props = prof.getProperties();
                    if (props != null && props.getProperty(Constants.HINT_CHECKSTYLE_FORMATTING) != null) {
                        val = prof.getProperties().getProperty(Constants.HINT_CHECKSTYLE_FORMATTING);
                    }
                }
                if (val == null) {
                    Properties props = handle.getPOMModel().getProject().getProperties();
                    if (props != null) {
                        val = props.getProperty(Constants.HINT_CHECKSTYLE_FORMATTING);
                    }
                }
                if (val == null) {
                    val = handle.getRawAuxiliaryProperty(Constants.HINT_CHECKSTYLE_FORMATTING, true);
                }
                if (val != null) {
                    Boolean ret = Boolean.parseBoolean(val);
                    return ret;
                }
                return null;
            }

            @Override
            public boolean getDefaultValue() {
                return Boolean.FALSE;
            }

            @Override
            public void setValue(Boolean value) {
                String val = value != null ? value.toString() : null;
                boolean hasConfig = handle.getRawAuxiliaryProperty(Constants.HINT_CHECKSTYLE_FORMATTING, true) != null;
                //TODO also try to take the value in pom vs inherited pom value into account.

                org.netbeans.modules.maven.model.profile.Profile prof = handle.getNetbeansPrivateProfile(false);
                if (prof != null) {
                    org.netbeans.modules.maven.model.profile.Properties profprops = prof.getProperties();
                    if (profprops != null && profprops.getProperty(Constants.HINT_CHECKSTYLE_FORMATTING) != null) {
                        profprops.setProperty(Constants.HINT_CHECKSTYLE_FORMATTING, val);
                        if (hasConfig) {
                            // in this case clean up the auxiliary config
                            handle.setRawAuxiliaryProperty(Constants.HINT_CHECKSTYLE_FORMATTING, null, true);
                        }
                        handle.markAsModified(handle.getProfileModel());
                        return;
                    }
                }

                if (handle.getProject().getProperties().containsKey(Constants.HINT_CHECKSTYLE_FORMATTING)) {
                    Properties modprops = handle.getPOMModel().getProject().getProperties();
                    if (modprops == null) {
                        modprops = handle.getPOMModel().getFactory().createProperties();
                        handle.getPOMModel().getProject().setProperties(modprops);
                    }
                    modprops.setProperty(Constants.HINT_CHECKSTYLE_FORMATTING, val); //NOI18N
                    handle.markAsModified(handle.getPOMModel());
                    if (hasConfig) {
                        // in this case clean up the auxiliary config
                        handle.setRawAuxiliaryProperty(Constants.HINT_CHECKSTYLE_FORMATTING, null, true);
                    }
                    return;
                }
                handle.setRawAuxiliaryProperty(Constants.HINT_CHECKSTYLE_FORMATTING, val, true);
            }
        };

        btnLearnMore.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLearnMore.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(new URL("http://maven.apache.org/plugins/maven-checkstyle-plugin"));
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

        });

    }

    @Override
    public void addNotify() {
        super.addNotify();
        boolean defines = AuxPropsImpl.definesCheckStyle(handle.getProject());
        boolean missing = !defines && !generated;
        lblMissing.setVisible(missing);
        btnMissing.setVisible(missing);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cbEnable = new javax.swing.JCheckBox();
        lblHint = new javax.swing.JLabel();
        lblMissing = new javax.swing.JLabel();
        btnMissing = new javax.swing.JButton();
        btnLearnMore = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(cbEnable, org.openide.util.NbBundle.getMessage(CheckstylePanel.class, "CheckstylePanel.cbEnable.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblHint, org.openide.util.NbBundle.getMessage(CheckstylePanel.class, "CheckstylePanel.lblHint.text")); // NOI18N
        lblHint.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        org.openide.awt.Mnemonics.setLocalizedText(lblMissing, org.openide.util.NbBundle.getMessage(CheckstylePanel.class, "CheckstylePanel.lblMissing.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnMissing, org.openide.util.NbBundle.getMessage(CheckstylePanel.class, "CheckstylePanel.btnMissing.text")); // NOI18N
        btnMissing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMissingActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnLearnMore, org.openide.util.NbBundle.getMessage(CheckstylePanel.class, "CheckstylePanel.btnLearnMore.text")); // NOI18N
        btnLearnMore.setBorderPainted(false);
        btnLearnMore.setContentAreaFilled(false);
        btnLearnMore.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(cbEnable)
                    .add(layout.createSequentialGroup()
                        .add(lblMissing)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnMissing))
                    .add(layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(btnLearnMore)
                            .add(lblHint, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(cbEnable)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(lblHint, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 162, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btnLearnMore)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 31, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblMissing)
                    .add(btnMissing))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnMissingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMissingActionPerformed
        generated = true;
        //generate now
        POMModel mdl = handle.getPOMModel();
        Reporting rep = mdl.getProject().getReporting();
        if (rep == null) {
            rep = mdl.getFactory().createReporting();
            mdl.getProject().setReporting(rep);
        }
        ReportPlugin plg = rep.findReportPluginById(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_CHECKSTYLE);
        if (plg == null) {
            plg = mdl.getFactory().createReportPlugin();
            plg.setGroupId(Constants.GROUP_APACHE_PLUGINS);
            plg.setArtifactId(Constants.PLUGIN_CHECKSTYLE);
            Configuration conf = mdl.getFactory().createConfiguration();
            conf.setSimpleParameter("configLocation", "config/sun_checks.xml"); //NOI18N
            plg.setConfiguration(conf);
            rep.addReportPlugin(plg);
        }
        handle.markAsModified(handle.getPOMModel());
        
        //hide the button, we're done
        lblMissing.setVisible(false);
        btnMissing.setVisible(false);


    }//GEN-LAST:event_btnMissingActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLearnMore;
    private javax.swing.JButton btnMissing;
    private javax.swing.JCheckBox cbEnable;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblMissing;
    // End of variables declaration//GEN-END:variables

}
