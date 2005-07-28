/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.ui;

import com.sun.collablet.CollabManager;
import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.CollabSession;

import org.openide.*;
import org.openide.awt.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.windows.*;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.util.*;

import javax.swing.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.*;
import org.netbeans.modules.collab.ui.actions.*;


/**
 *
 * @author  todd
 */
public class OnlineStatusIndicator extends JPanel implements PropertyChangeListener {
    ////////////////////////////////////////////////////////////////////////////
    // Class fields
    ////////////////////////////////////////////////////////////////////////////
    public static final String OPEN_IMAGE_RESOURCE = SessionNode.OPEN_ICON;
    public static final String CLOSED_IMAGE_RESOURCE = SessionNode.CLOSED_ICON;
    public static final String AWAY_IMAGE_RESOURCE = SessionNode.AWAY_ICON;
    public static final String BUSY_IMAGE_RESOURCE = SessionNode.BUSY_ICON;
    public static final String IDLE_IMAGE_RESOURCE = SessionNode.IDLE_ICON;
    private static Image OPEN_IMAGE = Utilities.loadImage(OPEN_IMAGE_RESOURCE);
    private static Image CLOSED_IMAGE = Utilities.loadImage(CLOSED_IMAGE_RESOURCE);
    private static Image AWAY_IMAGE = Utilities.loadImage(AWAY_IMAGE_RESOURCE);
    private static Image BUSY_IMAGE = Utilities.loadImage(BUSY_IMAGE_RESOURCE);
    private static Image IDLE_IMAGE = Utilities.loadImage(IDLE_IMAGE_RESOURCE);
    private static OnlineStatusIndicator instance;
    private static boolean installed;

    ////////////////////////////////////////////////////////////////////////////
    // Instance fields
    ////////////////////////////////////////////////////////////////////////////
    private JLabel label;
    private int currentStatus = CollabPrincipal.STATUS_UNKNOWN;

    /**
     *
     *
     */
    protected OnlineStatusIndicator() {
        super();
        initialize();
    }

    /**
     *
     *
     */
    private void initialize() {
        setLayout(new BorderLayout());

        add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.WEST);

        label = new JLabel();
        add(label, BorderLayout.CENTER);

        label.addMouseListener(
            new MouseAdapter() {
                public void mouseClicked(MouseEvent event) {
                    CollabExplorerPanel.getInstance().open();
                }
            }
        );
    }

    /**
     *
     *
     */
    public int getStatus() {
        return currentStatus;
    }

    /**
     *
     *
     */
    public void setStatus(int value) {
        currentStatus = value;

        final Image statusIcon = getStatusIcon(value);

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    label.setIcon(new ImageIcon(statusIcon));
                    label.setToolTipText(getStatusToolTip());
                }
            }
        );
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
                try {
                    int status = sessions[i].getUserPrincipal().getStatus();

                    if (sharedStatus == CollabPrincipal.STATUS_OFFLINE) {
                        sharedStatus = status;
                    }

                    if (status != sharedStatus) {
                        unilateral = false;
                    }
                } catch (Exception e) {
                    Debug.debugNotify(e);
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
    // PropertyChangeListener methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getSource() instanceof CollabPrincipal) {
            updateStatus();
        } else if (event.getSource() instanceof CollabManager) {
            attachListeners(this);
            updateStatus();
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
                    try {
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
                    } catch (Exception e) {
                        Debug.debugNotify(e);
                    }
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
    public static synchronized OnlineStatusIndicator getDefault() {
        if (instance == null) {
            instance = new OnlineStatusIndicator();
        }

        return instance;
    }

    /**
     *
     *
     */
    public static synchronized void install() {
        if (installed) {
            return;
        }

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    Container parent = WindowManager.getDefault().getMainWindow();
                    install(parent);
                }
            }
        );

        installed = true;
    }

    /**
     *
     *
     */
    private static boolean install(Container parent) {
        try {
            for (int i = 0; i < parent.getComponentCount(); i++) {
                Component component = parent.getComponent(i);

                // If we've found the status line component, insert 
                // ourselves in its parent container
                if (component.getClass().getName().equals("org.netbeans.core.windows.view.ui.StatusLine")) // NOI18N
                 {
                    // Note, by installing ourselves as the first child, we 
                    // take precedence over any component in the NORTH 
                    // position.  We specifically need to do this to avoid 
                    // some painting problems with the JSeparator that is 
                    // normally in this position.
                    Container statusLinePanel = component.getParent();
                    statusLinePanel.add(getDefault(), BorderLayout.EAST, 0);
                    statusLinePanel.validate();

                    attachListeners(getDefault());
                    getDefault().updateStatus();

                    return true;
                }

                // Recurse
                if (component instanceof Container) {
                    if (install((Container) component)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Debug.logDebugException("Could not install notification bar", e, true); // NOI18N
            Debug.debugNotify(e);
        }

        return false;
    }

    /**
     *
     *
     */
    public static synchronized void uninstall() {
        if (installed && (instance != null)) {
            final OnlineStatusIndicator _component = instance;
            instance = null;
            installed = false;

            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        _component.getParent().remove(_component);

                        detachListeners(_component);
                    }
                }
            );
        }
    }

    /**
     *
     *
     */
    protected static void attachListeners(OnlineStatusIndicator indicator) {
        final CollabManager manager = CollabManager.getDefault();

        if (manager != null) {
            manager.removePropertyChangeListener(indicator);
            manager.addPropertyChangeListener(indicator);

            // Add the listener from each collab session principal
            CollabSession[] sessions = manager.getSessions();

            for (int i = 0; i < sessions.length; i++) {
                try {
                    sessions[i].getUserPrincipal().removePropertyChangeListener(indicator);
                    sessions[i].getUserPrincipal().addPropertyChangeListener(indicator);
                } catch (Exception e) {
                    Debug.debugNotify(e);
                }
            }
        }
    }

    /**
     *
     *
     */
    protected static void detachListeners(OnlineStatusIndicator indicator) {
        final CollabManager manager = CollabManager.getDefault();

        if (manager != null) {
            manager.removePropertyChangeListener(indicator);

            // Remove the listener from each collab session principal
            CollabSession[] sessions = manager.getSessions();

            for (int i = 0; i < sessions.length; i++) {
                try {
                    sessions[i].getUserPrincipal().removePropertyChangeListener(indicator);
                } catch (Exception e) {
                    Debug.debugNotify(e);
                }
            }
        }
    }
}
