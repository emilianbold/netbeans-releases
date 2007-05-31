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

package org.netbeans.api.autoupdate;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.autoupdate.services.OperationContainerImpl;
import org.netbeans.modules.autoupdate.services.OperationSupportImpl;

/**
 * @author Radek Matous
 */
public final class OperationSupport {
    OperationSupport() {}
    public void doOperation(ProgressHandle progress/*or null*/) throws OperationException {
        OperationSupportImpl impl = null;
        OperationContainerImpl.OperationType type = container.impl.getType();
        switch (type) {
        case INSTALL:
            impl = OperationSupportImpl.forInstall();
            break;
        case UNINSTALL:
            impl = OperationSupportImpl.forUninstall();
            break;
        case UPDATE:
            impl = OperationSupportImpl.forUpdate();
            break;
        case ENABLE:
            impl = OperationSupportImpl.forEnable();
            break;
        case DISABLE:
            impl = OperationSupportImpl.forDisable();
            break;
        case CUSTOM_INSTALL:
            impl = OperationSupportImpl.forCustomInstall ();
            break;
        default:
            assert false : "Unknown OperationSupport for type " + type;
        }
     
        assert impl != null;
        impl.doOperation(progress, container);
    }
    
    public void doCancel () throws OperationException {
        OperationSupportImpl impl = null;
        OperationContainerImpl.OperationType type = container.impl.getType();
        switch (type) {
        case INSTALL:
            impl = OperationSupportImpl.forInstall();
            break;
        case UNINSTALL:
            impl = OperationSupportImpl.forUninstall();
            break;
        case UPDATE:
            impl = OperationSupportImpl.forUpdate();
            break;
        case ENABLE:
            impl = OperationSupportImpl.forEnable();
            break;
        case DISABLE:
            impl = OperationSupportImpl.forDisable();
            break;
        case CUSTOM_INSTALL:
            impl = OperationSupportImpl.forCustomInstall ();
            break;
        default:
            assert false : "Unknown OperationSupport for type " + type;
        }
     
        assert impl != null;
        // finds and deletes possible downloaded files
        impl.doCancel ();
    }

    //end of API - next just impl details
    private OperationContainer<OperationSupport> container;
    void setContainer(OperationContainer<OperationSupport> c) {container = c;}
}
