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
 * File       : SynchronizedAction.java
 * Created on : Sep 18, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class SynchronizedAction
    extends CompositeAction
    implements ISynchronizedAction
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ISynchronizedAction#addSubAction(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction)
     */
    public void addSubAction(IAction pAction)
    {
        addChild("UML:SynchronizedAction.subAction"
                    ,"UML:SynchronizedAction.subAction"
                    ,pAction);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ISynchronizedAction#getSubActions()
     */
    public ETList<IAction> getSubActions()
    {
        return new ElementCollector< IAction >()
            .retrieveElementCollection((ISynchronizedAction)this, "UML:SynchronizedAction.subAction/*", IAction.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ISynchronizedAction#removeSubAction(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction)
     */
    public void removeSubAction(IAction pAction)
    {
        UMLXMLManip.removeChild(m_Node, pAction);
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:SynchronizedAction", doc, node);
    }      

}
