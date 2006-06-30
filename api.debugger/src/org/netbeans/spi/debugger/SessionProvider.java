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

