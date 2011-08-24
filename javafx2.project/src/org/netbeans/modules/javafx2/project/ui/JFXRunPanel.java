/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

/*
 * JFXRunPanel.java
 *
 * Created on 3.8.2011, 18:58:14
 */
package org.netbeans.modules.javafx2.project.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.UIResource;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javafx2.project.JFXProjectProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.MouseUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Somol
 */
public class JFXRunPanel extends javax.swing.JPanel implements HelpCtx.Provider {

    private Project project;
    private PropertyEvaluator evaluator;
    private JTextField[] data;
    private JLabel[] dataLabels;
    private String[] keys;
    private Map<String/*|null*/,Map<String,String/*|null*/>/*|null*/> configs;
    private JFXProjectProperties jfxProps;
    private File lastHtmlFolder = null;

    /**
     * Creates new form JFXRunPanel
     */
    public JFXRunPanel(JFXProjectProperties props) {
        this.jfxProps = props;
        initComponents();

        project = jfxProps.getProject();
        evaluator = jfxProps.getEvaluator();
        configs = jfxProps.getRunConfigs();
        
        data = new JTextField[] {
            textFieldAppClass,
            textFieldParams,
            textFieldPreloaderClass,
            textFieldVMOptions,
            textFieldWebPage,
            textFieldHeight,
            textFieldWidth,
            textFieldWorkDir,
        };
        dataLabels = new JLabel[] {
            labelAppClass,
            labelParams,
            labelPreloaderClass,
            labelVMOptions,
            labelWebPage,
            labelHeight,
            labelWidth,
            labelWorkDir,
        };
        keys = new String[] {
            JFXProjectProperties.MAIN_CLASS,
            JFXProjectProperties.APPLICATION_ARGS,
            JFXProjectProperties.PRELOADER_CLASS,
            JFXProjectProperties.RUN_JVM_ARGS,
            JFXProjectProperties.RUN_IN_HTMLPAGE,
            JFXProjectProperties.RUN_APP_HEIGHT,
            JFXProjectProperties.RUN_APP_WIDTH,
            JFXProjectProperties.RUN_WORK_DIR,
        };
        assert data.length == keys.length;
        
        configChanged(jfxProps.getActiveConfig());
        
        comboConfig.setRenderer(new ConfigListCellRenderer());
        
        for (int i = 0; i < data.length; i++) {
            final JTextField field = data[i];
            final String prop = keys[i];
            final JLabel label = dataLabels[i];
            field.getDocument().addDocumentListener(new DocumentListener() {
                Font basefont = label.getFont();
                Font emphfont = basefont.deriveFont(Font.ITALIC);
                {
                    updateFont();
                }
                @Override
                public void insertUpdate(DocumentEvent e) {
                    changed();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    changed();
                }
                @Override
                public void changedUpdate(DocumentEvent e) {}
                void changed() {
                    String config = (String) comboConfig.getSelectedItem();
                    if (config.length() == 0) {
                        config = null;
                    }
                    String v = field.getText();
                    if (v != null && config != null && v.equals(configs.get(null).get(prop))) {
                        // default value, do not store as such
                        v = null;
                    }
                    configs.get(config).put(prop, v);
                    updateFont();
                }
                void updateFont() {
                    String v = field.getText();
                    String config = (String) comboConfig.getSelectedItem();
                    if (config.length() == 0) {
                        config = null;
                    }
                    String def = configs.get(null).get(prop);
                    label.setFont(config != null && !Utilities.compareObjects(v != null ? v : "", def != null ? def : "") ? emphfont : basefont);
                }
            });
        }
        
        buttonAppClass.addActionListener( new MainClassListener( project, evaluator ) );
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroupRunAs = new javax.swing.ButtonGroup();
        configPanel = new javax.swing.JPanel();
        labelConfig = new javax.swing.JLabel();
        comboConfig = new javax.swing.JComboBox();
        buttonNew = new javax.swing.JButton();
        buttonDelete = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        mainPanel = new javax.swing.JPanel();
        labelAppClass = new javax.swing.JLabel();
        textFieldAppClass = new javax.swing.JTextField();
        buttonAppClass = new javax.swing.JButton();
        labelParams = new javax.swing.JLabel();
        textFieldParams = new javax.swing.JTextField();
        buttonParams = new javax.swing.JButton();
        labelVMOptions = new javax.swing.JLabel();
        textFieldVMOptions = new javax.swing.JTextField();
        labelVMOptionsRemark = new javax.swing.JLabel();
        checkBoxPreloader = new javax.swing.JCheckBox();
        textFieldPreloader = new javax.swing.JTextField();
        buttonPreloader = new javax.swing.JButton();
        labelPreloaderClass = new javax.swing.JLabel();
        textFieldPreloaderClass = new javax.swing.JTextField();
        buttonPreloaderClass = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        labelRunAs = new javax.swing.JLabel();
        panelRunAsChoices = new javax.swing.JPanel();
        radioButtonSA = new javax.swing.JRadioButton();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(99, 0), new java.awt.Dimension(99, 0), new java.awt.Dimension(99, 32767));
        radioButtonWS = new javax.swing.JRadioButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(109, 0), new java.awt.Dimension(109, 0), new java.awt.Dimension(109, 32767));
        radioButtonBE = new javax.swing.JRadioButton();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(95, 0), new java.awt.Dimension(95, 0), new java.awt.Dimension(95, 32767));
        labelSAProps = new javax.swing.JLabel();
        labelWorkDir = new javax.swing.JLabel();
        textFieldWorkDir = new javax.swing.JTextField();
        buttonWorkDir = new javax.swing.JButton();
        labelWSBAProps = new javax.swing.JLabel();
        labelWidth = new javax.swing.JLabel();
        textFieldWidth = new javax.swing.JTextField();
        labelHeight = new javax.swing.JLabel();
        textFieldHeight = new javax.swing.JTextField();
        labelWebPage = new javax.swing.JLabel();
        textFieldWebPage = new javax.swing.JTextField();
        buttonWebPage = new javax.swing.JButton();
        labelWebPageRemark = new javax.swing.JLabel();
        labelWebBrowser = new javax.swing.JLabel();
        comboBoxWebBrowser = new javax.swing.JComboBox();
        buttonWebBrowser = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));

        setLayout(new java.awt.GridBagLayout());

        configPanel.setLayout(new java.awt.GridBagLayout());

        labelConfig.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelConfig.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        configPanel.add(labelConfig, gridBagConstraints);

        comboConfig.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<default config>" }));
        comboConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboConfigActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        configPanel.add(comboConfig, gridBagConstraints);

        buttonNew.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonNew.text")); // NOI18N
        buttonNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonNewActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        configPanel.add(buttonNew, gridBagConstraints);

        buttonDelete.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonDelete.text")); // NOI18N
        buttonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDeleteActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        configPanel.add(buttonDelete, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        add(configPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        add(jSeparator1, gridBagConstraints);

        mainPanel.setLayout(new java.awt.GridBagLayout());

        labelAppClass.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelAppClass.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        mainPanel.add(labelAppClass, gridBagConstraints);

        textFieldAppClass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textFieldAppClassActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 20, 0);
        mainPanel.add(textFieldAppClass, gridBagConstraints);

        buttonAppClass.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonAppClass.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 20, 0);
        mainPanel.add(buttonAppClass, gridBagConstraints);

        labelParams.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelParams.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        mainPanel.add(labelParams, gridBagConstraints);

        textFieldParams.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        mainPanel.add(textFieldParams, gridBagConstraints);

        buttonParams.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonParams.text")); // NOI18N
        buttonParams.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        mainPanel.add(buttonParams, gridBagConstraints);

        labelVMOptions.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelVMOptions.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        mainPanel.add(labelVMOptions, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        mainPanel.add(textFieldVMOptions, gridBagConstraints);

        labelVMOptionsRemark.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelVMOptionsRemark.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
        mainPanel.add(labelVMOptionsRemark, gridBagConstraints);

        checkBoxPreloader.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.checkBoxPreloader.text")); // NOI18N
        checkBoxPreloader.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        mainPanel.add(checkBoxPreloader, gridBagConstraints);

        textFieldPreloader.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        mainPanel.add(textFieldPreloader, gridBagConstraints);

        buttonPreloader.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonPreloader.text")); // NOI18N
        buttonPreloader.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        mainPanel.add(buttonPreloader, gridBagConstraints);

        labelPreloaderClass.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelPreloaderClass.text")); // NOI18N
        labelPreloaderClass.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        mainPanel.add(labelPreloaderClass, gridBagConstraints);

        textFieldPreloaderClass.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        mainPanel.add(textFieldPreloaderClass, gridBagConstraints);

        buttonPreloaderClass.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonPreloaderClass.text")); // NOI18N
        buttonPreloaderClass.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        mainPanel.add(buttonPreloaderClass, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        mainPanel.add(jSeparator2, gridBagConstraints);

        labelRunAs.setFont(labelRunAs.getFont().deriveFont(labelRunAs.getFont().getStyle() | java.awt.Font.BOLD));
        labelRunAs.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelRunAs.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 15, 0);
        mainPanel.add(labelRunAs, gridBagConstraints);

        panelRunAsChoices.setLayout(new java.awt.GridBagLayout());

        buttonGroupRunAs.add(radioButtonSA);
        radioButtonSA.setFont(radioButtonSA.getFont().deriveFont(radioButtonSA.getFont().getStyle() | java.awt.Font.BOLD));
        radioButtonSA.setSelected(true);
        radioButtonSA.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.radioButtonSA.text")); // NOI18N
        radioButtonSA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonSAActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        panelRunAsChoices.add(radioButtonSA, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        panelRunAsChoices.add(filler2, gridBagConstraints);

        buttonGroupRunAs.add(radioButtonWS);
        radioButtonWS.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.radioButtonWS.text")); // NOI18N
        radioButtonWS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonWSActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        panelRunAsChoices.add(radioButtonWS, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        panelRunAsChoices.add(filler3, gridBagConstraints);

        buttonGroupRunAs.add(radioButtonBE);
        radioButtonBE.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.radioButtonBE.text")); // NOI18N
        radioButtonBE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonBEActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        panelRunAsChoices.add(radioButtonBE, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        panelRunAsChoices.add(filler4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 15, 0);
        mainPanel.add(panelRunAsChoices, gridBagConstraints);

        labelSAProps.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelSAProps.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        mainPanel.add(labelSAProps, gridBagConstraints);

        labelWorkDir.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWorkDir.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 20, 0);
        mainPanel.add(labelWorkDir, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 20, 0);
        mainPanel.add(textFieldWorkDir, gridBagConstraints);

        buttonWorkDir.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonWorkDir.text")); // NOI18N
        buttonWorkDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonWorkDirActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 20, 0);
        mainPanel.add(buttonWorkDir, gridBagConstraints);

        labelWSBAProps.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWSBAProps.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        mainPanel.add(labelWSBAProps, gridBagConstraints);

        labelWidth.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWidth.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 5, 0);
        mainPanel.add(labelWidth, gridBagConstraints);

        textFieldWidth.setMinimumSize(new java.awt.Dimension(70, 20));
        textFieldWidth.setPreferredSize(new java.awt.Dimension(70, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        mainPanel.add(textFieldWidth, gridBagConstraints);

        labelHeight.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelHeight.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 0);
        mainPanel.add(labelHeight, gridBagConstraints);

        textFieldHeight.setMinimumSize(new java.awt.Dimension(70, 20));
        textFieldHeight.setPreferredSize(new java.awt.Dimension(70, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        mainPanel.add(textFieldHeight, gridBagConstraints);

        labelWebPage.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWebPage.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        mainPanel.add(labelWebPage, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        mainPanel.add(textFieldWebPage, gridBagConstraints);

        buttonWebPage.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonWebPage.text")); // NOI18N
        buttonWebPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonWebPageActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        mainPanel.add(buttonWebPage, gridBagConstraints);

        labelWebPageRemark.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWebPageRemark.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
        mainPanel.add(labelWebPageRemark, gridBagConstraints);

        labelWebBrowser.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.labelWebBrowser.text")); // NOI18N
        labelWebBrowser.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 5, 0);
        mainPanel.add(labelWebBrowser, gridBagConstraints);

        comboBoxWebBrowser.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        mainPanel.add(comboBoxWebBrowser, gridBagConstraints);

        buttonWebBrowser.setText(org.openide.util.NbBundle.getMessage(JFXRunPanel.class, "JFXRunPanel.buttonWebBrowser.text")); // NOI18N
        buttonWebBrowser.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        mainPanel.add(buttonWebBrowser, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        add(mainPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 0.1;
        add(filler1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void textFieldAppClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textFieldAppClassActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_textFieldAppClassActionPerformed

private void buttonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDeleteActionPerformed
        String config = (String) comboConfig.getSelectedItem();
        assert config != null;
        configs.put(config, null);
        configChanged(null);
        jfxProps.setActiveConfig(null);
}//GEN-LAST:event_buttonDeleteActionPerformed

private void comboConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboConfigActionPerformed
        String config = (String) comboConfig.getSelectedItem();
        if (config.length() == 0) {
            config = null;
        }
        configChanged(config);
        jfxProps.setActiveConfig(config);
}//GEN-LAST:event_comboConfigActionPerformed

private void buttonNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonNewActionPerformed
        NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine(
                NbBundle.getMessage(JFXRunPanel.class, "JFXConfigurationProvider.input.prompt"),  // NOI18N
                NbBundle.getMessage(JFXRunPanel.class, "JFXConfigurationProvider.input.title"));  // NOI18N
        if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.OK_OPTION) {
            return;
        }
        String name = d.getInputText();
        String config = name.replaceAll("[^a-zA-Z0-9_.-]", "_"); // NOI18N
        if (config.trim().length() == 0) {
            //#143764
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(JFXRunPanel.class, "JFXConfigurationProvider.input.empty", config),  // NOI18N
                    NotifyDescriptor.WARNING_MESSAGE));
            return;
            
        }
        if (configs.get(config) != null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(JFXRunPanel.class, "JFXConfigurationProvider.input.duplicate", config),  // NOI18N
                    NotifyDescriptor.WARNING_MESSAGE));
            return;
        }
        Map<String,String> m = new HashMap<String,String>();
        if (!name.equals(config)) {
            m.put("$label", name); // NOI18N
        }
        configs.put(config, m);
        configChanged(config);
        jfxProps.setActiveConfig(config);
}//GEN-LAST:event_buttonNewActionPerformed

private void buttonWorkDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonWorkDirActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        
        String workDir = textFieldWorkDir.getText();
        if (workDir.equals("")) {
            workDir = FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath();
        }
        chooser.setSelectedFile(new File(workDir));
        chooser.setDialogTitle(NbBundle.getMessage(JFXRunPanel.class, "JFXConfigurationProvider_Run_Working_Directory_Browse_Title")); // NOI18N
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
            File file = FileUtil.normalizeFile(chooser.getSelectedFile());
            textFieldWorkDir.setText(file.getAbsolutePath());
        }
}//GEN-LAST:event_buttonWorkDirActionPerformed

    private void radioButtonWSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonWSActionPerformed
        runTypeChanged(JFXProjectProperties.RunAsType.ASWEBSTART);
    }//GEN-LAST:event_radioButtonWSActionPerformed

    private void radioButtonSAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonSAActionPerformed
        runTypeChanged(JFXProjectProperties.RunAsType.STANDALONE);
    }//GEN-LAST:event_radioButtonSAActionPerformed

    private void radioButtonBEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonBEActionPerformed
        runTypeChanged(JFXProjectProperties.RunAsType.INBROWSER);
    }//GEN-LAST:event_radioButtonBEActionPerformed
    
    void runTypeChanged(JFXProjectProperties.RunAsType runType) {
        String config = (String) comboConfig.getSelectedItem();
        if (config.length() == 0) {
            config = null;
        }
        switch(runType) {
            case STANDALONE : ;
        }
        String v = runType.getString();
        if (v != null && config != null && v.equals(configs.get(null).get(JFXProjectProperties.RUN_AS))) {
            // default value, do not store as such
            v = null;
        }
        configs.get(config).put(JFXProjectProperties.RUN_AS, v);
        
        Font basefont = radioButtonWS.getFont();
        Font plainfont = basefont.deriveFont(Font.PLAIN);
        Font boldfont = basefont.deriveFont(Font.BOLD);
        if(runType == JFXProjectProperties.RunAsType.STANDALONE) {
            radioButtonSA.setFont(boldfont);
            radioButtonSA.setSelected(true);
        } else {
            radioButtonSA.setFont(plainfont);
        }
        if(runType == JFXProjectProperties.RunAsType.ASWEBSTART) {
            radioButtonWS.setFont(boldfont);
            radioButtonWS.setSelected(true);
        } else {
            radioButtonWS.setFont(plainfont);
        }
        if(runType == JFXProjectProperties.RunAsType.INBROWSER) {
            radioButtonBE.setFont(boldfont);
            radioButtonBE.setSelected(true);
        } else {
            radioButtonBE.setFont(plainfont);
        }
    }

private void buttonWebPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonWebPageActionPerformed
    JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory(null);
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setMultiSelectionEnabled(false);
    chooser.setFileFilter(new HtmlFileFilter());
    if (lastHtmlFolder != null) {
        chooser.setSelectedFile(lastHtmlFolder);
    } else { // ???
        // workDir = FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath();
        // chooser.setSelectedFile(new File(workDir));
    }
    chooser.setDialogTitle(NbBundle.getMessage(JFXDeploymentPanel.class, "LBL_Select_HTML_File")); // NOI18N
    if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
        File file = FileUtil.normalizeFile(chooser.getSelectedFile());
        textFieldWebPage.setText(file.getAbsolutePath());
        lastHtmlFolder = file.getParentFile();
    }
}//GEN-LAST:event_buttonWebPageActionPerformed

    private void configChanged(String activeConfig) {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("");
        SortedSet<String> alphaConfigs = new TreeSet<String>(new Comparator<String>() {
            Collator coll = Collator.getInstance();
            @Override
            public int compare(String s1, String s2) {
                return coll.compare(label(s1), label(s2));
            }
            private String label(String c) {
                Map<String,String> m = configs.get(c);
                String label = m.get("$label"); // NOI18N
                return label != null ? label : c;
            }
        });
        for (Map.Entry<String,Map<String,String>> entry : configs.entrySet()) {
            String config = entry.getKey();
            if (config != null && entry.getValue() != null) {
                alphaConfigs.add(config);
            }
        }
        for (String c : alphaConfigs) {
            model.addElement(c);
        }
        comboConfig.setModel(model);
        comboConfig.setSelectedItem(activeConfig != null ? activeConfig : "");  // NOI18N
        Map<String,String> m = configs.get(activeConfig);
        Map<String,String> def = configs.get(null);
        if (m != null) {
            for (int i = 0; i < data.length; i++) {
                String v = m.get(keys[i]);
                if (v == null) {
                    // display default value
                    v = def.get(keys[i]);
                }
                data[i].setText(v);
            }
            String runType = m.get(JFXProjectProperties.RUN_AS);
            if(runType == null) {
                runTypeChanged(JFXProjectProperties.RunAsType.STANDALONE);
            } else {
                if(runType.equals(JFXProjectProperties.RunAsType.ASWEBSTART.getString())) {
                    runTypeChanged(JFXProjectProperties.RunAsType.ASWEBSTART);
                } else {
                    if(runType.equals(JFXProjectProperties.RunAsType.INBROWSER.getString())) {
                        runTypeChanged(JFXProjectProperties.RunAsType.INBROWSER);
                    } else {
                        runTypeChanged(JFXProjectProperties.RunAsType.STANDALONE);
                    }                
                }
            }
        } // else ??
        buttonDelete.setEnabled(activeConfig != null);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAppClass;
    private javax.swing.JButton buttonDelete;
    private javax.swing.ButtonGroup buttonGroupRunAs;
    private javax.swing.JButton buttonNew;
    private javax.swing.JButton buttonParams;
    private javax.swing.JButton buttonPreloader;
    private javax.swing.JButton buttonPreloaderClass;
    private javax.swing.JButton buttonWebBrowser;
    private javax.swing.JButton buttonWebPage;
    private javax.swing.JButton buttonWorkDir;
    private javax.swing.JCheckBox checkBoxPreloader;
    private javax.swing.JComboBox comboBoxWebBrowser;
    private javax.swing.JComboBox comboConfig;
    private javax.swing.JPanel configPanel;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel labelAppClass;
    private javax.swing.JLabel labelConfig;
    private javax.swing.JLabel labelHeight;
    private javax.swing.JLabel labelParams;
    private javax.swing.JLabel labelPreloaderClass;
    private javax.swing.JLabel labelRunAs;
    private javax.swing.JLabel labelSAProps;
    private javax.swing.JLabel labelVMOptions;
    private javax.swing.JLabel labelVMOptionsRemark;
    private javax.swing.JLabel labelWSBAProps;
    private javax.swing.JLabel labelWebBrowser;
    private javax.swing.JLabel labelWebPage;
    private javax.swing.JLabel labelWebPageRemark;
    private javax.swing.JLabel labelWidth;
    private javax.swing.JLabel labelWorkDir;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel panelRunAsChoices;
    private javax.swing.JRadioButton radioButtonBE;
    private javax.swing.JRadioButton radioButtonSA;
    private javax.swing.JRadioButton radioButtonWS;
    private javax.swing.JTextField textFieldAppClass;
    private javax.swing.JTextField textFieldHeight;
    private javax.swing.JTextField textFieldParams;
    private javax.swing.JTextField textFieldPreloader;
    private javax.swing.JTextField textFieldPreloaderClass;
    private javax.swing.JTextField textFieldVMOptions;
    private javax.swing.JTextField textFieldWebPage;
    private javax.swing.JTextField textFieldWidth;
    private javax.swing.JTextField textFieldWorkDir;
    // End of variables declaration//GEN-END:variables

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx( JFXRunPanel.class );
    }

     // Innerclasses -------------------------------------------------------------
     
     private class MainClassListener implements ActionListener /*, DocumentListener */ {
         
         private final JButton okButton;
         private final PropertyEvaluator evaluator;
         private final Project project;
         
         MainClassListener( final @NonNull Project p, final @NonNull PropertyEvaluator pe ) {            
             this.evaluator = pe;
             this.project = p;
             this.okButton  = new JButton (NbBundle.getMessage (JFXRunPanel.class, "LBL_ChooseMainClass_OK")); // NOI18N
             this.okButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (JFXRunPanel.class, "AD_ChooseMainClass_OK"));  // NOI18N
         }
         
         // Implementation of ActionListener ------------------------------------
         
         /** Handles button events
          */        
         @Override
         public void actionPerformed( ActionEvent e ) {
             
             // only chooseMainClassButton can be performed
             
             //final MainClassChooser panel = new MainClassChooser (sourceRoots.getRoots(), null, mainClassTextField.getText());
             final JFXApplicationClassChooser panel = new JFXApplicationClassChooser(project, evaluator);
             Object[] options = new Object[] {
                 okButton,
                 DialogDescriptor.CANCEL_OPTION
             };
             panel.addChangeListener (new ChangeListener () {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (e.getSource () instanceof MouseEvent && MouseUtils.isDoubleClick (((MouseEvent)e.getSource ()))) {
                        // click button and finish the dialog with selected class
                        okButton.doClick ();
                    } else {
                        okButton.setEnabled (panel.getSelectedClass () != null);
                    }
                }
             });
             okButton.setEnabled (false);
             DialogDescriptor desc = new DialogDescriptor (
                 panel,
                 NbBundle.getMessage (JFXRunPanel.class, "LBL_ChooseMainClass_Title" ),  // NOI18N
                 true, 
                 options, 
                 options[0], 
                 DialogDescriptor.BOTTOM_ALIGN, 
                 null, 
                 null);
             //desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
             Dialog dlg = DialogDisplayer.getDefault ().createDialog (desc);
             dlg.setVisible (true);
             if (desc.getValue() == options[0]) {
                textFieldAppClass.setText (panel.getSelectedClass ());
             } 
             dlg.dispose();
         }
    }
    private final class ConfigListCellRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        public ConfigListCellRenderer () {
            setOpaque(true);
        }
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // #93658: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            String config = (String) value;
            String label;
            if (config == null) {
                // uninitialized?
                label = null;
            } else if (config.length() > 0) {
                Map<String,String> m = configs.get(config);
                label = m != null ? m.get("$label") : /* temporary? */ null;
                if (label == null) {
                    label = config;
                }
            } else {
                label = NbBundle.getBundle("org.netbeans.modules.javafx2.project.ui.Bundle").getString("JFXConfigurationProvider.default.label"); // NOI18N
            }
            setText(label);
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
        
        // #93658: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
        
    }

    private static class HtmlFileFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String name = f.getName();
            int index = name.lastIndexOf('.');
            if (index > 0 && index < name.length() - 1) {
                String ext = name.substring(index+1).toLowerCase();
                if ("htm".equals(ext) || "html".equals(ext)) { // NOI18N
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(JFXRunPanel.class, "MSG_HtmlFileFilter_Description");  // NOI18N
        }

    }

}
