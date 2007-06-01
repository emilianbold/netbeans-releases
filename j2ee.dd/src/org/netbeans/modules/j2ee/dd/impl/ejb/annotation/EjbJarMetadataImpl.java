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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.dd.impl.ejb.annotation;

import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class EjbJarMetadataImpl implements EjbJarMetadata {
    
    private final EjbJar ejbJar;
    private final ClasspathInfo cpInfo;
    
    public EjbJarMetadataImpl(EjbJar ejbJar, ClasspathInfo cpInfo) {
        this.ejbJar = ejbJar;
        this.cpInfo = cpInfo;
    }

    public EjbJar getRoot() {
        return ejbJar;
    }

    public Ejb findByEjbClass(String ejbClass) {
        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
        if (enterpriseBeans != null) {
            for (Ejb ejb : enterpriseBeans.getEjbs()) {
                if (ejbClass.equals(ejb.getEjbClass())) {
                    return ejb;
                }
            }
        }
        return null;
    }

    public FileObject findResource(String resourceName) {
        return cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE).findResource(resourceName);
    }
    
}
