package org.netbeans.modules.j2ee.api.ejbjar;

import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.web.dd.EjbLocalRef;
import org.netbeans.api.web.dd.EjbRef;
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
     * 
     *@deprecated use generateJNDILookup(EjbRef, boolean) instead
     */
    public MethodElement generateJNDILookup(EjbRef ref);
    /**
     * 
     *@deprecated use generateJNDILookup(EjbLocalRef, boolean) instead
     */
    public MethodElement generateJNDILookup(EjbLocalRef ref);
    
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
