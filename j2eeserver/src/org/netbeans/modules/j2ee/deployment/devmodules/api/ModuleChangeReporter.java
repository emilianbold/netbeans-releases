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

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor;

/** 
 * This interface allows a development module to express what about a module
 * or application has changed since the last deployment.  This information
 * can be passed to the plugin via the ModuleChangeDescriptor interface.
 * An implementation of this interface should be provided in the build target
 * lookup adjacent to the J2eeModule implementation.
 * @author  George Finklang
 */
public interface ModuleChangeReporter {
    
    /* Get all the changes since the time indicated by the timestmap. */
    public EjbChangeDescriptor getEjbChanges(long timestamp);
    
    public boolean isManifestChanged(long timestamp);

}
