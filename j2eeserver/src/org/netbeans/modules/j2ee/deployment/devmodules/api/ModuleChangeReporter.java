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
