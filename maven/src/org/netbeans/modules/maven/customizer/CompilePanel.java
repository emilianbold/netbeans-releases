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

package org.netbeans.modules.maven.customizer;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.plaf.UIResource;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.modules.maven.api.customizer.support.CheckBoxUpdater;
import org.netbeans.modules.maven.api.customizer.support.ComboBoxUpdater;
import org.netbeans.modules.maven.classpath.BootClassPathImpl;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.maven.options.DontShowAgainSettings;
import org.netbeans.modules.maven.options.MavenSettings;
import org.netbeans.modules.maven.options.MavenVersionSettings;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author mkleint
 */
public class CompilePanel extends javax.swing.JPanel implements WindowFocusListener {
    private static final String[] LABELS = new String[] {
        NbBundle.getMessage(CompilePanel.class, "COS_ALL"),
        NbBundle.getMessage(CompilePanel.class, "COS_APP"),
        NbBundle.getMessage(CompilePanel.class, "COS_TESTS"),
        NbBundle.getMessage(CompilePanel.class, "COS_NONE")
    };
    private static final String[] VALUES = new String[] {
        "all",//NOI18N
        "app",//NOI18N
        "test",//NOI18N
        "none"//NOI18N
    };

    private static final String PARAM_DEBUG = "debug";//NOI18N
    private static final String PARAM_DEPRECATION = "showDeprecation";

    private static final int COS_ALL = 0;
    private static final int COS_APP = 1;
    private static final int COS_TESTS = 2;
    private static final int COS_NONE = 3;

    private ComboBoxUpdater<String> listener;
    private final ModelHandle handle;
    private final Project project;
    private CheckBoxUpdater debugUpdater;
    private CheckBoxUpdater deprecateUpdater;
    private static boolean warningShown = false;

    private Color origComPlatformFore;

    /** Creates new form CompilePanel */
    public CompilePanel(ModelHandle handle, Project prj) {
        initComponents();
        this.handle = handle;
        project = prj;
        ComboBoxModel mdl = new DefaultComboBoxModel(LABELS);
        comCompileOnSave.setModel(mdl);
        comJavaPlatform.setModel(new PlatformsModel());
        comJavaPlatform.setRenderer(new PlatformsRenderer());

        origComPlatformFore = comJavaPlatform.getForeground();

        initValues();
    }

    private String valueToLabel(String value) {
        for (int i = 0; i < VALUES.length; i++) {
            if (VALUES[i].equalsIgnoreCase(value)) {
                return LABELS[i];
            }
        }
        return LABELS[COS_TESTS];
    }

    private String labelToValue(String label) {
        for (int i = 0; i < LABELS.length; i++) {
            if (LABELS[i].equalsIgnoreCase(label)) {
                return VALUES[i];
            }
        }
        return VALUES[COS_TESTS];
    }

    private void initValues() {
        listener = new ComboBoxUpdater<String>(comCompileOnSave, lblCompileOnSave) {

            public String getDefaultValue() {
                return LABELS[COS_TESTS];
            }

            public String getValue() {
                org.netbeans.modules.maven.model.profile.Profile prof = handle.getNetbeansPrivateProfile(false);
                String val = null;
                if (prof != null) {
                    org.netbeans.modules.maven.model.profile.Properties props = prof.getProperties();
                    if (props != null && props.getProperty(Constants.HINT_COMPILE_ON_SAVE) != null) {
                        val = prof.getProperties().getProperty(Constants.HINT_COMPILE_ON_SAVE);
                    }
                }
                if (val == null) {
                    Properties props = handle.getPOMModel().getProject().getProperties();
                    if (props != null) {
                        val = props.getProperty(Constants.HINT_COMPILE_ON_SAVE);
                    }
                }
                if (val == null) {
                    handle.getRawAuxiliaryProperty(Constants.HINT_COMPILE_ON_SAVE, true);
                }
                if (val != null) {
                    return valueToLabel(val);
                }
                return LABELS[COS_TESTS];
            }

            public void setValue(String label) {
                String value = labelToValue(label);
                if (value != null && value.equals(VALUES[COS_TESTS])) {
                    //just reset the value, no need to persist default.
                    value = null;
                }
                if (VALUES[COS_ALL].equals(value) || VALUES[COS_APP].equals(value)) {
                    if (!warningShown && DontShowAgainSettings.getDefault().showWarningAboutApplicationCoS()) {
                        WarnPanel panel = new WarnPanel(NbBundle.getMessage(CompilePanel.class, "HINT_ApplicationCoS"));
                        NotifyDescriptor dd = new NotifyDescriptor.Message(panel, NotifyDescriptor.PLAIN_MESSAGE);
                        DialogDisplayer.getDefault().notify(dd);
                        if (panel.disabledWarning()) {
                            DontShowAgainSettings.getDefault().dontshowWarningAboutApplicationCoSAnymore();
                        }
                        warningShown = true;
                    }
                }

                boolean hasConfig = handle.getRawAuxiliaryProperty(Constants.HINT_COMPILE_ON_SAVE, true) != null;

                org.netbeans.modules.maven.model.profile.Profile prof = handle.getNetbeansPrivateProfile(false);
                if (prof != null) {
                    org.netbeans.modules.maven.model.profile.Properties profprops = prof.getProperties();
                    if (profprops != null && profprops.getProperty(Constants.HINT_COMPILE_ON_SAVE) != null) {
                        profprops.setProperty(Constants.HINT_COMPILE_ON_SAVE, value == null ? null : value);
                        if (hasConfig) {
                            // in this case clean up the auxiliary config
                            handle.setRawAuxiliaryProperty(Constants.HINT_COMPILE_ON_SAVE, null, true);
                        }
                        handle.markAsModified(handle.getProfileModel());
                        return;
                    }
                }

                if (handle.getProject().getProperties().containsKey(Constants.HINT_COMPILE_ON_SAVE)) {
                    Properties modprops = handle.getPOMModel().getProject().getProperties();
                    if (modprops == null) {
                        modprops = handle.getPOMModel().getFactory().createProperties();
                        handle.getPOMModel().getProject().setProperties(modprops);
                    }
                    modprops.setProperty(Constants.HINT_COMPILE_ON_SAVE, value == null ? null : value); //NOI18N
                    handle.markAsModified(handle.getPOMModel());
                    if (hasConfig) {
                        // in this case clean up the auxiliary config
                        handle.setRawAuxiliaryProperty(Constants.HINT_COMPILE_ON_SAVE, null, true);
                    }
                    return;
                }
                handle.setRawAuxiliaryProperty(Constants.HINT_COMPILE_ON_SAVE, value == null ? null : value, true);
            }
        };
        debugUpdater = new CheckBoxUpdater(cbDebug) {
            public Boolean getValue() {
                String val = getCompilerParam(handle,PARAM_DEBUG);
                if (val != null) {
                    return Boolean.valueOf(val);
                }
                return null;
            }

            public void setValue(Boolean value) {
                String text;
                if (value == null) {
                    //TODO we should attempt to remove the configuration
                    // from pom if this parameter is the only one defined.
                    text = "true";//NOI18N
                } else {
                    text = value.toString();
                }
                checkCompilerParam(handle, PARAM_DEBUG, text);
            }

            public boolean getDefaultValue() {
                return true;
            }
        };

        deprecateUpdater = new CheckBoxUpdater(cbDeprecate) {
            public Boolean getValue() {
                String val = getCompilerParam(handle,PARAM_DEPRECATION);
                if (val != null) {
                    return Boolean.valueOf(val);
                }
                return null;
            }

            public void setValue(Boolean value) {
                String text;
                if (value == null) {
                    //TODO we should attempt to remove the configuration
                    // from pom if this parameter is the only one defined.
                    text = "false";//NOI18N
                } else {
                    text = value.toString();
                }
                checkCompilerParam(handle, PARAM_DEPRECATION, text);
            }

            public boolean getDefaultValue() {
                return false;
            }
        };

        // java platform updater
        new ComboBoxUpdater<JavaPlatform>(comJavaPlatform, lblJavaPlatform) {

            @Override
            public JavaPlatform getValue() {
                org.netbeans.modules.maven.model.profile.Profile prof = handle.getNetbeansPrivateProfile(false);
                String val = null;
                if (prof != null) {
                    org.netbeans.modules.maven.model.profile.Properties props = prof.getProperties();
                    if (props != null && props.getProperty(Constants.HINT_JDK_PLATFORM) != null) {
                        val = props.getProperty(Constants.HINT_JDK_PLATFORM);
                    }
                }
                if (val == null) {
                    Properties props = handle.getPOMModel().getProject().getProperties();
                    if (props != null) {
                        val = props.getProperty(Constants.HINT_JDK_PLATFORM);
                    }
                }
                if (val == null) {
                    val = handle.getRawAuxiliaryProperty(Constants.HINT_JDK_PLATFORM, true);
                }
                if (val != null) {
                    return BootClassPathImpl.getActivePlatform(val);
                } else {
                    return getSelPlatform();
                }
            }

            @Override
            public JavaPlatform getDefaultValue() {
                return getSelPlatform();
            }

            @Override
            public void setValue(JavaPlatform value) {
                JavaPlatform platf = value == null ? JavaPlatformManager.getDefault().getDefaultPlatform() : value;
                String platformId = platf.getProperties().get("platform.ant.name"); //NOI18N
                if (JavaPlatformManager.getDefault().getDefaultPlatform().equals(platf)) {
                    platformId = null;
                }

                boolean hasConfig = handle.getRawAuxiliaryProperty(Constants.HINT_JDK_PLATFORM, true) != null;
                //TODO also try to take the value in pom vs inherited pom value into account.

                org.netbeans.modules.maven.model.profile.Profile prof = handle.getNetbeansPrivateProfile(false);
                if (prof != null) {
                    org.netbeans.modules.maven.model.profile.Properties profprops = prof.getProperties();
                    if (profprops != null && profprops.getProperty(Constants.HINT_JDK_PLATFORM) != null) {
                        profprops.setProperty(Constants.HINT_JDK_PLATFORM, platformId);
                        if (hasConfig) {
                            // in this case clean up the auxiliary config
                            handle.setRawAuxiliaryProperty(Constants.HINT_JDK_PLATFORM, null, true);
                        }
                        handle.markAsModified(handle.getProfileModel());
                        return;
                    }
                }

                if (handle.getProject().getProperties().containsKey(Constants.HINT_JDK_PLATFORM)) {
                    Properties modprops = handle.getPOMModel().getProject().getProperties();
                    if (modprops == null) {
                        modprops = handle.getPOMModel().getFactory().createProperties();
                        handle.getPOMModel().getProject().setProperties(modprops);
                    }
                    modprops.setProperty(Constants.HINT_JDK_PLATFORM, platformId); //NOI18N
                    handle.markAsModified(handle.getPOMModel());
                    if (hasConfig) {
                        // in this case clean up the auxiliary config
                        handle.setRawAuxiliaryProperty(Constants.HINT_JDK_PLATFORM, null, true);
                    }
                    return;
                }
                handle.setRawAuxiliaryProperty(Constants.HINT_JDK_PLATFORM, platformId, true);
            }
        };

        checkExternalMaven();
        
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // for external maven availability checking
        SwingUtilities.getWindowAncestor(this).addWindowFocusListener(this);
    }

    private JavaPlatform getSelPlatform () {
        String platformId = project.getLookup().lookup(AuxiliaryProperties.class).
                get(Constants.HINT_JDK_PLATFORM, true);
        return BootClassPathImpl.getActivePlatform(platformId);
    }

    private boolean isExternalMaven () {
        AuxiliaryProperties props = project.getLookup().lookup(AuxiliaryProperties.class);
        String val = props.get(Constants.HINT_USE_EXTERNAL, true);
        boolean useEmbedded = "false".equalsIgnoreCase(val);

        return !useEmbedded && MavenSettings.canFindExternalMaven();
    }

    private void checkExternalMaven () {
        if (isExternalMaven()) {
            lblWarnPlatform.setVisible(false);
            btnSetupHome.setVisible(false);
            comJavaPlatform.setEnabled(true);
        } else {
            comJavaPlatform.setEnabled(false);
            lblWarnPlatform.setVisible(true);
            btnSetupHome.setVisible(true);
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

        lblCompileOnSave = new javax.swing.JLabel();
        comCompileOnSave = new javax.swing.JComboBox();
        lblHint1 = new javax.swing.JLabel();
        lblHint2 = new javax.swing.JLabel();
        cbDebug = new javax.swing.JCheckBox();
        cbDeprecate = new javax.swing.JCheckBox();
        lblJavaPlatform = new javax.swing.JLabel();
        comJavaPlatform = new javax.swing.JComboBox();
        btnMngPlatform = new javax.swing.JButton();
        lblWarnPlatform = new javax.swing.JLabel();
        btnSetupHome = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(576, 303));

        lblCompileOnSave.setLabelFor(comCompileOnSave);
        org.openide.awt.Mnemonics.setLocalizedText(lblCompileOnSave, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.lblCompileOnSave.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblHint1, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.lblHint1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblHint2, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.lblHint2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbDebug, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.cbDebug.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbDeprecate, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.cbDeprecate.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblJavaPlatform, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.lblJavaPlatform.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnMngPlatform, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.btnMngPlatform.text")); // NOI18N
        btnMngPlatform.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMngPlatformActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblWarnPlatform, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.lblWarnPlatform.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnSetupHome, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.btnSetupHome.text")); // NOI18N
        btnSetupHome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetupHomeActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblHint1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)
                    .add(lblHint2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)
                    .add(cbDebug)
                    .add(cbDeprecate)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblCompileOnSave)
                            .add(lblJavaPlatform))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(comJavaPlatform, 0, 302, Short.MAX_VALUE)
                            .add(comCompileOnSave, 0, 302, Short.MAX_VALUE))
                        .add(16, 16, 16)
                        .add(btnMngPlatform))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(lblWarnPlatform, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnSetupHome)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblJavaPlatform)
                    .add(comJavaPlatform, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(btnMngPlatform))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblCompileOnSave)
                    .add(comCompileOnSave, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblHint1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblHint2)
                .add(18, 18, 18)
                .add(cbDebug)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbDeprecate)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 109, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(btnSetupHome)
                    .add(lblWarnPlatform))
                .addContainerGap())
        );

        btnMngPlatform.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.btnMngPlatform.AccessibleContext.accessibleDescription")); // NOI18N
        btnSetupHome.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.btnSetupHome.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void btnMngPlatformActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMngPlatformActionPerformed
        // TODO add your handling code here:
        PlatformsCustomizer.showCustomizer(getSelPlatform());
}//GEN-LAST:event_btnMngPlatformActionPerformed

    private void btnSetupHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetupHomeActionPerformed
        // TODO add your handling code here:
        OptionsDisplayer.getDefault().open(OptionsDisplayer.ADVANCED + "/Maven"); //NOI18N - the id is the name of instance in layers.
}//GEN-LAST:event_btnSetupHomeActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnMngPlatform;
    private javax.swing.JButton btnSetupHome;
    private javax.swing.JCheckBox cbDebug;
    private javax.swing.JCheckBox cbDeprecate;
    private javax.swing.JComboBox comCompileOnSave;
    private javax.swing.JComboBox comJavaPlatform;
    private javax.swing.JLabel lblCompileOnSave;
    private javax.swing.JLabel lblHint1;
    private javax.swing.JLabel lblHint2;
    private javax.swing.JLabel lblJavaPlatform;
    private javax.swing.JLabel lblWarnPlatform;
    // End of variables declaration//GEN-END:variables

    private static final String CONFIGURATION_EL = "configuration";//NOI18N

    /**
     * update the debug param of project to given value.
     *
     * @param handle handle which models are to be updated
     * @param sourceLevel the sourcelevel to set
     */
    public static void checkCompilerParam(ModelHandle handle, String param, String value) {
        String debug = PluginPropertyUtils.getPluginProperty(handle.getProject(),
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, param,
                "compile"); //NOI18N
        if (debug != null && debug.contains(value)) {
            return;
        }
        POMModel model = handle.getPOMModel();
        Plugin old = null;
        Plugin plugin;
        Build bld = model.getProject().getBuild();
        if (bld != null) {
            old = bld.findPluginById(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER);
        } else {
            bld = model.getFactory().createBuild();
            model.getProject().setBuild(bld);
        }
        if (old != null) {
            plugin = old;
        } else {
            plugin = model.getFactory().createPlugin();
            plugin.setGroupId(Constants.GROUP_APACHE_PLUGINS);
            plugin.setArtifactId(Constants.PLUGIN_COMPILER);
            plugin.setVersion(MavenVersionSettings.getDefault().getVersion(MavenVersionSettings.VERSION_COMPILER));
            bld.addPlugin(plugin);
        }
        Configuration config = plugin.getConfiguration();
        if (config == null) {
            config = model.getFactory().createConfiguration();
            plugin.setConfiguration(config);
        }
        config.setSimpleParameter(param, value);
        handle.markAsModified(handle.getPOMModel());
    }

    public static String getCompilerParam(ModelHandle handle, String param) {
        Build bld = handle.getPOMModel().getProject().getBuild();
        if (bld != null) {
            Plugin plugin = bld.findPluginById(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER);
            if (plugin != null) {
                Configuration config = plugin.getConfiguration();
                if (config != null) {
                    String val = config.getSimpleParameter(param);
                    if (val != null) {
                        return val;
                    }
                }
            }
        }

        String value = PluginPropertyUtils.getPluginProperty(handle.getProject(),
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, param,
                "compile"); //NOI18N
        if (value != null) {
            return value;
        }
        return null;
    }

    public void windowGainedFocus(WindowEvent e) {
        checkExternalMaven();
    }

    public void windowLostFocus(WindowEvent e) {
        // no op
    }

    private static class PlatformsModel extends AbstractListModel implements ComboBoxModel, PropertyChangeListener {

        private JavaPlatform[] data;
        private Object sel;

        public PlatformsModel() {
            JavaPlatformManager jpm = JavaPlatformManager.getDefault();
            data = jpm.getInstalledPlatforms();
            jpm.addPropertyChangeListener(WeakListeners.propertyChange(this, jpm));
            sel = jpm.getDefaultPlatform();
        }

        public int getSize() {
            return data.length;
        }

        public Object getElementAt(int index) {
            return data[index];
        }

        public void setSelectedItem(Object anItem) {
            sel = anItem;
            fireContentsChanged(this, 0, data.length);
        }

        public Object getSelectedItem() {
            return sel;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            JavaPlatformManager jpm = JavaPlatformManager.getDefault();
            data = jpm.getInstalledPlatforms();
            fireContentsChanged(this, 0, data.length);
        }

    }

    private class PlatformsRenderer extends JLabel implements ListCellRenderer, UIResource {

        public PlatformsRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected,
                boolean cellHasFocus) {
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N

            setText(((JavaPlatform)value).getDisplayName());

            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(isExternalMaven() ? list.getSelectionForeground() : java.awt.SystemColor.textInactiveText);
            } else {
                setBackground(list.getBackground());
                setForeground(isExternalMaven() ? list.getForeground() : java.awt.SystemColor.textInactiveText);
            }

            return this;
        }

        // #89393: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    } // end of PlatformsRenderer

}
