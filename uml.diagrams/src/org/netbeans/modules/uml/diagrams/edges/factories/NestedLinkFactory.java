/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.uml.diagrams.edges.factories;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationship;
import org.netbeans.modules.uml.core.metamodel.structure.IModel;
import org.netbeans.modules.uml.drawingarea.palette.RelationshipFactory;

/**
 *
 * @author treyspiva
 */
public class NestedLinkFactory implements RelationshipFactory
{

    public NestedLinkFactory()
    {
    }

    public IRelationship create(IElement source, IElement target)
    {
        // A nested link is not a relationship.  Therefore, it does not return
        // a relationship.  Therefore I had to implement a palette model instead.
        return null;
    }

    public void reconnectSource(IElement relationship, 
                                IElement oldSource, 
                                IElement source,
                                IElement target)
    {
        if(target instanceof INamedElement) 
        {
            INamedElement namedElement = (INamedElement)target;
            
            if (oldSource instanceof INamespace)
            {
                INamespace space = (INamespace) oldSource;
                space.removeOwnedElement(namedElement);
            }

            
            if(source instanceof INamespace)
            {
               INamespace space = (INamespace)source; 
               space.addOwnedElement(namedElement);
            }
        }
    }

    public void reconnectTarget(IElement relationship, 
                                IElement oldTarget, 
                                IElement target,
                                IElement source)
    {
        if(source instanceof INamespace) 
        {
            INamespace space = (INamespace)source;
            
            if (oldTarget instanceof INamedElement)
            {
                INamedElement namedElement = (INamedElement) oldTarget;
                space.removeOwnedElement(namedElement);
                
                INamespace newSpace = space.getOwningPackage();
                if(newSpace != null)
                {
                    newSpace.addOwnedElement(namedElement);
                }
            }

            
            if(target instanceof INamedElement)
            {
               INamedElement namedElement = (INamedElement)target; 
               space.addOwnedElement(namedElement);
            }
        }
    }

    public void delete(boolean fromModel, IPresentationElement element, 
                       IElement source, IElement target)
    {
        if(fromModel && (source instanceof INamespace) && (target instanceof INamedElement))
        {
            // If the namespace is the model, we can not move it up any levels.
            // Therefore, do nothing if the namespace is the model.
            if(!(source instanceof IModel))
            {
                INamespace space = (INamespace)source;
                INamedElement namedElement = (INamedElement)target;
                space.removeOwnedElement(namedElement);
                
                INamespace newSpace = space.getOwningPackage();
                if(newSpace != null)
                {
                    newSpace.addOwnedElement(namedElement);
                }
            }
            
        }
    }
    
    public String getElementType()
    {
        return "NestedLink";
    }
    
}
