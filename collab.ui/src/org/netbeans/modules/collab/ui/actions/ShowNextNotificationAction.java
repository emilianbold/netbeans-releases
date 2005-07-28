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
package org.netbeans.modules.collab.ui.actions;

import org.openide.*;
import org.openide.awt.*;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.windows.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.ui.*;


/**
 *
 *
 * @author Todd Fast <todd.fast@sun.com>
 */
public class ShowNextNotificationAction extends SystemAction implements NotificationListener {
    ////////////////////////////////////////////////////////////////////////////
    // Class fields
    ////////////////////////////////////////////////////////////////////////////
    private static final Image NORMAL_IMAGE = (ToolbarPool.getDefault().getPreferredIconSize() == 16)
        ? Utilities.loadImage("org/netbeans/modules/collab/ui/resources/chat_png.gif")
        : Utilities.loadImage("org/netbeans/modules/collab/ui/resources/chat_png24.gif");
    private static final Image ALERT_IMAGE = (ToolbarPool.getDefault().getPreferredIconSize() == 16)
        ? Utilities.loadImage("org/netbeans/modules/collab/ui/resources/conversation_notify_png.gif")
        : Utilities.loadImage("org/netbeans/modules/collab/ui/resources/conversation_notify_png24.gif");
    private static final Icon NORMAL_ICON = (NORMAL_IMAGE != null) ? new ImageIcon(NORMAL_IMAGE) : new ImageIcon();
    private static final Icon ALERT_ICON = (ALERT_IMAGE != null) ? new ImageIcon(ALERT_IMAGE) : new ImageIcon();
    private static final Object PAUSE_LOCK = new Object();
    private static final String ACTION_PERFORMER = "actionPerformer";
    private static NotificationThread notificationThread;

    /**
     *
     *
     */
    public ShowNextNotificationAction() {
        super();

        setIcon(NORMAL_ICON);
        setEnabled(false);

        // Attach ourselves as a notification listener (weakly)
        NotificationRegistry.getDefault().addNotificationListener(
            (NotificationListener) WeakListeners.create(
                NotificationListener.class, NotificationListener.class, this, NotificationRegistry.getDefault()
            )
        );
    }

    /**
     *
     *
     */
    public String getName() {
        return NbBundle.getMessage(ShowNextNotificationAction.class, "LBL_ShowNextNotificationAction_Name");
    }

    /**
     *
     *
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     *
     *
     */
    protected boolean asynchronous() {
        return false;
    }

    /**
     * First a property change after setting the icon
     *
     */
    private void _setIcon(final Icon icon) {
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    ShowNextNotificationAction.super.setIcon(icon);
                    firePropertyChange(SystemAction.PROP_ICON, Boolean.FALSE, Boolean.TRUE);
                }
            }
        );
    }

    /**
     *
     *
     */
    public ActionPerformer getActionPerformer() {
        return (ActionPerformer) getValue(ACTION_PERFORMER);
    }

    /**
     *
     *
     */
    public void setActionPerformer(ActionPerformer performer) {
        putValue(ACTION_PERFORMER, performer);

        if (performer == null) {
            _setIcon(NORMAL_ICON);
            setEnabled(false);
        } else {
            setEnabled(true);
        }
    }

    /**
     *
     *
     */
    public void actionPerformed(ActionEvent event) {
        ActionPerformer performer = getActionPerformer();

        if (performer != null) {
            performer.performAction(this);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Listener methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void notificationStateChanged(boolean state) {
        if (state) {
            _setIcon(ALERT_ICON);
        } else {
            _setIcon(NORMAL_ICON);
        }
    }

    /**
     *
     *
     */
    public void notificationSuspended() {
        notificationStateChanged(false);
    }

    /**
     *
     *
     */
    public void notificationResumed() {
        // Do nothing
    }
}
