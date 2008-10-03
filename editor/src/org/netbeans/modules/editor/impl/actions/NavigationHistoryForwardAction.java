/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.editor.impl.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.netbeans.editor.BaseKit;
import org.netbeans.modules.editor.lib.NavigationHistory;
import org.openide.awt.DropDownButtonFactory;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Vita Stejskal
 */
public final class NavigationHistoryForwardAction extends TextAction implements ContextAwareAction, Presenter.Toolbar,  PropertyChangeListener {
    
    private static final Logger LOG = Logger.getLogger(NavigationHistoryForwardAction.class.getName());
    
    private final JTextComponent component;
    private final NavigationHistory.Waypoint waypoint;
    private final JPopupMenu popupMenu;
    
    public NavigationHistoryForwardAction() {
        this(null, null, null);
    }

    private NavigationHistoryForwardAction(JTextComponent component, NavigationHistory.Waypoint waypoint, String actionName) {
        super(BaseKit.jumpListNextAction);
        
        this.component = component;
        this.waypoint = waypoint;
        
        if (waypoint != null) {
            putValue(NAME, actionName);
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NavigationHistoryBackAction.class, 
                "NavigationHistoryForwardAction_Tooltip", actionName)); //NOI18N
            this.popupMenu = null;
        } else if (component != null) {
            putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/editor/resources/navigate_forward_16.png"))); //NOI18N
            this.popupMenu = new JPopupMenu();
            update();
            NavigationHistory nav = NavigationHistory.getNavigations();
            nav.addPropertyChangeListener(WeakListeners.propertyChange(this, nav));
        } else {
            this.popupMenu = null;
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NavigationHistoryForwardAction.class, 
                "NavigationHistoryForwardAction_Tooltip_simple")); //NOI18N
        }
    }
    
    public Action createContextAwareInstance(Lookup actionContext) {
        JTextComponent c = NavigationHistoryBackAction.findComponent(actionContext);
        return new NavigationHistoryForwardAction(c, null, null);
    }

    public void actionPerformed(ActionEvent evt) {
        NavigationHistory history = NavigationHistory.getNavigations();
        NavigationHistory.Waypoint wpt = waypoint != null ? 
            history.navigateTo(waypoint) : history.navigateForward();
        
        if (wpt != null) {
            NavigationHistoryBackAction.show(wpt);
        }
    }

    public Component getToolbarPresenter() {
        if (popupMenu != null) {
            JButton button = DropDownButtonFactory.createDropDownButton(
                (ImageIcon) getValue(SMALL_ICON), 
                popupMenu
            );
            button.putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
            button.setAction(this);
            return button;
        } else {
            return new JButton(this);
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        update();
    }
    
    private void update() {
        List<NavigationHistory.Waypoint> waypoints = NavigationHistory.getNavigations().getNextWaypoints();

        // Update popup menu
        if (popupMenu != null) {
            popupMenu.removeAll();

            int count = 0;
            String lastFileName = null;
            NavigationHistory.Waypoint lastWpt = null;
            
            for(int i = 0; i < waypoints.size(); i++) {
                NavigationHistory.Waypoint wpt = waypoints.get(i);
                String fileName = NavigationHistoryBackAction.getWaypointName(wpt);
                
                if (fileName == null) {
                    continue;
                }
                
                if (lastFileName == null || !fileName.equals(lastFileName)) {
                    if (lastFileName != null) {
                        popupMenu.add(new NavigationHistoryForwardAction(component, lastWpt, 
                            count > 1 ? lastFileName + ":" + count : lastFileName)); //NOI18N
                    }
                    lastFileName = fileName;
                    lastWpt = wpt;
                    count = 1;
                } else {
                    count++;
                }
            }
            
            if (lastFileName != null) {
                popupMenu.add(new NavigationHistoryForwardAction(component, lastWpt,
                    count > 1 ? lastFileName + ":" + count : lastFileName)); //NOI18N
            }
        }
        
        // Set the short description
        if (!waypoints.isEmpty()) {
            NavigationHistory.Waypoint wpt = waypoints.get(0);
            String fileName = NavigationHistoryBackAction.getWaypointName(wpt);
            if (fileName != null) {
                putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NavigationHistoryForwardAction.class, 
                    "NavigationHistoryForwardAction_Tooltip", fileName)); //NOI18N
            } else {
                putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NavigationHistoryForwardAction.class, 
                    "NavigationHistoryForwardAction_Tooltip_simple")); //NOI18N
            }
            setEnabled(true);
        } else {
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NavigationHistoryForwardAction.class, 
                "NavigationHistoryForwardAction_Tooltip_simple")); //NOI18N
            setEnabled(false);
        }
    }
}
