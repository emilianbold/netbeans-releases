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

package org.netbeans.modules.project.ant;
import java.util.EventListener;

/**
 * Listener for changes in file existence and/or contents.
 * Unlike the Filesystems API, renames etc. are not considered special;
 * the "file" is identified uniquely by its path, not an object.
 * @author Jesse Glick
 */
public interface FileChangeSupportListener extends EventListener {
    
    void fileCreated(FileChangeSupportEvent event);
    
    void fileDeleted(FileChangeSupportEvent event);
    
    void fileModified(FileChangeSupportEvent event);
    
}
