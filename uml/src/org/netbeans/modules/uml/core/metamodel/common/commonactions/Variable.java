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
 * File       : Variable.java
 * Created on : Sep 17, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.dom4j.Document;
import org.dom4j.Node;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.TypedElement;

/**
 * @author Aztec
 */
public class Variable extends TypedElement implements IVariable
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IVariable#getScope()
     */
    public IGroupAction getScope()
    {
		return OwnerRetriever.getOwnerByType(this, IGroupAction.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IVariable#setScope(org.netbeans.modules.uml.core.metamodel.common.commonactions.IGroupAction)
     */
    public void setScope(IGroupAction pGrpAction)
    {
        setOwner(pGrpAction);
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:Variable", doc, node);
    }        

}
