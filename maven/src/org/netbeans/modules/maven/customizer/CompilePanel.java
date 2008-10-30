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

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import org.apache.maven.profiles.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.MavenProjectPropsImpl;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.modules.maven.api.customizer.support.CheckBoxUpdater;
import org.netbeans.modules.maven.api.customizer.support.ComboBoxUpdater;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.maven.options.MavenVersionSettings;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class CompilePanel extends javax.swing.JPanel {
    private ModelHandle handle;
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
    private static final int COS_ALL = 0;
    private static final int COS_APP = 1;
    private static final int COS_TESTS = 2;
    private static final int COS_NONE = 3;

    private ComboBoxUpdater<String> listener;
    private Project project;
    private CheckBoxUpdater debugUpdater;
    private CheckBoxUpdater deprecateUpdater;

    /** Creates new form CompilePanel */
    public CompilePanel(ModelHandle handle, Project prj) {
        initComponents();
        this.handle = handle;
        project = prj;
        ComboBoxModel mdl = new DefaultComboBoxModel(LABELS);
        comCompileOnSave.setModel(mdl);
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
                Profile prof = handle.getNetbeansPrivateProfile(false);
                String val = null;
                if (prof != null && prof.getProperties().getProperty(Constants.HINT_COMPILE_ON_SAVE) != null) {
                    val = prof.getProperties().getProperty(Constants.HINT_COMPILE_ON_SAVE);
                }
                if (val == null) {
                    Properties props = handle.getPOMModel().getProject().getProperties();
                    if (props != null) {
                        val = props.getProperty(Constants.HINT_COMPILE_ON_SAVE);
                    }
                }
                if (val == null) {
                    MavenProjectPropsImpl props = project.getLookup().lookup(MavenProjectPropsImpl.class);
                    val = props.get(Constants.HINT_COMPILE_ON_SAVE, true, false);
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
                MavenProjectPropsImpl props = project.getLookup().lookup(MavenProjectPropsImpl.class);
                boolean hasConfig = props.get(Constants.HINT_COMPILE_ON_SAVE, true, false) != null;
                //TODO also try to take the value in pom vs inherited pom value into account.

                Profile prof = handle.getNetbeansPrivateProfile(false);
                if (prof != null && prof.getProperties().getProperty(Constants.HINT_COMPILE_ON_SAVE) != null) {
                    prof.getProperties().setProperty(Constants.HINT_COMPILE_ON_SAVE, value == null ? null : value);
                    if (hasConfig) {
                        // in this case clean up the auxiliary config
                        props.put(Constants.HINT_COMPILE_ON_SAVE, null, true);
                    }
                    handle.markAsModified(handle.getProfileModel());
                    return;
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
                        props.put(Constants.HINT_COMPILE_ON_SAVE, null, true);
                    }
                    return;
                }
                props.put(Constants.HINT_COMPILE_ON_SAVE, value == null ? null : value, true);
            }
        };
        debugUpdater = new CheckBoxUpdater(cbDebug) {
            public Boolean getValue() {
                String val = getCompilerParam(handle, "debug");
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
                    text = "true";
                } else {
                    text = value.toString();
                }
                checkCompilerParam(handle, "debug", text);
            }

            public boolean getDefaultValue() {
                return true;
            }
        };

        deprecateUpdater = new CheckBoxUpdater(cbDeprecate) {
            public Boolean getValue() {
                String val = getCompilerParam(handle, "showDeprecation");
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
                    text = "false";
                } else {
                    text = value.toString();
                }
                checkCompilerParam(handle, "showDeprecation", text);
            }

            public boolean getDefaultValue() {
                return false;
            }
        };

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

        lblCompileOnSave.setLabelFor(comCompileOnSave);
        org.openide.awt.Mnemonics.setLocalizedText(lblCompileOnSave, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.lblCompileOnSave.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblHint1, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.lblHint1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblHint2, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.lblHint2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbDebug, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.cbDebug.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbDeprecate, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.cbDeprecate.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(lblCompileOnSave)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(comCompileOnSave, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 266, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(lblHint1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 545, Short.MAX_VALUE)
                    .add(lblHint2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(cbDebug)
                    .add(cbDeprecate))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
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
                .addContainerGap(170, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbDebug;
    private javax.swing.JCheckBox cbDeprecate;
    private javax.swing.JComboBox comCompileOnSave;
    private javax.swing.JLabel lblCompileOnSave;
    private javax.swing.JLabel lblHint1;
    private javax.swing.JLabel lblHint2;
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
}
