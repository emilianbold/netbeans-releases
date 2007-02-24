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
 * File       : ConditionalAction.java
 * Created on : Sep 18, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class ConditionalAction
    extends CompositeAction
    implements IConditionalAction
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IConditionalAction#addClause(org.netbeans.modules.uml.core.metamodel.common.commonactions.IClause)
     */
    public void addClause(IClause pClause)
    {
        addChild("UML:ConditionalAction.clause"
                    , "UML:ConditionalAction.clause"
                    , pClause);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IConditionalAction#getClauses()
     */
    public ETList<IClause> getClauses()
    {
        return new ElementCollector< IClause >()
            .retrieveElementCollection(this, "UML:ConditionalAction.clause/*", IClause.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IConditionalAction#getIsAssertion()
     */
    public boolean getIsAssertion()
    {
        return getBooleanAttributeValue("isAssertion", false);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IConditionalAction#getIsDeterminate()
     */
    public boolean getIsDeterminate()
    {
        return getBooleanAttributeValue("isDeterminate", false);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IConditionalAction#removeClause(org.netbeans.modules.uml.core.metamodel.common.commonactions.IClause)
     */
    public void removeClause(IClause pClause)
    {
        UMLXMLManip.removeChild(m_Node, pClause);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IConditionalAction#setIsAssertion(boolean)
     */
    public void setIsAssertion(boolean isAssertion)
    {
        setBooleanAttributeValue("isAssertion", isAssertion);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IConditionalAction#setIsDeterminate(boolean)
     */
    public void setIsDeterminate(boolean isDeterminate)
    {
        setBooleanAttributeValue("isDeterminate", isDeterminate);
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:ConditionalAction", doc, node);
    }        

}
