package org.netbeans.modules.sql.framework.ui.graph.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.undo.UndoManager;

import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.view.ETLCollaborationTopPanel;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.model.SQLUIModel;
import org.openide.util.HelpCtx;

public final class UndoAction extends AbstractAction {

    private static final String LOG_CATEGORY = UndoAction.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(UndoAction.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public String getName() {
        String nbBundle = mLoc.t("BUND320: Undo");
        return nbBundle.substring(15);
    }

    protected String iconResource() {
        return "org/netbeans/modules/sql/framework/ui/resources/images/undo.png";
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
        if (undoManager != null && undoManager.canUndo()) {
            undoManager.undo();
        //refreshUndoRedo(undoManager);
        }
    }
}

