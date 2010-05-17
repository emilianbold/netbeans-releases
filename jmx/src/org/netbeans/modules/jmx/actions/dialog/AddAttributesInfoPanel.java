/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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

package org.netbeans.modules.jmx.actions.dialog;

import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.JButton;
import org.netbeans.modules.jmx.MBeanAttribute;
import org.netbeans.modules.jmx.actions.AddAttrAction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Class responsible for the warning message shown when you use Add Attributes...
 * popup action in the contextual management menu and there is already an 
 * existing implementation of a specified attribute.
 * @author  tl156378
 */
public class AddAttributesInfoPanel extends javax.swing.JPanel {
    
    private ResourceBundle bundle;
    private String title;
    private JButton btnOK;
     
    /** 
     * Creates new form Panel.
     */
    public AddAttributesInfoPanel(String itfName, String mbeanClassName, MBeanAttribute[] attributes) {
        bundle = NbBundle.getBundle(AddAttributesInfoPanel.class);
        
        // init tags
        
        initComponents();
        
        //init labels
        StringBuffer methodsList = new StringBuffer();
        for (int i = 0; i < attributes.length; i ++) {
            if (attributes[i].getIsMethodExits())
                    methodsList.append(" - is" + attributes[i].getName() + "\n"); // NOI18N
            else if (attributes[i].getGetMethodExits())
                    methodsList.append(" - get" + attributes[i].getName() + "\n"); // NOI18N
            
            if (attributes[i].getSetMethodExits() && attributes[i].isWritable())
                    methodsList.append(" - set" + attributes[i].getName() + "\n"); // NOI18N
        }
        
        MessageFormat formAttribute = 
                new MessageFormat(bundle.getString("LBL_AttrMethodsAlreadyExist")); // NOI18N
        Object[] args = {mbeanClassName, mbeanClassName, mbeanClassName, methodsList.toString()};
        String msg = formAttribute.format(args);
        
        formAttribute = 
                new MessageFormat(bundle.getString("LBL_AddAttributesAction.Title")); // NOI18N
        Object[] args2 = {itfName};
        title = formAttribute.format(args2);
        
        infoTextArea.setText(msg);
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_PANEL"));// NOI18N
    }
    
    public boolean isAcceptable() {
        return true;
    }
    
    /**
     * Displays a configuration dialog and updates Register MBean options 
     * according to the user's settings.
     * @return <CODE>boolean</CODE> true only if user clicks on Ok button.
     */
    public boolean configure() {
        
        // create and display the dialog

        btnOK = new JButton(bundle.getString("LBL_OK")); // NOI18N
        btnOK.setEnabled(isAcceptable());
        btnOK.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_OK_DESCRIPTION")); // NOI18N
        
        Object returned = DialogDisplayer.getDefault().notify(
                new DialogDescriptor (
                        this,
                        title,
                        true,                       //modal
                        new Object[] {btnOK, DialogDescriptor.CANCEL_OPTION},
                        btnOK,                      //initial value
                        DialogDescriptor.DEFAULT_ALIGN,
                        new HelpCtx(AddAttrAction.class),
                        (ActionListener) null
                ));
        
        if (returned == btnOK) {
            return true;
        }
        return false;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        infoTextArea = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        infoTextArea.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        infoTextArea.setEditable(false);
        infoTextArea.setFont(new java.awt.Font("Arial", 0, 12));
        infoTextArea.setBorder(null);
        infoTextArea.setFocusable(false);
        infoTextArea.setName("infoTextArea");
        infoTextArea.setSelectionColor(javax.swing.UIManager.getDefaults().getColor("textText"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 11, 12);
        add(infoTextArea, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea infoTextArea;
    // End of variables declaration//GEN-END:variables
    
}
