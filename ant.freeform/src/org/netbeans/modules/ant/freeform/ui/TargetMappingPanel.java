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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import org.netbeans.modules.ant.freeform.FreeformProjectGenerator;
import org.netbeans.modules.ant.freeform.Util;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;

// XXX: validate that entered target really exists in the Ant script, etc.

/**
 * @author  David Konecny
 */
public class TargetMappingPanel extends javax.swing.JPanel implements ProjectCustomizer.Panel {

    public static String BUILD_ACTION = "build"; // NOI18N
    public static String CLEAN_ACTION = "clean"; // NOI18N
    public static String REBUILD_ACTION = "rebuild"; // NOI18N
    public static String JAVADOC_ACTION = "javadoc"; // NOI18N
    public static String RUN_ACTION = "run"; // NOI18N
    public static String TEST_ACTION = "test"; // NOI18N
    public static String REDEPLOY_ACTION = "redeploy"; // NOI18N

    private boolean initialized;
    private List/*<String>*/ targetNames;
    private List/*<TargetMapping>*/ targetMappings;
    private String defaultScript = null;

    private String projectType;
    
    public TargetMappingPanel(String type) {
        initComponents();
        targetMappings = new ArrayList();
        projectType = type;
        
        jLabel7.setVisible(projectType.equals("j2se")); // NOI18N
        testCombo.setVisible(projectType.equals("j2se")); // NOI18N
        jLabel3.setVisible(projectType.equals("webapps")); // NOI18N
        redeployCombo.setVisible(projectType.equals("webapps")); // NOI18N
    }
    
    public void setTargetNames(List list) {
        targetNames = list;
        targetNames.add(0, ""); //NOI18N
        updateCombos();
    }

    public void setScript(String script) {
        this.defaultScript = script;
    }

    private void updateCombos() {
        Iterator it = targetNames.iterator();
        while (it.hasNext()) {
            String name = (String)it.next();
            buildCombo.addItem(name);
            cleanCombo.addItem(name);
            javadocCombo.addItem(name);
            runCombo.addItem(name);
            if (projectType.equals("j2se")) //NOI18N
                testCombo.addItem(name);
            else if (projectType.equals("webapps")) //NOI18N
                redeployCombo.addItem(name);
        }
        selectItem(buildCombo, "build", false); //NOI18N
        selectItem(cleanCombo, "clean", false); //NOI18N
        selectItem(javadocCombo, "javadoc", false); //NOI18N
        selectItem(runCombo, "run", false); //NOI18N
        if (projectType.equals("j2se")) //NOI18N
            selectItem(testCombo, "test", false);
        else if (projectType.equals("webapps")) //NOI18N
            selectItem(redeployCombo, "run-deploy", false); //NOI18N
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

    private void initMappings(List/*<FreeformProjectGenerator.TargetMapping>*/ list) {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            FreeformProjectGenerator.TargetMapping tm = (FreeformProjectGenerator.TargetMapping)it.next();
            if (tm.name.equals(BUILD_ACTION)) {
                selectItem(buildCombo, getListAsString(tm.targets), true);
            }
            if (tm.name.equals(CLEAN_ACTION)) {
                selectItem(cleanCombo, getListAsString(tm.targets), true);
            }
            if (tm.name.equals(JAVADOC_ACTION)) {
                selectItem(javadocCombo, getListAsString(tm.targets), true);
            }
            if (tm.name.equals(RUN_ACTION)) {
                selectItem(runCombo, getListAsString(tm.targets), true);
            }
            if (tm.name.equals(TEST_ACTION)) {
                selectItem(testCombo, getListAsString(tm.targets), true);
            }
            if (tm.name.equals(REDEPLOY_ACTION)) {
                selectItem(redeployCombo, getListAsString(tm.targets), true);
            }
        }
        targetMappings = list;
    }

    private String getListAsString(List list) {
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

    private List getStringAsList(String str) {
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

    private boolean storeTarget(String key, JComboBox combo) {
        if (combo.getModel().getSelectedItem() == null) {
            return false;
        }
        String value = (String)combo.getModel().getSelectedItem();
        if (value.length() == 0) {
            return false;
        }
        FreeformProjectGenerator.TargetMapping tm = getTargetMapping(key);
        tm.targets = getStringAsList(value);
        return true;
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
        tm.script = defaultScript;
        targetMappings.add(tm);
        return tm;
    }

    public List/*<FreeformProjectGenerator.TargetMapping>*/ getMapping() {
        if (storeTarget(BUILD_ACTION, buildCombo) && storeTarget(CLEAN_ACTION, cleanCombo)) {
            FreeformProjectGenerator.TargetMapping tm = getTargetMapping(REBUILD_ACTION);
            String val = (String)cleanCombo.getModel().getSelectedItem()+","+(String)buildCombo.getModel().getSelectedItem();
            tm.targets = getStringAsList(val);
        }
        storeTarget(RUN_ACTION, runCombo);
        storeTarget(JAVADOC_ACTION, javadocCombo);
        if (projectType.equals("j2se")) //NOI18N
            storeTarget(TEST_ACTION, testCombo);
        else if (projectType.equals("webapps")) //NOI18N
            storeTarget(REDEPLOY_ACTION, redeployCombo);
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

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Specify Ant targets executed by common menu items.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabel1, gridBagConstraints);

        jLabel2.setText("Build:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(jLabel2, gridBagConstraints);

        jLabel4.setText("Clean:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(jLabel4, gridBagConstraints);

        jLabel5.setText("Run:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(jLabel5, gridBagConstraints);

        jLabel6.setText("Generate Javadoc:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(jLabel6, gridBagConstraints);

        jLabel7.setText("Test:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(jLabel7, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(buildCombo, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(cleanCombo, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(javadocCombo, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(runCombo, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(testCombo, gridBagConstraints);

        jLabel3.setText("Redeploy:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(redeployCombo, gridBagConstraints);

    }//GEN-END:initComponents

    public void initValues(AntProjectHelper helper, List panels) {
        if (!initialized) {
            FileObject as = FreeformProjectGenerator.getAntScript(helper);
            List l = Util.getAntScriptTargetNames(as);
            if (l != null) {
                setTargetNames(l);
            }
            initMappings(FreeformProjectGenerator.getTargetMappings(helper));
            defaultScript = FreeformProjectGenerator.getProperties(helper).getProperty(FreeformProjectGenerator.PROP_ANT_SCRIPT);
            initialized = true;
        }
    }

    public void storeValues(AntProjectHelper helper) {
        if (!initialized) {
            return;
        }
        FreeformProjectGenerator.putTargetMappings(helper, getMapping());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox buildCombo;
    private javax.swing.JComboBox cleanCombo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JComboBox javadocCombo;
    private javax.swing.JComboBox redeployCombo;
    private javax.swing.JComboBox runCombo;
    private javax.swing.JComboBox testCombo;
    // End of variables declaration//GEN-END:variables

}
