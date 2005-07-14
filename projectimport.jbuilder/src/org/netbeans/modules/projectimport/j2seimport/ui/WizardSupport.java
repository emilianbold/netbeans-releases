/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.projectimport.j2seimport.ui;
import java.awt.Dialog;
import javax.swing.JDialog;
import javax.swing.WindowConstants;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;


/**
 *
 * @author Radek Matous
 */
public class WizardSupport extends WizardDescriptor implements BasicPanel.ErrorMessages {
    
    public static BasicPanel.WizardData show(final String title, final BasicPanel[] panels, final BasicPanel.WizardData data) {
        WizardDescriptor.Panel[] wpanels = new WizardDescriptor.Panel[panels.length];
        for (int i = 0; i < panels.length; i++) {
            wpanels[i] = panels[i].getWizardPanel();
        }
        
        WizardSupport ws = new WizardSupport(wpanels, data);
        ws.setTitleFormat(new java.text.MessageFormat("{1}")); // NOI18N
        //TODO
        ws.setTitle(title); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(ws);
        dialog.setVisible(true);
        dialog.toFront();
        return (ws.getValue() != WizardDescriptor.FINISH_OPTION) ?
            null : data;
         
        /*DialogDescriptor desc = new DialogDescriptor(panels[0],"balbla",true, new Object[]{}, null, 0, null, null);
        desc.setClosingOptions(new Object[]{});
        
        final Dialog progressDialog = DialogDisplayer.getDefault().createDialog(desc);
        //((JDialog) progressDialog).setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        progressDialog.setVisible(true);        
        return null;
         **/
    }
     
     
    /** Creates a new instance of WizardSupport */
    private WizardSupport(WizardDescriptor.Panel[] panels, BasicPanel.WizardData data) {
        super(panels, data);
        data.setErrorMessages(this);
    }
    
    public void setError(String message) {
        this.putProperty("WizardPanel_errorMessage", // NOI18N
                message);
    }
            
    public static final class ErrorMessages {
        public void setError(String mesage) {
        }
    }    
}
