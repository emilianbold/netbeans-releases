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


package org.netbeans.modules.uml.core.metamodel.dynamics;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.Element;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;


public class InterGateConnector extends Element implements IInterGateConnector
{
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector#getMessage()
     */
    public IMessage getMessage()
    {
        return new ElementCollector<IMessage>()
            .retrieveSingleElementWithAttrID( this, "message", IMessage.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector#setMessage(org.netbeans.modules.uml.core.metamodel.dynamics.IMessage)
     */
    public void setMessage(IMessage value)
    {
        setElement(value, "message");
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector#getEventOccurrence()
     */
    public IEventOccurrence getEventOccurrence()
    {
        return new ElementCollector<IEventOccurrence>()
            .retrieveSingleElementWithAttrID( this, "event", IEventOccurrence.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector#setEventOccurrence(org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence)
     */
    public void setEventOccurrence(final IEventOccurrence event)
    {
        new ElementConnector<IInterGateConnector>()
            .addChildAndConnect( 
                this, true, "event", "event",
                event,
                new IBackPointer<IInterGateConnector>()
                {
                    public void execute(IInterGateConnector iigc)
                    {
                        event.setConnection(iigc);
                    }
                } );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector#getFragment()
     */
    public IInteractionFragment getFragment()
    {
        return new ElementCollector<IInteractionFragment>()
            .retrieveSingleElementWithAttrID( this, "fragment", IInteractionFragment.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector#setFragment(org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment)
     */
    public void setFragment(IInteractionFragment frag)
    {
        new ElementConnector<IInterGateConnector>( )
            .setSingleElementAndConnect( 
                this, frag, "fragment",
                new IBackPointer<IInteractionFragment>( )
                {
                    public void execute(IInteractionFragment ifrag)
                    {
                        ifrag.addGateConnector(InterGateConnector.this);
                    }
                },
                new IBackPointer<IInteractionFragment>( )
                {
                    public void execute(IInteractionFragment ifrag)
                    {
                        ifrag.removeGateConnector(InterGateConnector.this);
                    }
                } );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector#getToGate()
     */
    public IGate getToGate()
    {
        return new ElementCollector<IGate>( )
            .retrieveSingleElementWithAttrID( this, "toGate", IGate.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector#setToGate(org.netbeans.modules.uml.core.metamodel.dynamics.IGate)
     */
    public void setToGate(final IGate value)
    {
        new ElementConnector<IInterGateConnector>( )
            .addChildAndConnect( 
                this, true, "toGate", "toGate", 
                value,
                new IBackPointer<IInterGateConnector>( )
                {
                    public void execute(IInterGateConnector iigc)
                    {
                        value.setToConnector(iigc);
                    }
                } );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector#getFromGate()
     */
    public IGate getFromGate()
    {
        return new ElementCollector<IGate>( )
            .retrieveSingleElementWithAttrID( this, "fromGate", IGate.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector#setFromGate(org.netbeans.modules.uml.core.metamodel.dynamics.IGate)
     */
    public void setFromGate(final IGate value)
    {
        new ElementConnector<IInterGateConnector>( )
            .addChildAndConnect( 
                this, true, "fromGate", "fromGate", 
                value,
                new IBackPointer<IInterGateConnector>( )
                {
                    public void execute(IInterGateConnector iigc)
                    {
                        value.setFromConnector(iigc);
                    }
                } );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#establishNodePresence(org.dom4j.Document, org.dom4j.Node)
     */
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:InterGateConnector", doc, node);
    }
}
