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
 * File       : Activity.java
 * Created on : Sep 16, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Behavior;

/**
 * @author Aztec
 */
public class Activity extends Behavior implements IActivity
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity#addEdge(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge)
     */
    public void addEdge(IActivityEdge pEdge)
    {
        addOwnedElement(pEdge);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity#addGroup(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityGroup)
     */
    public void addGroup(final IActivityGroup pGroup)
    {
        new ElementConnector<IActivity>()
            .addChildAndConnect(this, false, "UML:Activity.group", "UML:Activity.group/*",
                                pGroup,
                                new IBackPointer<IActivity>() {
                                    public void execute(IActivity obj) {
                                        pGroup.setActivity(obj);
                                    }
                                }
        );

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity#addNode(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode)
     */
    public void addNode(IActivityNode pNode)
    {
        addOwnedElement(pNode);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity#addPartition(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition)
     */
    public void addPartition(IActivityPartition pPartition)
    {
       addGroup(pPartition);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity#getEdges()
     */
    public ETList<IActivityEdge> getEdges()
    {
        return new ElementCollector< IActivityEdge >().
            retrieveElementCollection((IElement)this, "UML:Element.ownedElement/*", IActivityEdge.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity#getGroups()
     */
    public ETList<IActivityGroup> getGroups()
    {
        return new ElementCollector< IActivityGroup >().
            retrieveElementCollection((IElement)this, "UML:Element.ownedElement/*", IActivityGroup.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity#getIsSingleCopy()
     */
    public boolean getIsSingleCopy()
    {
        return getBooleanAttributeValue("isSingleCopy", false);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity#getKind()
     */
    public int getKind()
    {
        return getActivityKindValue("kind");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity#getNodes()
     */
    public ETList<IActivityNode> getNodes()
    {
        return new ElementCollector< IActivityNode >().
            retrieveElementCollection((IElement)this, "UML:Element.ownedElement/*", IActivityNode.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity#getPartitions()
     */
    public ETList<IActivityPartition> getPartitions()
    {
        return new ElementCollector< IActivityPartition >().
            retrieveElementCollection((IElement)this, "UML:Element.ownedElement/*", IActivityPartition.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity#removeEdge(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge)
     */
    public void removeEdge(IActivityEdge pEdge)
    {
       removeElement(pEdge);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity#removeGroup(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityGroup)
     */
    public void removeGroup(final IActivityGroup pGroup)
    {
        new ElementConnector<IActivity>()
            .removeElement(this, pGroup, "UML:Activity.group/*",
                                new IBackPointer<IActivity>() {
                                    public void execute(IActivity obj) {
                                        pGroup.setActivity(obj);
                                    }
                                }
        );

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity#removeNode(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode)
     */
    public void removeNode(IActivityNode pNode)
    {
        removeElement(pNode);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity#removePartition(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition)
     */
    public void removePartition(IActivityPartition pPartition)
    {
        removeGroup(pPartition);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity#setIsSingleCopy(boolean)
     */
    public void setIsSingleCopy(boolean value)
    {
        setBooleanAttributeValue("isSingleCopy", value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity#setKind(int)
     */
    public void setKind(int value)
    {
        setActivityKindValue("kind", value);
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:Activity", doc, node);
    }    

}
