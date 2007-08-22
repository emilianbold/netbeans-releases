/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.uml.propertysupport.options.panels;

import java.util.prefs.Preferences;
import javax.swing.JComboBox;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.support.drawingproperties.DrawingPropertyResource;
import org.netbeans.modules.uml.ui.support.drawingproperties.FontColorDialogs.ApplicationColorsAndFonts;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingPropertyProvider;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingQuestionDialogImpl;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.util.DummyCorePreference;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author  krichard
 */
public class GeneralOptionsPanelForm extends javax.swing.JPanel {

    private final String PSK_ALWAYS = "PSK_ALWAYS";
    private final String PSK_NEVER = "PSK_NEVER";
    private final String PSK_SELECTED = "PSK_SELECTED";

    public final String PSK_RESIZE_ASNEEDED = "PSK_RESIZE_ASNEEDED";
    public final String PSK_RESIZE_EXPANDONLY = "PSK_RESIZE_EXPANDONLY";
    public final String PSK_RESIZE_UNLESSMANUAL = "PSK_RESIZE_UNLESSMANUAL";
    public final String PSK_RESIZE_NEVER = "PSK_RESIZE_NEVER";

    //for menu display
    private final String ALWAYS = NbBundle.getMessage(GeneralOptionsPanelForm.class, "ALWAYS");
    private final String NEVER = NbBundle.getMessage(GeneralOptionsPanelForm.class, "NEVER");
    private final String SELECTED = NbBundle.getMessage(GeneralOptionsPanelForm.class, "SELECTED");
    private final String ASNEEDED = NbBundle.getMessage(GeneralOptionsPanelForm.class, "ASNEEDED");
    private final String EXPANDONLY = NbBundle.getMessage(GeneralOptionsPanelForm.class, "EXPANDONLY");
    private final String UNLESSMANUAL = NbBundle.getMessage(GeneralOptionsPanelForm.class, "UNLESSMANUAL");
    private final String RESIZE_NEVER = NbBundle.getMessage(GeneralOptionsPanelForm.class, "RESIZE_NEVER");

    private final String[] displayChoices = {ALWAYS, SELECTED, NEVER};
    private final String[] mappedChoices = {PSK_ALWAYS, PSK_SELECTED, PSK_NEVER};

    private final String[] resizeDisplayChoices = {ASNEEDED, EXPANDONLY, UNLESSMANUAL, RESIZE_NEVER};
    private final String[] resizeMappedChoices = {PSK_RESIZE_ASNEEDED, PSK_RESIZE_EXPANDONLY, PSK_RESIZE_UNLESSMANUAL, PSK_RESIZE_NEVER};

    /** Creates new form GeneralOptionsPanel */
    public GeneralOptionsPanelForm() {
        initComponents();
    }

    public void store() {
        int autoResizeIndex = autoResizeElementsComboBox.getSelectedIndex();
        int displayCompartmentIndex = displayCompartmentTitlesComboBox.getSelectedIndex();

        Preferences prefs = NbPreferences.forModule(DummyCorePreference.class);

        prefs.putBoolean("UML_Show_Aliases", showAlias.isSelected());
        prefs.putBoolean("UML_Open_Project_Diagrams", openProjectDiagramsCB.isSelected());

        prefs.put("UML_Automatically_Size_Elements", resizeMappedChoices[autoResizeIndex]);
        prefs.put("UML_Display_Compartment_Titles", mappedChoices[displayCompartmentIndex]);

        prefs.putBoolean("UML_Display_Empty_Lists", displayEmpty.isSelected());
        prefs.putBoolean("UML_Gradient_Background", gradient.isSelected());
        prefs.putBoolean("UML_Reconnect_to_Presentation_Boundary", reconnect.isSelected());
        prefs.putBoolean("UML_Resize_with_Show_Aliases_Mode", resizeCB.isSelected());
        prefs.putBoolean("UML_Show_Stereotype_Icons", showStereotype.isSelected());
        prefs.putBoolean("UML_Ask_Before_Layout", askLayoutCB.isSelected());


    }

    public void load() {
        Preferences prefs = NbPreferences.forModule(DummyCorePreference.class);

        if (prefs.getBoolean("UML_Show_Aliases", false)) {
            showAlias.setSelected(true);
        } else {
            showAlias.setSelected(false);
        }
        if (prefs.getBoolean("UML_Open_Project_Diagrams", true)) {
            openProjectDiagramsCB.setSelected(true);
        } else {
            openProjectDiagramsCB.setSelected(false);
        }
        if (prefs.getBoolean("UML_Display_Empty_Lists", true)) {
            displayEmpty.setSelected(true);
        } else {
            displayEmpty.setSelected(false);
        }
        if (prefs.getBoolean("UML_Gradient_Background", true)) {
            gradient.setSelected(true);
        } else {
            gradient.setSelected(false);
        }
        if (prefs.getBoolean("UML_Reconnect_to_Presentation_Boundary", true)) {
            reconnect.setSelected(true);
        } else {
            reconnect.setSelected(false);
        }
        if (prefs.getBoolean("UML_Resize_with_Show_Aliases_Mode", false)) {
            resizeCB.setSelected(true);
        } else {
            resizeCB.setSelected(false);
        }
        if (prefs.getBoolean("UML_Show_Stereotype_Icons", true)) {
            showStereotype.setSelected(true);
        } else {
            showStereotype.setSelected(false);
        }
        if (prefs.getBoolean("UML_Ask_Before_Layout", true)) {
            askLayoutCB.setSelected(true);
        } else {
            askLayoutCB.setSelected(false);
        }
        String autoResizeValue = prefs.get("UML_Automatically_Size_Elements", null);
        String displayCompartmentValue = prefs.get("UML_Display_Compartment_Titles", null);

        int autoResizeIndex = getMappedIndex(resizeMappedChoices, autoResizeValue);
        int compartmentIndex = getMappedIndex(mappedChoices, displayCompartmentValue);

        autoResizeElementsComboBox.setSelectedIndex(autoResizeIndex);

        displayCompartmentTitlesComboBox.setSelectedIndex(compartmentIndex);
    }

    public void cancel() {
        //do nothing ;
    }

    private int getMappedIndex(String[] a, String s) {

        int n = a.length;

        for (int i = 0; i < n; i++) {
            if (a[i].equals(s)) {
                return i;
            }
        }

        return 0;
    }

    public void showFontsAndColorsDialog() {
        new ApplicationColorsAndFonts().setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        advancedPropsPanel = new javax.swing.JPanel();
        openProjectDiagramsCB = new javax.swing.JCheckBox();
        showAlias = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        displayEmpty = new javax.swing.JCheckBox();
        reconnect = new javax.swing.JCheckBox();
        resizeCB = new javax.swing.JCheckBox();
        showStereotype = new javax.swing.JCheckBox();
        gradient = new javax.swing.JCheckBox();
        askLayoutCB = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        autoResizeElementsComboBox = new JComboBox(resizeDisplayChoices);
        displayCompartmentTitlesComboBox = new JComboBox (displayChoices);
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        openProjectDiagramsCB.setSelected(true);
        openProjectDiagramsCB.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanel.openProjectDiagramsCB.text")); // NOI18N
        openProjectDiagramsCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        showAlias.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanelForm.showAlias.text")); // NOI18N
        showAlias.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout advancedPropsPanelLayout = new org.jdesktop.layout.GroupLayout(advancedPropsPanel);
        advancedPropsPanel.setLayout(advancedPropsPanelLayout);
        advancedPropsPanelLayout.setHorizontalGroup(
            advancedPropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, advancedPropsPanelLayout.createSequentialGroup()
                .add(advancedPropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, openProjectDiagramsCB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 595, Short.MAX_VALUE)
                    .add(showAlias, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 595, Short.MAX_VALUE))
                .addContainerGap())
        );
        advancedPropsPanelLayout.setVerticalGroup(
            advancedPropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(advancedPropsPanelLayout.createSequentialGroup()
                .add(openProjectDiagramsCB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(showAlias)
                .addContainerGap())
        );

        displayEmpty.setSelected(true);
        displayEmpty.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanel.displayEmpty.text")); // NOI18N
        displayEmpty.setMargin(new java.awt.Insets(0, 0, 0, 0));

        reconnect.setSelected(true);
        reconnect.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanel.reconnect.text")); // NOI18N
        reconnect.setMargin(new java.awt.Insets(0, 0, 0, 0));

        resizeCB.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanel.resizeCB.text")); // NOI18N
        resizeCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        showStereotype.setSelected(true);
        showStereotype.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanel.showStereotype.text")); // NOI18N
        showStereotype.setMargin(new java.awt.Insets(0, 0, 0, 0));

        gradient.setSelected(true);
        gradient.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanel.gradient.text")); // NOI18N
        gradient.setMargin(new java.awt.Insets(0, 0, 0, 0));

        askLayoutCB.setSelected(true);
        askLayoutCB.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanelForm.askLayoutCB.text")); // NOI18N
        askLayoutCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(displayEmpty, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                    .add(reconnect, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                    .add(resizeCB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                    .add(showStereotype, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                    .add(gradient, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                    .add(askLayoutCB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(displayEmpty)
                .add(7, 7, 7)
                .add(reconnect)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(resizeCB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(showStereotype)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(gradient)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(askLayoutCB)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel2.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanel.jLabel2.text")); // NOI18N

        autoResizeElementsComboBox.setDoubleBuffered(true);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanel.jLabel1.text")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 163, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 157, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(displayCompartmentTitlesComboBox, 0, 225, Short.MAX_VALUE)
                    .add(autoResizeElementsComboBox, 0, 225, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(autoResizeElementsComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(displayCompartmentTitlesComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        jLabel3.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanelForm.jLabel3.text")); // NOI18N

        jLabel4.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanelForm.jLabel4.text")); // NOI18N

        jLabel5.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanelForm.jLabel5.text")); // NOI18N

        jButton1.setText(org.openide.util.NbBundle.getMessage(GeneralOptionsPanelForm.class, "GeneralOptionsPanel.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton1ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(advancedPropsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel4)
                        .add(2, 2, 2)
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 589, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 164, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(jButton1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 312, Short.MAX_VALUE))
                            .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(20, 20, 20)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel3)
                    .add(advancedPropsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(53, 53, 53)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel4)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton1))
                    .add(jLabel5))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        showFontsAndColorsDialog();
}//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel advancedPropsPanel;
    private javax.swing.JCheckBox askLayoutCB;
    private javax.swing.JComboBox autoResizeElementsComboBox;
    private javax.swing.JComboBox displayCompartmentTitlesComboBox;
    private javax.swing.JCheckBox displayEmpty;
    private javax.swing.JCheckBox gradient;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JCheckBox openProjectDiagramsCB;
    private javax.swing.JCheckBox reconnect;
    private javax.swing.JCheckBox resizeCB;
    private javax.swing.JCheckBox showAlias;
    private javax.swing.JCheckBox showStereotype;
    // End of variables declaration//GEN-END:variables
}
