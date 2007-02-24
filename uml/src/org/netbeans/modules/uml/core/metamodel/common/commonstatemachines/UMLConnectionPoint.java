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
 * File       : UMLConnectionPoint.java
 * Created on : Sep 19, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonstatemachines;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class UMLConnectionPoint
    extends StateVertex
    implements IUMLConnectionPoint
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IUMLConnectionPoint#addEntry(org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IPseudoState)
     */
    public void addEntry(IPseudoState pState)
    {
        addElementByID(pState, "entry");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IUMLConnectionPoint#getDefinition()
     */
    public IUMLConnectionPoint getDefinition()
    {
        return new ElementCollector< IUMLConnectionPoint >()
            .retrieveSingleElementWithAttrID(this, "definition", IUMLConnectionPoint.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IUMLConnectionPoint#getEntries()
     */
    public ETList<IPseudoState> getEntries()
    {
        return new ElementCollector< IPseudoState >()
            .retrieveElementCollectionWithAttrIDs(this, "entry", IPseudoState.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IUMLConnectionPoint#removeEntry(org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IPseudoState)
     */
    public void removeEntry(IPseudoState pState)
    {
        removeElementByID(pState, "entry");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IUMLConnectionPoint#setDefinition(org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IUMLConnectionPoint)
     */
    public void setDefinition(IUMLConnectionPoint value)
    {
        setElement(value, "definition");
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:UMLConnectionPoint", doc, node);
    }      

}
