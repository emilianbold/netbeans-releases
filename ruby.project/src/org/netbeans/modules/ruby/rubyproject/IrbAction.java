package org.netbeans.modules.ruby.rubyproject;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Action which shows Irb component.
 */
public class IrbAction extends AbstractAction {
    
    public IrbAction() {
        super(NbBundle.getMessage(IrbAction.class, "CTL_IrbAction"));
        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(IrbTopComponent.ICON_PATH, true)));
    }
    
    public void actionPerformed(ActionEvent evt) {
        TopComponent win = IrbTopComponent.findInstance();
        win.open();
        win.requestActive();
    }
    
}
