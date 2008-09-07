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

/*
 * TemplateEntry.java
 *
 * Created on June 19, 2007, 10:18 AM
 */

package org.netbeans.modules.uml.codegen.ui.customizer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import javax.accessibility.AccessibleContext;
import javax.swing.JTextArea;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.DomainObject;
import org.openide.util.NbBundle;

/**
 *
 * @author  treyspiva
 */
public class TemplateEntry extends javax.swing.JPanel
{
    private DomainObject domainObject = null;
    private DescriptionTextArea templateDescription = new DescriptionTextArea();
    
    public TemplateEntry(
        String familyName, DomainObject domain, boolean isChecked)
    {
        initComponents();
        setLabelFont();
                
        descContainer.add(templateDescription, BorderLayout.CENTER);
        domainObject = domain;
        setTemplateName(domainObject.getName());
        setTemplateDescription(domainObject.getDescription());
        setModelElement(domainObject.getModelElement());
        setStereroType(domainObject.getStereotype());
        
        templateNameCheckBox.setSelected(isChecked);
        templateNameCheckBox.putClientProperty("familyName", familyName); // NOI18N
        
    }
    
    private void setLabelFont()
    {
        String type = getFont().getFamily();
        int size = getFont().getSize()-1;

        templateDescription.setFont(new Font(type, Font.ITALIC, size));
        elementTypeLabel.setFont(new Font(type, Font.ITALIC, size));
        elementTypeValue.setFont(new Font(type, Font.ITALIC, size)); // NOI18N
        stereotypeLabel.setFont(new Font(type, Font.ITALIC, size)); // NOI18N
        stereotypeValue.setFont(new Font(type, Font.ITALIC, size)); // NOI18N
    }

    public void setTemplateName(String name)
    {
        templateNameCheckBox.setText(name);
        
        String description = NbBundle.getMessage(TemplateEntry.class, "LBL_TemplateName", name);
        templateNameCheckBox.getAccessibleContext().setAccessibleDescription(description);
    }

    public void setTemplateDescription(String desc)
    {
        templateDescription.setText(desc);
        Dimension preferredSize = templateDescription.getPreferredSize();
        templateDescription.setSize(preferredSize);
    }

    public void setStereroType(String value)
    {
        if ((value == null) || (value.length() == 0))
            value = org.openide.util.NbBundle.getMessage(TemplateEntry.class, "LBL_NA"); // NOI18N
        
        stereotypeValue.setText(value);
    }

    public void setModelElement(String value)
    {
        elementTypeValue.setText(value);
    }

    
    public javax.swing.JCheckBox getTemplateNameField()
    {
        return templateNameCheckBox;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        templateNameCheckBox = new javax.swing.JCheckBox();
        elementTypeLabel = new javax.swing.JLabel();
        elementTypeValue = new javax.swing.JLabel();
        stereotypeLabel = new javax.swing.JLabel();
        stereotypeValue = new javax.swing.JLabel();
        descContainer = new javax.swing.JPanel();

        setOpaque(false);

        templateNameCheckBox.setText("<domainName>");
        templateNameCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        elementTypeLabel.setLabelFor(elementTypeValue);
        elementTypeLabel.setText(org.openide.util.NbBundle.getMessage(TemplateEntry.class, "LBL_ElementType")); // NOI18N

        elementTypeValue.setText("<elementType>");

        stereotypeLabel.setLabelFor(stereotypeValue);
        stereotypeLabel.setText(org.openide.util.NbBundle.getMessage(TemplateEntry.class, "LBL_Stereotype")); // NOI18N

        stereotypeValue.setText(org.openide.util.NbBundle.getMessage(TemplateEntry.class, "LBL_NA")); // NOI18N

        descContainer.setBackground(new java.awt.Color(255, 255, 0));
        descContainer.setEnabled(false);
        descContainer.setOpaque(false);
        descContainer.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(templateNameCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(39, 39, 39)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(descContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(elementTypeLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(elementTypeValue, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(18, 18, 18)
                                .add(stereotypeLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(stereotypeValue, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(templateNameCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(descContainer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(elementTypeLabel)
                    .add(elementTypeValue)
                    .add(stereotypeValue)
                    .add(stereotypeLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        templateNameCheckBox.getAccessibleContext().setAccessibleDescription("Template Category Name ");
        descContainer.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TemplateEntry.class, "LBL_TEMPLATE_DESCRIPTOIN_Name")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel descContainer;
    private javax.swing.JLabel elementTypeLabel;
    private javax.swing.JLabel elementTypeValue;
    private javax.swing.JLabel stereotypeLabel;
    private javax.swing.JLabel stereotypeValue;
    private javax.swing.JCheckBox templateNameCheckBox;
    // End of variables declaration//GEN-END:variables
    
    /**
     * This JTextArea is used to make sure that all rows are visible.  If I use 
     * the JTextArea in the group layout, then the text area will never grow to 
     * fit all of the rows of text.  If I use something like a Gridbag layout 
     * then the text area is too big by default.  So, I have created a text area
     * that makes sure the bounds of the text area will fit all of the rows of
     * text when word wrapping is turned on.
     */
    public class DescriptionTextArea extends JTextArea
    {

        public DescriptionTextArea()
        {
            setOpaque(false);
            setLineWrap(true);
            setWrapStyleWord(true);
            
            AccessibleContext context = getAccessibleContext();
            context.setAccessibleName(NbBundle.getMessage(TemplateEntry.class, "LBL_TEMPLATE_DESCRIPTOIN_Name")); // NOI18N
            setFocusable(false);
        }

        @Override
        public void setText(String t)
        {
            super.setText(t);
            
            AccessibleContext context = getAccessibleContext();
            context.setAccessibleDescription(t);
        }
        
        
        @Override
        public void setBounds(int x, int y, int w, int h)
        {
            super.setBounds(x, y, w, h);
            
            int rows = getRows();
            int height = getRowHeight();
            
            if(h == rows * height)
            {
                setBounds(x, y, w, rows * height);
            }
        }

        @Override
        public void setBounds(Rectangle bounds)
        {
            super.setBounds(bounds);
            
            int rows = getRows();
            int height = getRowHeight();
            
            if(bounds.height == rows * height)
            {
                setBounds(bounds.x, bounds.y, bounds.width, rows * height);
            }
        }
        
    }
    
}
