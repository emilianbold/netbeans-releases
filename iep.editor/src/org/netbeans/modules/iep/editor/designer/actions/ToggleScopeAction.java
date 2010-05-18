package org.netbeans.modules.iep.editor.designer.actions;

import com.nwoods.jgo.JGoSelection;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.netbeans.modules.iep.editor.designer.PlanCanvas;
import org.netbeans.modules.iep.model.IEPModel;

public class ToggleScopeAction extends AbstractAction {

    private PlanCanvas mView;
    private IEPModel mModel;
    
    
    public ToggleScopeAction(PlanCanvas view, 
            IEPModel model) {
    
        this.mView = view;
        this.mModel = model;
        
    }
    
    public void actionPerformed(ActionEvent e) {
        
        JGoSelection selection = this.mView.getSelection();
        
    }
}
