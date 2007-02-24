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
 * File       : PseudoState.java
 * Created on : Sep 19, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonstatemachines;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IPseudostateKind;

/**
 * @author Aztec
 */
public class PseudoState extends StateVertex implements IPseudoState
{
    public String getExpandedElementType()
    {
        int kind = getKind();

        String type = getElementType();

        switch(kind)
        {
            case IPseudostateKind.PK_CHOICE: return "ChoicePseudoState";
            case IPseudostateKind.PK_DEEPHISTORY: return "DeepHistoryState";
            case IPseudostateKind.PK_FORK: return "ForkState";
            case IPseudostateKind.PK_INITIAL: return "InitialState";
            case IPseudostateKind.PK_JOIN: return "JoinState";
            case IPseudostateKind.PK_JUNCTION: return "JunctionState";
            case IPseudostateKind.PK_SHALLOWHISTORY: return "ShallowHistoryState";
            case IPseudostateKind.PK_ENTRYPOINT: return "EntryPointState";
            case IPseudostateKind.PK_STOP: return "StopState";                    
        }
        
        return type;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IPseudoState#getKind()
     */
    public int getKind()
    {
        return getPseudostateKind("kind");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IPseudoState#setKind(int)
     */
    public void setKind(int value)
    {
        setPseudostateKind("kind", value);
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:PseudoState", doc, node);
    }      

	/**
	 * Does this element have an expanded element type or is the expanded element type always the element type?
	 */
	public boolean getHasExpandedElementType()
	{
		return true;
	}

}
