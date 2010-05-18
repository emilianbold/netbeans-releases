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
import java.beans.*;
import java.io.IOException;

import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;

import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.CollabSession;
import org.netbeans.modules.collab.ui.actions.*;

/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class SessionNode extends AbstractNode implements CollabSessionCookie, PropertyChangeListener {
    //	/**
    //	 *
    //	 *
    //	 */
    //	public Sheet createSheet()
    //	{
    //		Sheet sheet=Sheet.createDefault();
    //		Sheet.Set propertiesSet=sheet.get(Sheet.PROPERTIES);
    //
    //		propertiesSet.put(new EnableCollabServerProperty());
    //
    //		return sheet;
    //	}
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    public static final String ICON_BASE = "org/netbeans/modules/collab/ui/resources/empty.gif"; // NOI18N

    //		"org/netbeans/modules/collab/core/resources/account_png.gif"; // NOI18N
    public static final String OPEN_ICON = "org/netbeans/modules/collab/ui/resources/online_png.gif"; // NOI18N
    public static final String CLOSED_ICON = "org/netbeans/modules/collab/ui/resources/offline_png.gif"; // NOI18N
    public static final String AWAY_ICON = "org/netbeans/modules/collab/ui/resources/away_png.gif"; // NOI18N
    public static final String CHAT_ICON = "org/netbeans/modules/collab/ui/resources/chat_png.gif"; // NOI18N
    public static final String BUSY_ICON = "org/netbeans/modules/collab/ui/resources/busy_png.gif"; // NOI18N
    public static final String PENDING_ICON = "org/netbeans/modules/collab/ui/resources/pending_png.gif"; // NOI18N
    public static final String IDLE_ICON = "org/netbeans/modules/collab/ui/resources/idle_png.gif"; // NOI18N
    public static final String WATCHED_ICON = "org/netbeans/modules/collab/ui/resources/watched_png.gif"; // NOI18N
    public static final String DEFAULT_ACCOUNT_ICON = "org/netbeans/modules/collab/ui/resources/check_png.gif"; // NOI18N
    private static final SystemAction[] DEFAULT_ACTIONS = new SystemAction[] {
            SystemAction.get(AddContactAction.class), SystemAction.get(AddContactGroupAction.class), null,
            SystemAction.get(ChangeStatusAction.class), null, SystemAction.get(ChangePasswordAction.class),
            
            //			SystemAction.get(DeleteAction.class),
            //			null,
            SystemAction.get(LogoutAction.class)
        };

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private CollabSession session;
    private Image currentIcon = null;
    private Image baseIcon = Utilities.loadImage(ICON_BASE);
    private Image openIcon = Utilities.loadImage(OPEN_ICON);
    private Image closedIcon = Utilities.loadImage(CLOSED_ICON);
    private Image awayIcon = Utilities.loadImage(AWAY_ICON);
    private Image chatIcon = Utilities.loadImage(CHAT_ICON);
    private Image busyIcon = Utilities.loadImage(BUSY_ICON);
    private Image pendingIcon = Utilities.loadImage(PENDING_ICON);
    private Image idleIcon = Utilities.loadImage(IDLE_ICON);
    private Image watchedIcon = Utilities.loadImage(WATCHED_ICON);
    private Image defaultAccountIcon = Utilities.loadImage(DEFAULT_ACCOUNT_ICON);

    /**
     *
     *
     */
    public SessionNode(CollabSession session) throws IntrospectionException {
        super(new SessionNodeChildren(session));
        this.session = session;

        setName(
            NbBundle.getMessage(
                SessionNode.class, "LBL_SessionNode_DisplayName", session.getDisplayName(),
                
        //				session.getUserPrincipal().getDisplayName(),
        session.getUserPrincipal().getIdentifier()
            )
        );
        updateIcon();
        systemActions = DEFAULT_ACTIONS;

        // Add ourselves as a cookie
        getCookieSet().add(this);

        session.getUserPrincipal().addPropertyChangeListener(this);
    }

    /**
     *
     *
     */
    public CollabSession getCollabSession() {
        return session;
    }

    /**
     *
     *
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(SessionNode.class);
    }

    /**
     *
     *
     */
    public boolean canCut() {
        return false;
    }

    /**
     *
     *
     */
    public boolean canCopy() {
        return false;
    }

    /**
     *
     *
     */
    public boolean canDestroy() {
        return false;
    }

    /**
     *
     *
     */
    public boolean canRename() {
        return false;
    }

    /**
     *
     *
     */
    public void destroy() throws IOException {
        //		getCollabSession().logout();
        //		try
        //		{
        //			getCollabSession().unregister();
        //		}catch (CollabException e)
        //		{
        //			
        //		}
        super.destroy();
    }

    /**
     *
     *
     */
    public Image getIcon(int kind) {
        return currentIcon;
    }

    /**
     *
     *
     */
    public Image getOpenedIcon(int kind) {
        return currentIcon;
    }

    /**
     *
     *
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getSource() instanceof CollabPrincipal) {
            if (event.getPropertyName().equals(CollabPrincipal.PROP_STATUS)) {
                updateIcon();
            }
        }
    }

    //	/**
    //	 *
    //	 *
    //	 */
    //	protected void updateIcon()
    //	{
    //		int status=getCollabSession().getUserPrincipal().getStatus();
    //		
    //		switch (status) 
    //		{
    //			case CollabPrincipal.STATUS_AWAY:
    //				setIconBase(AWAY_ICON);
    //				break;
    //			case CollabPrincipal.STATUS_BUSY:
    //				setIconBase(BUSY_ICON);
    //				break;
    //			case CollabPrincipal.STATUS_CHAT:
    //				setIconBase(CHAT_ICON);
    //				break;
    //			case CollabPrincipal.STATUS_IDLE:
    //				setIconBase(IDLE_ICON);
    //				break;
    //			case CollabPrincipal.STATUS_OFFLINE:
    //				setIconBase(CLOSED_ICON);
    //				break;
    //			case CollabPrincipal.STATUS_ONLINE:
    //				setIconBase(OPEN_ICON);
    //				break;
    //			default:
    //				setIconBase(CLOSED_ICON);
    //		}
    //	}

    /**
     *
     *
     */
    protected void updateIcon() {
        int status = getCollabSession().getUserPrincipal().getStatus();
        Image statusIcon = null;
        String description = null;

        switch (status) {
        case CollabPrincipal.STATUS_AWAY:
            statusIcon = awayIcon;
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusAway"); // NOI18N

            break;

        case CollabPrincipal.STATUS_BUSY:
            statusIcon = busyIcon;
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusBusy"); // NOI18N

            break;

        case CollabPrincipal.STATUS_CHAT:
            statusIcon = chatIcon;
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusChat"); // NOI18N

            break;

        case CollabPrincipal.STATUS_IDLE:
            statusIcon = idleIcon;
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusIdle"); // NOI18N

            break;

        case CollabPrincipal.STATUS_OFFLINE:
            statusIcon = closedIcon;
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusOffline"); // NOI18N

            break;

        case CollabPrincipal.STATUS_ONLINE:
            statusIcon = openIcon;
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusOnline"); // NOI18N

            break;

        case CollabPrincipal.STATUS_PENDING:
            statusIcon = pendingIcon;
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusPending"); // NOI18N

            break;

        case CollabPrincipal.STATUS_WATCHED:
            statusIcon = watchedIcon;
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusWatched"); // NOI18N

            break;

        default:
            statusIcon = closedIcon;
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusUnknown"); // NOI18N
        }

        setShortDescription(description);

        //		currentIcon=Utilities.mergeImages(statusIcon,baseIcon,16,0);
        currentIcon = Utilities.mergeImages(baseIcon, statusIcon, 0, 0);

        //		if (getCollabSession().getAccount()==
        //			CollabManager.getDefault().getUserInterface().getDefaultAccount())
        //		{
        //			currentIcon=Utilities.mergeImages(statusIcon,
        //				defaultAccountIcon,16,0);
        //		}
        fireIconChange();
    }
}
