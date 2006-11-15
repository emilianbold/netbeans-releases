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

package org.netbeans.modules.j2ee.ejbcore.test;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.metadata.MetadataUnit;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class EjbJarImplementationImpl implements EjbJarImplementation {
    
    private final String j2eePlatformVersion;
    private final FileObject ddFileObject;
    private final FileObject[] sources;
    
    public EjbJarImplementationImpl(String j2eePlatformVersion, FileObject ddFileObject, FileObject[] sources) {
        this.j2eePlatformVersion = j2eePlatformVersion;
        this.ddFileObject = ddFileObject;
        this.sources = sources;
    }
    
    public String getJ2eePlatformVersion() {
        return j2eePlatformVersion;
    }
    
    public FileObject getMetaInf() {
        return ddFileObject.getParent();
    }
    
    public FileObject getDeploymentDescriptor() {
        return ddFileObject;
    }
    
    public FileObject[] getJavaSources() {
        return sources;
    }
    
    public MetadataUnit getMetadataUnit() {
        return new MetadataUnit() {
            public FileObject getDeploymentDescriptor() {
                return ddFileObject;
            }
            public ClassPath getClassPath() {
                return ClassPathSupport.createClassPath(sources);
            }
        };
        
    }
    
}
