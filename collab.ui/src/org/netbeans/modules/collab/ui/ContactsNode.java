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

import java.io.IOException;

import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;

import com.sun.collablet.CollabSession;
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
