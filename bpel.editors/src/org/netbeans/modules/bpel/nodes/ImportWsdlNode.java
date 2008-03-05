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
package org.netbeans.modules.bpel.nodes;

import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 7 April 2006
 *
 */
public class ImportWsdlNode extends ImportNode {

    public ImportWsdlNode(Import reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }

    public ImportWsdlNode(Import reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.IMPORT_WSDL;
    }

    @Override
    public boolean canCopy() {
        return true;
    }

    @Override
    public boolean canCut() {
        return false;
    }

    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.OPEN_IN_EDITOR,
            ActionType.SEPARATOR,
            ActionType.ADD_PROPERTY_TO_WSDL,
            ActionType.ADD_PROPERTY_ALIAS_TO_WSDL,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }
}
