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
 * EnumLiteralChangeFacility.java
 *
 * Created on April 8, 2005, 12:48 PM
 */

package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.core.roundtripframework.EnumLiteralChangeFacility;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;

/**
 *
 * @author Administrator
 */
public class JavaEnumLiteralChangeFacility extends EnumLiteralChangeFacility 
                                           implements IJavaEnumLiteralChangeFacility
{
   private ILanguage m_Language = null;
   private JavaEnumLiteralChangeHandler mHandler = new JavaEnumLiteralChangeHandler();
   
   /** Creates a new instance of EnumLiteralChangeFacility */
   public JavaEnumLiteralChangeFacility()
   {
   }

    public void added(IEnumerationLiteral literal)
    {
       if (literal != null)
       {
          IEnumeration cl = literal.getEnumeration();
          mHandler.added(literal, true, cl);
       }
    }

    public void deleted(IEnumerationLiteral literal, IEnumeration enumeration)
    {
       if (literal != null || enumeration != null)
       {
          mHandler.deleted(literal, enumeration);
       }
    }

    public ILanguage getLanguage()
    {
       if (m_Language == null)
        {
            ICoreProduct product = ProductRetriever.retrieveProduct();
            m_Language = product.getLanguageManager().getLanguage("Java");
        }
        return m_Language;
    }

    public void nameChanged(IEnumerationLiteral literal)
    {
       if (literal == null)
       {        
           IEnumeration cl = literal.getEnumeration();
           mHandler.nameChange(literal, cl);
       }
    }
   
}
