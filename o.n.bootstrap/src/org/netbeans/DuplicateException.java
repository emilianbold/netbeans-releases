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

/** Exception indicating that a module with a given code name base
 * is already being managed, and that it is not permitted to add
 * another with the same name.
 * @author Jesse Glick
 */
public final class DuplicateException extends Exception {

    private transient Module old, nue;

    DuplicateException(Module old, Module nue) {
        // XXX if nue.jarFile == old.jarFile, produce special message
        super(getInfo(nue) + " is a duplicate of " + getInfo(old)); // NOI18N
        this.old = old;
        this.nue = nue;
    }
    private static String getInfo(Module m) {
        if (m.getJarFile() != null) {
            return m.getJarFile().getAbsolutePath();
        } else {
            return m.getCodeNameBase();
        }
    }
    
    /** Get the module which is already known to exist.
     */
    public Module getOldModule() {
        return old;
    }
    
    /** Get the module whose creation was attempted.
     * <strong>Warning:</strong> this module will be invalid,
     * so do not attempt to do anything with it beyond asking
     * it for its version and things like that.
     */
    public Module getNewModule() {
        return nue;
    }
    
}
