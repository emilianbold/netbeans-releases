/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.fcb.facebookmodule;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Action which shows Fcb component.
 */
public class FcbAction extends AbstractAction {

    public FcbAction() {
        super(NbBundle.getMessage(FcbAction.class, "CTL_FcbAction"));
//        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(FcbTopComponent.ICON_PATH, true)));
    }

    public void actionPerformed(ActionEvent evt) {
        TopComponent win = FcbTopComponent.findInstance();
        win.open();
        win.requestActive();
    }

}
