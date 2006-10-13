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

package org.netbeans.modules.websvc.registry.netbeans;

import java.security.PermissionCollection;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.AllPermission;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;


public class ExtensionClassLoader extends URLClassLoader {

    /** Creates new ExtensionClassLoader  ....*/
    public ExtensionClassLoader() throws MalformedURLException, RuntimeException {
        super(new URL[0]);
    }

    public ExtensionClassLoader( ClassLoader _loader) throws MalformedURLException, RuntimeException {
        super(new URL[0], _loader);
    }
    

        
    public void addURL(File f) throws MalformedURLException, RuntimeException {
            if (f.isFile()){
                addURL(f.toURI().toURL());
              ///  System.out.println("adding file  = "+f.getAbsolutePath());
              ///  System.out.println("adding file  exists= "+f.exists() );
            }
            else{
              ///  System.out.println("file does not exist!!! = "+f.getAbsolutePath());
            }
    }
    
    protected PermissionCollection getPermissions(CodeSource _cs) {
        Permissions p = new Permissions();
        p.add(new AllPermission());
        return p;
    }

}
