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


/*
 * File       : ElseConditionalStateHandler.java
 * Created on : Dec 11, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;

/**
 * @author Aztec
 */
public class ElseConditionalStateHandler extends MethodConditionalStateHandler
{

    /**
     * @param language
     * @param forceClause
     */
    public ElseConditionalStateHandler(String language)
    {
        super(language, true);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.StateHandler#initialize()
     */
    public void initialize()
    {
        IUMLParserEventDispatcher pDispatcher = getEventDispatcher();
      
        if(pDispatcher != null)
        {            
            pDispatcher.fireBeginClause(null);
        }      
      
        Node pCondClause = getDOMNode();
        setClauseGroupNode(pCondClause);

        if(pCondClause != null)
        {
            Node pClauseNode = createNode(pCondClause, "UML:Clause"); 
            setDOMNode(pClauseNode);
        }

        beginScope();
    }

}
