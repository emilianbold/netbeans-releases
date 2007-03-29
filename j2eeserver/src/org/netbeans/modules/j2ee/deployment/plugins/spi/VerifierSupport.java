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

package org.netbeans.modules.j2ee.deployment.plugins.spi;

import java.io.OutputStream;
import org.netbeans.modules.j2ee.deployment.common.api.ValidationException;
import org.openide.filesystems.FileObject;

/**
 * Verifier service to be implmeneted by Server Integration Plugin.
 * Instance of this service needs to be declared in plugin module layer.xml.
 *
 * @author nn136682
 */
public abstract class VerifierSupport {

    /**
     * Whether the verifier support this module type; default to supports all types.
     */
    public boolean supportsModuleType(Object moduleType) {
        return true;
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
    public abstract void verify(FileObject target, OutputStream logger) throws ValidationException;
    
}
