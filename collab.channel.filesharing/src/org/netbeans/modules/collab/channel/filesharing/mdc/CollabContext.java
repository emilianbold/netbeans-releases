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

import com.sun.collablet.*;


/**
 * Bean that holds channel context
 *
 * @author Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class CollabContext extends Object {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* currentVersion */
    protected String currentVersion = null;

    /* channel */
    protected Collablet collablet = null;

    /**
         *
         * @param channel
         */
    public CollabContext(String currentVersion, Collablet collablet) {
        super();
        this.currentVersion = currentVersion;
        this.collablet = collablet;
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     * @return version
     */
    public String getCurrentVersion() {
        return this.currentVersion;
    }

    /**
     * getChannel
     *
     * @return channel
     */
    public Collablet getChannel() {
        return this.collablet;
    }
}
