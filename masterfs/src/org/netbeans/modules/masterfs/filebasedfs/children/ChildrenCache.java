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

package org.netbeans.modules.masterfs.filebasedfs.children;

import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.openide.util.Mutex;

import java.util.Map;
import java.util.Set;

public interface ChildrenCache {
    Integer ADDED_CHILD = new Integer(0);
    Integer REMOVED_CHILD = new Integer(1);

    Set getChildren(boolean rescan);

    FileNaming getChild(String childName, boolean rescan);

    boolean existsInCache(String childName);    

    Map refresh();

    Mutex.Privileged getMutexPrivileged();
}
