/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.filesharing.eventhandler;

import org.openide.util.*;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventHandler;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventHandlerFactory;


/**
 * Default EventHandler Factory
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class JoinFilesharingEndHandlerFactory extends Object implements EventHandlerFactory {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////

    /* FileHandlerFactory ID */
    public static final String EventHandler_FACTORY_ID = "JoinFilesharingEndHandlerFactory"; // NOI18N            

    /**
     * constructor
     *
     */
    public JoinFilesharingEndHandlerFactory() {
        super();
    }

    /**
     * getter for ID
     *
     * @return        ID
     */
    public String getID() {
        return EventHandler_FACTORY_ID;
    }

    /**
     * getter for displayName
     *
     * @return        displayName
     */
    public String getDisplayName() {
        return NbBundle.getMessage(EventHandlerFactory.class, "LBL_EventHandlerFactory_DisplayName"); // NOI18N
    }

    /**
     * create FileHandler instance
     *
     * @return        CollabFileHandler
     */
    public EventHandler createEventHandler(CollabContext context) {
        return new JoinFilesharingEndHandler(context);
    }

    /**
     *
     * @return EventHandler Factory
     */
    public static EventHandlerFactory getDefault() {
        /*return (EventHandlerFactory)Lookup.getDefault().
                lookup(EventHandlerFactory.class);*/
        return new JoinFilesharingEndHandlerFactory();
    }
}
