/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uml.diagrams.edges.factories;

import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.StateRelationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;

/**
 *
 * @author Sheryl Su
 */
public final class StateTransitionFactory extends AbstractRelationshipFactory
{

    public StateTransitionFactory()
    {
    }

    public IElement create(IElement source, IElement target)
    {
        StateRelationFactory factory = new StateRelationFactory();


        if ((source instanceof IStateVertex) && (target instanceof IStateVertex))
        {
            IStateVertex sourceNode = (IStateVertex) source;
            IStateVertex targetNode = (IStateVertex) target;
            return factory.createTransition(sourceNode,targetNode,null);
            
        }
        return null;
    }

    protected void reconnectSource(IElement relationship, IElement source)
    {
        if ((relationship instanceof ITransition) &&
                (source instanceof IStateVertex))
        {
            IStateVertex sourceNode = (IStateVertex) source;
            ITransition transition = (ITransition) relationship;
            transition.setSource(sourceNode);
        }
    }

    protected void reconnectTarget(IElement relationship, IElement target)
    {
        if ((relationship instanceof ITransition) &&
                (target instanceof IStateVertex))
        {
            IStateVertex targetNode = (IStateVertex) target;
            ITransition transition = (ITransition) relationship;
            transition.setTarget(targetNode);
        }
    }

    public String getElementType()
    {
        return "Transition";
    }
}
