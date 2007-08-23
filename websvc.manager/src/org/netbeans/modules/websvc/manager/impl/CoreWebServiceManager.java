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
package org.netbeans.modules.websvc.manager.impl;

import javax.swing.Action;
import org.netbeans.modules.websvc.manager.api.WebServiceDescriptor;
import org.netbeans.modules.websvc.manager.spi.WebServiceManagerExt;

/**
 * Basic consumer of web service manager.
 * 
 * @author nam
 */
public class CoreWebServiceManager implements WebServiceManagerExt {

    public CoreWebServiceManager() {
    }

    public boolean wsServiceAddedExt(WebServiceDescriptor wsMetadataDesc) {
        // place-holder, nothing to do;
        return true;
    }

    public boolean wsServiceRemovedExt(WebServiceDescriptor wsMetadataDesc) {
        // place-holder, nothing to do;
        return true;
    }

    public static final Action[] EMPTY_ACTIONS = new Action[0];
    public Action[] getWebServicesRootActions() {
        return EMPTY_ACTIONS;
    }

    public Action[] getGroupActions() {
        return EMPTY_ACTIONS;
    }

    public Action[] getWebServiceActions() {
        return EMPTY_ACTIONS;
    }

    public Action[] getPortActions() {
        return EMPTY_ACTIONS;
    }

    public Action[] getMethodActions() {
        return EMPTY_ACTIONS;
    }

}
