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

import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;

/**
 *
 * Fake pattern to wrap Import element.
 * Required by schema DnD
 * Process pattern DO NOT create this pattern in createElementsImpl
 *
 */
public class ImportPattern extends EmptyPattern{

 /**
     * Creates a new instance of BasicActivityPattern.
     */
    public ImportPattern(DiagramModel model) {
        super(model);
    }

    public String getDefaultName() {
        return "Import"; // NOI18N
    }         

    public NodeType getNodeType() {
        return NodeType.IMPORT;
    }
    
}
