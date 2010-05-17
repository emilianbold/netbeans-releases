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

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement;


public class Gate extends NamedElement implements IGate
{
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IGate#getToConnector()
     */
    public IInterGateConnector getToConnector()
    {
        return new ElementCollector<IInterGateConnector>()
            .retrieveSingleElementWithAttrID( this, "toConnector", IInterGateConnector.class);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IGate#setToConnector(org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector)
     */
    public void setToConnector(final IInterGateConnector gateConnector)
    {
        new ElementConnector<IGate>()
            .addChildAndConnect( this, true, "toConnector", "toConnector", 
                gateConnector, 
                new IBackPointer<IGate>()
                {
                    public void execute(IGate g)
                    {
                        gateConnector.setToGate(g);
                    }
                } );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IGate#getFromConnector()
     */
    public IInterGateConnector getFromConnector()
    {
        return new ElementCollector<IInterGateConnector>()
            .retrieveSingleElementWithAttrID( this, "fromConnector", IInterGateConnector.class);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IGate#setFromConnector(org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector)
     */
    public void setFromConnector(final IInterGateConnector gateConnector)
    {
        new ElementConnector<IGate>()
            .addChildAndConnect( this, true, "fromConnector", "fromConnector", 
                gateConnector, 
                new IBackPointer<IGate>()
                {
                    public void execute(IGate g)
                    {
                        gateConnector.setFromGate(g);
                    }
                } );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IGate#getInteraction()
     */
    public IInteraction getInteraction()
    {
        INamespace space = getNamespace();
        return space instanceof IInteraction? (IInteraction) space : null;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IGate#setInteraction(org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction)
     */
    public void setInteraction(IInteraction inter)
    {
        if (inter instanceof INamespace)
            setNamespace((INamespace) inter);
    }
    
    public void establishNodePresence(Document doc, Node parent)
    {
        buildNodePresence("UML:Gate", doc, parent);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#performDependentElementCleanup(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement)
     */
    protected void performDependentElementCleanup(IVersionableElement elem)
    {
        IInterGateConnector connector = getToConnector();
        if (connector != null)
            connector.delete();
        
        connector = getFromConnector();
        if (connector != null)
            connector.delete();
        
        super.performDependentElementCleanup(elem);
    }
}
