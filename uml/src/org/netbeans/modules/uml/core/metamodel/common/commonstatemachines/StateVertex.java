/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * File       : StateVertex.java
 * Created on : Sep 19, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonstatemachines;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.Namespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class StateVertex extends Namespace implements IStateVertex
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex#addIncomingTransition(org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition)
     */
    public void addIncomingTransition(final ITransition pTran)
    {
        ensureOwnership(pTran);
        
        new ElementConnector< IStateVertex >()
            .addChildAndConnect(
                        this, 
                        true, 
                        "incoming", 
                        "incoming", 
                        pTran, 
                        new IBackPointer<IStateVertex>()
                        {
                            public void execute(IStateVertex obj)
                            {
                                pTran.setTarget(obj);
                            }
                        }
        );

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex#addOutgoingTransition(org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition)
     */
    public void addOutgoingTransition(final ITransition pTran)
    {
        ensureOwnership(pTran);
        
        new ElementConnector< IStateVertex >()
            .addChildAndConnect(
                        this, 
                        true, 
                        "outgoing", 
                        "outgoing", 
                        pTran, 
                        new IBackPointer<IStateVertex>()
                        {
                            public void execute(IStateVertex obj)
                            {
                                pTran.setSource(obj);
                            }
                        }
        );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex#getContainer()
     */
    public IRegion getContainer()
    {
		return OwnerRetriever.getOwnerByType(this, IRegion.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex#getIncomingTransitions()
     */
    public ETList<ITransition> getIncomingTransitions()
    {
        return new ElementCollector< ITransition >()
            .retrieveElementCollectionWithAttrIDs(this, "incoming", ITransition.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex#getOutgoingTransitions()
     */
    public ETList<ITransition> getOutgoingTransitions()
    {
        return new ElementCollector< ITransition >()
            .retrieveElementCollectionWithAttrIDs(this, "outgoing", ITransition.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex#removeIncomingTransition(org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition)
     */
    public void removeIncomingTransition(final ITransition pTran)
    {
        new ElementConnector< IStateVertex >()
            .removeByID(
                    this, 
                    pTran, 
                    "incoming",
                    new IBackPointer<IStateVertex>()
                   {
                       public void execute(IStateVertex obj)
                       {
                           pTran.setTarget(obj);
                       }
                   }
            );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex#removeOutgoingTransition(org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition)
     */
    public void removeOutgoingTransition(final ITransition pTran)
    {
        new ElementConnector< IStateVertex >()
            .removeByID(
                    this, 
                    pTran, 
                    "outgoing",
                    new IBackPointer<IStateVertex>()
                   {
                       public void execute(IStateVertex obj)
                       {
                           pTran.setSource(obj);
                       }
                   }
            );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex#setContainer(org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IRegion)
     */
    public void setContainer(IRegion value)
    {
        setNamespace(value);
    }
    
    protected void ensureOwnership(ITransition pTran)
    {
        IElement owner = pTran.getOwner();
        
        if(owner == null)
        {
            IRegion cont = getContainer();
            
            if(cont != null)
                cont.addTransition(pTran);
        }
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:StateVertex", doc, node);
    }  

}
