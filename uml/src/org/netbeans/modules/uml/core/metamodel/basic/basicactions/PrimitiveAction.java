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


package org.netbeans.modules.uml.core.metamodel.basic.basicactions;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class PrimitiveAction extends Action implements IPrimitiveAction
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IPrimitiveAction#addArgument(org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification)
     */
    public void addArgument(IValueSpecification valueSpec)
    {
        addInput(valueSpec);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IPrimitiveAction#getArguments()
     */
    public ETList <IValueSpecification> getArguments()
    {
        return getInputs();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IPrimitiveAction#getTarget()
     */
    public IValueSpecification getTarget()
    {
        ElementCollector< IValueSpecification > col = new ElementCollector< IValueSpecification >();
        return col.retrieveSingleElement(this, "UML:PrimitiveAction.target/*", IValueSpecification.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IPrimitiveAction#removeArgument(org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification)
     */
    public void removeArgument(IValueSpecification valueSpec)
    {
        removeInput(valueSpec);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IPrimitiveAction#setTarget(org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification)
     */
    public void setTarget(IValueSpecification valueSpec)
    {
        addChild("UML:PrimitiveAction.target", "UML:PrimitiveAction.target", valueSpec);
    }

}
