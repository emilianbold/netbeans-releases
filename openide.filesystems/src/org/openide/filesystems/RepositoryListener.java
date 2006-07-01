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
package org.openide.filesystems;


/** Listener to changes in the filesystem pool.
*
* @author Jaroslav Tulach
* @version 0.10 November 4, 1997
*/
public interface RepositoryListener extends java.util.EventListener {
    /** Called when new filesystem is added to the pool.
    * @param ev event describing the action
    */
    public void fileSystemAdded(RepositoryEvent ev);

    /** Called when a filesystem is removed from the pool.
    * @param ev event describing the action
    */
    public void fileSystemRemoved(RepositoryEvent ev);

    /** Called when a filesystem pool is reordered. */
    public void fileSystemPoolReordered(RepositoryReorderedEvent ev);
}
