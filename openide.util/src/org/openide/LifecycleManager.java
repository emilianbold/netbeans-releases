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

package org.openide;

import org.openide.util.Lookup;

/** Manages major aspects of the NetBeans lifecycle - currently saving all objects and exiting.
 * @author Jesse Glick
 * @since 3.14
 */
public abstract class LifecycleManager {
    /** Subclass constructor. */
    protected LifecycleManager() {
    }

    /**
     * Get the default lifecycle manager.
     * Normally this is found in {@link Lookup#getDefault} but if no instance is
     * found there, a fallback instance is returned which behaves as follows:
     * <ol>
     * <li>{@link #saveAll} does nothing
     * <li>{@link #exit} calls {@link System#exit} with an exit code of 0
     * </ol>
     * This is useful for unit tests and perhaps standalone library usage.
     * @return the default instance (never null)
     */
    public static LifecycleManager getDefault() {
        LifecycleManager lm = Lookup.getDefault().lookup(LifecycleManager.class);

        if (lm == null) {
            lm = new Trivial();
        }

        return lm;
    }

    /** Save all opened objects.
     */
    public abstract void saveAll();

    /** Exit NetBeans.
     * This method will return only if {@link java.lang.System#exit} fails, or if at least one component of the
     * system refuses to exit (because it cannot be properly shut down).
     */
    public abstract void exit();

    /** Fallback instance. */
    private static final class Trivial extends LifecycleManager {
        public Trivial() {
        }

        public void exit() {
            System.exit(0);
        }

        public void saveAll() {
        }
    }
}
