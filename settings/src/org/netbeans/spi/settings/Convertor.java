/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.settings;

/** Convertor allows to read/write objects in own format and notify about
 * object changes.
 *
 * @author  Jan Pokorsky
 */
public abstract class Convertor {
    
    /** Subclasses can implement own storing format.
     * @param w stream into which inst is written
     * @param inst the setting object to be written
     * @exception IOException if the object cannot be written
     */
    public abstract void write (java.io.Writer w, Object inst) throws java.io.IOException;
    
    /** Subclasses have to be able to read format implemented by {@link #write}.
     * @param r stream containing stored object
     * @return the read setting object
     * @exception IOException if the object cannot be read
     * @exception ClassNotFoundException if the object class cannot be resolved
     */
    public abstract Object read (java.io.Reader r) throws java.io.IOException, ClassNotFoundException;
    
    /** register {@link Saver saver}; convertor can provide own policy notifing
     * the saver about changes of setting object. (e.g. register property
     * change listener)
     * @param inst setting object
     * @param s saver implementation
     */
    public abstract void registerSaver (Object inst, Saver s);
    
    /** unregister {@link Saver saver}
     * @param inst setting object
     * @param s saver implementation
     * @see #registerSaver
     */
    public abstract void unregisterSaver (Object inst, Saver s);
}
