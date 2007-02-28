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

package org.netbeans.modules.websvc.core;

import java.util.Collection;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author mkuchtiak
 */
public class WebServiceActionProvider {

    private static final Lookup.Result<AddOperationActionProvider> addOperationActionProviders =
        Lookup.getDefault().lookup(new Lookup.Template<AddOperationActionProvider>(AddOperationActionProvider.class));
    
    /** Find AddOperationCookie for given FileObject (target source)
     */
    public static AddOperationCookie getAddOperationAction(FileObject targetSource) {
        Collection<? extends AddOperationActionProvider> instances = addOperationActionProviders.allInstances();
        for (AddOperationActionProvider impl: instances) {
            AddOperationCookie cookie = impl.getAddOperationCookie(targetSource);
            if (cookie != null) {
                return cookie;
            }
        }
        return null;
    }
    
    private static final Lookup.Result<InvokeOperationActionProvider> invokeOperationActionProviders =
        Lookup.getDefault().lookup(new Lookup.Template<InvokeOperationActionProvider>(InvokeOperationActionProvider.class));

    /** Find InvokeOperationCookie for given FileObject (target source)
     */
    public static InvokeOperationCookie getInvokeOperationAction(FileObject targetSource) {
        Collection<? extends InvokeOperationActionProvider> instances = invokeOperationActionProviders.allInstances();
        for (InvokeOperationActionProvider impl: instances) {
            InvokeOperationCookie cookie = impl.getInvokeOperationCookie(targetSource);
            if (cookie != null) {
                return cookie;
            }
        }
        return null;
    }
}
