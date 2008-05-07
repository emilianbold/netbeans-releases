/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.hibernate.hqleditor.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Action which shows HQLEditorOutput component.
 */
public class HQLEditorOutputAction extends AbstractAction {

    public HQLEditorOutputAction() {
        super(NbBundle.getMessage(HQLEditorOutputAction.class, "CTL_HQLEditorOutputAction"));
        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(HQLEditorTopComponent.ICON_PATH, true)));
    }

    public void actionPerformed(ActionEvent evt) {
//        TopComponent win = HQLEditorOutputTopComponent.findInstance();
//        win.open();
//        win.requestActive();
    }
}
