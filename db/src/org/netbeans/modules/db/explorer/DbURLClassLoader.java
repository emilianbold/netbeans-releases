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

package org.netbeans.modules.db.explorer;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Arrays;

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
    
    public String toString() {
        return "DbURLClassLoader[urls=" + Arrays.asList(getURLs()) + "]"; // NOI18N
    }
}
