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
package org.netbeans.modules.sql.framework.ui.view.validation;

import org.netbeans.modules.sql.framework.model.SQLCastOperator;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLCastAsGraphNode;


/**
 * Handles request to edit a cast-as operator as referenced by a validation error message.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class CastAsValidationHandler implements ValidationHandler {

    private IGraphView graphView;

    /**
     * Constructs a new instance of CastAsValidationHandler, referencing the
     * given IGraphView instance.
     * 
     * @param gView IGraphView instance in which cast-as operators are displayed
     */
    public CastAsValidationHandler(IGraphView gView) {
        this.graphView = gView;
    }

    /*
     * @see org.netbeans.modules.sql.framework.ui.view.validation.ValidationHandler#editValue(java.lang.Object)
     */
    public void editValue(Object val) {
        SQLCastOperator castOp = (SQLCastOperator) val;
        
        IGraphNode node = graphView.findGraphNode(castOp);
        if (!(node instanceof SQLCastAsGraphNode)) {
            return;
        }
        
        SQLCastAsGraphNode castNode = (SQLCastAsGraphNode) node;
        castNode.showCastAsDialog();
    }
}

