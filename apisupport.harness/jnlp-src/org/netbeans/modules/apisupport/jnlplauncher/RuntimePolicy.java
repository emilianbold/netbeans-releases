/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Nokia. Portions Copyright 2006 Nokia. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.jnlplauncher;

import java.security.*;

/**
 * Policy giving all permissions to all of the code.
 *
 * @author David Strupl
 */
class RuntimePolicy extends Policy {
    /** PermissionCollection with an instance of AllPermission. */
    private static PermissionCollection permissions;
    /** @return initialized set of AllPermissions */
    private static synchronized PermissionCollection getAllPermissionCollection() {
        if (permissions == null) {
            permissions = new Permissions();
            permissions.add(new AllPermission());
            permissions.setReadOnly();
        }
        return permissions;
    }

    public PermissionCollection getPermissions(CodeSource codesource) {
        return getAllPermissionCollection();
    }
    
    public boolean implies(ProtectionDomain domain, Permission permission) {
        return getPermissions(domain.getCodeSource()).implies(permission);
    }
    
    public PermissionCollection getPermissions(ProtectionDomain domain) {
        return getPermissions(domain.getCodeSource());
    }
    
    public void refresh() {
    }
}
