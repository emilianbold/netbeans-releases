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

package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public class RPMethodSelection implements IRPMethodSelection
{
	/**
	 * Allows the user to override a parents operations.  The operations
	 * that are possible to override are gathered from the element.
	 *
	 * @param parentWnd [in] The parent window.
	 * @param *pElement [in] The element that will override the methods.
	 */
  public void overrideMethods(IClassifier pElement)
  {
  	if (pElement != null)
  	{
  		JavaChangeHandlerUtilities utils = new JavaChangeHandlerUtilities();
  		ETList<IClassifier> pClassifiers = utils.getGeneralizations(pElement);
  		if (pClassifiers != null)
  		{
  			RequestValidator request = new RequestValidator();
  			OperationCollectionBehavior behavior = new OperationCollectionBehavior();
  			utils.applyInheritedOperations(request, pClassifiers, pElement, behavior);
  		}
  	}
  }

  /**
   * Allows the user to override a parents operations.  The operations that are
   * possible to override are specified.
   *
   * @param parentWnd [in] The parent window.
   * @param pElement [in] The elements that will override the methods.
   * @param pRedefinOps [in] The operations that already override a parents methods.
   * @param pNoRedefOps [in] The operatoins that can be overriden.
   */
  public void overrideMethods(ETList<IClassifier> pElements, ETList<IOperation> pRedefinOps, ETList<IOperation> pNoRedefOps)
  {
  	//MethodsSelectionDialog dialog = new MethodsSelectionDialog();
  }

}
