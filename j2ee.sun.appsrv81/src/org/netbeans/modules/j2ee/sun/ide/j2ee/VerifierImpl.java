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
/*
 * VerifierImpl.java
 *
 * Created on December 8, 2004, 2:54 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee;
import java.io.OutputStream;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.j2ee.deployment.common.api.ValidationException;
/**
 *
 * @author ludo
 */
public  class VerifierImpl extends org.netbeans.modules.j2ee.deployment.plugins.api.VerifierSupport {
    
    /** Creates a new instance of VerifierImpl */
    public VerifierImpl() {
    }
    
    /**
     * Verify the provided target J2EE module or application, including both
     * standard J2EE and platform specific deployment info.  The provided
     * service could include invoking its own specific UI displaying of verification
     * result. In this case, the service could have limited or no output to logger stream.
     *
     * @param target The an archive, directory or file to verify.
     * @param logger Log stream to write verification output to.
     * @exception ValidationException if the target fails the validation.
     */
    public  void verify(FileObject target, OutputStream logger) throws ValidationException {
        //System.out.println("In Verifier...."+target);
        final String jname = FileUtil.toFile(target).getAbsolutePath();
        
        VerifierSupport.launchVerifier(jname,logger);

        
    }
}

