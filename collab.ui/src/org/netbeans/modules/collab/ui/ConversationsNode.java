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

import com.sun.collablet.CollabSession;

import org.openide.*;
import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.util.datatransfer.NewType;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.io.*;

import java.lang.reflect.*;

import java.util.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.ui.CollabSessionCookie;
import org.netbeans.modules.collab.ui.actions.*;


/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class ConversationsNode extends AbstractNode implements CollabSessionCookie {
    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////
    //	/**
    //	 *
    //	 *
    //	 */
    //	public class NewCollabPartner extends NewType
    //	{
    //		/**
    //		 *
    //		 *
    //		 */
    //		public String getName()
    //		{
    //			return NbBundle.getBundle(ConversationsNode.class).getString(
    //				"LBL_ConversationsNode_NewCollabPartner_Name"); // NOI18N
    //		}
    //
    //
    //		/**
    //		 *
    //		 *
    //		 */
    //		public HelpCtx getHelpCtx()
    //		{
    //			return HelpCtx.DEFAULT_HELP;
    //		}
    //
    //
    //		/**
    //		 *
    //		 *
    //		 */
    //		public void create()
    //			throws IOException  
    //		{
    //			int length=CollabManager.getDefault().getPartners().length;
    //			CollabPartner partner=new CollabPartner();
    //			partner.setName("partner"+(length+1));
    //			CollabManager.getDefault().addPartner(partner);
    //
    //			((ConversationsNodeChildren)
    //				ConversationsNode.this.getChildren()).refreshChildren();
    //		}
    //	}
    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////
    //	/**
    //	 *
    //	 *
    //	 */
    //	protected class EnableCollabServerProperty
    //		extends PropertySupport.ReadWrite
    //	{
    //		/**
    //		 *
    //		 *
    //		 */
    //		public EnableCollabServerProperty()
    //		{
    //			super(
    //				PROP_ENABLE_COLLAB_SERVER,
    //				Boolean.class,
    //				NbBundle.getBundle(ConversationsNode.class).getString(
    //					"PROP_EnableCollabServer_DisplayName"),
    //				NbBundle.getBundle(ConversationsNode.class).getString(
    //					"PROP_EnableCollabServer_Description"));
    //		}
    //
    //
    //		/**
    //		 *
    //		 *
    //		 */
    //		public Object getValue()
    //			throws IllegalAccessException, InvocationTargetException
    //		{
    //			return new Boolean(CollabServer.getDefault().isStarted());
    //		}
    //
    //
    //		/**
    //		 *
    //		 *
    //		 */
    //		public void setValue(Object obj) 
    //			throws IllegalAccessException, IllegalArgumentException, 
    //				InvocationTargetException
    //		{
    //			boolean value=((Boolean)obj).booleanValue();
    //
    //			if (value)
    //			{
    //				if (!CollabServer.getDefault().isStarted())
    //					CollabServer.getDefault().startServer();
    //			}
    //			else
    //			{
    //				CollabServer.getDefault().stopServer();
    //			}
    //		}
    //	}
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    public static final String ICON_BASE = "org/netbeans/modules/collab/ui/resources/chat_png"; // NOI18N
    private static final SystemAction[] DEFAULT_ACTIONS = new SystemAction[] {
            
            //			SystemAction.get(NewAction.class),
            // TAF: Removing this for now, I think the UI is probably better
            // in just starting a conversation by clicking on a buddy
            //			SystemAction.get(NewConversationAction.class),
            SystemAction.get(CreateConversationAction.class), null,
            SystemAction.get(CreatePublicConversationAction.class), SystemAction.get(AddPublicConversationAction.class),
        };

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private CollabSession session;

    /**
     *
     *
     */
    public ConversationsNode(CollabSession session) {
        super(createChildren(session));
        this.session = session;
        setName(NbBundle.getBundle(ConversationsNode.class).getString("LBL_ConversationsNode_Name")); // NOI18N
        setIconBase(ICON_BASE);
        systemActions = DEFAULT_ACTIONS;

        // Add ourselves as a cookie
        getCookieSet().add(this);
    }

    /**
     *
     *
     */
    protected static ConversationsNodeChildren createChildren(CollabSession session) {
        return new ConversationsNodeChildren(session);
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
        return new HelpCtx(ConversationsNode.class);
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
        super.destroy();
    }

    //	/**
    //	 *
    //	 *
    //	 */
    //	public NewType[] getNewTypes()
    //	{
    //		return new NewType[] {new NewCollabPartner()};
    //	}
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

    /**
     *
     *
     */
    public ConversationsNodeChildren getConversationsNodeChildren() {
        return (ConversationsNodeChildren) getChildren();
    }
}
