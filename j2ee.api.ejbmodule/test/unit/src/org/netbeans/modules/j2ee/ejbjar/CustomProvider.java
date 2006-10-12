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

package org.netbeans.modules.j2ee.ejbjar;

import java.util.Collections;
import java.util.HashMap;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.api.ejbjar.*;
import org.netbeans.modules.j2ee.metadata.MetadataUnit;
import org.netbeans.modules.j2ee.spi.ejbjar.*;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;

/** A dummy provider that things that any *.foo file belongs to its web module.
 *
 * @author  Pavel Buzek
 */
public class CustomProvider implements EjbJarProvider {

    private HashMap cache = new HashMap ();

    public CustomProvider () {
    }

    public EjbJar findEjbJar (FileObject file) {
        if (file.getExt ().equals ("foo")) {
            EjbJar em  = (EjbJar) cache.get (file.getParent ());
            if (em == null) {
                em = EjbJarFactory.createEjbJar (new EM (file.getParent (), EjbProjectConstants.J2EE_14_LEVEL));
                cache.put (file.getParent (), em);
            }
            return em;
        }
        return null;
    }
    
    private class EM implements EjbJarImplementation {
        FileObject root;
        String ver;
        private MetadataUnit metadataUnit;
        
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

        public FileObject[] getJavaSources() {
            return null;
        }

        public MetadataUnit getMetadataUnit() {
            synchronized (this) {
                if (metadataUnit == null) {
                    metadataUnit = new MetadataUnitImpl();
                }
                return metadataUnit;
            }
        }

        private class MetadataUnitImpl implements MetadataUnit {
            public ClassPath getClassPath() {
                return ClassPathSupport.createClassPath(Collections.<PathResourceImplementation>emptyList());
            }
            public FileObject getDeploymentDescriptor() {
                return EM.this.getDeploymentDescriptor();
            }
        }
    }
}
