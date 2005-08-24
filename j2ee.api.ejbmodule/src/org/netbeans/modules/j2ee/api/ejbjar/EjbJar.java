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
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.ejbjar.EjbJarAccessor;
import org.netbeans.modules.j2ee.spi.ejbjar.*;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * EjbJar should be used to access properties of an ejb jar module.
 * <p>
 * A client may obtain a EjbJar instance using 
 * <code>EjbJar.getEjbJar(fileObject)</code> static method, for any 
 * FileObject in the ejb jar module directory structure.
 * </p>
 * <div class="nonnormative">
 * Note that the particular directory structure for ejb jar module is not guaranteed 
 * by this API.
 * </div>
 *
 * @author  Pavel Buzek
 */
public final class EjbJar {
    private EjbJarImplementation impl;
    private static final Lookup.Result implementations =
        Lookup.getDefault().lookup(new Lookup.Template(EjbJarProvider.class));
    
    static  {
        EjbJarAccessor.DEFAULT = new EjbJarAccessor() {
            public EjbJar createEjbJar(EjbJarImplementation spiEjbJar) {
                return new EjbJar(spiEjbJar);
            }

            public EjbJarImplementation getEjbJarImplementation(EjbJar wm) {
                return wm == null ? null : wm.impl;
            }
        };
    }
    
    private EjbJar (EjbJarImplementation impl) {
        if (impl == null)
            throw new IllegalArgumentException ();
        this.impl = impl;
    }
    
    /** Find the EjbJar for given file or null if the file does not belong
     * to any web module.
     */
    public static EjbJar getEjbJar (FileObject f) {
        if (f == null) {
            throw new NullPointerException("Passed null to EjbJar.getEjbJar(FileObject)"); // NOI18N
        }
        Iterator it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            EjbJarProvider impl = (EjbJarProvider)it.next();
            EjbJar wm = impl.findEjbJar (f);
            if (wm != null) {
                return wm;
            }
        }
        return null;
    }

    /** Find EjbJar(s) for all ejb modules within a given project.
     * @return an array of EjbJar instance (empty array if no instance are found).
     */
    public static EjbJar [] getEjbJars (Project project) {
        EjbJarsInProject providers = (EjbJarsInProject) project.getLookup().lookup(EjbJarsInProject.class);
        if (providers != null) {
            EjbJar jars [] = providers.getEjbJars();
            if (jars != null) {
                return jars;
            }
        }
        return new EjbJar[] {};
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

    /** Source roots associated with the EJB module.
     * <div class="nonnormative">
     * Note that not all the java source roots in the project (e.g. in a freeform project)
     * belong to the EJB module.
     * </div>
     */
    public FileObject[] getJavaSources() {
        return impl.getJavaSources();
    }
    
    /** Meta-inf
     */
    public FileObject getMetaInf() {
        return impl.getMetaInf();
    }
}
