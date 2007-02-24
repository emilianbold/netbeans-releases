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
 * File       : InterruptibleActivityRegion.java
 * Created on : Sep 17, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import org.dom4j.Document;
import org.dom4j.Node;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class InterruptibleActivityRegion extends ActivityGroup
                                         implements IInterruptibleActivityRegion
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInterruptibleActivityRegion#addInterruptingEdge(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge)
     */
    public void addInterruptingEdge(IActivityEdge pEdge)
    {
        addElementByID(pEdge, "interruptingEdge");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInterruptibleActivityRegion#getInterruptingEdges()
     */
    public ETList<IActivityEdge> getInterruptingEdges()
    {
        return new ElementCollector< IActivityEdge >()
            .retrieveElementCollectionWithAttrIDs(this, "interruptingEdge", IActivityEdge.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInterruptibleActivityRegion#removeInterruptingEdge(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge)
     */
    public void removeInterruptingEdge(IActivityEdge pEdge)
    {
        removeElementByID(pEdge, "interruptingEdge");
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:InterruptibleActivityRegion", doc, node);
    }  

}
