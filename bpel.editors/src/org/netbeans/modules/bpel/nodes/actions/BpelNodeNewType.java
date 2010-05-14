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
package org.netbeans.modules.bpel.nodes.actions;

import org.netbeans.modules.bpel.nodes.actions.BpelNodeAction;
import java.io.IOException;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.BpelProcessNode;
import org.openide.nodes.Node;
import org.openide.util.datatransfer.NewType;

/**
 *
 * @author Vitaly Bychkov
 */
public class BpelNodeNewType extends NewType {
    private BpelNodeAction action;
    private BpelNode node;

    public BpelNodeNewType(BpelNodeAction action, BpelNode node) {
        this.action = action;
        this.node = node;
    }


    public String getName() {
        return action.getName();
    }
    
    public void create() throws IOException {
        if (action.enable(new Node[] {node})) {
            action.performAction(new Node[] {node});
        }
    }
}
