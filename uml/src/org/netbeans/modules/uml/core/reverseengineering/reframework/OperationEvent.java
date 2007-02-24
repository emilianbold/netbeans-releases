/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.dom4j.Node;

/**
 */
public class OperationEvent extends ParserData implements IOperationEvent
{

    /**
     * Retrieves the operation.
     * @param pVal The operation data.
     */
    public IREOperation getREOperation()
    {
        // The XML node that is the event data is also the operation.  So
        // get the XML node and create a REOperation instance.
        return createOperation(getEventData());
    }

    /**
     * Create a new operation and adds it to the collection of operations.  The 
     * IREOperation implementation created depends on the operation type.
     * 
     * @param node
     * @return
     */
    private IREOperation createOperation(Node node)
    {
        IREOperation op = new REOperation();
        op.setEventData(node);
        return op;
    }
}