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
 * Created on Feb 6, 2004
 *
 */
package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.eventframework.EventContext;

/**
 * @author sumitabhk
 *
 */
public class OperationSignatureChangeContext extends EventContext implements IOperationSignatureChangeContext
{

	/**
	 *
	 * The operation associated with this context
	 *
	 * @param pVal[out] The operation
	 *
	 * @return HRESULT
	 *
	 */
	public IOperation getOperation()
	{
		IOperation retOper = null;

		// The operation is stored on the context in the data field, 
		// which is a variant.
		Object obj = getData();
		if (obj != null && obj instanceof IOperation)
		{
			retOper = (IOperation)obj;
		}
		return retOper;
	}

	/**
	 *
	 * The operation associated with this context
	 *
	 * @param newVal[in] The operation
	 *
	 * @return HRESULT
	 *
	 */
	public void setOperation(IOperation newVal) 
	{
		// The operation is stored on the context in the data field, 
		// which is a variant.
		setData(newVal);
	}

}



