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

/** The Saver should be used as a callback to the framework implementation
 * to handle setting object changes.
 *
 * @author  Jan Pokorsky
 */
public interface Saver {
    /** Notify the framework to be aware of the setting object is changed.
     */
    public void markDirty();
    
    /** Notify the framework the setting object is changed and can be written down
     * @exception IOException if the save cannot be performed
     */
    public void requestSave() throws java.io.IOException;
}
