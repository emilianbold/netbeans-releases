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

package threaddemo.apps.index;

import java.util.Map;
import javax.swing.event.ChangeListener;
import threaddemo.locking.RWLock;
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
    Map<String,Integer> getData();
    
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
    RWLock getLock();
    
}
