/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package threaddemo.apps.index;

import java.util.Map;
import javax.swing.event.ChangeListener;
import threaddemo.locking.Lock;
import threaddemo.model.Phadhail;

/**
 * Maintains a read-only index of elements in XML phadhail documents.
 * @author Jesse Glick
 */
interface Index {
    
    /**
     * Get the root of the tree being searched.
     */
    Phadhail getRoot();
    
    /**
     * Begin parsing, if it has not already been begun.
     */
    void start();
    
    /**
     * Stop any ongoing processing if there was any.
     * Results will not generally be valid after this.
     */
    void cancel();
    
    /**
     * Get the index data.
     * Keys are XML element names.
     * Values are occurrence counts.
     * <p>Must be called with lock held, and result may only be accessed with it held.
     */
    Map/*<String,int>*/ getData();
    
    /**
     * Add a listener to changes in the data.
     */
    void addChangeListener(ChangeListener l);
    
    /**
     * Remove a listener.
     */
    void removeChangeListener(ChangeListener l);
    
    /**
     * Associated lock.
     */
    Lock getLock();
    
}
