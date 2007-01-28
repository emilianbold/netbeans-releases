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


package org.netbeans.modules.visualweb.insync.live;


import com.sun.rave.designtime.DesignBean;
import org.netbeans.modules.visualweb.api.designtime.idebridge.DesigntimeIdeBridge;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;


/**
 * Implementation of <code>DesigntimeService</code> service.
 *
 * @author Peter Zavadsky
 */
public class DesigntimeIdeBridgeImpl implements DesigntimeIdeBridge {

    /** Creates a new instance of DesigntimeServiceImpl */
    public DesigntimeIdeBridgeImpl() {
    }


    public Node getNodeRepresentation(DesignBean designBean) {
        if (designBean instanceof SourceDesignBean) {
            return ((SourceDesignBean)designBean).getNode();
        } else {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalArgumentException("Unknown design bean instance =" + designBean)); // NOI18N
            return new BrokenNode(designBean);
        }
    }


    private static class BrokenNode extends AbstractNode {

        public BrokenNode(DesignBean designBean) {
            super(Children.LEAF);

            setDisplayName(NbBundle.getMessage(
                    DesigntimeIdeBridgeImpl.class,
                    "LBL_BrokenNode",
                    (designBean == null ? null : designBean.getInstanceName())));
        }
    } // End of BrokenNode.

}
