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
