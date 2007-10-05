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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.components.strikeiron;

import javax.swing.Action;
import org.netbeans.modules.websvc.components.strikeiron.actions.FindServiceAction;
import org.netbeans.modules.websvc.manager.api.WebServiceDescriptor;
import org.netbeans.modules.websvc.manager.spi.WebServiceManagerExt;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author nam
 */
public class StrikeIronWebServiceManager implements WebServiceManagerExt {
    public static final String STRIKE_IRON_GROUP = NbBundle.getMessage(StrikeIronWebServiceManager.class, "STRIKE_IRON_GROUP");
    public Action[] getGroupActions(Node node) {
        /*if (node.getName().startsWith(STRIKE_IRON_GROUP)) {
            return new Action[] { SystemAction.get(FindServiceAction.class) };
        } else {*/
            return EMPTY_ACTIONS;
        //}
    }

    public Action[] getMethodActions(Node node) {
        return EMPTY_ACTIONS;
    }

    public Action[] getPortActions(Node node) {
        return EMPTY_ACTIONS;
    }

    public Action[] getWebServiceActions(Node node) {
        return EMPTY_ACTIONS;
    }

    public Action[] getWebServicesRootActions(Node node) {
        return EMPTY_ACTIONS;
    }

    public boolean wsServiceAddedExt(WebServiceDescriptor wsMetadataDesc) {
        return true;
    }

    public boolean wsServiceRemovedExt(WebServiceDescriptor wsMetadataDesc) {
        return true;
    }
    
}