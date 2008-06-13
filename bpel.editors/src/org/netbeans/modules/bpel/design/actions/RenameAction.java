/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.bpel.design.actions;

import java.awt.event.ActionEvent;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.DiagramView;
import org.netbeans.modules.bpel.design.NameEditor;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.selection.EntitySelectionModel;

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
        
        Pattern currentPattern = null;
        DesignView design = getDesignView();
        EntitySelectionModel selModel = design == null 
                        ? null : design.getSelectionModel();
        currentPattern = selModel == null ? null : selModel.getSelectedPattern();
        DiagramView diagram = null;
        if (currentPattern != null) {
            diagram = currentPattern.getView();
        }
        
        NameEditor nameEditor = null;
        if (diagram != null) {
            nameEditor = diagram.getNameEditor();
        }
        if (nameEditor != null) {
            nameEditor.startEdit(currentPattern);
        }
    }
}
