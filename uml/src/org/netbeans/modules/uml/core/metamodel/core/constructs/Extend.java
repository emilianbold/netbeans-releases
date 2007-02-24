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
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public class Extend extends DirectedRelationship implements IExtend
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IExtend#addExtensionLocation(org.netbeans.modules.uml.core.metamodel.core.constructs.IExtensionPoint)
     */
    public void addExtensionLocation(IExtensionPoint extLoc)
    {
        addElementByID( extLoc, "extensionLocation");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IExtend#getBase()
     */
    public IUseCase getBase()
    {
        ElementCollector< IUseCase > col =  new ElementCollector< IUseCase >();
        return col.retrieveSingleElementWithAttrID( this, "target", IUseCase.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IExtend#getCondition()
     */
    public IConstraint getCondition()
    {
        ElementCollector< IConstraint > col = new ElementCollector< IConstraint >();
        return col.retrieveSingleElement( this, "UML:Element.ownedElement/*", IConstraint.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IExtend#getExtension()
     */
    public IUseCase getExtension()
    {
		return OwnerRetriever.getOwnerByType(this, IUseCase.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IExtend#getExtensionLocations()
     */
    public ETList <IExtensionPoint> getExtensionLocations()
    {
        ElementCollector< IExtensionPoint > col = new ElementCollector< IExtensionPoint >();
        return col.retrieveElementCollectionWithAttrIDs(this,"extensionLocation", IExtensionPoint.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IExtend#removeExtensionLocation(org.netbeans.modules.uml.core.metamodel.core.constructs.IExtensionPoint)
     */
    public void removeExtensionLocation(IExtensionPoint extLoc)
    {
        removeElementByID(extLoc,"extensionLocation");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IExtend#setBase(org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase)
     */
    public void setBase(IUseCase useCase)
    {
        // Remove the old target before adding the new one
        IUseCase curr = getBase();
        boolean isSame = false;
        if(curr != null)
        {
            if(!(isSame = curr.isSame(useCase)))
            {
                curr.removeExtendedBy(this);
                removeTarget(curr);
            }
        }
        
        // Now add the new target, verify that the current target is null before
        // going on
        if(!isSame)
        {
            curr = getBase();
            if(curr == null)
            {
                addTarget(useCase);
                if (useCase != null)
                    useCase.addExtendedBy(this);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IExtend#setCondition(org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint)
     */
    public void setCondition(IConstraint constraint)
    {
        addElement(constraint);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IExtend#setCondition2(java.lang.String)
     */
    public void setCondition2(String conditionBody)
    {
        TypedFactoryRetriever<IConstraint> retriever = 
                                        new TypedFactoryRetriever<IConstraint>();
        IConstraint con = retriever.createType("Constraint");
        
        if(con != null)
        {
            setCondition(con);
            con.setExpression(conditionBody);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IExtend#setExtension(org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase)
     */
    public void setExtension(IUseCase useCase)
    {
        setOwner(useCase);
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:Extend", doc, node);
    }

}
