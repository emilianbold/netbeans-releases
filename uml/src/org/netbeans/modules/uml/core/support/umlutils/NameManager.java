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



