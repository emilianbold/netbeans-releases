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
                addURL(f.toURL());
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
