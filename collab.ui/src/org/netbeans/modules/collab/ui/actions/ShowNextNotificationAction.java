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
