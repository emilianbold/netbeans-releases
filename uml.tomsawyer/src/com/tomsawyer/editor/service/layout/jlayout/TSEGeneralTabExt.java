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

import com.tomsawyer.editor.TSEResourceBundleWrapper;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import com.tomsawyer.service.TSServiceInputData;
import com.tomsawyer.editor.TSEGraph;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


public class TSEGeneralTabExt extends TSEGeneralTab
{
    public static TSEResourceBundleWrapper tsBundle = TSEResourceBundleWrapper.getSystemLabelBundle();
    
    /**
     * This constructor creates a new Symmetric tab.
     * @param
     * 		graph the <code>TSEGraph</code> whose layout
     * 		properties are being edited.
     * @param
     * 		inputData the service input data object where the
     * 		layout options are stored
     * @param
     * 		dialog the Layout Properties dialog that this
     * 		tab belongs to.
     */
    public TSEGeneralTabExt(TSEGraph graph,
        TSServiceInputData inputData,
        JDialog dialog)
    {
        super(graph, inputData, dialog);
        setA11yFeatures();
    }
    
    /**
     * This method creates the Margin Spacing panel of this tab.
     */
    protected JPanel makeMarginSpacingPanel()
    {
        JPanel marginSpacing = new JPanel();
        
        // Create the radio buttons
        
        this.proportionalMarginSpacing = this.createRadioButton(
            "Proportional_Spacing",
            PROPORTIONAL_MARGIN_SPACING);
        this.constantMarginSpacing = this.createRadioButton(
            "Constant_Spacing",
            CONSTANT_MARGIN_SPACING);
        
        // Add the radio buttons to a group
        
        ButtonGroup spacingGroup = new ButtonGroup();
        spacingGroup.add(this.proportionalMarginSpacing);
        spacingGroup.add(this.constantMarginSpacing);
        
        // Layout the Margin Spacing panel.
        
        marginSpacing.setLayout(
            new BoxLayout(marginSpacing, BoxLayout.Y_AXIS));
        
        this.proportionalMarginSpacing.setAlignmentX(LEFT_ALIGNMENT);
        this.constantMarginSpacing.setAlignmentX(LEFT_ALIGNMENT);
        
        // Create the spacing panel and its components
        
        JPanel spacing = new JPanel();
        
        
        this.leftMarginSpacingField = this.createDoubleField(4, 0, 9999);
        this.topMarginSpacingField = this.createDoubleField(4, 0, 9999);
        this.rightMarginSpacingField = this.createDoubleField(4, 0, 9999);
        this.bottomMarginSpacingField = this.createDoubleField(4, 0, 9999);
        
        JLabel leftLabel = this.createLabel("Left",
            null, leftMarginSpacingField);
        JLabel topLabel = this.createLabel("Top",
            null, topMarginSpacingField);
        JLabel rightLabel = this.createLabel("Right",
            null, rightMarginSpacingField);
        JLabel bottomLabel = this.createLabel("Bottom",
            null, bottomMarginSpacingField);
        
        spacing.setLayout(new BoxLayout(spacing, BoxLayout.X_AXIS));
        JPanel leftPanel = new JPanel();
        JPanel rightPanel = new JPanel();
        
        // Layout the components of the spacing panel
        
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        
        leftPanel.add(leftLabel);
        leftPanel.add(this.createVerticalRigidArea(9));
        leftPanel.add(topLabel);
        leftPanel.add(this.createVerticalRigidArea(9));
        leftPanel.add(rightLabel);
        leftPanel.add(this.createVerticalRigidArea(9));
        leftPanel.add(bottomLabel);
        
        rightPanel.add(this.leftMarginSpacingField);
        rightPanel.add(this.createVerticalRigidArea(5));
        rightPanel.add(this.topMarginSpacingField);
        rightPanel.add(this.createVerticalRigidArea(5));
        rightPanel.add(this.rightMarginSpacingField);
        rightPanel.add(this.createVerticalRigidArea(5));
        rightPanel.add(this.bottomMarginSpacingField);
        
        spacing.add(leftPanel);
        spacing.add(this.createHorizontalRigidArea(40));
        spacing.add(rightPanel);
        this.createCompoundBorder(spacing,
            "",
            6,
            6,
            5,
            5);
        
        leftPanel.setAlignmentY(TOP_ALIGNMENT);
        rightPanel.setAlignmentY(TOP_ALIGNMENT);
        spacing.setAlignmentX(LEFT_ALIGNMENT);
        
        marginSpacing.add(this.constantMarginSpacing);
        marginSpacing.add(this.createVerticalRigidArea(5));
        marginSpacing.add(this.proportionalMarginSpacing);
        marginSpacing.add(this.createVerticalRigidArea(5));
        marginSpacing.add(spacing);
        
        // Add the titled borders
        
        this.createCompoundBorder(marginSpacing,
            "Margin_Spacing",
            0,
            6,
            6,
            6);
        
        return (marginSpacing);
    }
    
    
    /**
     * This method creates the Nested View Spacing panel of this tab.
     */
    protected JPanel makeNestedViewSpacingPanel()
    {
        JPanel nestedViewSpacing = new JPanel();
        
        // Create the radio buttons
        
        this.proportionalNestedViewSpacing = this.createRadioButton(
            "Proportional_Spacing",
            PROPORTIONAL_NESTED_VIEW_SPACING);
        this.constantNestedViewSpacing = this.createRadioButton(
            "Constant_Spacing",
            CONSTANT_NESTED_VIEW_SPACING);
        
        // Add the radio buttons to a group
        
        ButtonGroup spacingGroup = new ButtonGroup();
        spacingGroup.add(this.proportionalNestedViewSpacing);
        spacingGroup.add(this.constantNestedViewSpacing);
        
        // Layout the Nested View Spacing panel.
        
        nestedViewSpacing.setLayout(
            new BoxLayout(nestedViewSpacing, BoxLayout.Y_AXIS));
        
        this.proportionalNestedViewSpacing.setAlignmentX(LEFT_ALIGNMENT);
        this.constantNestedViewSpacing.setAlignmentX(LEFT_ALIGNMENT);
        
        // Create spacing Panel and its components
        JPanel spacing = new JPanel();
        
        
        this.leftNestedViewSpacingField = this.createDoubleField(4, 0, 9999);
        this.topNestedViewSpacingField = this.createDoubleField(4, 0, 9999);
        this.rightNestedViewSpacingField = this.createDoubleField(4, 0, 9999);
        this.bottomNestedViewSpacingField = this.createDoubleField(4, 0, 9999);
        
        JLabel leftLabel = this.createLabel("Left",
            null, leftNestedViewSpacingField);
        JLabel topLabel = this.createLabel("Top",
            null, topNestedViewSpacingField);
        JLabel rightLabel = this.createLabel("Right",
            null, rightNestedViewSpacingField);
        JLabel bottomLabel = this.createLabel("Bottom",
            null, bottomNestedViewSpacingField);
        
        spacing.setLayout(new BoxLayout(spacing, BoxLayout.X_AXIS));
        JPanel leftPanel = new JPanel();
        JPanel rightPanel = new JPanel();
        
        // Layout the components of the spacing panel
        
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        
        leftPanel.add(leftLabel);
        leftPanel.add(this.createVerticalRigidArea(9));
        leftPanel.add(topLabel);
        leftPanel.add(this.createVerticalRigidArea(9));
        leftPanel.add(rightLabel);
        leftPanel.add(this.createVerticalRigidArea(9));
        leftPanel.add(bottomLabel);
        
        rightPanel.add(this.leftNestedViewSpacingField);
        rightPanel.add(this.createVerticalRigidArea(5));
        rightPanel.add(this.topNestedViewSpacingField);
        rightPanel.add(this.createVerticalRigidArea(5));
        rightPanel.add(this.rightNestedViewSpacingField);
        rightPanel.add(this.createVerticalRigidArea(5));
        rightPanel.add(this.bottomNestedViewSpacingField);
        
        spacing.add(leftPanel);
        spacing.add(this.createHorizontalRigidArea(40));
        spacing.add(rightPanel);
        this.createCompoundBorder(spacing,
            "",
            6,
            6,
            5,
            5);
        
        leftPanel.setAlignmentY(TOP_ALIGNMENT);
        rightPanel.setAlignmentY(TOP_ALIGNMENT);
        spacing.setAlignmentX(LEFT_ALIGNMENT);
        
        nestedViewSpacing.add(this.constantNestedViewSpacing);
        nestedViewSpacing.add(this.createVerticalRigidArea(5));
        nestedViewSpacing.add(this.proportionalNestedViewSpacing);
        nestedViewSpacing.add(this.createVerticalRigidArea(5));
        nestedViewSpacing.add(spacing);
        
        // Add the titled borders
        
        this.createCompoundBorder(nestedViewSpacing,
            "Nested_View_Spacing",
            0,
            6,
            6,
            6);
        
        return (nestedViewSpacing);
    }
    
    
    /**
     * This method creates the Intergraph Edge Spacing panel of this tab.
     */
    protected JPanel makeIntergraphEdgeSpacingPanel()
    {
        // create the panel
        
        JPanel intergraphEdgeSpacing = new JPanel();
        
        intergraphEdgeSpacing.setLayout(
            new BoxLayout(intergraphEdgeSpacing, BoxLayout.X_AXIS));
        intergraphEdgeSpacing.setAlignmentX(LEFT_ALIGNMENT);
        
        // create the subpanels of the panel
        
        JPanel leftPanel = new JPanel();
        JPanel rightPanel = new JPanel();
        
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        
        leftPanel.setAlignmentY(TOP_ALIGNMENT);
        rightPanel.setAlignmentY(TOP_ALIGNMENT);
        
        // create the components of the panel
        
        
        this.intergraphEdgeHorizontalSpacingField = this.createDoubleField(4, 0, 9999);
        this.intergraphEdgeVerticalSpacingField = this.createDoubleField(4, 0, 9999);
        
        JLabel horizontalSpacing = this.createLabel("Horizontal_Spacing",
            "Horizontal_Spacing", intergraphEdgeHorizontalSpacingField);
        JLabel verticalSpacing = this.createLabel("Vertical_Spacing",
            null, intergraphEdgeVerticalSpacingField);
        
        // add the components to the subpanels
        leftPanel.add(horizontalSpacing);
        leftPanel.add(this.createVerticalRigidArea(9));
        leftPanel.add(verticalSpacing);
        
        rightPanel.add(this.intergraphEdgeHorizontalSpacingField);
        rightPanel.add(this.createVerticalRigidArea(5));
        rightPanel.add(this.intergraphEdgeVerticalSpacingField);
        
        // add the subpanels to the panel
        
        intergraphEdgeSpacing.add(leftPanel);
        intergraphEdgeSpacing.add(this.createHorizontalRigidArea(20));
        intergraphEdgeSpacing.add(rightPanel);
        
        this.createCompoundBorder(intergraphEdgeSpacing,
            "Intergraph_Edge_Spacing",
            0,
            6,
            5,
            5);
        
        return intergraphEdgeSpacing;
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
     * @param mnemonicKey the for which the mnemonic will be obtained from the resource bundle
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
        String label = "";
        if (labelKey != null && labelKey.trim().length() > 0)
        {
            label = this.tsBundle.getStringSafely(labelKey);
        }
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
        
        // set nmemonics for radio buttons (only 1st one in a component group)
        proportionalMarginSpacing.setMnemonic(AccessiblityUtils.getMnemonic("Proportional_Spacing"));
        proportionalNestedViewSpacing.setMnemonic(AccessiblityUtils.getMnemonic("Proportional_Spacing_2"));
        
        // set font to use the default font
        constantMarginSpacing.setFont(defaultFont);
        constantNestedViewSpacing.setFont(defaultFont);
        proportionalMarginSpacing.setFont(defaultFont);
        proportionalNestedViewSpacing.setFont(defaultFont);
        
        // set accessibility name and descritpion
        AccessiblityUtils.setAccessibleProperties(constantMarginSpacing,
            null, "Constant_Spacing");
        AccessiblityUtils.setAccessibleProperties(proportionalMarginSpacing,
            null, "Proportional_Spacing");
        AccessiblityUtils.setAccessibleProperties(constantNestedViewSpacing,
            null, "Constant_Spacing");
        AccessiblityUtils.setAccessibleProperties(proportionalNestedViewSpacing,
            null, "Proportional_Spacing");
        
        AccessiblityUtils.setAccessibleProperties(leftMarginSpacingField,
            null, "Left");
        AccessiblityUtils.setAccessibleProperties(topMarginSpacingField,
            null, "Top");
        AccessiblityUtils.setAccessibleProperties(rightMarginSpacingField,
            null, "Right");
        AccessiblityUtils.setAccessibleProperties(bottomMarginSpacingField,
            null, "Bottom");
        
        AccessiblityUtils.setAccessibleProperties(leftNestedViewSpacingField,
            null, "Left");
        AccessiblityUtils.setAccessibleProperties(topNestedViewSpacingField,
            null, "Top");
        AccessiblityUtils.setAccessibleProperties(rightNestedViewSpacingField,
            null, "Right");
        AccessiblityUtils.setAccessibleProperties(bottomNestedViewSpacingField,
            null, "Bottom");
        
        AccessiblityUtils.setAccessibleProperties(intergraphEdgeHorizontalSpacingField,
            null, "Horizontal_Spacing");
        AccessiblityUtils.setAccessibleProperties(intergraphEdgeVerticalSpacingField,
            null, "Vertical_Spacing");
        
        this.getAccessibleContext().setAccessibleDescription(this.getAccessibleContext().getAccessibleName());
    }
}
