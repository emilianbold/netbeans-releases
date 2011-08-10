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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.identity.server.manager.ui;

import java.awt.Color;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * This class is an extension of the DialogDescriptor for creating Edit
 * dialogs.
 *
 * Created on July 26, 2006, 10:51 PM
 *
 * @author ptliu
 */
public abstract class EditDialogDescriptor extends DialogDescriptor
        implements ChangeListener {
    public static final String STATUS_PREFIX = "Status:";      //NOI18N
    
    /** Creates a new instance of EditDialog */
    public EditDialogDescriptor(javax.swing.JPanel panel, String title,
            boolean add, JComponent[] components, HelpCtx helpCtx) {
        this(new InnerPanel(panel), title, add, components, helpCtx);
    }
    
    private EditDialogDescriptor(InnerPanel innerPanel, String title,
            boolean add, JComponent[] components, HelpCtx helpCtx) {
        super(innerPanel, getTitle(add, title), true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.BOTTOM_ALIGN,
                helpCtx,
                null);
        
        if (components != null) {
            DocListener listener = new DocListener(this);
            for (JComponent component : components) {
                if (component instanceof JTextField) {
                    ((JTextField) component).getDocument().addDocumentListener(listener);
                }
            }
        }
        
        checkValues();
    }
    
    private static String getTitle(boolean add, String title) {
        return (add ? NbBundle.getMessage(EditDialogDescriptor.class, "TTL_Add", title) :
            NbBundle.getMessage(EditDialogDescriptor.class, "TTL_Edit", title));
    }
    
    /** Calls validation of panel components, displays or removes the error message
     * Should be called from listeners listening to component changes.
     */
    public final void checkValues() {
        String errorMessage = validate();
        if (errorMessage==null) {
            setValid(true);
        } else {
            setValid(false);
        }
        javax.swing.JLabel errorLabel = ((InnerPanel)getMessage()).getErrorLabel();
        
        if (errorMessage != null) {
            if (errorMessage.startsWith(STATUS_PREFIX)) {
                errorMessage = errorMessage.substring(STATUS_PREFIX.length());
                errorLabel.setForeground(Color.BLACK);
            } else {
                errorLabel.setForeground(Color.RED);
            }
        }
        
        errorLabel.setText(errorMessage==null ? " " : errorMessage);    //NOI18N
    }
    
    /** Provides validation for panel components */
    protected abstract String validate();
    
    public void stateChanged(ChangeEvent e) {
        checkValues();
        
        //innerPanel.repaint();
    }
    
    public JLabel getErrorLabel() {
        return ((InnerPanel)getMessage()).getErrorLabel();
    }
    
    private static class InnerPanel extends javax.swing.JPanel {
        javax.swing.JLabel errorLabel;
        InnerPanel(javax.swing.JPanel panel) {
            /*
            super(new java.awt.BorderLayout());
            errorLabel = new javax.swing.JLabel(" ");        //NOI18N
            errorLabel.setBorder(new javax.swing.border.EmptyBorder(12,12,0,0));
            errorLabel.setForeground(Color.RED);
            add(panel, java.awt.BorderLayout.CENTER);
            add(errorLabel, java.awt.BorderLayout.SOUTH);
             */
            
            javax.swing.JSeparator separator = new javax.swing.JSeparator();
            errorLabel = new javax.swing.JLabel(" ");        //NOI18N
            errorLabel.setForeground(Color.RED);
            
            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(separator,javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addContainerGap())
                    .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(errorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addContainerGap())
                    );
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(errorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
                    );
        }
        
        void setErrorMessage(String message) {
            errorLabel.setText(message);
        }
        
        javax.swing.JLabel getErrorLabel() {
            return errorLabel;
        }
    }
    
    /** Useful DocumentListener class that can be added to the panel's text compoents */
    public static class DocListener implements javax.swing.event.DocumentListener {
        EditDialogDescriptor dialog;
        
        public DocListener(EditDialogDescriptor dialog) {
            this.dialog=dialog;
        }
        /**
         * Method from DocumentListener
         */
        public void changedUpdate(javax.swing.event.DocumentEvent evt) {
            dialog.checkValues();
        }
        
        /**
         * Method from DocumentListener
         */
        public void insertUpdate(javax.swing.event.DocumentEvent evt) {
            dialog.checkValues();
        }
        
        /**
         * Method from DocumentListener
         */
        public void removeUpdate(javax.swing.event.DocumentEvent evt) {
            dialog.checkValues();
        }
    }
    
    public interface Panel {
        JComponent[] getEditableComponents();
    }
}

