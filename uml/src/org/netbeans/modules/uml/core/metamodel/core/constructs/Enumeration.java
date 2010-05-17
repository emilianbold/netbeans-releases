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


package org.netbeans.modules.uml.core.metamodel.core.constructs;

import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.EventContextManager;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.dom4j.Document;
import org.dom4j.Node;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Feature;


public class Enumeration extends DataType implements IEnumeration
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration#addLiteral(org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral)
     */
    public void addLiteral(final IEnumerationLiteral literal)
    {
        if (literal != null)
        {
            // Pop the context that has been plugging events
            // for feature.
            revokeEventContext(literal);

            EventDispatchRetriever ret = EventDispatchRetriever.instance();

            IClassifierEventDispatcher disp =
                    (IClassifierEventDispatcher) ret.getDispatcher(
                    EventDispatchNameKeeper.classifier());

            boolean proceed = true;

            if (disp != null)
            {
                IEventPayload payload = disp.createPayload("EnumerationLiteralPreAdded");
                proceed = disp.fireEnumerationLiteralPreAdded(this, literal, payload);
            }

            for(IEnumerationLiteral curLiteral : getLiterals())
            {
                if(curLiteral.isSame(literal) == true)
                {
                    proceed = false;
                    break;
                }
            }
            
            if (proceed)
            {
                new ElementConnector<IEnumeration>().addChildAndConnect(
                        this,
                        false,
                        "UML:Enumeration.literal", // NOI18N
                        "UML:Enumeration.literal", // NOI18N
                        literal,
                        new IBackPointer<IEnumeration>()
                        {

                            public void execute(IEnumeration enumeration)
                            {
                                literal.setEnumeration(enumeration);
                            }
                        });

                if (disp != null)
                {
                    IEventPayload payload = disp.createPayload("EnumerationLiteralAdded");
                    disp.fireEnumerationLiteralAdded(this, literal, payload);
                } else
                {
                    proceed = false;
                }
            }
        }
    }
    
	
    protected void revokeEventContext(IEnumerationLiteral feature)
	{
		EventContextManager man = new EventContextManager();
		man.revokeEventContext(feature, null);
	}
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration#getLiterals()
     */
    public ETList <IEnumerationLiteral> getLiterals()
    {
        ElementCollector< IEnumerationLiteral > col = new ElementCollector< IEnumerationLiteral >();
        return col.retrieveElementCollection(m_Node, "UML:Enumeration.literal/*", IEnumerationLiteral.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration#removeLiteral(org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral)
     */
    public void removeLiteral(final IEnumerationLiteral literal)
    {
        new ElementConnector< IEnumeration >().removeElement( 
                        this, 
                        literal, 
                        "UML:Enumeration.literal/*",
                        new IBackPointer<IEnumeration>() {
                            public void execute(IEnumeration enumeration) {
                                literal.setEnumeration(enumeration);
                            }
                        } 
        );
    }
    
    public IEnumerationLiteral createLiteral(String name)
    {
       IEnumerationLiteral retVal = null;
       FactoryRetriever ret = FactoryRetriever.instance();
       
       Object obj = ret.createType("EnumerationLiteral", null);
       if (obj != null && obj instanceof IEnumerationLiteral)
       {
          retVal = (IEnumerationLiteral)obj;
          
          addLiteral(retVal);
          retVal.setName(name);
       }
       return retVal;
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:Enumeration", doc, node);
    }
    
    public ETList <String> getCollidingNamesForElement(INamedElement elem)
    {
       ETArrayList <String> list = new  ETArrayList <String>();

       list.add("UML:Class");
       list.add("UML:Interface");
       list.add("UML:Enumeration");

       return (ETList)list;
    } 
}
