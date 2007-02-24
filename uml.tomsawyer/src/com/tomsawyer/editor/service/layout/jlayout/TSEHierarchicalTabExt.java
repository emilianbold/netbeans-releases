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
import java.util.Iterator;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JDialog;
import com.tomsawyer.service.TSServiceInputData;
import com.tomsawyer.editor.TSEGraph;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


public class TSEHierarchicalTabExt extends TSEHierarchicalTab
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
    public TSEHierarchicalTabExt(TSEGraph graph,
        TSServiceInputData inputData,
        JDialog dialog)
    {
        super(graph, inputData, dialog);
        setA11yFeatures();
    }
    

    /**
     * This method creates the Orientation panel of this tab.
     */
    protected JPanel makeOrientationPanel()
    {
        JPanel panel = this.createBoxLayoutPanel(BoxLayout.X_AXIS);
        
        // create the subpanels
        JPanel orientation = this.createBoxLayoutPanel(BoxLayout.Y_AXIS);
        
        // Create the components
        this.orientationTopToBottom =
            this.createRadioButton("Top_To_Bottom", ORIENTATION_TOP_TO_BOTTOM);
        this.orientationLeftToRight =
            this.createRadioButton("Left_To_Right", ORIENTATION_LEFT_TO_RIGHT);
        this.orientationBottomToTop =
            this.createRadioButton("Bottom_To_Top", ORIENTATION_BOTTOM_TO_TOP);
        this.orientationRightToLeft =
            this.createRadioButton("Right_To_Left", ORIENTATION_RIGHT_TO_LEFT);
        
        
        // Add the buttons to a group
        
        ButtonGroup orientationGroup = new ButtonGroup();
        orientationGroup.add(this.orientationTopToBottom);
        orientationGroup.add(this.orientationLeftToRight);
        orientationGroup.add(this.orientationBottomToTop);
        orientationGroup.add(this.orientationRightToLeft);
        
        // Layout the components
        orientation.add(this.orientationTopToBottom);
        orientation.add(this.createVerticalRigidArea(5));
        orientation.add(this.orientationLeftToRight);
        orientation.add(this.createVerticalRigidArea(5));
        orientation.add(this.orientationBottomToTop);
        orientation.add(this.createVerticalRigidArea(5));
        orientation.add(this.orientationRightToLeft);
        
        panel.add(orientation);
        panel.add(Box.createRigidArea(new Dimension (5,0)));
 
        // Create a border
        this.createCompoundBorder(panel,
            "Orientation",
            0,
            5,
            5,
            5);
        
//        System.out.println("orientation panel pref size="+panel.getPreferredSize().toString() +
//            " mim Size="+ panel.getMinimumSize().toString() +
//            " max size="+ panel.getMaximumSize().toString());
        
        return panel;
    }
    
    /**
     * This method creates the Level Alignment panel of this tab.
     */
    protected JPanel makeLevelAlignmentPanel()
    {
        
        JPanel panel = this.createBoxLayoutPanel(BoxLayout.X_AXIS);
        JPanel levelAlignment = this.createBoxLayoutPanel(BoxLayout.Y_AXIS);
        
        // Create the components
        
        this.alignmentTop = this.createRadioButton("Top");
        this.alignmentCenter = this.createRadioButton("Center");
        this.alignmentBottom = this.createRadioButton("Bottom");
        
        ButtonGroup levelAlignmentGroup = new ButtonGroup();
        levelAlignmentGroup.add(this.alignmentTop);
        levelAlignmentGroup.add(this.alignmentCenter);
        levelAlignmentGroup.add(this.alignmentBottom);
        
         // Layout the components
        levelAlignment.add(this.alignmentTop);
        levelAlignment.add(Box.createRigidArea(new Dimension (0,5)));
        levelAlignment.add(this.alignmentCenter);
        levelAlignment.add(Box.createRigidArea(new Dimension (0,5)));
        levelAlignment.add(this.alignmentBottom);
        levelAlignment.add(Box.createRigidArea(new Dimension (0,5)));
        
        panel.add(levelAlignment);
        panel.add(Box.createRigidArea(new Dimension (5,0)));
        
        // Create a border
        this.createCompoundBorder(
            panel,
            "Level_Alignment",
            0,
            5,
            5,
            5);
        
        return (panel);
    }
    
    /**
     * This method creates the Horizontal Spacing panel of this tab.
     */
    protected JPanel makeHorizontalSpacingPanel()
    {
        JPanel panel = this.createBoxLayoutPanel(BoxLayout.X_AXIS);
        
        // create the subpanels
        
        JPanel leftPanel = this.createBoxLayoutPanel(BoxLayout.Y_AXIS);
        JPanel rightPanel = this.createBoxLayoutPanel(BoxLayout.Y_AXIS);
        
        // Create the components
        
        this.horizontalSpacingBetweenNodesField = this.createDoubleField(4, 0, 9999);
        this.horizontalSpacingBetweenEdgesField = this.createDoubleField(4, 0, 9999);
        
        JLabel spacingBetweenNodesLabel = this.createLabel(
            "Spacing_Between_Nodes",
            "Horizontal_Spacing_Between_Nodes_1", horizontalSpacingBetweenNodesField);
        
        JLabel spacingBetweenEdgesLabel = this.createLabel(
            "Spacing_Between_Edges",
            null, horizontalSpacingBetweenEdgesField);
        
        
        // add the components to the subpanel
        leftPanel.add(spacingBetweenNodesLabel);
        leftPanel.add(this.createVerticalRigidArea(9));
        leftPanel.add(spacingBetweenEdgesLabel);
        
        rightPanel.add(this.horizontalSpacingBetweenNodesField);
        rightPanel.add(this.createVerticalRigidArea(5));
        rightPanel.add(this.horizontalSpacingBetweenEdgesField);
        
        // add the subpanels to the panel
        
        panel.add(leftPanel);
        panel.add(this.createHorizontalRigidArea(15));
        panel.add(rightPanel);
        
        // Create a border
        this.createCompoundBorder(panel,
            "Horizontal_Spacing",
            0,
            5,
            5,
            5);
        
        return panel;
    }
    
    /**
     * This method creates the Vertical Spacing panel of this tab.
     */
    protected JPanel makeVerticalSpacingPanel()
    {
        JPanel panel = this.createBoxLayoutPanel(BoxLayout.X_AXIS);
        
        // create the subpanels
        
        JPanel leftPanel = this.createBoxLayoutPanel(BoxLayout.Y_AXIS);
        JPanel rightPanel = this.createBoxLayoutPanel(BoxLayout.Y_AXIS);
        
        // Create the components
        this.verticalSpacingBetweenNodesField = this.createDoubleField(4, 0, 9999);
        this.verticalSpacingBetweenEdgesField = this.createDoubleField(4, 0, 9999);
        
        JLabel spacingBetweenNodesLabel = this.createLabel(
            "Spacing_Between_Nodes",
            "Vertical_Spacing_Between_Nodes_2", verticalSpacingBetweenNodesField);
        JLabel spacingBetweenEdgesLabel = this.createLabel(
            "Spacing_Between_Edges",
            null, verticalSpacingBetweenEdgesField);
        
        // add the components to the subpanel
        leftPanel.add(spacingBetweenNodesLabel);
        leftPanel.add(this.createVerticalRigidArea(9));
        leftPanel.add(spacingBetweenEdgesLabel);
        
        rightPanel.add(this.verticalSpacingBetweenNodesField);
        rightPanel.add(this.createVerticalRigidArea(5));
        rightPanel.add(this.verticalSpacingBetweenEdgesField);
        
        // add the subpanels to the panel
        
        panel.add(leftPanel);
        panel.add(this.createHorizontalRigidArea(15));
        panel.add(rightPanel);
        
        // Create a border
        this.createCompoundBorder(panel,
            "Vertical_Spacing",
            0,
            5,
            5,
            5);
        
        return panel;
    }
    
    /**
     * This method creates the Polyline Routing panel of this tab.
     */
    protected JPanel makePolylineRoutingPanel()
    {
        JPanel panel = this.createBoxLayoutPanel(BoxLayout.X_AXIS);
        
        this.spacingBetweenBendsField = this.createDoubleField(4, 0, 9999);
        spacingBetweenBendsField.setAlignmentY(TOP_ALIGNMENT);
        JLabel spacingBetweenBendsLabel = this.createLabel(
            "Spacing_Between_Bends",
            "Spacing_Between_Bends", spacingBetweenBendsField);
        
        panel.add(spacingBetweenBendsLabel);
        panel.add(createHorizontalRigidArea(15));
        panel.add(spacingBetweenBendsField);
        
        this.createCompoundBorder(panel,
            "Polyline_Routing",
            0,
            5,
            5,
            5);
        
        return panel;
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
        String label = this.tsBundle.getStringSafely(labelKey);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(label);
        titledBorder.setTitleColor(labelColor);
        panel.setBorder(
            BorderFactory.createCompoundBorder(
            titledBorder,
            BorderFactory.createEmptyBorder(top, left, bottom, right)));
    }
    
    
    // Override the super method to fix the component display issue with different fontsizes
    // Fixed issue #84056
    protected int normalizeComponentDimensions(List components, boolean xDim, boolean yDim)
    {
        int width = -1;
        return width;
    }
    
    public void setA11yFeatures()
    {
        Font defaultFont = this.getFont();
        // set nmemonics checkboxes
        orientationBottomToTop.setMnemonic(AccessiblityUtils.getMnemonic("Bottom_To_Top"));
        alignmentTop.setMnemonic(AccessiblityUtils.getMnemonic("Top"));
        qualityDraftRadioButton.setMnemonic(AccessiblityUtils.getMnemonic("Draft"));
        routingOrthogonalRadioButton.setMnemonic(AccessiblityUtils.getMnemonic("Orthogonal"));
        variableLevelSpacing.setMnemonic(AccessiblityUtils.getMnemonic("Variable_Level_Spacing"));
        
        // set font to use the default font
        orientationLeftToRight.setFont(defaultFont);
        orientationBottomToTop.setFont(defaultFont);
        orientationRightToLeft.setFont(defaultFont);
        orientationTopToBottom.setFont(defaultFont);
        
        alignmentTop.setFont(defaultFont);
        alignmentCenter.setFont(defaultFont);
        alignmentBottom.setFont(defaultFont);
        
        qualityDraftRadioButton.setFont(defaultFont);
        qualityDefaultRadioButton.setFont(defaultFont);
        qualityProofRadioButton.setFont(defaultFont);
        
        routingOrthogonalRadioButton.setFont(defaultFont);
        routingPolylineRadioButton.setFont(defaultFont);
        
        fixNodeSizesCheckBox.setFont(defaultFont);
        variableLevelSpacing.setFont(defaultFont);
        undirected.setFont(defaultFont);
        
        
        // set accessibility name and descritpion
        AccessiblityUtils.setAccessibleProperties(orientationLeftToRight,
            null, "Left_To_Right");
        AccessiblityUtils.setAccessibleProperties(orientationBottomToTop,
            null, "Bottom_To_Top");
        AccessiblityUtils.setAccessibleProperties(orientationRightToLeft,
            null, "Right_To_Left");
        AccessiblityUtils.setAccessibleProperties(orientationTopToBottom,
            null, "Top_To_Bottom");
        
        AccessiblityUtils.setAccessibleProperties(alignmentTop,
            null, "Top");
        AccessiblityUtils.setAccessibleProperties(alignmentCenter,
            null, "Center");
        AccessiblityUtils.setAccessibleProperties(alignmentBottom,
            null, "Bottom");
        
        AccessiblityUtils.setAccessibleProperties(qualityDraftRadioButton,
            null, "Draft");
        AccessiblityUtils.setAccessibleProperties(qualityDefaultRadioButton,
            null, "Default");
        AccessiblityUtils.setAccessibleProperties(qualityProofRadioButton,
            null, "Proof");
        
        AccessiblityUtils.setAccessibleProperties(routingOrthogonalRadioButton,
            null, "Orthogonal");
        AccessiblityUtils.setAccessibleProperties(routingPolylineRadioButton,
            null, "Polyline");
        
        
        AccessiblityUtils.setAccessibleProperties(fixNodeSizesCheckBox,
            null, "Fix_Node_Sizes");
        AccessiblityUtils.setAccessibleProperties(variableLevelSpacing,
            null, "Variable_Level_Spacing");
        AccessiblityUtils.setAccessibleProperties(undirected,
            null, "Undirected_Layout");
        
        AccessiblityUtils.setAccessibleProperties(horizontalSpacingBetweenEdgesField,
            null, "Horizontal_Spacing_Between_Edges");
        AccessiblityUtils.setAccessibleProperties(horizontalSpacingBetweenNodesField,
            null, "Horizontal_Spacing_Between_Nodes");
        AccessiblityUtils.setAccessibleProperties(verticalSpacingBetweenEdgesField,
            null, "Vertical_Spacing_Between_Edges");
        AccessiblityUtils.setAccessibleProperties(verticalSpacingBetweenNodesField,
            null, "Vertical_Spacing_Between_Nodes");
        AccessiblityUtils.setAccessibleProperties(spacingBetweenBendsField,
            null, "Spacing_Between_Bends");
        
        this.getAccessibleContext().setAccessibleDescription(this.getAccessibleContext().getAccessibleName());
    }
}
