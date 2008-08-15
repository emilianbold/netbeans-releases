/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        //namedElem.addElement(pPartition);
        addOwnedElement( pPartition );  
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
