/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A showDialog of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.ui.copy;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.netbeans.modules.subversion.settings.HistorySettings;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;

/**
 *
 * @author Tomas Stupka
 */
public abstract class CopyDialog implements PropertyChangeListener {

    private DialogDescriptor dialogDescriptor;
    private JButton okButton;
    private JPanel panel;

    private Map<String, JComboBox> urlComboBoxes;
    
    public CopyDialog(JPanel panel, String title, String okLabel) {                
        this.panel = panel;
        dialogDescriptor = new DialogDescriptor(panel, title); 
        
        okButton = new JButton(okLabel);
        okButton.setEnabled(false);
        dialogDescriptor.setOptions(new Object[] {okButton, "Cancel"});
        
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(this.getClass()));
        dialogDescriptor.setValid(false);
    }

    protected void resetUrlComboBoxes() {
        getUrlComboBoxes().clear();
    }
    
    protected void setupUrlComboBox(JComboBox cbo, String key) {
        if(cbo==null) {
            return;
        }
        List<String> recentFolders = HistorySettings.getRecent(key);
        ComboBoxModel rootsModel = new DefaultComboBoxModel(new Vector<String>(recentFolders));
        cbo.setModel(rootsModel);        
                
        getUrlComboBoxes().put(key, cbo);
    }    
    
    private Map<String, JComboBox> getUrlComboBoxes() {
        if(urlComboBoxes == null) {
            urlComboBoxes = new HashMap<String, JComboBox>();
        }
        return urlComboBoxes;
    }
    
    protected JPanel getPanel() {
        return panel;
    }       
    
    public boolean showDialog() {                        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
        dialog.setVisible(true);
        boolean ret = dialogDescriptor.getValue()==okButton;
        if(ret) {
            storeValidValues();
        }
        return ret;       
    }        
    
    private void storeValidValues() {
        for (Iterator it = urlComboBoxes.keySet().iterator(); it.hasNext();) {
            String key = (String)  it.next();
            JComboBox cbo = (JComboBox) urlComboBoxes.get(key);
            Object item = cbo.getEditor().getItem();
            if(item != null && !item.equals("")) {
                HistorySettings.addRecent(key, (String) item); 
            }            
        }                
    }       

    public void propertyChange(PropertyChangeEvent evt) {
        if( evt.getPropertyName().equals(RepositoryPaths.PROP_VALID) ) {
            boolean valid = ((Boolean)evt.getNewValue()).booleanValue();
            getOKButton().setEnabled(valid);
        }        
    }
    
    protected JButton getOKButton() {
        return okButton;
    }

}
