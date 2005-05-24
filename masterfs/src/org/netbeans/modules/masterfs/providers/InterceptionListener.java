/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.masterfs.providers;

/**
 *
 * @author Radek Matous
 */

public interface InterceptionListener {
    void beforeDelete(org.openide.filesystems.FileObject fo);
    void deleteSuccess(org.openide.filesystems.FileObject fo);
    void deleteFailure(org.openide.filesystems.FileObject fo);    
}
