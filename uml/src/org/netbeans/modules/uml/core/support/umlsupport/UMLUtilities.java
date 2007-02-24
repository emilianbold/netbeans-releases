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

/*
 * File       : UMLUtilities.java
 * Created on : Oct 7, 2003
 * Author     : aztec
 */
package org.netbeans.modules.uml.core.support.umlsupport;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;

/**
 * @author aztec
 */
public class UMLUtilities
{
    /**
     * Returns the classifier that owns @a pElement.  Walks up the tree of
     * owners if @a pElement's immediate owner is not an IClassifier.  
     * Note: If @a pElement is already an IClassifier, this method will search
     * for the IClassifer that owns @a pElement (i.e. the class that nests 
     * @a pElement).
     * 
     * @param element the IElement whose owning IClassifier you want.
     * @return the IClassifier that owns (directly or indirectly) @a pElement
     */
    public static IClassifier getOwningClassifier(IElement element)
    {
        IClassifier owningClassifier = null;
        IElement    owner;
        owner = element.getOwner();
        
        while (owner != null)
        {
            if (owner instanceof IClassifier)
            {
                owningClassifier = (IClassifier) owner;
                break;
            }

            owner = owner.getOwner();
        }
        return owningClassifier;
    }
    
    public static IClassifier getOutermostNestingClass(IClassifier classifier)
    {
        IClassifier outermost = classifier;
        IElement owner = classifier;
        while (owner != null)
        {
            owner = owner.getOwner();
            if (owner instanceof IClassifier)
            {
                outermost = (IClassifier) owner;
            }
            else if (owner instanceof INamespace)
            {
                break;
            }
        }
        
        return outermost;
    }
}