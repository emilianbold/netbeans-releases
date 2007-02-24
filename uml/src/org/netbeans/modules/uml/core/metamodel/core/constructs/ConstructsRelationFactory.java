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


package org.netbeans.modules.uml.core.metamodel.core.constructs;

import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationshipEventsHelper;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;


public class ConstructsRelationFactory implements IConstructsRelationFactory
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IConstructsRelationFactory#createExtend(org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase, org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase)
     */
    public IInclude createInclude(IUseCase from, IUseCase to)
    {
        TypedFactoryRetriever<IInclude> retriever = 
                                            new TypedFactoryRetriever<IInclude>();
        IInclude inc = retriever.createType("Include");
        
        if(inc != null)
        {
            RelationshipEventsHelper helper = new RelationshipEventsHelper(inc);
            if(helper != null)
            {
                if(helper.firePreRelationCreated(from, to))
                {
                    inc.setBase(from);
                    inc.setAddition(to);
                    helper.fireRelationCreated(); 
                    return inc;                  
                }
            }
            else
            {
                //TODO - Throw some exception
                return null; 
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IConstructsRelationFactory#createInclude(org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase, org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase)
     */
    public IExtend createExtend(IUseCase from, IUseCase to)
    {
        TypedFactoryRetriever<IExtend> retriever = 
                                            new TypedFactoryRetriever<IExtend>();
        IExtend ext = retriever.createType("Extend");
        
        if(ext != null)
        {
            RelationshipEventsHelper helper = new RelationshipEventsHelper(ext);
            if(helper != null)
            {
                if(helper.firePreRelationCreated(from, to))
                {
                    ext.setExtension(from);
                    ext.setBase(to);                    
                    helper.fireRelationCreated();
                    return ext;               
                }
            }
            else
            {
                //TODO - Throw some exception
                return null;
            }
        }
        return null;
    }

}
