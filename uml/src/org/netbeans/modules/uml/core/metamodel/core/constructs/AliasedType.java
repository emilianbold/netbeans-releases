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
