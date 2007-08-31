package org.netbeans.modules.cnd.navigation.hierarchy;

import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;

/**
 * Action which shows Hierarchy component.
 */
public class HierarchyAction extends CallableSystemAction {

    public HierarchyAction() {
        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(HierarchyTopComponent.ICON_PATH, true)));
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        performAction();
    }

    public void performAction() {
        TopComponent win = HierarchyTopComponent.findInstance();
        win.open();
        win.requestActive();
    }

    public String getName() {
        return NbBundle.getMessage(HierarchyAction.class, "CTL_HierarchyAction"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
	return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isEnabled() {
        return CsmModelAccessor.getModel().projects().size()>0;
    }
    
    @Override
    protected boolean asynchronous () {
        return false;
    }
    

    
    
}