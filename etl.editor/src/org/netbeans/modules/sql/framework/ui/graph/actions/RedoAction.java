package org.netbeans.modules.sql.framework.ui.graph.actions;

import java.awt.event.ActionEvent;
import javax.swing.undo.UndoManager;
import javax.swing.AbstractAction;

import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.view.ETLCollaborationTopPanel;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.model.SQLUIModel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


public final class RedoAction extends AbstractAction {
    
    private static final String LOG_CATEGORY = RedoAction.class.getName();
        
    public String getName() {
        return NbBundle.getMessage(RedoAction.class, "CTL_RedoAction");
    }
    
    protected String iconResource() {
        return "org/netbeans/modules/sql/framework/ui/resources/images/redo.png";
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    public void actionPerformed(ActionEvent e) {
        ETLCollaborationTopPanel topComp = null;
        try {
            topComp = DataObjectProvider.getProvider().getActiveDataObject().getETLEditorTopPanel();
        } catch (Exception ex) {
            // ignore
        }
        IGraphView graphView = topComp.getGraphView();
        SQLUIModel model = (SQLUIModel) graphView.getGraphModel();
        UndoManager undoManager = model.getUndoManager();
        if (undoManager != null && undoManager.canRedo()) {
            undoManager.redo();
            //refreshUndoRedo(undoManager);
        }        
    }
}
