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

package org.netbeans.modules.websvc.core.jaxws.actions;

import org.netbeans.modules.websvc.core.AddOperationCookie;
import org.openide.filesystems.FileObject;
import java.io.IOException;
import static org.netbeans.api.java.source.JavaSource.Phase;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

 /** JaxWsAddOperation.java
 * Created on December 12, 2006, 4:36 PM
 *
 * @author mkuchtiak
 */
public class JaxWsAddOperation implements AddOperationCookie {
    
    private FileObject implClassFo;
    
    /** Creates a new instance of JaxWsAddOperation */
    public JaxWsAddOperation(FileObject implClassFo) {
        this.implClassFo=implClassFo;
    }
    
    public void addOperation(FileObject implementationClass) {
        AddWsOperationHelper strategy = new AddWsOperationHelper(
                NbBundle.getMessage(JaxWsAddOperation.class, "LBL_OperationAction"));
        try {
            String className = _RetoucheUtil.getMainClassName(implementationClass);
            if (className != null) {
                strategy.addMethod(implementationClass, className);
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
}
