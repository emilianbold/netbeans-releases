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


package com.tomsawyer.editor.service.layout.jlayout;

import java.awt.Font;
import javax.swing.JDialog;
import com.tomsawyer.service.TSServiceInputData;
import com.tomsawyer.editor.TSEGraph;
import com.tomsawyer.editor.TSEResourceBundleWrapper;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


public class TSEDisconnectedTabExt extends TSEDisconnectedTab
{
    public static TSEResourceBundleWrapper tsBundle = TSEResourceBundleWrapper.getSystemLabelBundle();
    /**
     * This constructor creates a new Disconnected tab.
     * @param graph the <code>TSEGraph</code> whose layout properties are being
     * edited.
     * @param inputData the service input data object where the layout
     * properties are stored
     * @param dialog the Layout Properties dialog that this tab belongs to.
     */
    public TSEDisconnectedTabExt(TSEGraph graph,
        TSServiceInputData inputData,
        JDialog dialog)
    {
        super(graph, inputData, dialog);
        setA11yFeatures();
    }
    
    /**
     * This method creates the Components panel of this tab.
     */
    protected JPanel makeComponentsPanel()
    {
        JPanel result = new JPanel();
        
        // Create the components
        
        this.detectConn =
            this.createCheckbox("Detect_Components", DETECT_COMPONENTS);
        
        // create doubld fields
        this.connConstantSpacingField = this.createDoubleField(4, 0, 1000);
        this.connProportionalSpacingField = this.createDoubleField(4, 0, 1000);
        
        // create labels
        JLabel connConstantSpacingLabel = this.createLabel(
            "Constant_Spacing",
            null,
            connConstantSpacingField);
        
        JLabel connProportionalSpacingLabel = this.createLabel(
            "Proportional_Spacing",
            null,
            connProportionalSpacingField);
        
        // Layout the components
        result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
        
        JPanel leftPanel = new JPanel();
        JPanel rightPanel = new JPanel();
        
        // Layout the components
        JPanel table = new JPanel();
        
        table.setLayout(new BoxLayout(table, BoxLayout.X_AXIS));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        
        leftPanel.setAlignmentX(LEFT_ALIGNMENT);
        rightPanel.setAlignmentX(LEFT_ALIGNMENT);
        table.setAlignmentX(LEFT_ALIGNMENT);
        
        leftPanel.add(connConstantSpacingLabel);
        leftPanel.add(this.createVerticalRigidArea(9));
        leftPanel.add(connProportionalSpacingLabel);
        
        rightPanel.add(this.connConstantSpacingField);
        rightPanel.add(this.createVerticalRigidArea(5));
        rightPanel.add(this.connProportionalSpacingField);
        
        table.add(leftPanel);
        table.add(this.createHorizontalRigidArea(40));
        table.add(rightPanel);
        
        result.add(this.detectConn);
        result.add(this.createVerticalRigidArea(5));
        result.add(table);
        
        this.createCompoundBorder(result,
            "Components",
            0,
            6,
            5,
            5);
        
        return result;
    }
    
    /**
     * This method creates the Disconnected Nodes panel of this tab.
     */
    protected JPanel makeDisconnectedPanel()
    {
        JPanel result = new JPanel();
        
        // Create the components.
        
        this.detectDisc = this.createCheckbox("Detect_Disconnected_Nodes",
            DETECT_DISCONNECTED_NODES);
        
        this.discConstantSpacingField = this.createDoubleField(4, 0, 1000);
        this.discProportionalSpacingField = this.createDoubleField(4, 0, 1000);
        
        JLabel discConstantSpacingLabel = this.createLabel(
            "Constant_Spacing",
            null,
            discConstantSpacingField);
        
        JLabel discProportionalSpacingLabel = this.createLabel(
            "Proportional_Spacing",
            null,
            discProportionalSpacingField);
        
        // Layout the components
        result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
        
        JPanel leftPanel = new JPanel();
        JPanel rightPanel = new JPanel();
        
        // Layout the components
        JPanel table = new JPanel();
        
        table.setLayout(new BoxLayout(table, BoxLayout.X_AXIS));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        
        leftPanel.setAlignmentX(LEFT_ALIGNMENT);
        rightPanel.setAlignmentX(LEFT_ALIGNMENT);
        table.setAlignmentX(LEFT_ALIGNMENT);
        
        leftPanel.add(discConstantSpacingLabel);
        leftPanel.add(this.createVerticalRigidArea(9));
        leftPanel.add(discProportionalSpacingLabel);
        
        rightPanel.add(this.discConstantSpacingField);
        rightPanel.add(this.createVerticalRigidArea(5));
        rightPanel.add(this.discProportionalSpacingField);
        
        table.add(leftPanel);
        table.add(this.createHorizontalRigidArea(40));
        table.add(rightPanel);
        
        result.add(this.detectDisc);
        result.add(this.createVerticalRigidArea(5));
        result.add(table);
        
        this.createCompoundBorder(result,
            "Disconnected_Nodes",
            0,
            6,
            5,
            5);
        
        return result;
    }
    
    /**
     * This method creates a new JPanel and sets its layout manager to
     * a BoxLayout with the input layout style.
     */
    protected JPanel createBoxLayoutPanel(int layout)
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, layout));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setAlignmentY(TOP_ALIGNMENT);
        return panel;
    }
    
    /**
     * This method creates a new <code>JLabel</code> with the given text
     * @param labelKey the key for which the label text will be obtained from the resource bundle
     * @param mnemonicKey the for which the mnemonic will be obtained from the resource bundle.
     * set it to null if you don't want to set mnemonic for this label.
     * @param labelForComp the component for which the label is set
     */
    protected JLabel createLabel(String labelKey, String mnemonicKey, Component labelForComp)
    {
        JLabel label = new JLabel(tsBundle.getStringSafely(labelKey) +":");
        label.setForeground(labelColor);
        label.setAlignmentX(LEFT_ALIGNMENT);
        label.setAlignmentY(TOP_ALIGNMENT);
        //A11y
        label.setLabelFor(labelForComp);
        if (mnemonicKey != null)
        {
            label.setDisplayedMnemonic(AccessiblityUtils.getMnemonic(mnemonicKey));
        }
        return (label);
    }
    
    public void createCompoundBorder(
        JPanel panel,
        String labelKey,
        int top,
        int left,
        int bottom,
        int right)
    {
        String label = this.tsBundle.getStringSafely(labelKey);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(label);
        titledBorder.setTitleColor(labelColor);
        panel.setBorder(
            BorderFactory.createCompoundBorder(
            titledBorder,
            BorderFactory.createEmptyBorder(top, left, bottom, right)));
    }
    
    public void setA11yFeatures()
    {
        Font defaultFont = this.getFont();
        
        // set nmemonics for checkboxes radio buttons (only the 1st one in a component group)
        detectConn.setMnemonic(AccessiblityUtils.getMnemonic("Detect_Components"));
        detectDisc.setMnemonic(AccessiblityUtils.getMnemonic("Detect_Disconnected_Nodes"));
        aspectRatioAutomaticRadioButton.setMnemonic(AccessiblityUtils.getMnemonic("Automatic"));
        
        // set font to use the default font
        detectConn.setFont(defaultFont);
        detectDisc.setFont(defaultFont);
        aspectRatioAutomaticRadioButton.setFont(defaultFont);
        aspectRatioCustomRadioButton.setFont(defaultFont);
        
        // set accessibility name and descritpion
        AccessiblityUtils.setAccessibleProperties(detectConn,
            null, "Detect_Components");
        AccessiblityUtils.setAccessibleProperties(detectDisc,
            null, "Detect_Disconnected_Nodes");
        AccessiblityUtils.setAccessibleProperties(aspectRatioAutomaticRadioButton,
            null, "Automatic");
        AccessiblityUtils.setAccessibleProperties(aspectRatioCustomRadioButton,
            null, "Custom");
        
        AccessiblityUtils.setAccessibleProperties(connConstantSpacingField,
            null, "Constant_Spacing");
        AccessiblityUtils.setAccessibleProperties(connProportionalSpacingField,
            null, "Proportional_Spacing");
        AccessiblityUtils.setAccessibleProperties(discConstantSpacingField,
            null, "Constant_Spacing");
        AccessiblityUtils.setAccessibleProperties(discProportionalSpacingField,
            null, "Proportional_Spacing");
        AccessiblityUtils.setAccessibleProperties(aspectRatioCustomField,
            "Aspect_Ratio_Custom", "Aspect_Ratio_Custom");
        
        // set accessible description of the tab itself to be the same as the accesible name
        this.getAccessibleContext().setAccessibleDescription(this.getAccessibleContext().getAccessibleName());
    }
}
