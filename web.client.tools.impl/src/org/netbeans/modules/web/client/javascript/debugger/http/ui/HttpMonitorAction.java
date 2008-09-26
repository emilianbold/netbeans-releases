/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.client.javascript.debugger.http.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Action which shows HttpMonitor component.
 */
public class HttpMonitorAction extends AbstractAction {

    public HttpMonitorAction() {
        // When changed, update also mf-layer.xml, where are the properties duplicated because of Actions.alwaysEnabled()
        super(NbBundle.getMessage(HttpMonitorAction.class, "CTL_HttpMonitorAction"));
        putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage(HttpMonitorTopComponent.ICON_PATH, true)));
    }

    public void actionPerformed(ActionEvent evt) {
        TopComponent win = HttpMonitorTopComponent.findInstance();
        win.open();
        win.requestActive();
    }
}
