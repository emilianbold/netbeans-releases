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
