/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.hibernate.hqleditor.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.netbeans.modules.hibernate.hqleditor.HQLEditorController;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Action which shows HQLEditor component.
 */
public class HQLEditorAction extends AbstractAction {

    public HQLEditorAction() {
        super(NbBundle.getMessage(HQLEditorAction.class, "CTL_HQLEditorAction"));
        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(HQLEditorTopComponent.ICON_PATH, true)));
    }

    public void actionPerformed(ActionEvent evt) {
        HQLEditorController.getDefault().init();
    }
}
