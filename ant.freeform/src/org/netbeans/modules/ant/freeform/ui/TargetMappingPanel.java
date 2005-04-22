/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform.ui;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.ant.freeform.FreeformProjectGenerator;
import org.netbeans.modules.ant.freeform.Util;
import org.netbeans.modules.ant.freeform.spi.ProjectConstants;
import org.netbeans.modules.ant.freeform.spi.ProjectPropertiesPanel;
import org.netbeans.modules.ant.freeform.spi.TargetDescriptor;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author  David Konecny
 */
public class TargetMappingPanel extends javax.swing.JPanel implements java.awt.event.ActionListener, org.openide.util.HelpCtx.Provider {

    public static String BUILD_ACTION = "build"; // NOI18N
    public static String CLEAN_ACTION = "clean"; // NOI18N
    public static String REBUILD_ACTION = "rebuild"; // NOI18N
    public static String JAVADOC_ACTION = "javadoc"; // NOI18N
    public static String RUN_ACTION = "run"; // NOI18N
    public static String TEST_ACTION = "test"; // NOI18N

    private static List DEFAULT_BUILD_TARGETS = Arrays.asList(new String[]{"build", "compile", "jar", "dist", "all", ".*jar.*"}); // NOI18N
    private static List DEFAULT_CLEAN_TARGETS = Arrays.asList(new String[]{"clean", ".*clean.*"}); // NOI18N
    private static List DEFAULT_REBUILD_TARGETS = Arrays.asList(new String[]{"rebuild", ".*rebuild.*"}); // NOI18N
    private static List DEFAULT_JAVADOC_TARGETS = Arrays.asList(new String[]{"javadoc", "javadocs", "docs", "doc", ".*javadoc.*", ".*doc.*"}); // NOI18N
    private static List DEFAULT_RUN_TARGETS = Arrays.asList(new String[]{"run", "start", ".*run.*", ".*start.*"}); // NOI18N
    private static List DEFAULT_TEST_TARGETS = Arrays.asList(new String[]{"test", ".*test.*"}); // NOI18N
    
    private List/*<String>*/ targetNames;
    private List/*<TargetMapping>*/ targetMappings;
    private List/*<FreeformProjectGenerator.CustomTarget>*/ custTargets;
    private CustomTargetsModel customTargetsModel;
    private String antScript;
    
    private ArrayList combos = new ArrayList();
    private ArrayList targetDescs = new ArrayList();
    
    /** Any change in standard tasks which needs to be persisted? */
    private boolean dirtyRegular;
    
    /** Any change in custom tasks which needs to be persisted? */
    private boolean dirtyCustom;

    private AntProjectHelper helper;
    
    public TargetMappingPanel(boolean advancedPart) {
        this(new ArrayList(), advancedPart);
    }
    
    public TargetMappingPanel(List extraTargets, boolean advancedPart) {
        initComponents();
        targetMappings = new ArrayList();

        custTargets = new ArrayList();
        customTargetsModel = new CustomTargetsModel();
        customTargets.setModel(customTargetsModel);
        customTargets.getTableHeader().setReorderingAllowed(false);
        
        addTargets(extraTargets);
        showAdvancedPart(advancedPart);
    }

    public TargetMappingPanel(List extraTargets, PropertyEvaluator evaluator, AntProjectHelper helper) {
        this(extraTargets, true);
        this.helper = helper;
        
        FileObject as = FreeformProjectGenerator.getAntScript(helper, evaluator);
        List l = null;
        // #50933 - script can be null
        if (as != null) {
            l = Util.getAntScriptTargetNames(as);
        }
        if (l != null) {
            setTargetNames(l, false);
            initAntTargetEditor(l);
        }
        antScript = defaultAntScript(evaluator);
        initMappings(FreeformProjectGenerator.getTargetMappings(helper), antScript);

        custTargets = FreeformProjectGenerator.getCustomContextMenuActions(helper);
        customTargetsModel.fireTableDataChanged();

        updateButtons();
    }
    
    /**
     * Get the default name of a project's Ant script, as for project.xml usage.
     * @param evaluator a property evaluator for the project
     * @return the script name to use, or null for the default
     * @see FreeformProjectGenerator.TargetMapping#script
     */
    static String defaultAntScript(PropertyEvaluator evaluator) {
        String antScript = evaluator.getProperty(ProjectConstants.PROP_ANT_SCRIPT);
        if (antScript == null) {
            // Default, i.e. build.xml.
            return null;
        } else {
            // Set to something specific; refer to it symbolically.
            return "${" + ProjectConstants.PROP_ANT_SCRIPT + "}"; // NOI18N
        }
    }

    
    private void addTargets(List extraTargets) {
        combos.add(buildCombo);
        targetDescs.add(new TargetDescriptor(BUILD_ACTION, DEFAULT_BUILD_TARGETS, null, null));
        combos.add(cleanCombo);
        targetDescs.add(new TargetDescriptor(CLEAN_ACTION, DEFAULT_CLEAN_TARGETS, null, null));
        combos.add(javadocCombo);
        targetDescs.add(new TargetDescriptor(JAVADOC_ACTION, DEFAULT_JAVADOC_TARGETS, null, null));
        combos.add(runCombo);
        targetDescs.add(new TargetDescriptor(RUN_ACTION, DEFAULT_RUN_TARGETS, null, null));
        combos.add(testCombo);
        targetDescs.add(new TargetDescriptor(TEST_ACTION, DEFAULT_TEST_TARGETS, null, null));
        int y = 5;
        Iterator it = extraTargets.iterator();
        while (it.hasNext()) {
            TargetDescriptor desc = (TargetDescriptor)it.next();
            targetDescs.add(desc);

            JComboBox combo = new JComboBox();
            combo.setEditable(true);
            java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = y;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
            targetsPanel.add(combo, gridBagConstraints);

            JLabel label = new JLabel();
            label.setLabelFor(combo);
            org.openide.awt.Mnemonics.setLocalizedText(label, desc.getIDEActionLabel());
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = y;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
            targetsPanel.add(label, gridBagConstraints);
            label.getAccessibleContext().setAccessibleDescription(desc.getAccessibleLabel());
            
            combos.add(combo);
            y++;
        }
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx( TargetMappingPanel.class );
    }
    
    private void showAdvancedPart(boolean show) {
        additionalTargetsLabel.setVisible(show);
        jScrollPane1.setVisible(show);
        customTargets.setVisible(show);
        add.setVisible(show);
        remove.setVisible(show);
        // handle panel resizing:
        remainder.setVisible(show);
        specialRemainder.setVisible(!show);
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
        targetNames.add(0, ""); // NOI18N
        updateCombos(selectDefaults);
    }

    public void setScript(String script) {
        this.antScript = script;
        Iterator it = targetMappings.iterator();
        while (it.hasNext()) {
            FreeformProjectGenerator.TargetMapping tm = (FreeformProjectGenerator.TargetMapping)it.next();
            tm.script = script;
        }
    }

    private void updateCombos(boolean selectDefaults) {
        // In case you go back and choose a different script:
        Iterator it = combos.iterator();
        Iterator it3 = targetDescs.iterator();
        while (it.hasNext()) {
            JComboBox combo = (JComboBox)it.next();
            TargetDescriptor desc = (TargetDescriptor)it3.next();
            combo.removeAllItems();
            Iterator it2 = targetNames.iterator();
            while (it2.hasNext()) {
                String name = (String)it2.next();
                combo.addItem(name);
            }
            if (selectDefaults) {
                selectItem(combo, desc.getDefaultTargets(), false); // NOI18N
            }
        }
    }

    /**
     * @param items concrete item to be selected (or added) or list of
     *  regular expressions to match
     */
    private void selectItem(JComboBox combo, List items, boolean add) {
        ComboBoxModel model = combo.getModel();
        Iterator it = items.iterator();
        while (it.hasNext()) {
            String item = (String)it.next();
            Pattern pattern = Pattern.compile(item);
            for (int i=0; i<model.getSize(); i++) {
                String target = (String)model.getElementAt(i);
                Matcher matcher = pattern.matcher(target);
                if (matcher.matches()) {
                    model.setSelectedItem(target);
                    return;
                }
            }
        }
        if (add) {
            assert items.size() == 1 : "There should be only one item in this case"; // NOI18N
            combo.addItem(items.get(0));
            model.setSelectedItem(items.get(0));
        } else {
            model.setSelectedItem(""); // NOI18N
        }
    }

    private void initMappings(List/*<FreeformProjectGenerator.TargetMapping>*/ list, String antScript) {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            FreeformProjectGenerator.TargetMapping tm = (FreeformProjectGenerator.TargetMapping)it.next();
            Iterator it2 = targetDescs.iterator();
            Iterator it3 = combos.iterator();
            while (it2.hasNext()) {
                TargetDescriptor desc = (TargetDescriptor)it2.next();
                assert it3.hasNext();
                JComboBox combo = (JComboBox)it3.next();
                if (tm.name.equals(desc.getIDEActionName())) {
                    selectItem(combo, Collections.singletonList(getListAsString(tm.targets)), true);
                    checkAntScript(combo, antScript, tm.script);
                }
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
                sb.append(" "); // NOI18N
            }
        }
        return sb.toString();
    }

    static List getStringAsList(String str) {
        ArrayList l = new ArrayList(2);
        StringTokenizer tok = new StringTokenizer(str, " "); // NOI18N
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
        Iterator it2 = targetDescs.iterator();
        Iterator it3 = combos.iterator();
        while (it2.hasNext()) {
            TargetDescriptor desc = (TargetDescriptor)it2.next();
            JComboBox combo = (JComboBox)it3.next();
            storeTarget(desc.getIDEActionName(), combo);
        }
        // update rebuilt:
        if (cleanCombo.getModel().getSelectedItem() != null &&
                ((String)cleanCombo.getModel().getSelectedItem()).length() > 0 &&
                buildCombo.getModel().getSelectedItem() != null &&
                ((String)buildCombo.getModel().getSelectedItem()).length() > 0) {
            FreeformProjectGenerator.TargetMapping tm = getTargetMapping(REBUILD_ACTION);
            String val = (String)cleanCombo.getModel().getSelectedItem() + " " + (String)buildCombo.getModel().getSelectedItem(); // NOI18N
            tm.targets = getStringAsList(val);
        } else {
            removeTargetMapping(REBUILD_ACTION);
        }
        return targetMappings;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        mainLabel = new javax.swing.JLabel();
        additionalTargetsLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        customTargets = new javax.swing.JTable();
        add = new javax.swing.JButton();
        remove = new javax.swing.JButton();
        remainder = new javax.swing.JPanel();
        targetsPanel = new javax.swing.JPanel();
        buildLabel = new javax.swing.JLabel();
        cleanLabel = new javax.swing.JLabel();
        runLabel = new javax.swing.JLabel();
        javadocLabel = new javax.swing.JLabel();
        testLabel = new javax.swing.JLabel();
        buildCombo = new javax.swing.JComboBox();
        cleanCombo = new javax.swing.JComboBox();
        javadocCombo = new javax.swing.JComboBox();
        runCombo = new javax.swing.JComboBox();
        testCombo = new javax.swing.JComboBox();
        specialRemainder = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(300, 280));
        mainLabel.setLabelFor(this);
        org.openide.awt.Mnemonics.setLocalizedText(mainLabel, org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "LBL_TargetMappingPanel_jLabel1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(mainLabel, gridBagConstraints);
        mainLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "ACSD_TargetMappingPanel_jLabel1"));

        additionalTargetsLabel.setLabelFor(customTargets);
        org.openide.awt.Mnemonics.setLocalizedText(additionalTargetsLabel, org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "LBL_TargetMappingPanel_jLabel10"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(additionalTargetsLabel, gridBagConstraints);
        additionalTargetsLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "ACSD_TargetMappingPanel_jLabel10"));

        jScrollPane1.setViewportView(customTargets);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(add, org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "BTN_TargetMappingPanel_add"));
        add.addActionListener(this);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(add, gridBagConstraints);
        add.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "ACSD_TargetMappingPanel_add"));

        org.openide.awt.Mnemonics.setLocalizedText(remove, org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "BTN_TargetMappingPanel_remove"));
        remove.addActionListener(this);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(remove, gridBagConstraints);
        remove.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "ACSD_TargetMappingPanel_remove"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(remainder, gridBagConstraints);

        targetsPanel.setLayout(new java.awt.GridBagLayout());

        buildLabel.setLabelFor(buildCombo);
        org.openide.awt.Mnemonics.setLocalizedText(buildLabel, org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "LBL_TargetMappingPanel_jLabel2"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        targetsPanel.add(buildLabel, gridBagConstraints);
        buildLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "ACSD_TargetMappingPanel_jLabel2"));

        cleanLabel.setLabelFor(cleanCombo);
        org.openide.awt.Mnemonics.setLocalizedText(cleanLabel, org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "LBL_TargetMappingPanel_jLabel4"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        targetsPanel.add(cleanLabel, gridBagConstraints);
        cleanLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "ACSD_TargetMappingPanel_jLabel4"));

        runLabel.setLabelFor(runCombo);
        org.openide.awt.Mnemonics.setLocalizedText(runLabel, org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "LBL_TargetMappingPanel_jLabel5"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        targetsPanel.add(runLabel, gridBagConstraints);
        runLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "ACSD_TargetMappingPanel_jLabel5"));

        javadocLabel.setLabelFor(javadocCombo);
        org.openide.awt.Mnemonics.setLocalizedText(javadocLabel, org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "LBL_TargetMappingPanel_jLabel6"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        targetsPanel.add(javadocLabel, gridBagConstraints);
        javadocLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "ACSD_TargetMappingPanel_jLabel6"));

        testLabel.setLabelFor(testCombo);
        org.openide.awt.Mnemonics.setLocalizedText(testLabel, org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "LBL_TargetMappingPanel_jLabel7"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        targetsPanel.add(testLabel, gridBagConstraints);
        testLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "ACSD_TargetMappingPanel_jLabel7"));

        buildCombo.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        targetsPanel.add(buildCombo, gridBagConstraints);

        cleanCombo.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        targetsPanel.add(cleanCombo, gridBagConstraints);

        javadocCombo.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        targetsPanel.add(javadocCombo, gridBagConstraints);

        runCombo.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        targetsPanel.add(runCombo, gridBagConstraints);

        testCombo.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        targetsPanel.add(testCombo, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(targetsPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(specialRemainder, gridBagConstraints);

    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == add) {
            TargetMappingPanel.this.addActionPerformed(evt);
        }
        else if (evt.getSource() == remove) {
            TargetMappingPanel.this.removeActionPerformed(evt);
        }
    }//GEN-END:initComponents

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
    
    public static class Panel implements ProjectPropertiesPanel {
        
        private List extraTargets;
        private PropertyEvaluator evaluator;
        private AntProjectHelper helper;
        private TargetMappingPanel panel;
        
        public Panel(List extraTargets, PropertyEvaluator evaluator, AntProjectHelper helper) {
            this.helper = helper;
            this.extraTargets = extraTargets;
            this.evaluator = evaluator;
        }
        
        /* ProjectCustomizer.Panel save */
        public void storeValues() {
            if (panel == null) {
                return;
            }
            List mapping = panel.getMapping();
            if (panel.dirtyRegular) {
                FreeformProjectGenerator.putTargetMappings(helper, mapping);
                FreeformProjectGenerator.putContextMenuAction(helper, mapping);
            }

            if (panel.dirtyCustom) {
                ArrayList l = new ArrayList(panel.custTargets);
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

        public String getDisplayName() {
            return NbBundle.getMessage(TargetMappingPanel.class, "LBL_ProjectCustomizer_Category_Targets");
        }

        public JComponent getComponent() {
            if (panel == null) {
                panel = new TargetMappingPanel(extraTargets, evaluator, helper);
            }
            return panel;
        }

        public int getPreferredPosition() {
            return 1000;
        }
        
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton add;
    private javax.swing.JLabel additionalTargetsLabel;
    private javax.swing.JComboBox buildCombo;
    private javax.swing.JLabel buildLabel;
    private javax.swing.JComboBox cleanCombo;
    private javax.swing.JLabel cleanLabel;
    private javax.swing.JTable customTargets;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox javadocCombo;
    private javax.swing.JLabel javadocLabel;
    private javax.swing.JLabel mainLabel;
    private javax.swing.JPanel remainder;
    private javax.swing.JButton remove;
    private javax.swing.JComboBox runCombo;
    private javax.swing.JLabel runLabel;
    private javax.swing.JPanel specialRemainder;
    private javax.swing.JPanel targetsPanel;
    private javax.swing.JComboBox testCombo;
    private javax.swing.JLabel testLabel;
    // End of variables declaration//GEN-END:variables

    private class CustomTargetsModel extends AbstractTableModel {
        
        public CustomTargetsModel() {
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public String getColumnName(int column) {
            switch (column) {
                case 0: return org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "LBL_TargetMappingPanel_Target");
                default: return org.openide.util.NbBundle.getMessage(TargetMappingPanel.class, "LBL_TargetMappingPanel_Label");
            }
        }
        
        public int getRowCount() {
            return custTargets.size();
        }
        
        public boolean isCellEditable(int row, int column) {
            if (column == 0) {
                FreeformProjectGenerator.CustomTarget ct = getItem(row);
                if (ct.targets != null && ct.targets.size() > 1) {
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
        boolean[] adv = {false, true};
        for (int j = 0; j < adv.length; j++) {
            JDialog dlg = new JDialog((Frame) null, "advancedMode=" + adv[j], false); // NOI18N
            dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            TargetMappingPanel panel = new TargetMappingPanel(adv[j]);
            panel.setTargetNames(new ArrayList(Arrays.asList(new String[] {"build", "clean", "test"})), true); // NOI18N
            dlg.getContentPane().add(panel);
            dlg.pack();
            dlg.setSize(700, 500);
            dlg.setVisible(true);
        }
    }
    
}
