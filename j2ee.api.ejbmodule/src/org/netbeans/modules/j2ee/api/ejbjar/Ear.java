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
package org.netbeans.modules.j2ee.api.ejbjar;

import java.util.Iterator;
import org.netbeans.modules.j2ee.ejbjar.EarAccessor;
import org.netbeans.modules.j2ee.spi.ejbjar.*;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.netbeans.modules.web.api.webmodule.WebModule;

/**
 * Ear should be used to access properties of an ear module.
 * <p>
 * A client may obtain a Ear instance using 
 * <code>Ear.getEar(fileObject)</code> static method, for any 
 * FileObject in the ear module directory structure.
 * </p>
 * <div class="nonnormative">
 * Note that the particular directory structure for ear module is not guaranteed 
 * by this API.
 * </div>
 *
 * @author  Pavel Buzek
 */
public final class Ear {
    private EarImplementation impl;
    private static final Lookup.Result implementations =
        Lookup.getDefault().lookup(new Lookup.Template(EarProvider.class));
    
    static  {
        EarAccessor.DEFAULT = new EarAccessor() {
            public Ear createEar(EarImplementation spiEar) {
                return new Ear(spiEar);
            }

            public EarImplementation getEarImplementation(Ear wm) {
                return wm == null ? null : wm.impl;
            }
        };
    }
    
    private Ear (EarImplementation impl) {
        if (impl == null)
            throw new IllegalArgumentException ();
        this.impl = impl;
    }
    
    /** Find the Ear for given file or null if the file does not belong
     * to any web module.
     */
    public static Ear getEar (FileObject f) {
        if (f == null) {
            throw new NullPointerException("Passed null to Ear.getEar(FileObject)"); // NOI18N
        }
        Iterator it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            EarProvider impl = (EarProvider)it.next();
            Ear wm = impl.findEar (f);
            if (wm != null) {
                return wm;
            }
        }
        return null;
    }

    
    /** J2EE platform version - one of the constants 
     * defined in {@link org.netbeans.modules.j2ee.api.common.EjbProjectConstants}.
     * @return J2EE platform version
     */
    public String getJ2eePlatformVersion () {
        return impl.getJ2eePlatformVersion();
    }
    
    /** Deployment descriptor (ejb-jar.xml file) of the ejb module.
     */
    public FileObject getDeploymentDescriptor () {
        return impl.getDeploymentDescriptor();
    }
    /** Add j2ee webmodule into application.
     * @param module the module to be added
     */
    public void addWebModule (WebModule module) {
        impl.addWebModule (module);
    }
    
    /** Add j2ee Ear module into application.
     * @param module the module to be added
     */
    public void addEjbJarModule (EjbJar module) {
        impl.addEjbJarModule (module);
    }
}
