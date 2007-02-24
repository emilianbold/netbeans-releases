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
 * File       : DecisionNode.java
 * Created on : Sep 17, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;

/**
 * @author Aztec
 */
public class DecisionNode extends ControlNode implements IDecisionNode
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IDecisionNode#getDecisionInput()
     */
    public IBehavior getDecisionInput()
    {
        return new ElementCollector< IBehavior >()
            .retrieveSingleElementWithAttrID(this, "decisionInput", IBehavior.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IDecisionNode#setDecisionInput(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior)
     */
    public void setDecisionInput(IBehavior value)
    {
        setElement(value, "decisionInput");
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:DecisionNode", doc, node);
    }      

}
