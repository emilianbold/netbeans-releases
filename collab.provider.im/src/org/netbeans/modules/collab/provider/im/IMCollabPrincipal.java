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
package org.netbeans.modules.collab.provider.im;

import com.sun.collablet.CollabException;
import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.CollabSession;

import org.netbeans.lib.collab.*;


/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class IMCollabPrincipal extends CollabPrincipal {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private IMCollabSession session;
    private CollaborationPrincipal principal;

    /**
     *
     *
     */
    protected IMCollabPrincipal(IMCollabSession session, CollaborationPrincipal principal) {
        super(
            StringUtility.removeResource(principal.getUID()), 
        //principal.getName());
        parseName(principal.getName()), 
        //principal.getDisplayName());
        parseName(principal.getDisplayName())
        );
        this.session = session;
        this.principal = principal;
    }

    /**
     *
     *
     */
    public CollabSession getCollabSession() {
        return session;
    }

    //	/**
    //	 *
    //	 *
    //	 */
    //	protected IMCollabPrincipal(String identifier)
    //	{
    //		super(identifier,identifier,identifier);
    //	}
    //
    //
    //	/**
    //	 *
    //	 *
    //	 */
    //	protected IMCollabPrincipal(String identifier, boolean parseIdentifier)
    //	{
    //		this(identifier,
    //			parseIdentifier ? parseName(identifier) : identifier,
    //			parseIdentifier ? parseName(identifier) : identifier);
    //	}
    //
    //
    //	/**
    //	 *
    //	 *
    //	 */
    //	protected IMCollabPrincipal(String identifier, String name, 
    //		String displayName)
    //	{
    //		super(identifier,name,displayName);
    //	}

    /**
     *
     *
     */

    /*pkg*/ static String parseName(String identifier) {
        String result = identifier;

        // I identifier contains a name after a trailing slash (/), that 
        // is the display name of the contact.  Otherwise, the display name 
        // is the string before the @ symbol.
        int index = identifier.indexOf("/");

        if (index != -1) {
            result = identifier.substring(index + 1);
        } else {
            index = identifier.indexOf("@");

            if (index != -1) {
                result = identifier.substring(0, index);
            }
        }

        return result;
    }

    /**
     *
     *
     */
    protected CollaborationPrincipal getPrincipal() {
        return principal;
    }

    /**
     *
     *
     */
    public void subscribe() throws CollabException {
        try {
            ((IMCollabSession) getCollabSession()).getPresenceService().subscribe(getIdentifier());
        } catch (CollaborationException e) {
            throw new CollabException(e, "Exception subscribing to principal \"" + this + "\""); // NOI18N
        }
    }

    /**
     *
     *
     */
    public void unsubscribe() throws CollabException {
        try {
            ((IMCollabSession) getCollabSession()).getPresenceService().unsubscribe(getIdentifier());
        } catch (CollaborationException e) {
            throw new CollabException(e, "Exception unsubscribing from principal \"" + // NOI18N
                this + "\""
            ); // NOI18N
        }
    }
}
