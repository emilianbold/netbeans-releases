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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.impl.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseKit;
import org.netbeans.modules.editor.lib.NavigationHistory;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Vita Stejskal
 */
public final class NavigationHistoryForwardAction extends BaseAction implements PropertyChangeListener {
    
    private static final Logger LOG = Logger.getLogger(NavigationHistoryForwardAction.class.getName());
    
    public NavigationHistoryForwardAction() {
        super(BaseKit.jumpListNextAction);
        putValue(ICON_RESOURCE_PROPERTY, "org/netbeans/modules/editor/resources/edit_next.png"); // NOI18N

        update();
        NavigationHistory.getDefault().addPropertyChangeListener(
            WeakListeners.propertyChange(this, NavigationHistory.getDefault()));
    }
    
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        NavigationHistory.Waypoint wpt = NavigationHistory.getDefault().navigateForward();
        if (wpt != null) {
            NavigationHistoryBackAction.show(wpt);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        update();
    }
    
    private void update() {
        List<NavigationHistory.Waypoint> waypoints = NavigationHistory.getDefault().getNextWaypoints();
        if (!waypoints.isEmpty()) {
            NavigationHistory.Waypoint wpt = waypoints.get(0);
            String fileName = NavigationHistoryBackAction.getWaypointName(wpt);
            if (fileName != null) {
                putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NavigationHistoryForwardAction.class, 
                    "NavigationHistoryForwardAction_Tooltip", fileName));
            } else {
                putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NavigationHistoryForwardAction.class, 
                    "NavigationHistoryForwardAction_Tooltip_simple"));
            }
            setEnabled(true);
        } else {
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NavigationHistoryForwardAction.class, 
                "NavigationHistoryForwardAction_Tooltip_simple"));
            setEnabled(false);
        }
    }
}
