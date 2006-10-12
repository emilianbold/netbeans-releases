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

import org.openide.nodes.Node;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.jmi.javamodel.Feature;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;

/**
 *
 * @author Martin Adamek
 */
public interface EjbReference extends Node.Cookie {
    
    boolean supportsLocalInvocation();
    
    boolean supportsRemoteInvocation();
    
    void populateReference(EjbRef ref);
    
    void populateReference(EjbLocalRef ref);

    AntArtifact getClientJarTarget();
    
    EjbRef createRef();
    
    EjbLocalRef createLocalRef();
    
    Feature generateReferenceCode(JavaClass target, EjbRef ref, boolean throwExceptions);
    
    Feature generateReferenceCode(JavaClass target, EjbLocalRef ref, boolean throwExceptions);
    
    Feature generateServiceLocatorLookup(JavaClass target, EjbRef ref, String serviceLocatorName, boolean throwExceptions);
    
    Feature generateServiceLocatorLookup(JavaClass target, EjbLocalRef ref, String serviceLocatorName, boolean throwExceptions);
    
}
