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

package org.netbeans.modules.uml.core.metamodel.dynamics;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IFlow;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IReference;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;


public class ActionOccurrence extends ExecutionOccurrence 
    implements IActionOccurrence
{
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:ActionOccurrence", doc, node);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IActionOccurrence#getAction()
     */
    public IAction getAction()
    {
        return new ElementCollector<IAction>().
            retrieveSingleElementWithAttrID(this, "action", IAction.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IActionOccurrence#setAction(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction)
     */
    public void setAction(IAction value)
    {
        setElement(value, "action");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IActionOccurrence#getContainingExecOccurrence()
     */
    public IExecutionOccurrence getContainingExecOccurrence()
    {
        return new ElementCollector<IExecutionOccurrence>().
            retrieveSingleElementWithAttrID(this, "containing", IExecutionOccurrence.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IActionOccurrence#setContainingExecOccurrence(org.netbeans.modules.uml.core.metamodel.dynamics.IExecutionOccurrence)
     */
    public void setContainingExecOccurrence(IExecutionOccurrence value)
    {
        setElement(value, "containing");
    }
    
    protected void performDependentElementCleanup()
    {
        IAction act = getAction();
        if (act != null)
            act.delete();
    }
}