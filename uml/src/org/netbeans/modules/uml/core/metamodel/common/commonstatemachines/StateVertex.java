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
 * File       : StateVertex.java
 * Created on : Sep 19, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonstatemachines;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.Namespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class StateVertex extends Namespace implements IStateVertex
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex#addIncomingTransition(org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition)
     */
    public void addIncomingTransition(final ITransition pTran)
    {
        ensureOwnership(pTran);
        
        new ElementConnector< IStateVertex >()
            .addChildAndConnect(
                        this, 
                        true, 
                        "incoming", 
                        "incoming", 
                        pTran, 
                        new IBackPointer<IStateVertex>()
                        {
                            public void execute(IStateVertex obj)
                            {
                                pTran.setTarget(obj);
                            }
                        }
        );

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex#addOutgoingTransition(org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition)
     */
    public void addOutgoingTransition(final ITransition pTran)
    {
        ensureOwnership(pTran);
        
        new ElementConnector< IStateVertex >()
            .addChildAndConnect(
                        this, 
                        true, 
                        "outgoing", 
                        "outgoing", 
                        pTran, 
                        new IBackPointer<IStateVertex>()
                        {
                            public void execute(IStateVertex obj)
                            {
                                pTran.setSource(obj);
                            }
                        }
        );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex#getContainer()
     */
    public IRegion getContainer()
    {
		return OwnerRetriever.getOwnerByType(this, IRegion.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex#getIncomingTransitions()
     */
    public ETList<ITransition> getIncomingTransitions()
    {
        return new ElementCollector< ITransition >()
            .retrieveElementCollectionWithAttrIDs(this, "incoming", ITransition.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex#getOutgoingTransitions()
     */
    public ETList<ITransition> getOutgoingTransitions()
    {
        return new ElementCollector< ITransition >()
            .retrieveElementCollectionWithAttrIDs(this, "outgoing", ITransition.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex#removeIncomingTransition(org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition)
     */
    public void removeIncomingTransition(final ITransition pTran)
    {
        new ElementConnector< IStateVertex >()
            .removeByID(
                    this, 
                    pTran, 
                    "incoming",
                    new IBackPointer<IStateVertex>()
                   {
                       public void execute(IStateVertex obj)
                       {
                           pTran.setTarget(obj);
                       }
                   }
            );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex#removeOutgoingTransition(org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition)
     */
    public void removeOutgoingTransition(final ITransition pTran)
    {
        new ElementConnector< IStateVertex >()
            .removeByID(
                    this, 
                    pTran, 
                    "outgoing",
                    new IBackPointer<IStateVertex>()
                   {
                       public void execute(IStateVertex obj)
                       {
                           pTran.setSource(obj);
                       }
                   }
            );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex#setContainer(org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IRegion)
     */
    public void setContainer(IRegion value)
    {
        setNamespace(value);
    }
    
    protected void ensureOwnership(ITransition pTran)
    {
        IElement owner = pTran.getOwner();
        
        if(owner == null)
        {
            IRegion cont = getContainer();
            
            if(cont != null)
                cont.addTransition(pTran);
        }
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:StateVertex", doc, node);
    }  

}
