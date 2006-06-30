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
