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
