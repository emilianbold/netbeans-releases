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

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface; 
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationship;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;

/**
 *
 * @author treyspiva
 */
public class GeneralizationFactory extends AbstractRelationshipFactory
{

    public GeneralizationFactory()
    {
    }

    public IRelationship create(IElement source, IElement target)
    {
        IRelationFactory factory = new org.netbeans.modules.uml.core.metamodel.infrastructure.RelationFactory();
        
        IRelationship retVal = null;
        
        if((source instanceof IClassifier) && (target instanceof IClassifier)
           && !(target instanceof ICollaboration)) 
        {
            // link to self is not allowed 
            if ( source == target || source.isSame(target))
            {
               return retVal;
            }
            
            if ((target instanceof IInterface) && !(source instanceof IInterface))
            {
                IPackage space = source.getOwningPackage();
            
                ETPairT<IInterface, IDependency> value = 
                    factory.createImplementation((IClassifier)source, 
                                                 (IInterface)target,
                                                 space);
               retVal = value.getParamTwo();
            }
            else
            {
                retVal = factory.createGeneralization((IClassifier)target, 
                                                      (IClassifier)source);
            }
        }
        
        return retVal;
    }

    public void reconnectSource(IElement relationship, IElement source)
    {
        if((relationship instanceof IGeneralization) &&
           (source instanceof IClassifier))
        {
            IGeneralization generalization = (IGeneralization)relationship;
            generalization.setSpecific((IClassifier)source);
        }
    }

    public void reconnectTarget(IElement relationship, IElement target)
    {
        if((relationship instanceof IGeneralization) &&
           (target instanceof IClassifier))
        {
            IGeneralization generalization = (IGeneralization)relationship;
            generalization.setGeneral((IClassifier)target);
        }
    }
    
    public String getElementType()
    {
        return "Generalization";
    }
}
