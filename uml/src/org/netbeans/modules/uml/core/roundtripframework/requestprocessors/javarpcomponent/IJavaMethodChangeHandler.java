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
 * Created on Nov 5, 2003
 *
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author aztec
 *
 */
public interface IJavaMethodChangeHandler extends IJavaChangeHandler 
{
	public void handleRequest(IRequestValidator request);
	
   public void transformToEnumeration (IRequestValidator requestValidator, 
                                       IOperation pOperation, 
								               IClassifier pClass, 
                                       boolean doAbstractChange);
                                       
	public void transformToClass (IRequestValidator pRequest, IOperation pOperation, IClassifier pClass, boolean doAbstractChange);
	
	public void transformToInterface(IRequestValidator request, IOperation pOperation, 
										IClassifier pClass, boolean doAbstractChange);
	
	public void deleted(IOperation oper);
	
	public void added(IRequestValidator requestValidator, IOperation pOperation, IClassifier pClass);
	
	public void parameterNameChange(IRequestValidator requestValidator, IParameter pParameter);
	
	public void parameterAdded(IRequestValidator request, IParameter pParameter );
	
	public void parameterChange(IRequestValidator request, IParameter pParameter);
	
	public void parameterChange(IParameter pParameter);
	
	public void abstractChange(IRequestValidator request, IOperation pOperation);
	
	public void abstractChange(IRequestValidator request, IOperation pOperation, IClassifier pClass);
	
	public void nameChange(IOperation pOperation);
	
	public void moved(IRequestValidator request, IOperation pOperation, 
					  IClassifier pFromClass, IClassifier pToClass);
					  
	public void parameterDeleted(IOperation pOperation, IParameter pParameter);
	
	public void typeChange(IRequestValidator request, IOperation pOperation);
	
	public void typeChange(IOperation pOperation);
	
	public void addDependency ( IRequestValidator request, IOperation pOperation );
	
	public void addDependency ( IRequestValidator request, IOperation pOperation, IClassifier pDependentClass );
	
	public void addDependency ( IRequestValidator request, IParameter pParameter, IClassifier pDependentClass );
	
	public void addDependency ( IRequestValidator request, IParameter pParameter, IOperation pOperationOfDependentClass );

	public void deleteList(ETList<IOperation> ops, boolean queryOnce);
}



