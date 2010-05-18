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
