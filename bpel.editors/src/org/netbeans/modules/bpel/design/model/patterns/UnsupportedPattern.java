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

package org.netbeans.modules.bpel.design.model.patterns;

import javax.swing.JPopupMenu;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.nodes.actions.DeleteAction;
import org.openide.awt.Actions;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Alexey
 */
public class UnsupportedPattern extends EmptyPattern{

    /** Creates a new instance of UnsupportedPattern */
    public UnsupportedPattern(DiagramModel model) {
        super(model);
    }
    public String getDefaultName() {
        return "Unsupported element"; // NOI18N
    }
    
    public NodeType getNodeType() {
        return NodeType.UNKNOWN_TYPE;
    }

}
