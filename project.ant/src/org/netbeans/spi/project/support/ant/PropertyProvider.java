/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.support.ant;

import java.util.Map;
import javax.swing.event.ChangeListener;

/**
 * Provides a set of Ant property definitions that might be evaluated in
 * some context.
 * <p>
 * This interface defines no independent thread safety, but in typical usage
 * it will be used with the project manager mutex. Changes should be fired
 * synchronously.
 * @author Jesse Glick
 */
/*XXX public*/ interface PropertyProvider {
    
    /**
     * Get all defined properties.
     * The values might contain Ant-style property references.
     * @return all properties defined in this block
     */
    Map/*<String,String>*/ getProperties();
    
    /**
     * Add a change listener.
     * When the set of available properties, or some of the values, change,
     * this listener should be notified.
     * @param l a listener to add
     */
    void addChangeListener(ChangeListener l);
    
    /**
     * Remove a change listener.
     * @param l a listener to remove
     */
    void removeChangeListener(ChangeListener l);
    
}
