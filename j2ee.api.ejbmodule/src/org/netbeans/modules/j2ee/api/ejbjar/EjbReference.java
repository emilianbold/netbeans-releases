/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.api.ejbjar;

import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.openide.src.MethodElement;

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
    public MethodElement generateJNDILookup(EjbRef ref, boolean throwExceptions);
    
    /**
     * Create JNDI Lookup method and do no throw any checked exceptions in the
     * generated method.
     */
    public MethodElement generateJNDILookup(EjbLocalRef ref, boolean throwExceptions);

}
