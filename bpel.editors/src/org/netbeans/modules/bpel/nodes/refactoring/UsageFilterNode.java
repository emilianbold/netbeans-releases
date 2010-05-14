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
package org.netbeans.modules.bpel.nodes.refactoring;

import javax.swing.Action;
import org.netbeans.modules.bpel.nodes.actions.GoToSourceAction;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public abstract class UsageFilterNode extends FilterNode {
    public UsageFilterNode(Node originalNode) {
        super(originalNode);
    }

    public UsageFilterNode(Node originalNode
            , org.openide.nodes.Children children) 
    {
        super(originalNode, children);
    }
    
    public UsageFilterNode(Node originalNode
            , org.openide.nodes.Children children
            , Lookup lookup) 
    {
        super(originalNode, children, lookup);
    }

    public Node getOriginal() {
        return super.getOriginal();
    }

    // search for GO_TO_SOURCE_ACTION and invoke it
    public Action getPreferredAction() {
        return SystemAction.get(GoToSourceFilterAction.class);
    }
}
