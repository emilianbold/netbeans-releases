/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.windows;

import org.openide.util.Lookup;

/** Serves as a factory for I/O tabs.
 * @author Jesse Glick
 * @since 3.14
 */
public abstract class IOProvider {

    /** Get the default I/O provider.
     * @return the default instance from lookup
     */
    public static IOProvider getDefault() {
        return (IOProvider)Lookup.getDefault().lookup(IOProvider.class);
    }

    /** Subclass constructor. */
    protected IOProvider() {}

    /** Support reading from and writing to a specific tab on the Output Window or a similar output device.
     * @param name desired name of the tab
     * @param newIO if <tt>true</tt>, a new <code>InputOutput</code> is returned, else an existing <code>InputOutput</code> of the same name may be returned
     * @return an <code>InputOutput</code> class for accessing the new tab
     */
    public abstract InputOutput getIO(String name, boolean newIO);

    /** Support writing to the Output Window on the main tab or a similar output device.
     * @return a writer for the standard NetBeans output area
     */
    public abstract OutputWriter getStdOut();

}
