/*
 * ProcessListAction.java
 *
 * Created on April 13, 2005, 2:28 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.progress.module;

import javax.swing.AbstractAction;
import javax.swing.event.ListDataListener;
import org.netbeans.progress.module.ui.StatusLineComponent;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class ProgressListAction extends AbstractAction implements ListDataListener {
    
    /** Creates a new instance of ProcessListAction */
    public ProgressListAction() {
        putValue(NAME, NbBundle.getMessage(ProgressListAction.class, "CTL_ProcessListAction"));
        putValue(MNEMONIC_KEY, NbBundle.getMessage(ProgressListAction.class, "ProcessListAction.mnemonic"));
        Controller.getDefault().getModel().addListDataListener(this);
        updateEnabled();
    }
    
    /** Perform the action. Sets/unsets maximzed mode. */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        ((StatusLineComponent)Controller.getDefault().getVisualComponent()).showPopup();
    }

    private void updateEnabled() {
        setEnabled(Controller.getDefault().getModel().getSize() != 0);
    }    

    public void contentsChanged(javax.swing.event.ListDataEvent listDataEvent) {
        updateEnabled();
    }

    public void intervalAdded(javax.swing.event.ListDataEvent listDataEvent) {
        updateEnabled();
    }

    public void intervalRemoved(javax.swing.event.ListDataEvent listDataEvent) {
        updateEnabled();
    }
    
    
}
