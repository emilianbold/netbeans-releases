/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.properties;

import java.util.EventListener;

/**
 * Defines an interface for objects to be notified of changes to a bundle.
 *
 * @author Petr Jiricka
 */
public interface PropertyBundleListener extends EventListener {

    /**
     * Gives notification that a bundle has changed.
     *
     * @param  e  event that describes the change
     */
    public void bundleChanged(PropertyBundleEvent e);

}

