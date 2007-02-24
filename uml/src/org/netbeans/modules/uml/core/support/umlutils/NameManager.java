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
 * Created on Nov 12, 2003
 *
 */
package org.netbeans.modules.uml.core.support.umlutils;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NameResolver;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;

/**
 * @author aztec
 *
 */
public class NameManager
{

	/**
	 *
	 * Makes sure that the name used for a role name on a NavigableEnd is unique, given
	 * all the other attributes and navigableends associated with the classifier about
	 * to be modified
	 *
	 * @param pEnd[in]            The navigable end about to be named
	 * @param origRoleName[in]    The original name of the end
	 * @param roleName[in]        The proposed name
	 * @param count[in]           The number of times this method has recursed. This will be appended to the
	 *                            role name to ensure uniqueness.
	 *
	 * @return true if the name was successfully calculated, else false
	 */
	public static boolean ensureUniqueRoleName( INavigableEnd pEnd, 
												String origRoleName, 
												String roleName, 
												int count)
	{
		boolean success = true;
		if (pEnd != null)
		{
			IClassifier refClassifier = pEnd.getReferencingClassifier();
			if (refClassifier != null)
			{
				ETList<IAttribute> pAttrs = refClassifier.getAttributesAndNavEndsByName( roleName);
                for (int i = pAttrs.size() - 1; i >= count; --i) {
                    if (pEnd.isSame(pAttrs.get(i)))
                        return false;
                }
                    
				if (pAttrs != null)
				{
					if (pAttrs.size() > 0)
					{
						count++;
                        String attrName = origRoleName + count;
						success = ensureUniqueRoleName( pEnd, origRoleName, attrName, count ); 														
					}
					else
					{
						pEnd.setName(roleName);
					}
				}
				else
				{
					pEnd.setName(roleName);
				}
			}
		}
		return success;
	}
	
	/**
	 *
	 * Determines whether or not the passed in name is fully qualified
	 * in UML syntax ( "A::B::C" ). If it is, the type will be located.
	 *
	 * @param contextElement[in]  The element to query against
	 * @param name[in]            The name to check. If the name is not
	 *                            qualified, nothing occurs
	 * @param foundElement[out]   The found element, else 0
	 *
	 * @return true if the name coming in is qualified, else false
	 *
	 */
	public static INamedElement resolveFullyQualifiedName( IElement contextElement, String name) 
	{
		return NameResolver.resolveFullyQualifiedName(contextElement, name);								   
	}
							   
	

}



