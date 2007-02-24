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


package org.netbeans.modules.uml.reporting.dataobjects;

import java.util.ArrayList;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 *
 * @author Sheryl
 */
public class CollaborationData extends ClassData
{
    private ICollaboration element;
    
    /** Creates a new instance of CollaborationData */
    public CollaborationData()
    {
    }
    
    public void setElement(IElement e)
    {
        if (e instanceof ICollaboration)
            this.element = (ICollaboration)e;
    }
    
    public ICollaboration getElement()
    {
        return element;
    }
    
    public IClassifier[] getNestedClasses()
    {
        ArrayList<IClassifier> list = new ArrayList();
        ETList<IClassifier> nested = getElement().getNestedClassifiers();
        for (int i=0; i<nested.size(); i++)
        {
            list.add(nested.get(i));
        }
        IClassifier[] a = new IClassifier[list.size()];
        return list.toArray(a);
    }
    
}
