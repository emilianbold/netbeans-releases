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
package org.netbeans.modules.j2ee.api.ejbjar;

import org.netbeans.modules.j2ee.ejbjar.EarAccessor;
import org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation;
import org.netbeans.modules.j2ee.spi.ejbjar.EarProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.EarProvider;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

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
    
    private final EarImplementation impl;
    private static final Lookup.Result<EarProvider> implementations =
        Lookup.getDefault().lookup(new Lookup.Template<EarProvider>(EarProvider.class));
    
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
    
    /**
     * Find the Ear for given file or <code>null</code> if the file does not
     * belong to any Enterprise Application.
     */
    public static Ear getEar (FileObject f) {
        if (f == null) {
            throw new NullPointerException("Passed null to Ear.getEar(FileObject)"); // NOI18N
        }
        for (EarProvider earProvider : implementations.allInstances()) {
            Ear wm = earProvider.findEar(f);
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
    
    /** Add j2ee Ejb module into application.
     * @param module the module to be added
     */
    public void addEjbJarModule (EjbJar module) {
        impl.addEjbJarModule (module);
    }
    
    /** Add j2ee application client module into application.
     * @param module the module to be added
     */
    public void addCarModule(Car module) {
        impl.addCarModule(module);
    }
    
}
