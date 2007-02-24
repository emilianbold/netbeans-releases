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
 * File       : LinkAction.java
 * Created on : Sep 18, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.PrimitiveAction;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class LinkAction extends PrimitiveAction implements ILinkAction
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ILinkAction#addEndData(org.netbeans.modules.uml.core.metamodel.common.commonactions.ILinkEndData)
     */
    public void addEndData(ILinkEndData pEndData)
    {
        addElement(pEndData);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ILinkAction#getEndData()
     */
    public ETList <ILinkEndData> getEndData()
    {
        return new ElementCollector< ILinkEndData >()
            .retrieveElementCollection((ILinkAction)this, "UML:Element.ownedElement/UML:LinkEndData", ILinkEndData.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ILinkAction#removeEndData(org.netbeans.modules.uml.core.metamodel.common.commonactions.ILinkEndData)
     */
    public void removeEndData(ILinkEndData pEndData)
    {
        removeElement(pEndData);
    }

}
