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
package org.netbeans.modules.collab.channel.filesharing.context;

import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab;


/**
 * Bean that holds channel context
 *
 * @author Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class MessageContext extends EventContext {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////	

    /* collabbean */
    CCollab collabBean = null;

    /* isUserSame */
    boolean isUserSame = false;

    /* channel */
    protected String messageOriginator = null;

    /**
         *
         * @param collabBean
         * @param messageOriginator
         * @param isUserSame
         */
    public MessageContext(String eventID, CCollab collabBean, String messageOriginator, boolean isUserSame) {
        super(eventID, collabBean);
        this.messageOriginator = messageOriginator;
        this.isUserSame = isUserSame;
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * getChannel
     *
     * @return channel
     */
    public CCollab getCollab() {
        return (CCollab) super.getSource();
    }

    /**
     * find if login user same as messageOriginator
     *
     * @return status
     */
    public boolean isUserSame() {
        return this.isUserSame;
    }

    /**
     * find if login user same as messageOriginator
     *
     * @return messageOriginator
     */
    public String getMessageOriginator() {
        return this.messageOriginator;
    }
}
