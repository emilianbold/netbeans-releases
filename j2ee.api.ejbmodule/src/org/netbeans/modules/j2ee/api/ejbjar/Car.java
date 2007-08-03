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

import java.util.Collections;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.ejbjar.CarAccessor;
import org.netbeans.modules.j2ee.metadata.ClassPathSupport;
import org.netbeans.modules.j2ee.spi.ejbjar.CarImplementation;
import org.netbeans.modules.j2ee.spi.ejbjar.CarProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.CarsInProject;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Car should be used to access properties of an Enterprise application client module.
 * <p>
 * A client may obtain a Car instance using 
 * <code>Car.getCar(fileObject)</code> static method, for any 
 * FileObject in the application client module directory structure.
 * </p>
 * <div class="nonnormative">
 * Note that the particular directory structure for application client module
 * is not guaranteed by this API.
 * </div>
 * 
 * 
 * @author Pavel Buzek
 * @author Lukas Jungmann
 */
public final class Car {
    private CarImplementation impl;
    private static final Lookup.Result<CarProvider> implementations =
        Lookup.getDefault().lookup(new Lookup.Template<CarProvider>(CarProvider.class));
    
    static  {
        CarAccessor.DEFAULT = new CarAccessor() {
            public Car createCar(CarImplementation spiEjbJar) {
                return new Car(spiEjbJar);
            }

            public CarImplementation getCarImplementation(Car wm) {
                return wm == null ? null : wm.impl;
            }
        };
    }
    
    private Car (CarImplementation impl) {
        if (impl == null)
            throw new IllegalArgumentException ();
        this.impl = impl;
    }
    
    /**
     * Find the Car for given file or null if the file does not belong
     * to any application client module.
     */
    public static Car getCar (FileObject f) {
        if (f == null) {
            throw new NullPointerException("Passed null to Car.getCar(FileObject)"); // NOI18N
        }
        for (CarProvider impl : implementations.allInstances()) {
            Car wm = impl.findCar (f);
            if (wm != null) {
                return wm;
            }
        }
        return null;
    }

    /** Find Car(s) for all application clients within a given project.
     * @return an array of Car instance (empty array if no instance are found).
     */
    public static Car[] getCars (Project project) {
        CarsInProject providers = project.getLookup().lookup(CarsInProject.class);
        if (providers != null) {
            Car jars [] = providers.getCars();
            if (jars != null) {
                return jars;
            }
        }
        return new Car[] {};
    }
    
    /** J2EE platform version - one of the constants 
     * defined in {@link org.netbeans.modules.j2ee.api.common.J2eeProjectConstants}.
     * @return J2EE platform version
     */
    public String getJ2eePlatformVersion () {
        return impl.getJ2eePlatformVersion();
    }
    
    /** Deployment descriptor (application-client.xml file) of the application client module.
     */
    public FileObject getDeploymentDescriptor () {
        return impl.getDeploymentDescriptor();
    }

    /** Source roots associated with the Car module.
     * <div class="nonnormative">
     * Note that not all the java source roots in the project (e.g. in a freeform project)
     * belong to the Car module.
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
