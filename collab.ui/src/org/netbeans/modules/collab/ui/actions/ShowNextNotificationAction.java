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

import org.openide.util.*;
import org.openide.util.actions.*;

import org.netbeans.modules.collab.ui.*;


/**
 *
 *
 * @author Todd Fast <todd.fast@sun.com>
 */
public class ShowNextNotificationAction extends SystemAction implements NotificationListener {
    private static final String NORMAL_RES = "org/netbeans/modules/collab/ui/resources/chat_png.gif";
    private static final String ALERT_RES = "org/netbeans/modules/collab/ui/resources/conversation_notify_png.gif";
    private static final String ACTION_PERFORMER = "actionPerformer";

    private String currentIconRes = NORMAL_RES;



    /**
     *
     *
     */
    public ShowNextNotificationAction() {
        super();
        setEnabled(false);

        // Attach ourselves as a notification listener (weakly)
        NotificationRegistry.getDefault().addNotificationListener(
            (NotificationListener) WeakListeners.create(
                NotificationListener.class, NotificationListener.class, this, NotificationRegistry.getDefault()
            )
        );
    }

    private void setIconResource(final String resource) {
        javax.swing.SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    currentIconRes = resource;
                    firePropertyChange(SystemAction.PROP_ICON, null, null);
                }
            }
        );
    }
    protected String iconResource() {
        return currentIconRes;
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
            setIconResource(NORMAL_RES);
            setEnabled(false);
        } else {
            setEnabled(true);
        }
    }

    /**
     *
     *
     */
    public void actionPerformed(java.awt.event.ActionEvent event) {
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
            setIconResource(ALERT_RES);
        } else {
            setIconResource(NORMAL_RES);
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
