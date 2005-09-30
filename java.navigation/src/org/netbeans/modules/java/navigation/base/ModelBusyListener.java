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

package org.netbeans.modules.java.navigation.base;

/** Interface for navigator models to notify clients about their not-ready
 * state, during long computations.
 * 
 * Note, this is temporary and will be deleted and replaced by simpler JComponent
 * navigator based API.
 *
 * @author Dafe Simonek
 */
public interface ModelBusyListener {
    
    /** Computation started.
     * Threading: Can be called on any thread
     */
    public void busyStart ();
    
    /** Computation finished.
     * Threading: Can be called on any thread
     */
    public void busyEnd ();

    /** Called when new content was loaded and is ready. It means that 
     * list data change events was already fired and so the Swing component
     * which contains the model already knows about new data.
     * Currently used only to keep selection in swing components after 
     * load of new data.
     *
     * Threading: Always called from EQT 
     */ 
    public void newContentReady ();
    
}
