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

package org.netbeans.modules.j2ee.ejbjar;

import java.util.HashMap;
import org.netbeans.modules.j2ee.api.common.EjbProjectConstants;
import org.netbeans.modules.j2ee.api.ejbjar.*;
import org.netbeans.modules.j2ee.spi.ejbjar.*;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.web.api.webmodule.WebModule;

/** A dummy provider that things that any *.foo file belongs to its web module.
 *
 * @author  Pavel Buzek
 */
public class CustomProviderEar implements EarProvider {
    
    private HashMap cache = new HashMap ();
    
    public CustomProviderEar () {
    }
    
    public Ear findEar (FileObject file) {
        if (file.getExt ().equals ("foo")) {
            Ear em  = (Ear) cache.get (file.getParent ());
            if (em == null) {
                em = EjbJarFactory.createEar (new EM (file.getParent (), EjbProjectConstants.J2EE_14_LEVEL));
                cache.put (file.getParent (), em);
            }
            return em;
        }
        return null;
    }
    
    private class EM implements EarImplementation {
        FileObject root;
        String ver;
        
        public EM (FileObject root, String ver) {
            this.root = root;
            this.ver = ver;
        }
        
        public String getJ2eePlatformVersion () {
            return ver;
        }
        
        public FileObject getDeploymentDescriptor () {
            return root.getFileObject ("conf/ejb-jar.xml");
        }
        
        public FileObject getMetaInf () {
            return null;
        }
        
        public void addEjbJarModule(EjbJar module) {
            
        }
        public void addWebModule(WebModule module) {
            
        }
    }
}
