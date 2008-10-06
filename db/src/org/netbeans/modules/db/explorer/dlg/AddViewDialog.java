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

package org.netbeans.modules.db.explorer.dlg;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.*;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.*;
import org.openide.awt.Mnemonics;

public class AddViewDialog {

    private static final Logger LOGGER = Logger.getLogger(AddIndexDialog.class.getName());

    private final ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N
    boolean result = false;
    Dialog dialog = null;
    JTextField namefld;
    JTextArea tarea;

    public AddViewDialog(final Specification spec, final DatabaseNodeInfo info) {
        try {
            JPanel pane = new JPanel();
            pane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints con = new GridBagConstraints ();
            pane.setLayout (layout);

            // Index name

            JLabel label = new JLabel();
            Mnemonics.setLocalizedText(label, bundle.getString("AddViewName"));
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddViewNameA11yDesc"));
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 0;
            layout.setConstraints(label, con);
            pane.add(label);

            // Index name field

            con.fill = GridBagConstraints.HORIZONTAL;
            con.weightx = 1.0;
            con.gridx = 1;
            con.gridy = 0;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            namefld = new JTextField(35);
            namefld.setToolTipText(bundle.getString("ACS_AddViewNameTextFieldA11yDesc"));
            namefld.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddViewNameTextFieldA11yName"));
            label.setLabelFor(namefld);
            layout.setConstraints(namefld, con);
            pane.add(namefld);

            // Items list title

            label = new JLabel();
            Mnemonics.setLocalizedText(label, bundle.getString("AddViewLabel"));
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddViewLabelA11yDesc"));
            con.weightx = 0.0;
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 1;
            con.gridwidth = 2;
            layout.setConstraints(label, con);
            pane.add(label);

            // Editor list

            tarea = new JTextArea(5,50);
            tarea.setToolTipText(bundle.getString("ACS_AddViewTextAreaA11yDesc"));
            tarea.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddViewTextAreaA11yName"));
            label.setLabelFor(tarea);

            con.weightx = 1.0;
            con.weighty = 1.0;
            con.gridwidth = 2;
            con.fill = GridBagConstraints.BOTH;
            con.insets = new java.awt.Insets (0, 0, 0, 0);
            con.gridx = 0;
            con.gridy = 2;
            JScrollPane spane = new JScrollPane(tarea);
            layout.setConstraints(spane, con);
            pane.add(spane);

            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    
                    if (event.getSource() == DialogDescriptor.OK_OPTION) {
                        
                        try {
                            boolean wasException = DbUtilities.doWithProgress(null, new Callable<Boolean>() {
                                public Boolean call() throws Exception {
                                    return AddViewDDL.addView(spec, 
                                            (String)info.get(DatabaseNodeInfo.SCHEMA), 
                                            getViewName(), getViewCode());
                                }
                            });

                            result = !wasException;
                            
                            if (!wasException) {
                                dialog.setVisible(false);
                                dialog.dispose();
                            }
                        } catch (InvocationTargetException e) {
                            Throwable cause = e.getCause();
                            if (cause instanceof DDLException) {
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
                            } else {
                                LOGGER.log(Level.INFO, null, cause);
                                DbUtilities.reportError(bundle.getString("ERR_UnableToCreateView"), e.getMessage());
                            }
                        }
                    }
                }
            };

            pane.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddViewDialogA11yDesc")); //NOI18N

            DialogDescriptor descriptor = new DialogDescriptor(pane, bundle.getString("AddViewTitle"), true, listener); //NOI18N
            // inbuilt close of the dialog is only after CANCEL button click
            // after OK button is dialog closed by hand
            Object [] closingOptions = {DialogDescriptor.CANCEL_OPTION};
            descriptor.setClosingOptions(closingOptions);
            dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setResizable(true);
        } catch (MissingResourceException e) {
            e.printStackTrace();
        }
    }

    public boolean run()
    {
        if (dialog != null) dialog.setVisible(true);
        return result;
    }

    public void setViewName(String name)
    {
        namefld.setText(name);
    }

    public String getViewName()
    {
        return namefld.getText();
    }

    public String getViewCode()
    {
        return tarea.getText();
    }
}
