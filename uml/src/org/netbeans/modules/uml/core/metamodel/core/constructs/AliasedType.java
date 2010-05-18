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

package org.netbeans.modules.uml.core.metamodel.core.constructs;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;


public class AliasedType extends DataType implements IAliasedType
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IAliasedType#getActualType()
     */
    public IClassifier getActualType()
    {
        ElementCollector< IClassifier > col = new ElementCollector< IClassifier >();
        return col.retrieveSingleElementWithAttrID(this, "actualType", IClassifier.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IAliasedType#getAliasedName()
     */
    public String getAliasedName()
    {
        return getAttributeValue("aliasedName");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IAliasedType#getTypeDecoration()
     */
    public String getTypeDecoration()
    {
        return getAttributeValue("typeDecoration");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IAliasedType#setActualType(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void setActualType(IClassifier actualType)
    {
        setElement(actualType, "actualType");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IAliasedType#setActualType2(java.lang.String)
     */
    public void setActualType2(String actualType)
    {
        if(actualType != null)
        {
            IClassifier classifier =
                (IClassifier) resolveSingleTypeFromString(actualType);
            setActualType(classifier);
            
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IAliasedType#setAliasedName(java.lang.String)
     */
    public void setAliasedName(String name)
    {
        setAttributeValue("aliasedName", name);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IAliasedType#setTypeDecoration(java.lang.String)
     */
    public void setTypeDecoration(String typeDec)
    {
        setAttributeValue("typeDecoration", typeDec);
    }
    
    public String getName()
    {
        String aliasedName = getAliasedName();
        if(aliasedName != null && aliasedName.trim().length() > 0)
        {
            aliasedName += getTypeDecoration();
            return aliasedName;            
        }
        return null;
    }
    
    public void setName(String name)
    {
        super.setName(name);
        setAliasedName(name);
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:AliasedType", doc, node);
    }

}
