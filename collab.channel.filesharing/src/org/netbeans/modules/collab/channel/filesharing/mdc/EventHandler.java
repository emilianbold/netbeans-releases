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
package org.netbeans.modules.collab.channel.filesharing.mdc;

import com.sun.collablet.CollabException;


/**
 * Interface for Collab Filesharing FileHandlers
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public interface EventHandler {
    ////////////////////////////////////////////////////////////////////////////
    // Event Handler methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * exec
     *
     * @param        context                                        Context
     */
    public void exec(String eventID, EventContext evContext)
    throws CollabException;

    /**
     * setValid
     *
     * @param        valid
     */
    public void setValid(boolean valid);

    /**
     * test if the filehandler is valid
     *
     * @return        true/false                                true if valid
     */
    public boolean isValid();
}
