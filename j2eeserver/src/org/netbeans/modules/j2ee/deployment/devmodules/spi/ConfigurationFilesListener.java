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

package org.netbeans.modules.j2ee.deployment.devmodules.spi;

import org.openide.filesystems.FileObject;

/**
 * Listener to existency change of configuration files in a J2EE module
 * provider project.  Possible configuration file list for a module provider
 * consists of configuration files for the J2EE module type from all 
 * registered server platforms.
 *
 * @author  nn136682
 */
public interface ConfigurationFilesListener {
    /**
     * A configuration file has been created.
     * @param added the newly created configuration file.
     */
    public void fileCreated(FileObject added);

    /**
     * A configuration file has been removed.
     * @param added the newly removed configuration file.
     */
    public void fileDeleted(FileObject removed);
    
}
