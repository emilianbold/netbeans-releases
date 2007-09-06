/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
