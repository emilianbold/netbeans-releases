/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans;

import java.io.IOException;

/** Exception thrown indicating that a module's contents are ill-formed.
 * This could be a parse error in the manifest, or an inability to load
 * certain resources from the classloader.
 * ErrorManager should be used where needed to attach related exceptions
 * or user-friendly annotations.
 * @author Jesse Glick
 */
public final class InvalidException extends IOException {
    
    private final Module m;
    
    public InvalidException(String detailMessage) {
        super(detailMessage);
        m = null;
    }
    
    public InvalidException(Module m, String detailMessage) {
        super(m + ": " + detailMessage); // NOI18N
        this.m = m;
    }
    
    /** Affected module. May be null if this is hard to determine
     * (for example a problem which would make the module ill-formed,
     * during creation or reloading).
     */
    public Module getModule() {
        return m;
    }
    
}
