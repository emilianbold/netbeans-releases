/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.casaeditor.nodes.actions;

import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.awt.Actions;
import java.awt.event.ActionEvent;
import javax.swing.*;

/**
 * DOCUMENT ME!
 *
 * @author tli
 * @version
 */
public class WSDLEndpointAction extends AbstractAction implements Presenter.Popup {

    public WSDLEndpointAction() {
        super(NbBundle.getMessage(WSDLEndpointAction.class, "LBL_WSDLEndpointAction_Name")); // NOI18N
    }

    public void actionPerformed(ActionEvent e) {
        // dummy, not used
    }

    public JMenuItem getPopupPresenter() {
        JMenu menu = new JMenu((String) this.getValue(Action.NAME));
        JMenuItem serverItem = new JMenuItem();
        JMenuItem clientItem = new JMenuItem();
        Actions.connect(serverItem, (Action) SystemAction.get(WsitServerConfigAction.class), true);
        Actions.connect(clientItem, (Action) SystemAction.get(WsitClientConfigAction.class), true);
        menu.add(serverItem);
        menu.add(clientItem);
        return menu;
    }

}
