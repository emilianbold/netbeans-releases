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
    
    public MethodElement generateJNDILookup(EjbRef ref);
    
    public MethodElement generateJNDILookup(EjbLocalRef ref);
}
