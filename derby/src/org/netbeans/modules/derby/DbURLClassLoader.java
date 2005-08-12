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

package org.netbeans.modules.derby;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;

/**
 * This class loader is used to load JDBC drivers from their locations.
 * Needed since JDBC drivers can reside in arbitrary locations, which the
 * system class loader does not know about.
 */
public class DbURLClassLoader extends URLClassLoader {
    
    /** Creates a new instance of DbURLClassLoader */
    public DbURLClassLoader(URL[] urls) {
        super(urls);
    }
    
    protected PermissionCollection getPermissions(CodeSource codesource) {
        Permissions permissions = new Permissions();
        permissions.add(new AllPermission());
        permissions.setReadOnly();
        
        return permissions;
    }
}
