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

package org.netbeans.modules.j2ee.deployment.plugins.api;

import java.io.File;

/** 
 * This interface allows a plugin to find out what about a module
 * or application has changed since the last deployment. 
 * @author  George Finklang
 */
public interface ModuleChangeDescriptor {
    /**
     * Return true if any of the standard or server specific deployment descriptors have changed.
     */
    public boolean descriptorChanged();
    
    /**
     * Return true if any of the standard or server specific deployment descriptors have changed.
     */
    public boolean serverDescriptorChanged();
    
    /**
     * Return true if any file changes require the module class loader refresh.
     */
    public boolean classesChanged();
    
    /**
     * Return true if the manifest.mf of the module has changed.
     */
    public boolean manifestChanged();

    /**
     * Returns distribution relative paths of changed files.
     */
    public File[] getChangedFiles();
}
