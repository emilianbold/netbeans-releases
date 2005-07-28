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
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;

import java.beans.*;

import java.util.*;

import org.netbeans.modules.collab.*;


/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class SessionNodeChildren extends Children.Array {
    //	/**
    //	 *
    //	 *
    //	 */
    //	protected void removeNotify()
    //	{
    //	}
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private CollabSession session;
    private Node[] childNodes;

    /**
     *
     *
     */
    public SessionNodeChildren(CollabSession session) {
        super();
        this.session = session;
    }

    /**
     *
     *
     */
    public CollabSession getSession() {
        return session;
    }

    /**
     *
     *
     */
    protected void addNotify() {
        childNodes = new Node[] { new ContactsNode(getSession()), new ConversationsNode(getSession()) };

        add(childNodes);
        super.addNotify();
    }
}
