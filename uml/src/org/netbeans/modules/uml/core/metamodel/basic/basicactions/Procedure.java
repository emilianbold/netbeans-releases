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

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Behavior;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class Procedure extends Behavior implements IProcedure
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IProcedure#addAction(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction)
     */
    public void addAction(IAction pAction)
    {
        // C++ code does nothing
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IProcedure#getActions()
     */
    public ETList <IAction> getActions()
    {
        // C++ code does nothing
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.basic.basicactions.IProcedure#removeAction(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction)
     */
    public void removeAction(IAction pAction)
    {
        // C++ code does nothing
    }
    
    public void establishNodePresence(Document doc, Node parent)
    {
        buildNodePresence("UML:Procedure", doc, parent);
    }  

}
