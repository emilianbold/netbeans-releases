/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.collab.ui;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;

import org.openide.util.*;

import com.sun.collablet.CollabManager;
import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.CollabSession;

/**
 *
 * @author  todd
 */

class OnlineStatusIndicator {
    private static Image OPEN_IMAGE = Utilities.loadImage(SessionNode.OPEN_ICON);
    private static Image CLOSED_IMAGE = Utilities.loadImage(SessionNode.CLOSED_ICON);
    private static Image AWAY_IMAGE = Utilities.loadImage(SessionNode.AWAY_ICON);
    private static Image BUSY_IMAGE = Utilities.loadImage(SessionNode.BUSY_ICON);
    private static Image IDLE_IMAGE = Utilities.loadImage(SessionNode.IDLE_ICON);
    private static OnlineStatusIndicator instance;


    ////////////////////////////////////////////////////////////////////////////
    // Instance fields
    ////////////////////////////////////////////////////////////////////////////
    private JLabel label;
    private Helper helper;
    private int currentStatus = CollabPrincipal.STATUS_UNKNOWN;


    Component getComponent() {
        return label;
    }
    
    /**
     *
     *
     */
    protected OnlineStatusIndicator() {
        helper = new Helper();
        label = new JLabel();
        label.addMouseListener(helper);
        initListening(1);
    }
    
    private void initListening(final int level) {
        CollabManager man = CollabManager.getDefault();
        if (man == null) {
            // manager not yet registered. This is a transient condition during 
            // module enablement because of manager registration mechanism.
            // Retry 5s later
            assert level<10;
            
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    initListening(level+1);
                }
            }, level*5000);
        } else {
            man.addPropertyChangeListener(helper);
            attachListeners();
            updateStatus();
        }
    }


    private void setStatus(int value) {
        currentStatus = value;
        SwingUtilities.invokeLater(helper);
    }

    /**
     *
     *
     */
    protected void updateStatus() {
        final CollabManager manager = CollabManager.getDefault();

        if (manager != null) {
            int sharedStatus = CollabPrincipal.STATUS_OFFLINE;
            boolean unilateral = true;

            CollabSession[] sessions = manager.getSessions();

            for (int i = 0; i < sessions.length; i++) {
                int status = sessions[i].getUserPrincipal().getStatus();

                if (sharedStatus == CollabPrincipal.STATUS_OFFLINE) {
                    sharedStatus = status;
                }

                if (status != sharedStatus) {
                    unilateral = false;
                }
            }

            if (unilateral) {
                // This will occur if either:
                // 1) No sessions were found
                // 2) All available sessions had the same status
                setStatus(sharedStatus);
            } else {
                // Assume at least one session is online
                setStatus(CollabPrincipal.STATUS_ONLINE);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Helper methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public static String getStatusDescription(int status) {
        final String description;

        switch (status) {
        case CollabPrincipal.STATUS_AWAY:
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusAway"); // NOI18N

            break;

        case CollabPrincipal.STATUS_BUSY:
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusBusy"); // NOI18N

            break;

        case CollabPrincipal.STATUS_IDLE:
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusIdle"); // NOI18N

            break;

        case CollabPrincipal.STATUS_OFFLINE:
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusOffline"); // NOI18N

            break;

        case CollabPrincipal.STATUS_ONLINE:
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusOnline"); // NOI18N

            break;

        default:
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusUnknown"); // NOI18N
        }

        return description;
    }

    /**
     *
     *
     */
    public static Image getStatusIcon(int status) {
        final Image statusIcon;

        switch (status) {
        case CollabPrincipal.STATUS_AWAY:
            statusIcon = AWAY_IMAGE;

            break;

        case CollabPrincipal.STATUS_BUSY:
            statusIcon = BUSY_IMAGE;

            break;

        case CollabPrincipal.STATUS_IDLE:
            statusIcon = IDLE_IMAGE;

            break;

        case CollabPrincipal.STATUS_OFFLINE:
            statusIcon = CLOSED_IMAGE;

            break;

        case CollabPrincipal.STATUS_ONLINE:
            statusIcon = OPEN_IMAGE;

            break;

        default:
            statusIcon = CLOSED_IMAGE;
        }

        return statusIcon;
    }

    /**
     *
     *
     */
    protected static String getStatusToolTip() {
        StringBuffer result = new StringBuffer("<html>"); // NOI18N
        result.append("<table cellspacing=\"0\" border=\"0\">"); // NOI18N

        final CollabManager manager = CollabManager.getDefault();

        if (manager != null) {
            Set sessions = new TreeSet(
                    new Comparator() {
                        public int compare(Object o1, Object o2) {
                            String s1 = ((CollabSession) o1).getUserPrincipal().getDisplayName();
                            String s2 = ((CollabSession) o2).getUserPrincipal().getDisplayName();

                            return s1.compareTo(s2);
                        }
                    }
                );

            sessions.addAll(Arrays.asList(manager.getSessions()));

            if (sessions.size() == 0) {
                result.append("<tr><td>"); // NOI18N
                result.append(getStatusDescription(CollabPrincipal.STATUS_OFFLINE));
                result.append("</td></tr>"); // NOI18N
            } else {
                for (Iterator i = sessions.iterator(); i.hasNext();) {
                    CollabPrincipal principal = ((CollabSession) i.next()).getUserPrincipal();

                    result.append("<tr>"); // NOI18N
                    result.append("<td>"); // NOI18N
                    result.append("<b>"); // NOI18N
                    result.append(principal.getDisplayName());
                    result.append(": "); // NOI18N
                    result.append("</b>"); // NOI18N
                    result.append("</td>"); // NOI18N
                    result.append("<td>"); // NOI18N
                    result.append(getStatusDescription(principal.getStatus()));
                    result.append("</td>"); // NOI18N
                    result.append("</tr>"); // NOI18N
                }
            }
        }

        result.append("</table>"); // NOI18N

        return result.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Management methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    static synchronized OnlineStatusIndicator getDefault() {
        if (instance == null) {
            instance = new OnlineStatusIndicator();
        }

        return instance;
    }

    /**
     *
     *
     */
    private void attachListeners() {
        // Add the listener from each collab session principal
        CollabSession[] sessions = CollabManager.getDefault().getSessions();
        
        for (int i = 0; i < sessions.length; i++) {
            sessions[i].getUserPrincipal().removePropertyChangeListener(helper);
            sessions[i].getUserPrincipal().addPropertyChangeListener(helper);
        }
    }
    
    private class Helper extends MouseAdapter implements PropertyChangeListener, Runnable {
        public void mouseClicked(MouseEvent event) {
            CollabExplorerPanel.getInstance().open();
            CollabExplorerPanel.getInstance().requestActive();
        }
        
        public void propertyChange(PropertyChangeEvent event) {
            // session list changed
            if (event.getSource() instanceof CollabManager) {
                attachListeners();
            }
            
            // either session list or session status changed
            updateStatus();
        }
        
        public void run() {
            Image statusIcon = getStatusIcon(currentStatus);
            label.setIcon(new ImageIcon(statusIcon));
            label.setToolTipText(getStatusToolTip());
        }
    }

}
