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


package org.netbeans.modules.uml.core.metamodel.core.constructs;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.DirectedRelationship;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;


public class Include extends DirectedRelationship implements IInclude
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IInclude#getAddition()
     */
    public IUseCase getAddition()
    {
        ElementCollector< IUseCase > col = new ElementCollector< IUseCase >();
        return col.retrieveSingleElementWithAttrID(this,"target", IUseCase.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IInclude#getBase()
     */
    public IUseCase getBase()
    {
		return OwnerRetriever.getOwnerByType(this, IUseCase.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IInclude#setAddition(org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase)
     */
    public void setAddition(IUseCase addition)
    {
        // Remove the old target before adding the new one
        IUseCase curr = getAddition();
        boolean isSame = false;
        if(curr != null)
        {
            if(!(isSame = curr.isSame(addition)))
            {
                curr.removeIncludedBy(this);
                removeTarget(curr);
            }
        }
        
        // Now add the new target, verify that the current target is null before
        // going on
        if(!isSame)
        {
            curr = getAddition();
            if(curr == null)
            {
                addTarget(addition);
                if (addition != null)
                    addition.addIncludedBy(this);
            }
        }


    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IInclude#setBase(org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase)
     */
    public void setBase(IUseCase base)
    {
        setOwner(base);
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:Include", doc, node);
    }    

}
