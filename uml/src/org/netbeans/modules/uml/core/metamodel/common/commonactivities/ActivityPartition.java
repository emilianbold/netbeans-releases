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
 * File       : ActivityPartition.java
 * Created on : Sep 16, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class ActivityPartition
    extends ActivityGroup
    implements IActivityPartition
{
    private INamedElement namedElem = null;
    public ActivityPartition()
    {
        // TODO: Implement NamedElement methods
        namedElem = new NamedElement();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement#setNode(org.dom4j.Node)
     */
    public void setNode(Node n)
    {
        super.setNode(n);
        namedElem.setNode(n);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition#addSubPartition(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition)
     */
    public void addSubPartition(IActivityPartition pPartition)
    {
        namedElem.addElement(pPartition);
    }
    
    public IActivity getActivity()
    {
		return OwnerRetriever.getOwnerByType(namedElem, IActivity.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition#getIsDimension()
     */
    public boolean getIsDimension()
    {
        return getBooleanAttributeValue("isDimension", false);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition#getIsExternal()
     */
    public boolean getIsExternal()
    {
        return getBooleanAttributeValue("isExternal", false);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition#getRepresents()
     */
    public IElement getRepresents()
    {
        return new ElementCollector< IElement >()
            .retrieveSingleElementWithAttrID(this,"represents", IElement.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition#getSubPartitions()
     */
    public ETList<IActivityPartition> getSubPartitions()
    {
        return new ElementCollector< IActivityPartition > ()
            .retrieveElementCollection((IElement)this, "UML:Element.ownedElement/*", IActivityPartition.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition#removeSubPartition(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition)
     */
    public void removeSubPartition(IActivityPartition pPartition)
    {
        namedElem.removeElement(pPartition);
    }
    
    public void setActivity(IActivity pActivity)
    {
        setNamespace(pActivity);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition#setIsDimension(boolean)
     */
    public void setIsDimension(boolean value)
    {
        setBooleanAttributeValue("isDimension", value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition#setIsExternal(boolean)
     */
    public void setIsExternal(boolean value)
    {
        setBooleanAttributeValue("isExternal", value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition#setRepresents(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public void setRepresents(IElement value)
    {
        setElement(value, "represents");
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:ActivityPartition", doc, node);
    }       

}
