package org.netbeans.core.multiview;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static org.netbeans.core.multiview.SplitAction.clearSplit;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(
    category = "Tools",
    id = "org.netbeans.core.multiview.ClearSplitAction"
)
@ActionRegistration(
    displayName = "#LBL_ClearSplitAction"
)
@ActionReference(path = "Shortcuts", name = "DOS-C")
@NbBundle.Messages({
    "LBL_ClearSplitAction=&Clear",
    "LBL_ValueClearSplit=clearSplit"
})
public final class ClearSplitAction extends AbstractAction {
    public void initTopComponent(TopComponent tc) {
        putValue(Action.NAME, Bundle.LBL_ClearSplitAction());
        //hack to insert extra actions into JDev's popup menu
        putValue("_nb_action_id_", Bundle.LBL_ValueClearSplit()); //NOI18N
        if (tc instanceof Splitable) {
            setEnabled(((Splitable) tc).getSplitOrientation() != -1);
        } else {
            setEnabled(false);
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        final TopComponent tc = WindowManager.getDefault().getRegistry().getActivated();
        
        if (tc != null && ((Splitable)tc).getSplitOrientation() != -1) {
            clearSplit(tc, -1);
        }
    }
}