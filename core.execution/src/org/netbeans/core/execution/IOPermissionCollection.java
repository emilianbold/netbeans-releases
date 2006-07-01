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

package org.netbeans.core.execution;

import java.security.PermissionCollection;
import java.security.Permission;
import java.util.Enumeration;

import org.openide.windows.InputOutput;

/** Every running process is represented by several objects in the ide whether
* or not it is executed as a thread or standalone process. The representation
* of a process should be marked by the IOPermissionCollection that gives possibility
* to such process to do its System.out/in operations through the ide.
*
* @author Ales Novak
*/
final class IOPermissionCollection extends PermissionCollection implements java.io.Serializable {

    /** InputOutput for this collection */
    private InputOutput io;
    /** Delegated PermissionCollection. */
    private PermissionCollection delegated;
    /** TaskThreadGroup ref or null */
    final TaskThreadGroup grp;

    static final long serialVersionUID =2046381622544740109L;
    /** Constructs new ExecutionIOPermission. */
    protected IOPermissionCollection(InputOutput io, PermissionCollection delegated, TaskThreadGroup grp) {
        this.io = io;
        this.delegated = delegated;
        this.grp = grp;
    }

    /** Standard implies method see java.security.Permission.
    * @param p a Permission
    */
    public boolean implies(Permission p) {
        return delegated.implies(p);
    }
    /** @return Enumeration of all Permissions in this collection. */
    public Enumeration<Permission> elements() {
        return delegated.elements();
    }
    /** @param perm a Permission to add. */
    public void add(Permission perm) {
        delegated.add(perm);
    }

    /** @return "" */ // NOI18N
    public InputOutput getIO() {
        return io;
    }
    /** Sets new io for this PermissionCollection */
    public void setIO(InputOutput io) {
        this.io = io;
    }

    public String toString() {
        return delegated.toString();
    }
}
