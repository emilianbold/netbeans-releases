/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform.ui;

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.netbeans.modules.ant.freeform.FreeformProjectGenerator;
import org.netbeans.modules.ant.freeform.Util;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;

/**
 * @author  David Konecny
 */
public class TargetMappingPanel extends javax.swing.JPanel implements ProjectCustomizer.Panel, MouseListener, ActionListener, HelpCtx.Provider {

    public static String BUILD_ACTION = "build"; // NOI18N
    public static String CLEAN_ACTION = "clean"; // NOI18N
    public static String REBUILD_ACTION = "rebuild"; // NOI18N
    public static String JAVADOC_ACTION = "javadoc"; // NOI18N
    public static String RUN_ACTION = "run"; // NOI18N
    public static String TEST_ACTION = "test"; // NOI18N
    public static String DEBUG_ACTION = "debug"; // NOI18N
    public static String REDEPLOY_ACTION = "redeploy"; // NOI18N

    private boolean initialized;
    private List/*<String>*/ targetNames;
    private List/*<TargetMapping>*/ targetMappings;
    private List/*<FreeformProjectGenerator.CustomTarget>*/ custTargets;
    private CustomTargetsModel customTargetsModel;
    private String antScript;

    private String projectType;
    
    /** Any change in standard tasks which needs to be persisted? */
    private boolean dirtyRegular;
    
    /** Any change in custom tasks which needs to be persisted? */
    private boolean dirtyCustom;
    
    public TargetMappingPanel(String type) {
        this(type, false);
    }
    
    private TargetMappingPanel(String type, boolean advancedPart) {
        initComponents();
        targetMappings = new ArrayList();
        projectType = type;

        custTargets = new ArrayList();
        customTargetsModel = new CustomTargetsModel();
        customTargets.setModel(customTargetsModel);
        
        link.setCursor(Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        
        jLabel3.setVisible(projectType.equals("webapps")); // NOI18N
        redeployCombo.setVisible(projectType.equals("webapps")); // NOI18N
        showAdvancedPart(advancedPart);
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx( TargetMappingPanel.class );
    }
    
    private void showAdvancedPart(boolean show) {
        jLabel8.setVisible(show);
        link.setVisible(show);
        jLabel10.setVisible(show);
        jScrollPane1.setVisible(show);
        customTargets.setVisible(show);
        add.setVisible(show);
        remove.setVisible(show);
        remainder.setVisible(!show);
        jLabel9.setVisible(show);
        debugCombo.setVisible(show);
        jLabel1.setVisible(!show);
    }
    
    private void initAntTargetEditor(List targets) {
        DefaultCellEditor antTargetsEditor;
        JComboBox combo = new JComboBox();
        combo.setEditable(true);
        Iterator it = targets.iterator();
        while (it.hasNext()) {
            String target = (String)it.next();
            combo.addItem(target);
        }
        customTargets.setDefaultEditor(JComboBox.class, new DefaultCellEditor(combo));
    }
    
    private FreeformProjectGenerator.CustomTarget getItem(int index) {
        return (FreeformProjectGenerator.CustomTarget)custTargets.get(index);
    }

    public void setTargetNames(List list, boolean selectDefaults) {
        targetNames = list;
        targetNames.add(0, ""); //NOI18N
        updateCombos(selectDefaults);
    }

    public void setScript(String script) {
        this.antScript = script;
    }

    private void updateCombos(boolean selectDefaults) {
        // In case you go back and choose a different script:
        buildCombo.removeAllItems();
        cleanCombo.removeAllItems();
        javadocCombo.removeAllItems();
        runCombo.removeAllItems();
        debugCombo.removeAllItems();
        testCombo.removeAllItems();
        if (projectType.equals("webapps")) { // NOI18N
            redeployCombo.removeAllItems();
        }
        Iterator it = targetNames.iterator();
        while (it.hasNext()) {
            String name = (String)it.next();
            buildCombo.addItem(name);
            cleanCombo.addItem(name);
            javadocCombo.addItem(name);
            runCombo.addItem(name);
            debugCombo.addItem(name);
            testCombo.addItem(name);
            if (projectType.equals("webapps")) { // NOI18N
                redeployCombo.addItem(name);
            }
        }
        if (selectDefaults) {
            selectItem(buildCombo, "build", false); //NOI18N
            selectItem(cleanCombo, "clean", false); //NOI18N
            selectItem(javadocCombo, "javadoc", false); //NOI18N
            selectItem(runCombo, "run", false); //NOI18N
            selectItem(debugCombo, "debug", false); //NOI18N
            selectItem(testCombo, "test", false);
            if (projectType.equals("webapps")) { // NOI18N
                selectItem(redeployCombo, "run-deploy", false); //NOI18N
            }
        }
    }

    private void selectItem(JComboBox combo, String item, boolean add) {
        ComboBoxModel model = combo.getModel();
        for (int i=0; i<model.getSize(); i++) {
            if (model.getElementAt(i).equals(item)) {
                model.setSelectedItem(item);
                return;
            }
        }
        if (add) {
            combo.addItem(item);
            model.setSelectedItem(item);
        }
    }

    private void initMappings(List/*<FreeformProjectGenerator.TargetMapping>*/ list, String antScript) {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            FreeformProjectGenerator.TargetMapping tm = (FreeformProjectGenerator.TargetMapping)it.next();
            if (tm.name.equals(BUILD_ACTION)) {
                selectItem(buildCombo, getListAsString(tm.targets), true);
                checkAntScript(buildCombo, antScript, tm.script);
            }
            if (tm.name.equals(CLEAN_ACTION)) {
                selectItem(cleanCombo, getListAsString(tm.targets), true);
                checkAntScript(cleanCombo, antScript, tm.script);
            }
            if (tm.name.equals(JAVADOC_ACTION)) {
                selectItem(javadocCombo, getListAsString(tm.targets), true);
                checkAntScript(javadocCombo, antScript, tm.script);
            }
            if (tm.name.equals(RUN_ACTION)) {
                selectItem(runCombo, getListAsString(tm.targets), true);
                checkAntScript(runCombo, antScript, tm.script);
            }
            if (tm.name.equals(TEST_ACTION)) {
                selectItem(testCombo, getListAsString(tm.targets), true);
                checkAntScript(testCombo, antScript, tm.script);
            }
            if (tm.name.equals(DEBUG_ACTION)) {
                selectItem(debugCombo, getListAsString(tm.targets), true);
                checkAntScript(debugCombo, antScript, tm.script);
            }
            if (tm.name.equals(REDEPLOY_ACTION)) {
                selectItem(redeployCombo, getListAsString(tm.targets), true);
                checkAntScript(redeployCombo, antScript, tm.script);
            }
        }
        targetMappings = list;
    }
    
    private void checkAntScript(JComboBox combo, String antScript, String targetScript) {
        if ((antScript == null && targetScript == null) ||
            (antScript != null && antScript.equals(targetScript))) {
            combo.setEnabled(true);
        } else {
            combo.setEnabled(false);
        }
    }

    private static String getListAsString(List list) {
        assert list != null;
        StringBuffer sb = new StringBuffer();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            sb.append((String)it.next());
            if (it.hasNext()) {
                sb.append(", "); //NOI18N
            }
        }
        return sb.toString();
    }

    static List getStringAsList(String str) {
        ArrayList l = new ArrayList(2);
        StringTokenizer tok = new StringTokenizer(str, ",");
        while (tok.hasMoreTokens()) {
            String target = tok.nextToken().trim();
            if (target.length() == 0) {
                continue;
            }
            l.add(target);
        }
        return l;
    }

    private void storeTarget(String key, JComboBox combo) {
        if (combo.getModel().getSelectedItem() == null || ((String)combo.getModel().getSelectedItem()).length() == 0) {
            removeTargetMapping(key);
            return;
        }
        FreeformProjectGenerator.TargetMapping tm = getTargetMapping(key);
        String value = (String)combo.getModel().getSelectedItem();
        List l = getStringAsList(value);
        if (!l.equals(tm.targets)) {
            dirtyRegular = true;
        }
        tm.targets = l;
        return;
    }

    private FreeformProjectGenerator.TargetMapping getTargetMapping(String key) {
        Iterator it = targetMappings.iterator();
        while (it.hasNext()) {
            FreeformProjectGenerator.TargetMapping tm = (FreeformProjectGenerator.TargetMapping)it.next();
            if (tm.name.equals(key)) {
                return tm;
            }
        }
        FreeformProjectGenerator.TargetMapping tm = new FreeformProjectGenerator.TargetMapping();
        tm.name = key;
        tm.script = antScript;
        targetMappings.add(tm);
        dirtyRegular = true;
        return tm;
    }

    private void removeTargetMapping(String key) {
        Iterator it = targetMappings.iterator();
        while (it.hasNext()) {
            FreeformProjectGenerator.TargetMapping tm = (FreeformProjectGenerator.TargetMapping)it.next();
            if (tm.name.equals(key)) {
                it.remove();
                dirtyRegular = true;
                return;
            }
        }
    }

    public List/*<FreeformProjectGenerator.TargetMapping>*/ getMapping() {
        storeTarget(BUILD_ACTION, buildCombo);
        // update rebuilt:
        if (cleanCombo.getModel().getSelectedItem() != null &&
                ((String)cleanCombo.getModel().getSelectedItem()).length() > 0 &&
                buildCombo.getModel().getSelectedItem() != null &&
                ((String)buildCombo.getModel().getSelectedItem()).length() > 0) {
            FreeformProjectGenerator.TargetMapping tm = getTargetMapping(REBUILD_ACTION);
            String val = (String)cleanCombo.getModel().getSelectedItem()+","+(String)buildCombo.getModel().getSelectedItem();
            tm.targets = getStringAsList(val);
        } else {
            removeTargetMapping(REBUILD_ACTION);
        }
        storeTarget(CLEAN_ACTION, cleanCombo);
        storeTarget(JAVADOC_ACTION, javadocCombo);
        if (projectType.equals("webapps")) { // NOI18N
            storeTarget(REDEPLOY_ACTION, redeployCombo);
        }
        // XXX should have separator here, when that is permitted
        storeTarget(RUN_ACTION, runCombo);
        storeTarget(DEBUG_ACTION, debugCombo);
        storeTarget(TEST_ACTION, testCombo);
        return targetMappings;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        buildCombo = new javax.swing.JComboBox();
        cleanCombo = new javax.swing.JComboBox();
        javadocCombo = new javax.swing.JComboBox();
        runCombo = new javax.swing.JComboBox();
        testCombo = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        redeployCombo = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        debugCombo = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        link = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        customTargets = new javax.swing.JTable();
        add = new javax.swing.JButton();
        remove = new javax.swing.JButton();
        remainder = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(300, 280));
        jLabel1.setLabelFor(this);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "LBL_TargetMappingPanel_jLabel1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "ACSD_TargetMappingPanel_jLabel1"));

        jLabel2.setLabelFor(buildCombo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "LBL_TargetMappingPanel_jLabel2"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(jLabel2, gridBagConstraints);
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "ACSD_TargetMappingPanel_jLabel2"));

        jLabel4.setLabelFor(cleanCombo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "LBL_TargetMappingPanel_jLabel4"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(jLabel4, gridBagConstraints);
        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "ACSD_TargetMappingPanel_jLabel4"));

        jLabel5.setLabelFor(runCombo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "LBL_TargetMappingPanel_jLabel5"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(jLabel5, gridBagConstraints);
        jLabel5.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "ACSD_TargetMappingPanel_jLabel5"));

        jLabel6.setLabelFor(javadocCombo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "LBL_TargetMappingPanel_jLabel6"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(jLabel6, gridBagConstraints);
        jLabel6.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "ACSD_TargetMappingPanel_jLabel6"));

        jLabel7.setLabelFor(testCombo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "LBL_TargetMappingPanel_jLabel7"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(jLabel7, gridBagConstraints);
        jLabel7.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "ACSD_TargetMappingPanel_jLabel7"));

        buildCombo.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(buildCombo, gridBagConstraints);

        cleanCombo.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(cleanCombo, gridBagConstraints);

        javadocCombo.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(javadocCombo, gridBagConstraints);

        runCombo.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(runCombo, gridBagConstraints);

        testCombo.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(testCombo, gridBagConstraints);

        jLabel3.setLabelFor(redeployCombo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "LBL_TargetMappingPanel_jLabel3"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "ACSD_TargetMappingPanel_jLabel3"));

        redeployCombo.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(redeployCombo, gridBagConstraints);

        jLabel9.setLabelFor(debugCombo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "LBL_TargetMappingPanel_jLabel9"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(jLabel9, gridBagConstraints);
        jLabel9.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "ACSD_TargetMappingPanel_jLabel9"));

        debugCombo.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(debugCombo, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        link.setForeground(new java.awt.Color(102, 102, 153));
        org.openide.awt.Mnemonics.setLocalizedText(link, org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "URL_TargetMappingPanel_link"));
        link.addMouseListener(this);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(link, gridBagConstraints);

        jLabel8.setLabelFor(link);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "LBL_TargetMappingPanel_jLabel8"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(jLabel8, gridBagConstraints);
        jLabel8.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "ACSD_TargetMappingPanel_jLabel8"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 0);
        add(jPanel1, gridBagConstraints);

        jLabel10.setLabelFor(customTargets);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "LBL_TargetMappingPanel_jLabel10"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(jLabel10, gridBagConstraints);
        jLabel10.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "ACSD_TargetMappingPanel_jLabel10"));

        jScrollPane1.setViewportView(customTargets);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(add, org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "BTN_TargetMappingPanel_add"));
        add.addActionListener(this);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(add, gridBagConstraints);
        add.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "ACSD_TargetMappingPanel_add"));

        org.openide.awt.Mnemonics.setLocalizedText(remove, org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "BTN_TargetMappingPanel_remove"));
        remove.addActionListener(this);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(remove, gridBagConstraints);
        remove.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "ACSD_TargetMappingPanel_remove"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(remainder, gridBagConstraints);

    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == add) {
            TargetMappingPanel.this.addActionPerformed(evt);
        }
        else if (evt.getSource() == remove) {
            TargetMappingPanel.this.removeActionPerformed(evt);
        }
    }

    public void mouseClicked(java.awt.event.MouseEvent evt) {
    }

    public void mouseEntered(java.awt.event.MouseEvent evt) {
    }

    public void mouseExited(java.awt.event.MouseEvent evt) {
    }

    public void mousePressed(java.awt.event.MouseEvent evt) {
    }

    public void mouseReleased(java.awt.event.MouseEvent evt) {
        if (evt.getSource() == link) {
            TargetMappingPanel.this.linkMouseReleased(evt);
        }
    }//GEN-END:initComponents

    private void linkMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_linkMouseReleased
        try {
            // XXX: define some real URL here!!
            HtmlBrowser.URLDisplayer.getDefault().showURL(new java.net.URL("http://www.netbeans.org"));
        } catch (java.net.MalformedURLException exc) {
            ErrorManager.getDefault().notify(exc);
        }
    }//GEN-LAST:event_linkMouseReleased

    private void removeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeActionPerformed
        int index = customTargets.getSelectedRow();
        if (index == -1) {
            return;
        }
        custTargets.remove(index);
        customTargetsModel.fireTableDataChanged();        
        dirtyCustom = true;
        updateButtons();
    }//GEN-LAST:event_removeActionPerformed

    private void addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addActionPerformed
        FreeformProjectGenerator.CustomTarget ct = new FreeformProjectGenerator.CustomTarget();
        ct.targets = new ArrayList();
        ct.script = antScript;
        custTargets.add(ct);
        customTargetsModel.fireTableDataChanged();
        dirtyCustom = true;
        updateButtons();
    }//GEN-LAST:event_addActionPerformed

    private void updateButtons() {
        remove.setEnabled(custTargets.size() > 0);
    }
    
    /* ProjectCustomizer.Panel init */
    public void show(FreeformProject project, AntProjectHelper helper, ProjectCustomizer.ProjectModel model) {
        if (!initialized) {
            FileObject as = FreeformProjectGenerator.getAntScript(helper, project.evaluator());
            List l = Util.getAntScriptTargetNames(as);
            if (l != null) {
                setTargetNames(l, false);
                initAntTargetEditor(l);
            }
            antScript = project.evaluator().getProperty(FreeformProjectGenerator.PROP_ANT_SCRIPT);
            antScript = (antScript == null ? null : "${" + FreeformProjectGenerator.PROP_ANT_SCRIPT + "}"); // NOI18N
            initMappings(FreeformProjectGenerator.getTargetMappings(helper), antScript);
            
            custTargets = FreeformProjectGenerator.getCustomContextMenuActions(helper);
            customTargetsModel.fireTableDataChanged();
            
            showAdvancedPart(true);
            
            updateButtons();
            initialized = true;
            
        }
    }

    /* ProjectCustomizer.Panel hide */
    public void hide(FreeformProject project, AntProjectHelper helper, ProjectCustomizer.ProjectModel model) {
    }
    
    /* ProjectCustomizer.Panel save */
    public void storeValues(FreeformProject project, AntProjectHelper helper, ProjectCustomizer.ProjectModel model) {
        if (!initialized) {
            return;
        }
        List mapping = getMapping();
        if (dirtyRegular) {
            FreeformProjectGenerator.putTargetMappings(helper, mapping);
            FreeformProjectGenerator.putContextMenuAction(helper, mapping);
        }

        if (dirtyCustom) {
            ArrayList l = new ArrayList(custTargets);
            Iterator it = l.iterator();
            while (it.hasNext()) {
                FreeformProjectGenerator.CustomTarget ct = (FreeformProjectGenerator.CustomTarget)it.next();
                // ignore row if target was not set
                if (ct.targets == null || ct.targets.size() == 0) {
                    it.remove();
                    continue;
                }
                if (ct.label == null || ct.label.length() == 0) {
                    ct.label = (String)ct.targets.get(0);
                }
            }
            FreeformProjectGenerator.putCustomContextMenuActions(helper, l);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton add;
    private javax.swing.JComboBox buildCombo;
    private javax.swing.JComboBox cleanCombo;
    private javax.swing.JTable customTargets;
    private javax.swing.JComboBox debugCombo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox javadocCombo;
    private javax.swing.JLabel link;
    private javax.swing.JComboBox redeployCombo;
    private javax.swing.JPanel remainder;
    private javax.swing.JButton remove;
    private javax.swing.JComboBox runCombo;
    private javax.swing.JComboBox testCombo;
    // End of variables declaration//GEN-END:variables

    private class CustomTargetsModel extends AbstractTableModel {
        
        public CustomTargetsModel() {
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public String getColumnName(int column) {
            switch (column) {
                case 0: return "Ant Target";
                default: return "Label";
            }
        }
        
        public int getRowCount() {
            return custTargets.size();
        }
        
        public boolean isCellEditable(int row, int column) {
            if (column == 0) {
                FreeformProjectGenerator.CustomTarget ct = getItem(row);
                if (ct.targets.size() > 1) {
                    return false;
                }
                if ((antScript == null && ct.script == null) ||
                        (antScript != null && antScript.equals(ct.script))) {
                    return true;
                } else {
                    return false;
                }
            }
            return true;
        }
        
        public Class getColumnClass(int column) {
            switch (column) {
                case 0: return JComboBox.class;
                default: return String.class;
            }
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                if ((getItem(rowIndex) == null) || (getItem(rowIndex).targets == null)) {
                    return ""; // NOI18N
                } else {
                    return TargetMappingPanel.getListAsString(getItem(rowIndex).targets);
                }
            } else {
                return getItem(rowIndex).label;
            }
        }
        
        public void setValueAt(Object val, int rowIndex, int columnIndex) {
            if (rowIndex >= custTargets.size ()) {
                return ;
            }
            FreeformProjectGenerator.CustomTarget ct = getItem(rowIndex);
            if (columnIndex == 0) {
                if (((String)val).length() > 0) {
                    ct.targets = Collections.singletonList(val);
                } else {
                    ct.targets = null;
                }
            } else {
                ct.label = (String)val;
            }
            dirtyCustom = true;
        }
        
    }
    
    // For UI testing purposes.
    public static void main(String[] ignore) {
        String[] types = {"j2se", "webapps"}; // NOI18N
        boolean[] adv = {false, true};
        for (int i = 0; i < types.length; i++) {
            for (int j = 0; j < adv.length; j++) {
                JDialog dlg = new JDialog((Frame) null, "type=" + types[i] + " advancedMode=" + adv[j], false); // NOI18N
                dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                TargetMappingPanel panel = new TargetMappingPanel(types[i], adv[j]);
                panel.setTargetNames(new ArrayList(Arrays.asList(new String[] {"build", "clean", "test"})), true); // NOI18N
                dlg.getContentPane().add(panel);
                dlg.pack();
                dlg.setSize(700, 500);
                dlg.setVisible(true);
            }
        }
    }
    
}
