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

package org.netbeans.modules.j2ee.deployment.impl;

/**
 * Signals that a server exception of some sort has occurred.
 *
 * @author sherold
 */
public class ServerException extends Exception {
    
    public ServerException(String msg) {
        super(msg);
    }
    
    public ServerException(Throwable t) {
        super(t);
    }
    
    public ServerException(String s, Throwable t) {
        super(s, t);
    }
}
