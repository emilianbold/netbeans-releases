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
 * File       : InvocationNode.java
 * Created on : Sep 17, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import java.lang.StringBuilder;
import org.dom4j.Document;
import org.dom4j.Node;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class InvocationNode extends ActivityNode implements IInvocationNode
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#addLocalPostCondition(org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint)
     */
    public void addLocalPostCondition(IConstraint pConstraint)
    {
        addChild("UML:InvocationNode.localPostCondition", "UML:InvocationNode.localPostCondition", pConstraint);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#addLocalPrecondition(org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint)
     */
    public void addLocalPrecondition(IConstraint pConstraint)
    {
        addChild("UML:InvocationNode.localPreCondition", "UML:InvocationNode.localPreCondition", pConstraint);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#createCondition(java.lang.String)
     */
    public IConstraint createCondition(String condition)
    {
        TypedFactoryRetriever<IConstraint> factory 
                        = new TypedFactoryRetriever<IConstraint>();
        IConstraint constraint = factory.createType("Constraint");
        if(constraint != null)
            constraint.setExpression(condition);
        return constraint;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#getIsMultipleInvocation()
     */
    public boolean getIsMultipleInvocation()
    {
        return getBooleanAttributeValue("isMultipleInvocation",false);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#getIsSynchronous()
     */
    public boolean getIsSynchronous()
    {
        return getBooleanAttributeValue("isSynchronous", true);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#getLocalPostConditions()
     */
    public ETList<IConstraint> getLocalPostConditions()
    {
        return new ElementCollector< IConstraint >()
            .retrieveElementCollection((IElement)this,"UML:InvocationNode.localPostCondition/*", IConstraint.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#getLocalPreconditions()
     */
    public ETList<IConstraint> getLocalPreconditions()
    {
        return new ElementCollector< IConstraint >()
            .retrieveElementCollection((IElement)this,"UML:InvocationNode.localPreCondition/*", IConstraint.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#getMultiplicity()
     */
    public IMultiplicity getMultiplicity()
    {
        return new ElementCollector< IMultiplicity >()
            .retrieveSingleElement(this, "UML:InvocationNode.multiplicity/*", IMultiplicity.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#removeLocalPostcondition(org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint)
     */
    public void removeLocalPostcondition(IConstraint pConstraint)
    {
        UMLXMLManip.removeChild(m_Node, pConstraint);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#removeLocalPrecondition(org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint)
     */
    public void removeLocalPrecondition(IConstraint pConstraint)
    {
        UMLXMLManip.removeChild(m_Node, pConstraint);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#setIsMultipleInvocation(boolean)
     */
    public void setIsMultipleInvocation(boolean isMulInvoc)
    {
        setBooleanAttributeValue("isMultipleInvocation", isMulInvoc);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#setIsSynchronous(boolean)
     */
    public void setIsSynchronous(boolean sync)
    {
        setBooleanAttributeValue("isSynchronous", sync);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode#setMultiplicity(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity)
     */
    public void setMultiplicity(IMultiplicity mul)
    {
        addChild("UML:InvocationNode.multiplicity", "UML:InvocationNode.multiplicity", mul);
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:InvocationNode", doc, node);
    }       

    public String getRangeAsString()
    {
        return getMultiplicity().getRangeAsString();
    }

    public String getPreconditionsAsString()
    {
        StringBuilder val = new StringBuilder();
        for (IConstraint constraint: getLocalPreconditions())
        {
            val.append(constraint.getExpression());
            val.append(", ");
        }
        int index = val.lastIndexOf(", ");
        if (index > 0)
            return val.substring(0, index);
        return val.toString();
    }

    public String getPostConditionsAsString()
    {
        StringBuilder val = new StringBuilder();
        for (IConstraint constraint: getLocalPostConditions())
        {
            val.append(constraint.getExpression());
            val.append(", ");
        }
        int index = val.lastIndexOf(", ");
        if (index > 0)
            return val.substring(0, index);
        return val.toString();
    }
}
