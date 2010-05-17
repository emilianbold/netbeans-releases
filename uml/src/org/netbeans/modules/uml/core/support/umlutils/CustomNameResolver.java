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

package org.netbeans.modules.uml.core.support.umlutils;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.profiles.IStereotype;

//import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
//import org.netbeans.modules.uml.core.metamodel.profiles.IStereotype;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class CustomNameResolver implements ICustomNameResolver{

  public CustomNameResolver() {

  }

  /**
   * Validate the passed in values according to the Describe business rules.
   * See method for the rules.
   *
   * @param pDisp[in]			The dispatch that needs validating (the property element)
   * @param fieldName[in]		The name of the field to validate
   * @param fieldValue[in]	The string to validate
   * @param outStr[out]		The string changed to be valid (if necessary)
   * @param bValid[out]		Whether the string is valid as passed in
   *
   * @return HRESULT			S_OK if needs to still be processed
   *									S_FALSE if does not need to be processed (this routine handled it)
   */
  public boolean validate( Object pDisp, String fieldName, String fieldValue)
  {
    if (pDisp != null && pDisp instanceof IPropertyElement)
    {
      IPropertyElement elem = (IPropertyElement)pDisp;
      Object obj = elem.getElement();
      if (obj instanceof IElement)
      {
        IElement curElem = (IElement)obj;
        // right now only worried about stereotypes
        // we were originally calling put_Name on the stereotype, but this was causing
        // problems with the name collision code, so we created this object
        // to help in deciding whether or not the put method should be called
        if (obj instanceof IStereotype)
        {
          IStereotype ster = (IStereotype)obj;

          // if it is a stereotype, then this object will be responsible for swapping
          // out the stereotype, because the user is switching the name of the stereotype
          // ie. changing stereotypes (not renaming the stereotype)
          if (fieldName.equals("Name"))
          {
            // we are switching stereotypes, so we need to go up the property element chain
            // and get the right elements
            // our property element structure looks like:
            // Class
            //   Stereotype
            //		 Name      <== this is the property element that we are on when this method
            //                   is called
            // to remove and reassign stereotypes, we need to be at the class level
            IPropertyElement pParent = elem.getParent();
            if (pParent != null)
            {
              IPropertyElement gParent = pParent.getParent();
              if (gParent != null)
              {
                // have the right property element, so get the model element on it
                Object pParentDisp = gParent.getElement();
                if (pParentDisp instanceof IElement)
                {
                  IElement pParentEle = (IElement)pParentDisp;
                  pParentEle.removeStereotype(ster);
                  String value = elem.getValue();
                  // add the new stereotype for the class, this method will take care
                  // of adding it if it does not already exist
                  pParentEle.applyStereotype2(value);
                  // we are going to return s_false here because we have handled the
                  // setting of the data
                  // the method calling this routine will check the result and if it is
                  // s_false, it will not do the set data
                  return false;
                }
              }
            }
          }
        }
      }
    }
    return true;
  }

  public void whenValid( Object pDisp )
  {

  }

  public void whenInvalid( Object pDisp )
  {

  }
}
