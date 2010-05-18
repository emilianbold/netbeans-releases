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
package org.netbeans.modules.visualweb.propertyeditors.css;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 * Main Style Builder Dialog
 * @author  Winston Prakash
 */
public class StyleBuilderDialog extends JPanel implements PropertyChangeListener{
    DialogDescriptor dlg = null;
    JDialog dialog = null;
    
    /** Creates new form StyleBuilder */
    public StyleBuilderDialog() {
        initComponents();
        String cssStyleString = "font-family:'Arial', 'Times New Roman', 'sans-serif'" ;
        StyleBuilderPanel styleBuilderPanel = new StyleBuilderPanel(cssStyleString);
        styleBuilderPanel.addCssPropertyChangeListener(this);
        add(styleBuilderPanel, BorderLayout.CENTER);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println(evt.getNewValue());
    }
    
    public void showDialog(){
        // Add a listener to the dialog's buttons
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                Object o = evt.getSource();
                
                Object[] option = dlg.getOptions();
                
                if (o == option[1]) {
                    System.exit(0);
                } else if (o == option[0]) {
                    System.out.println("Dialog closed"); //NOI18N
                    System.exit(0);
                }
            }
        };
        dlg = new DialogDescriptor(this, "Style Builder", true, listener); //NOI18N
        dlg.setOptions(new Object[] { "Ok", "Cancel" }); //NOI18N
        dlg.setClosingOptions(new Object[] {"Cancel" }); //NOI18N
        dlg.setValid(false); 
        // when help is written, correct the helpID here...
        // dlg.setHelpCtx(new HelpCtx("projrave_ui_elements_project_nav_data_source_ref")); // NOI18N
        
        dialog = (JDialog) DialogDisplayer.getDefault().createDialog(dlg);
        //dialog.setResizable(false);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                System.exit(0);
            }
        });
        dialog.pack();
        dialog.show();
    }
    
    
    private void initComponents() {//GEN-BEGIN:initComponents

        setLayout(new java.awt.BorderLayout());

    }//GEN-END:initComponents
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); //NOI18N
                } catch (Exception e) { }
                new StyleBuilderDialog().showDialog();
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
