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

import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;

public interface EjbReference {
    public AntArtifact getClientJarTarget();
    public boolean supportsLocalInvocation();
    public boolean supportsRemoteInvocation();

    public EjbRef createRef();
    public EjbLocalRef createLocalRef();

    public void populateReference(EjbRef ref);
    public void populateReference(EjbLocalRef ref);
       
    /**
     * Create JNDI Lookup method and do no throw any checked exceptions in the
     * generated method.
     */
    public Method generateJNDILookup(EjbRef ref, boolean throwExceptions);
    
    /**
     * Create JNDI Lookup method and do no throw any checked exceptions in the
     * generated method.
     */
    public Method generateJNDILookup(EjbLocalRef ref, boolean throwExceptions);

}
