/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer;

/**
 * Class needed for Java Studio Enterprise feature.
 * @author  Administrator
 */
public class OpenConnection implements OpenConnectionInterface {
    
    /** Creates a new instance of OpenConnection */
    public OpenConnection() {
    }

    public boolean isFor(String driverName) {
        return true;
    }
    
    public void enable() {
        // No implementation in open source.
    }
    
    
    public void disable() {
        // No implementation in open source.
    }
}
