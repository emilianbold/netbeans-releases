/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package com.tomsawyer.editor.service.layout.jlayout;

import com.tomsawyer.editor.TSEResourceBundleWrapper;
import java.awt.Component;
import java.awt.Font;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import com.tomsawyer.service.TSServiceInputData;
import com.tomsawyer.editor.TSEGraph;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


public class TSERoutingTabExt extends TSERoutingTab
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
    public TSERoutingTabExt(TSEGraph graph,
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
        horizontalSpacingBetweenNodesField = createDoubleField(4, 0, 9999);
        horizontalSpacingBetweenEdgesField = createDoubleField(4, 0, 9999);
        
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
        verticalSpacingBetweenNodesField = createDoubleField(4, 0, 9999);
        verticalSpacingBetweenEdgesField = createDoubleField(4, 0, 9999);
        
        JLabel spacingBetweenNodesLabel = this.createLabel(
            "Spacing_Between_Nodes",
            "Vertical_Spacing_Between_Nodes",
            verticalSpacingBetweenNodesField);
        
        JLabel spacingBetweenEdgesLabel = this.createLabel(
            "Spacing_Between_Edges",
            null,
            verticalSpacingBetweenEdgesField);
        
        // add the components to the subpanel
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
    
    //override the super method to to do nothing inorder to fix the component display issue with different fontsizes
    protected int normalizeComponentDimensions(List compList, boolean xDim, boolean yDim)
    {
        return 0;
    }
    
    public void setA11yFeatures()
    {
        Font defaultFont = this.getFont();
        
        // set nmemonics for checkboxes (only the 1st one in a component group)
        fixNodeSizesCheckBox.setMnemonic(AccessiblityUtils.getMnemonic("Fix_Node_Sizes"));
        
        // set font to use the default font
        fixNodeSizesCheckBox.setFont(defaultFont);
        fixNodePositionsCheckBox.setFont(defaultFont);
        
        // set accessibility name and descritpion
        AccessiblityUtils.setAccessibleProperties(horizontalSpacingBetweenNodesField,
            null, "Horizontal_Spacing_Between_Nodes");
        AccessiblityUtils.setAccessibleProperties(verticalSpacingBetweenNodesField,
            null, "Vertical_Spacing_Between_Nodes");
        AccessiblityUtils.setAccessibleProperties(horizontalSpacingBetweenEdgesField,
            null, "Horizontal_Spacing_Between_Edges");
        AccessiblityUtils.setAccessibleProperties(verticalSpacingBetweenEdgesField,
            null, "Vertical_Spacing_Between_Edges");
        AccessiblityUtils.setAccessibleProperties(fixNodeSizesCheckBox,
            null, "Fix_Node_Sizes");
        AccessiblityUtils.setAccessibleProperties(fixNodePositionsCheckBox,
            null, "Fix_Node_Positions");
        
        this.getAccessibleContext().setAccessibleDescription(this.getAccessibleContext().getAccessibleName());
    }
}
