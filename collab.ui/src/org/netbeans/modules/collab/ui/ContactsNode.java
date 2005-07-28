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
public class ContactsNode extends AbstractNode implements CollabSessionCookie {
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
    //			return NbBundle.getBundle(ContactsNode.class).getString(
    //				"LBL_ContactsNode_NewCollabPartner_Name"); // NOI18N
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
    //			((ContactsNodeChildren)
    //				ContactsNode.this.getChildren()).refreshChildren();
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
    //				NbBundle.getBundle(ContactsNode.class).getString(
    //					"PROP_EnableCollabServer_DisplayName"),
    //				NbBundle.getBundle(ContactsNode.class).getString(
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
    public static final String ICON_BASE = "org/netbeans/modules/collab/ui/resources/group_png"; // NOI18N
    private static final SystemAction[] DEFAULT_ACTIONS = new SystemAction[] {
            SystemAction.get(AddContactAction.class), SystemAction.get(AddContactGroupAction.class),
        };

    //	public static final String PROP_ENABLE_COLLAB_SERVER="enableCollabServer";
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private CollabSession session;

    /**
     *
     *
     */
    public ContactsNode(CollabSession session) {
        super(createChildren(session));
        this.session = session;

        setName(NbBundle.getBundle(ContactsNode.class).getString("LBL_ContactsNode_Name")); // NOI18N
        setIconBase(ICON_BASE);
        systemActions = DEFAULT_ACTIONS;
        getCookieSet().add(this);
    }

    /**
     *
     *
     */
    protected static ContactsNodeChildren createChildren(CollabSession session) {
        return new ContactsNodeChildren(session);
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
        return new HelpCtx(ContactsNode.class);
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
    public ContactsNodeChildren getContactsNodeChildren() {
        return (ContactsNodeChildren) getChildren();
    }
}
