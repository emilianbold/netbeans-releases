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
 * File       : OpReturnStateHandler.java
 * Created on : Dec 11, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class OpReturnStateHandler extends ParameterStateHandler
{
    private Node m_OwnerNode = null;

    /**
     * @param language
     */
    public OpReturnStateHandler(String language)
    {
        super(language, "result");
        setTypeState(true);
    }

    /**
     * @param language
     * @param direction
     */
    public OpReturnStateHandler(String language, String stateName)
    {
        super(language, "result", stateName);
        setTypeState(true);
    }
    
    public void initialize() 
    {
        m_OwnerNode = getDOMNode();
        super.initialize();
    }
    
    protected void handleComment(ITokenDescriptor pToken) 
    {
        if(m_OwnerNode != null)
        {
           handleComment(m_OwnerNode, pToken);
        }
    }

    protected void writeStartToken() 
    {

        super.writeStartToken();
        
        if(m_OwnerNode != null)
        {
            writeStartToken(m_OwnerNode);
        }
    }

    protected Node getOwnerNode() 
    {
        return m_OwnerNode;
    }

}
