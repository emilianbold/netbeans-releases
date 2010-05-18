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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.palette.RelationshipFactory;

/**
 *
 * @author treyspiva
 */
public abstract class AbstractRelationshipFactory implements RelationshipFactory
{
    public void delete(boolean fromModel, 
                       IPresentationElement element,
                       IElement source, 
                       IElement target)
    {
        if(fromModel == true)
        {
            IElement modelElement = element.getFirstSubject(); 
            if(modelElement != null)
            {
                modelElement.delete();
            }
        }
            
        element.delete();
    }
    
    /**
     * Reconnects a new source to the relationship.  Since some relationships 
     * are not true relationships, but instead represent properties on one of 
     * the nodes, the old source and the target are also passed into the method.
     * 
     * To make it easy for true relationships, which are the majority of 
     * relationships, this implementation calls the abstract reconnectSource
     * that only takes the relationship and the source element.
     * 
     * @param relationship The model element associated with the edge.
     * @param oldSource The old source end of the edge.
     * @param source The new source end of the edge.
     * @param target The target end of the edge.
     * @param see #reconnectSource(IElement, IElement)
     */
    public void reconnectSource(IElement relationship, 
                                IElement oldSource, 
                                IElement source,
                                IElement target)
    {
        reconnectSource(relationship, source);
    }
    
    /**
     * Reconnects a new target to the relationship.  Since some relationships 
     * are not true relationships, but instead represent properties on one of 
     * the nodes, the old target and the source are also passed into the method.
     * 
     * To make it easy for true relationships, which are the majority of 
     * relationships, this implementation calls the abstract reconnectSource
     * that only takes the relationship and the target element.
     * 
     * @param relationship The model element associated with the edge.
     * @param oldTarget The old target end of the edge.
     * @param target The new target end of the edge.
     * @param source The source end of the edge.
     * @param see #reconnectTarget(IElement, IElement)
     */
    public void reconnectTarget(IElement relationship, 
                                IElement oldTarget, 
                                IElement target,
                                IElement source)
    {
        reconnectTarget(relationship, target);
    }
    
    /**
     * Since most relationship factories are true relationships, this method is 
     * provided to make it easier to reconnect the source of standard relationships.  
     * 
     * @param relationship The relationship.
     * @param source The relationships new source.
     */
    protected abstract void reconnectSource(IElement relationship, IElement source);
    
    /**
     * Since most relationship factories are true relationships, this method is 
     * provided to make it easier to reconnect the target of standard relationships.  
     * 
     * @param relationship The relationship.
     * @param source The relationships new target.
     */
    protected abstract void reconnectTarget(IElement relationship, IElement target);
}
