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
import java.awt.Dimension;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


public class TSEOrthogonalTabExt extends TSEOrthogonalTab
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
    public TSEOrthogonalTabExt(TSEGraph graph,
        TSServiceInputData inputData,
        JDialog dialog)
    {
        super(graph, inputData, dialog);
        setA11yFeatures();
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
            "Horizontal_Spacing_Between_Nodes",
            horizontalSpacingBetweenNodesField);
        
        JLabel spacingBetweenEdgesLabel = this.createLabel(
            "Spacing_Between_Edges",
            null,
            horizontalSpacingBetweenEdgesField);
        
        // add the components to the subpanele
        leftPanel.add(spacingBetweenNodesLabel);
        leftPanel.add(this.createVerticalRigidArea(9));
        leftPanel.add(spacingBetweenEdgesLabel);
        
        rightPanel.add(this.horizontalSpacingBetweenNodesField);
        rightPanel.add(this.createVerticalRigidArea(5));
        rightPanel.add(this.horizontalSpacingBetweenEdgesField);
        
        // add the subpanels to the panel
        panel.add(leftPanel);
        panel.add(this.createHorizontalRigidArea(20));
        panel.add(rightPanel);
        
        // Create a border
        this.createCompoundBorder(panel,
            "Horizontal_Spacing",
            0,
            6,
            5,
            6);
        
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
            "Vertical_Spacing_Between_Nodes",
            verticalSpacingBetweenNodesField);
        
        JLabel spacingBetweenEdgesLabel = this.createLabel(
            "Spacing_Between_Edges",
            null,
            verticalSpacingBetweenEdgesField);
        
        
        // add the components to the subpanele
        
        leftPanel.add(spacingBetweenNodesLabel);
        leftPanel.add(this.createVerticalRigidArea(9));
        leftPanel.add(spacingBetweenEdgesLabel);
        
        rightPanel.add(this.verticalSpacingBetweenNodesField);
        rightPanel.add(this.createVerticalRigidArea(5));
        rightPanel.add(this.verticalSpacingBetweenEdgesField);
        
        // add the subpanels to the panel
        
        panel.add(leftPanel);
        panel.add(this.createHorizontalRigidArea(20));
        panel.add(rightPanel);
        
        // Create a border
        this.createCompoundBorder(panel,
            "Vertical_Spacing",
            0,
            6,
            5,
            6);
        
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
    // The method loops through all the components in the component list to get the largest width.
    // Then for each component that has smaller width, set its maximum size with the new width.
    // Note 1:  the code does not set the maximun size for the component that has the same width
    // as the maximum width.  
    // Note 2: the 2 boolean paramters are not used in this method.
    protected int normalizeComponentDimensions(List components, boolean xDim, boolean yDim)
    {
        int width = -1;
        if (components != null && components.size() > 0)
        {
            // get the max width or max height of the components
            Iterator compIterator = components.iterator();
            while (compIterator.hasNext())
            {
                JComponent cmp = (JComponent) compIterator.next();
                if ( cmp instanceof JPanel)
                {
                    JPanel comp = (JPanel) cmp;
                    width = (int) Math.max(
                        comp.getPreferredSize().getWidth(),
                        width);
                }
            }
            
            // set the preferred size of all the components to that of the largest component
            Dimension compDimension =  null;
            
            compIterator = components.iterator();
            
            while (compIterator.hasNext())
            {
                JComponent cmp = (JComponent) compIterator.next();
                if ( cmp instanceof JPanel)
                {
                    JPanel comp = (JPanel) cmp;
                    
                    if(comp.getPreferredSize().width != width)
                    {
                        compDimension =
                            new Dimension(
                            width,
                            (int) comp.getPreferredSize().getHeight());
                        //comp.setPreferredSize(compDimension);
                        //comp.setMinimumSize(compDimension);
                        comp.setMaximumSize(compDimension);
                    }
                    
//                    System.out.println("pref size="+comp.getPreferredSize().toString() +
//                        " mim Size="+ comp.getMinimumSize().toString() +
//                        " max size="+ comp.getMaximumSize().toString());
                }
            }
        }
        return width;
    }
    
    public void setA11yFeatures()
    {
        Font defaultFont = this.getFont();
        // set nmemonics (just one per group box)
        qualityDraftRadioButton.setMnemonic(AccessiblityUtils.getMnemonic("Draft"));
        aspectRatioAutomaticRadioButton.setMnemonic(AccessiblityUtils.getMnemonic("Automatic"));
        fixNodeSizesCheckBox.setMnemonic(AccessiblityUtils.getMnemonic("Fix_Node_Sizes"));
        
        // set font to use the default font
        qualityDraftRadioButton.setFont(defaultFont);
        qualityDefaultRadioButton.setFont(defaultFont);
        qualityProofRadioButton.setFont(defaultFont);
        
        aspectRatioAutomaticRadioButton.setFont(defaultFont);
        aspectRatioCustomRadioButton.setFont(defaultFont);
        aspectRatioDisabledRadioButton.setFont(defaultFont);
        
        fixNodeSizesCheckBox.setFont(defaultFont);
        
        // set accessibility name and descritpion
        AccessiblityUtils.setAccessibleProperties(horizontalSpacingBetweenNodesField,
            null, "Horizontal_Spacing_Between_Nodes");
        AccessiblityUtils.setAccessibleProperties(verticalSpacingBetweenNodesField,
            null, "Vertical_Spacing_Between_Nodes");
        AccessiblityUtils.setAccessibleProperties(horizontalSpacingBetweenEdgesField,
            null, "Horizontal_Spacing_Between_Edges");
        AccessiblityUtils.setAccessibleProperties(verticalSpacingBetweenEdgesField,
            null, "Vertical_Spacing_Between_Edges");
        
        AccessiblityUtils.setAccessibleProperties(qualityDraftRadioButton,
            null, "Draft");
        AccessiblityUtils.setAccessibleProperties(qualityDefaultRadioButton,
            null, "Default");
        AccessiblityUtils.setAccessibleProperties(qualityProofRadioButton,
            null, "Proof");
        
        AccessiblityUtils.setAccessibleProperties(aspectRatioAutomaticRadioButton,
            null, "Automatic");
        AccessiblityUtils.setAccessibleProperties(aspectRatioCustomRadioButton,
            null, "Aspect_Ratio_Custom");
        AccessiblityUtils.setAccessibleProperties(aspectRatioCustomField,
            "Aspect_Ratio_Custom", "Aspect_Ratio_Custom");
        AccessiblityUtils.setAccessibleProperties(aspectRatioDisabledRadioButton,
            null, "Disabled");
        
        AccessiblityUtils.setAccessibleProperties(fixNodeSizesCheckBox,
            null, "Fix_Node_Sizes");
        
        this.getAccessibleContext().setAccessibleDescription(this.getAccessibleContext().getAccessibleName());
    }
}
