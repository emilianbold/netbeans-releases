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


package org.netbeans.modules.visualweb.api.designtime.idebridge;


import com.sun.rave.designtime.DesignBean;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 * Provides access to the <code>DesigntimeService</code> impl.
 *
 * @author Peter Zavadsky
 */
public final class DesigntimeIdeBridgeProvider {

    /** Creates a new instance of DesigntimeServiceProvider */
    private DesigntimeIdeBridgeProvider() {
    }


    /** Looks for the <code>DesigntimeIdeBridge</code> service in the globl lookup,
     * <code>Lookup.getDefault</code> and provides it. In case there
     * is no instance found, provides a dummy (no-op) impl. */
    public static DesigntimeIdeBridge getDefault() {
        DesigntimeIdeBridge designtimeService = (DesigntimeIdeBridge)Lookup.getDefault().
                lookup(DesigntimeIdeBridge.class);
        if(designtimeService == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new NullPointerException("No DesigntimeIdeBridge registered! Providing a dummy impl.")); // NOI18N

            designtimeService = new DummyDesigntimeService();
        }
        return designtimeService;
    }


    /** Dummy implementation of the service, as a fallback. */
    private static class DummyDesigntimeService implements DesigntimeIdeBridge {

        public Node getNodeRepresentation(DesignBean designBean) {
            return new BrokenNode(designBean);
        }
    } // End of DummyDesigntimeService.


    private static class BrokenNode extends AbstractNode {

        public BrokenNode(DesignBean designBean) {
            super(Children.LEAF);

            setDisplayName(NbBundle.getMessage(DesigntimeIdeBridgeProvider.class, "LBL_BrokenNode", designBean.getInstanceName()));
        }
    } // End of BrokenNode.
}
