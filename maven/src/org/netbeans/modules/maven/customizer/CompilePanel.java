/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.customizer;

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.UIResource;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.api.customizer.support.CheckBoxUpdater;
import org.netbeans.modules.maven.api.customizer.support.ComboBoxUpdater;
import org.netbeans.modules.maven.classpath.BootClassPathImpl;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.maven.options.DontShowAgainSettings;
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
public class CompilePanel extends javax.swing.JPanel {
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

    private final ModelHandle2 handle;
    private final Project project;
    private static boolean warningShown = false;

    private Color origComPlatformFore;

    /** Creates new form CompilePanel */
    public CompilePanel(ModelHandle2 handle, Project prj) {
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
        return LABELS[COS_NONE];
    }

    private String labelToValue(String label) {
        for (int i = 0; i < LABELS.length; i++) {
            if (LABELS[i].equalsIgnoreCase(label)) {
                return VALUES[i];
            }
        }
        return VALUES[COS_NONE];
    }

    private void initValues() {
        new ComboBoxUpdater<String>(comCompileOnSave, lblCompileOnSave) {
            private String modifiedValue;

            private ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {

                @Override
                public void performOperation(POMModel model) {
                    Properties modprops = model.getProject().getProperties();
                    if (modprops == null) {
                        modprops = model.getFactory().createProperties();
                        model.getProject().setProperties(modprops);
                    }
                    modprops.setProperty(Constants.HINT_COMPILE_ON_SAVE, modifiedValue); //NOI18N
                }
            };

            @Override
            public String getDefaultValue() {
                return LABELS[COS_NONE];
            }

            @Override
            public String getValue() {
                String val = modifiedValue;
                if (val == null) {
                    Properties props = handle.getPOMModel().getProject().getProperties();
                    if (props != null) {
                        val = props.getProperty(Constants.HINT_COMPILE_ON_SAVE);
                    }
                }
                if (val == null) {
                    val = handle.getRawAuxiliaryProperty(Constants.HINT_COMPILE_ON_SAVE, true);
                }
                if (val != null) {
                    return valueToLabel(val);
                }
                return LABELS[COS_NONE];
            }

            @Override
            public void setValue(String label) {
                handle.removePOMModification(operation);
                modifiedValue = null;
                String value = labelToValue(label);
                if (value != null && value.equals(VALUES[COS_NONE])) {
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

                if (handle.getProject().getProperties().containsKey(Constants.HINT_COMPILE_ON_SAVE)) {
                    modifiedValue = value;
                    handle.addPOMModification(operation);
                    if (hasConfig) {
                        // in this case clean up the auxiliary config
                        handle.setRawAuxiliaryProperty(Constants.HINT_COMPILE_ON_SAVE, null, true);
                    }
                } else {
                    handle.setRawAuxiliaryProperty(Constants.HINT_COMPILE_ON_SAVE, value, true);
                }
            }
        };
        new CheckBoxUpdater(cbDebug) {
            @Override
            public Boolean getValue() {
                String val = getCompilerParam(handle,PARAM_DEBUG);
                if (val != null) {
                    return Boolean.valueOf(val);
                }
                return null;
            }

            @Override
            public void setValue(Boolean value) {
                String text;
                if (value == null) {
                    //TODO we should attempt to remove the configuration
                    // from pom if this parameter is the only one defined.
                    text = "" + getDefaultValue();
                } else {
                    text = value.toString();
                }
                modifyCompilerParamOperation(handle, PARAM_DEBUG, text);
            }

            @Override
            public boolean getDefaultValue() {
                return true;
            }
        };

        new CheckBoxUpdater(cbDeprecate) {
            @Override
            public Boolean getValue() {
                String val = getCompilerParam(handle,PARAM_DEPRECATION);
                if (val != null) {
                    return Boolean.valueOf(val);
                }
                return null;
            }

            @Override
            public void setValue(Boolean value) {
                String text;
                if (value == null) {
                    //TODO we should attempt to remove the configuration
                    // from pom if this parameter is the only one defined.
                    text = "" + getDefaultValue();
                } else {
                    text = value.toString();
                }
                modifyCompilerParamOperation(handle, PARAM_DEPRECATION, text);
            }

            @Override
            public boolean getDefaultValue() {
                return false;
            }
        };

        // java platform updater
        new ComboBoxUpdater<JavaPlatform>(comJavaPlatform, lblJavaPlatform) {
            private String modifiedValue;
            private String DEFAULT_PLATFORM_VALUE = "@@DEFAU:T@@";
            private ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {

            @Override
                public void performOperation(POMModel model) {
                    Properties modprops = model.getProject().getProperties();
                    if (modprops == null) {
                        modprops = model.getFactory().createProperties();
                        model.getProject().setProperties(modprops);
                    }
                    modprops.setProperty(Constants.HINT_JDK_PLATFORM, modifiedValue); //NOI18N
                }
            };
            
            @Override
            public JavaPlatform getValue() {
                String val = modifiedValue;
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
                    if (val.equals(DEFAULT_PLATFORM_VALUE)) {
                        return JavaPlatformManager.getDefault().getDefaultPlatform();
                    }
                    return BootClassPathImpl.getActivePlatform(val);
                } else {
                    return getSelPlatform();
                }
            }

            @Override
            public JavaPlatform getDefaultValue() {
                return JavaPlatformManager.getDefault().getDefaultPlatform();
            }

            @Override
            public void setValue(JavaPlatform value) {
                handle.removePOMModification(operation);
                modifiedValue = null;
                JavaPlatform platf = value == null ? JavaPlatformManager.getDefault().getDefaultPlatform() : value;
                String platformId = platf.getProperties().get("platform.ant.name"); //NOI18N
                if (JavaPlatformManager.getDefault().getDefaultPlatform().equals(platf)) {
                    platformId = null;
                }

                boolean hasConfig = handle.getRawAuxiliaryProperty(Constants.HINT_JDK_PLATFORM, true) != null;
                //TODO also try to take the value in pom vs inherited pom value into account.
                modifiedValue = platformId == null ? DEFAULT_PLATFORM_VALUE : platformId;
                if (handle.getProject().getProperties().containsKey(Constants.HINT_JDK_PLATFORM)) {
                    
                    handle.addPOMModification(operation);
                    if (hasConfig) {
                        // in this case clean up the auxiliary config
                        handle.setRawAuxiliaryProperty(Constants.HINT_JDK_PLATFORM, null, true);
                    }
                } else {
                    handle.setRawAuxiliaryProperty(Constants.HINT_JDK_PLATFORM, platformId, true);
                }
            }
        };
    }

    private JavaPlatform getSelPlatform () {
        String platformId = project.getLookup().lookup(AuxiliaryProperties.class).
                get(Constants.HINT_JDK_PLATFORM, true);
        return BootClassPathImpl.getActivePlatform(platformId);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblJavaPlatform = new javax.swing.JLabel();
        comJavaPlatform = new javax.swing.JComboBox();
        btnMngPlatform = new javax.swing.JButton();
        lblCompileOnSave = new javax.swing.JLabel();
        comCompileOnSave = new javax.swing.JComboBox();
        lblHint1 = new javax.swing.JLabel();
        lblHint2 = new javax.swing.JLabel();
        cbDebug = new javax.swing.JCheckBox();
        cbDeprecate = new javax.swing.JCheckBox();

        setPreferredSize(new java.awt.Dimension(576, 303));

        lblJavaPlatform.setLabelFor(comJavaPlatform);
        org.openide.awt.Mnemonics.setLocalizedText(lblJavaPlatform, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.lblJavaPlatform.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnMngPlatform, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.btnMngPlatform.text")); // NOI18N
        btnMngPlatform.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMngPlatformActionPerformed(evt);
            }
        });

        lblCompileOnSave.setLabelFor(comCompileOnSave);
        org.openide.awt.Mnemonics.setLocalizedText(lblCompileOnSave, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.lblCompileOnSave.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblHint1, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.lblHint1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblHint2, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.lblHint2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbDebug, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.cbDebug.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbDeprecate, org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.cbDeprecate.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCompileOnSave)
                            .addComponent(lblJavaPlatform))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(comJavaPlatform, 0, 261, Short.MAX_VALUE)
                            .addComponent(comCompileOnSave, 0, 261, Short.MAX_VALUE))
                        .addGap(16, 16, 16)
                        .addComponent(btnMngPlatform))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbDebug)
                            .addComponent(cbDeprecate)
                            .addComponent(lblHint1)
                            .addComponent(lblHint2))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblJavaPlatform)
                    .addComponent(comJavaPlatform, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMngPlatform))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCompileOnSave)
                    .addComponent(comCompileOnSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblHint1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblHint2)
                .addGap(18, 18, 18)
                .addComponent(cbDebug)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbDeprecate)
                .addContainerGap(149, Short.MAX_VALUE))
        );

        btnMngPlatform.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CompilePanel.class, "CompilePanel.btnMngPlatform.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void btnMngPlatformActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMngPlatformActionPerformed
        // TODO add your handling code here:
        PlatformsCustomizer.showCustomizer(getSelPlatform());
}//GEN-LAST:event_btnMngPlatformActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnMngPlatform;
    private javax.swing.JCheckBox cbDebug;
    private javax.swing.JCheckBox cbDeprecate;
    private javax.swing.JComboBox comCompileOnSave;
    private javax.swing.JComboBox comJavaPlatform;
    private javax.swing.JLabel lblCompileOnSave;
    private javax.swing.JLabel lblHint1;
    private javax.swing.JLabel lblHint2;
    private javax.swing.JLabel lblJavaPlatform;
    // End of variables declaration//GEN-END:variables

    private static final String CONFIGURATION_EL = "configuration";//NOI18N

    private final Map<String, CompilerParamOperation> operations = new HashMap<String, CompilerParamOperation>();

    /**
     * update the debug param of project to given value.
     *
     * @param handle handle which models are to be updated
     * @param sourceLevel the sourcelevel to set
     */
    private void modifyCompilerParamOperation(ModelHandle2 handle, String param, String value) {
        String debug = PluginPropertyUtils.getPluginProperty(handle.getProject(),
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, param,
                "compile"); //NOI18N
        if (debug != null && debug.contains(value)) {
            ModelOperation<POMModel> removed = operations.remove(param);
            if (removed != null) {
                handle.removePOMModification(removed);
            }
            return;
        }
        ModelOperation<POMModel> removed = operations.remove(param);
        if (removed != null) {
            handle.removePOMModification(removed);
        }
        CompilerParamOperation added = new CompilerParamOperation(param, value);
        operations.put(param, added);
        handle.addPOMModification(added);
    }
    
    private class CompilerParamOperation implements ModelOperation<POMModel> {
        private final String value;
        private final String param;

        public CompilerParamOperation(String param, String value) {
            this.param = param;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public void performOperation(POMModel model) {
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
    }

                    }

    String getCompilerParam(ModelHandle2 handle, String param) {
        CompilerParamOperation oper = operations.get(param);
        if (oper != null) {
            return oper.getValue();
                }

        String value = PluginPropertyUtils.getPluginProperty(handle.getProject(),
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, param,
                "compile"); //NOI18N
        if (value != null) {
            return value;
        }
        return null;
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

        @Override
        public int getSize() {
            return data.length;
        }

        @Override
        public Object getElementAt(int index) {
            return data[index];
        }

        @Override
        public void setSelectedItem(Object anItem) {
            sel = anItem;
            fireContentsChanged(this, 0, data.length);
        }

        @Override
        public Object getSelectedItem() {
            return sel;
        }

        @Override
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

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected,
                boolean cellHasFocus) {
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            JavaPlatform jp = (JavaPlatform)value;
            //#171354 weird null value coming on mac..
            if (jp != null) {
                setText(jp.getDisplayName());
            } else {
                setText("");
            }

            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
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
