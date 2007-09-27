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
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import com.tomsawyer.service.TSServiceInputData;
import com.tomsawyer.editor.TSEGraph;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


public class TSESymmetricTabExt extends TSESymmetricTab
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
    public TSESymmetricTabExt(TSEGraph graph,
        TSServiceInputData inputData,
        JDialog dialog)
    {
        super(graph, inputData, dialog);
        setA11yFeatures();
    }
    
    /**
     * This method creates the Spacing Options panel of this tab.
     */
    protected JPanel makeSpacingOptionsPanel()
    {
        JPanel panel = this.createBoxLayoutPanel(BoxLayout.X_AXIS);
        
        // Create the components
        this.nodeSpacingField = this.createDoubleField(4, 0, 1000);
        JLabel nodeSpacingLabel = this.createLabel("Node_Spacing",
            "Node_Spacing", nodeSpacingField);
        
        // Layout the components
        JPanel labels = this.createBoxLayoutPanel(BoxLayout.Y_AXIS);
        labels.add(nodeSpacingLabel);
        
        JPanel fields = this.createBoxLayoutPanel(BoxLayout.Y_AXIS);
        this.nodeSpacingField.setAlignmentX(RIGHT_ALIGNMENT);
        fields.add(this.nodeSpacingField);
        
        panel.add(labels);
        panel.add(Box.createRigidArea(new Dimension(20, 0)));
        panel.add(fields);
        
        // Create a border
        this.createCompoundBorder(
            panel,
            "Spacing_Options",
            0,
            5,
            5,
            5);
        
        return (panel);
    }
    
    /**
     * This method creates the Layout Quality Options panel of this tab.
     */
    protected JPanel makeLayoutQualityPanel()
    {
        JPanel wrapperPanel = this.createBoxLayoutPanel(BoxLayout.X_AXIS);
        JPanel panel = this.createBoxLayoutPanel(BoxLayout.Y_AXIS);
        
        // create the components
        this.qualityDraftRadioButton =
            this.createRadioButton("Draft");
        this.qualityDefaultRadioButton =
            this.createRadioButton("Default");
        this.qualityProofRadioButton =
            this.createRadioButton("Proof");
        
        ButtonGroup buttonGroup = new ButtonGroup();
        
        buttonGroup.add(this.qualityDraftRadioButton);
        buttonGroup.add(this.qualityDefaultRadioButton);
        buttonGroup.add(this.qualityProofRadioButton);
        
        // add the components to the panel
        
        panel.add(this.qualityDraftRadioButton);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(this.qualityDefaultRadioButton);
        panel.add(Box.createRigidArea(new Dimension(0,5)));
        panel.add(this.qualityProofRadioButton);
        
        wrapperPanel.add(panel);
        wrapperPanel.add(Box.createRigidArea(new Dimension(30,0)));
        
        
        this.createCompoundBorder(
            wrapperPanel,
            "Layout_Quality",
            0,
            5,
            5,
            5);
        
        return wrapperPanel;
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
        // set nmemonics checkboxes
        qualityDraftRadioButton.setMnemonic(AccessiblityUtils.getMnemonic("Draft"));
        
        // set font to use the default font
        qualityDraftRadioButton.setFont(defaultFont);
        qualityDefaultRadioButton.setFont(defaultFont);
        qualityProofRadioButton.setFont(defaultFont);
        
        // set accessibility name and descritpion
        AccessiblityUtils.setAccessibleProperties(nodeSpacingField,
            null, "Node_Spacing");
        AccessiblityUtils.setAccessibleProperties(qualityDraftRadioButton,
            null, "Draft");
        AccessiblityUtils.setAccessibleProperties(qualityDefaultRadioButton,
            null, "Default");
        AccessiblityUtils.setAccessibleProperties(qualityProofRadioButton,
            null, "Proof");
        
        this.getAccessibleContext().setAccessibleDescription(this.getAccessibleContext().getAccessibleName());
    }
}
