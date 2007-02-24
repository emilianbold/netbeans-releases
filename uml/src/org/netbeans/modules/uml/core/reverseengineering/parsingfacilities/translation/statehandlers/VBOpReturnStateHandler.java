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
 * File       : VBOpReturnStateHandler.java
 * Created on : Dec 12, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.Identifier;

/**
 * @author Aztec
 */
public class VBOpReturnStateHandler extends OpReturnStateHandler
{

    public VBOpReturnStateHandler(String language)
    {
        super(language);
    }


    public VBOpReturnStateHandler(String language, String stateName)
    {
        super(language, stateName);
    }
    
    /**
     * Updates the operations's return type.
     *
     */
    protected void updateType()
    {
        Identifier typeIdent = getTypeIdentifier();
        long line = typeIdent.getStartLine();
        long col  = typeIdent.getStartColumn();
        long pos  = typeIdent.getStartPosition();
        long len  = typeIdent.getLength();

        Node pOwnerNode = getOwnerNode();
        if(pOwnerNode != null)
        {
            setTokenDescriptor(pOwnerNode, 
                                "OpHeadEndPosition", 
                                line, 
                                col, 
                                pos + 1, 
                                "", 
                                len);
        }
        super.updateType();    
    }

}
