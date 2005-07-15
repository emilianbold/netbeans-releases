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

package org.netbeans.spi.debugger;

/**
 * Creates a new instance of {@link org.netbeans.api.debugger.Session}
 * for some {@link org.netbeans.api.debugger.DebuggerInfo}.
 *
 * @author Jan Jancura
 */
public abstract class SessionProvider {

    
    /**
     * Name of the new session.
     * 
     * @return name of new session
     */
    public abstract String getSessionName ();
    
    /**
     * Location of the new session.
     * 
     * @return location of new session
     */
    public abstract String getLocationName ();
    
    /**
     * Returns identifier of {@link org.netbeans.api.debugger.Session} crated
     * by thisprovider.
     *
     * @return identifier of new Session
     */
    public abstract String getTypeID ();

    /**
     * Returns array of services for 
     * {@link org.netbeans.api.debugger.Session} provided by this 
     * SessionProvider.
     *
     * @return array of services
     */
    public abstract Object[] getServices ();
}

