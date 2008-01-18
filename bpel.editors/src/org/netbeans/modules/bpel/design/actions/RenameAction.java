/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.bpel.design.actions;

import java.awt.event.ActionEvent;
import org.netbeans.modules.bpel.design.DesignView;

/**
 *
 * @author Alexey
 */
public class RenameAction extends DesignModeAction {

    private static final long serialVersionUID = 1L;

    public RenameAction(DesignView view){
        super(view);
       
    }
    
    public void actionPerformed(ActionEvent e) {
        if (getDesignView().getModel().isReadOnly()) {
            return;
        }
        //FIXME getDesignView().getNameEditor().startEdit(getSelectionModel().getSelectedPattern());
    }
}