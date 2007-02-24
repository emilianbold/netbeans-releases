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



package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;

/**
 * @author sumitabhk
 *
 */
public class OperationSignatureChangeContextManager implements IOperationSignatureChangeContextManager
{
	private IOperation m_Operation = null;
	private String m_ContextName = null;
	
	/**
	 *
	 */
	public OperationSignatureChangeContextManager()
	{
		super();
	}

	/**
	 *
	 * Gets the operation whose signature change is encapsulated by a context managed by this manager. 
	 *
	 * @param pVal[out] The operation.
	 *
	 * @return HRESULT
	 *
	 */
	public IOperation getOperation()
	{
		return m_Operation;
	}

	/**
	 *
	 * Constructs a signature change context, setting the operation of the context,
	 * and pushes that context onto the event dispatch controller. If this manager
	 * is already responsible for a context, the current one is popped and a new 
	 * one is pushed. To change the signature of more than one operation without
	 * popping a context, create a new manager.
	 *
	 * @param newVal[in] The operation.
	 *
	 * @return HRESULT
	 *
	 */
	public void startSignatureChange(IOperation newVal)
	{
		// Before we start a new context, do we have an old one to pop?
		endSignatureChange();
		
		IEventDispatchController pController = getController();
		if (newVal != null && pController != null)
		{
			// Push a new context
			IOperationSignatureChangeContext pContext = new OperationSignatureChangeContext();
			String name = buildContextName(newVal);
			
			// Setup the context
			pContext.setName(name);
			pContext.setOperation(newVal);
			
			// Now push the final context
			pController.pushEventContext3(pContext);
			m_Operation = newVal;
		}
	}

	/**
	 *
	 * Pops the context from the event dispatch controller.
	 * This function can be called to force the context to be popped before the 
	 * manager destructs.
	 *
	 *
	 * @return HRESULT
	 *
	 */
	public void endSignatureChange()
	{
		if (m_Operation != null)
		{
			IEventDispatchController pController = getController();
			if (pController != null)
			{
				// Pop the context
				pController.removeEventContextByName(m_ContextName);
			}
		}
		m_Operation = null;
	}

	/**
	 *
	 * Get the dispatch controller on which the context will be pushed and popped.
	 *
	 * @param pController[out] The dispatch controller
	 *
	 * @return HRESULT
	 *
	 */
	private IEventDispatchController getController()
	{
		IEventDispatchController retObj = null;
		ICoreProduct pProd = ProductRetriever.retrieveProduct();
		if (pProd != null)
		{
			retObj = pProd.getEventDispatchController();
		}
		return retObj;
	}

	/**
	 *
	 * Each context pushed must have a name. We want this name to be unique so that
	 * the correct context is popped from the controller. The terms push and pop are
	 * not really accurate for contexts, because they might not be popped in a stack
	 * like fashion.
	 *
	 * @param pOperation[in] The operation
	 * @param sName[out] The context name
	 *
	 * @return HRESULT
	 *
	 */
	private String buildContextName(IOperation pOper)
	{
		String retName = null;
		if (pOper != null)
		{
			m_ContextName = "";
			
			// We must use a truly unique value. This means the XMI id of the op.
			String xmiid = pOper.getXMIID();
			
			// Now, we just add the "reason" of the context.
			m_ContextName = "Signature change : ";
			m_ContextName += xmiid;
			retName = m_ContextName;
		}
		return retName;
	}

	/**
	 *
	 * The destructor ensures that any current context is popped.
	 *
	 */
	protected void finalize()
	{
		try 
		{
         //This is a deviation from C++, but we need to make sure that this endSignatureChanged is called 
         //when we are done with this object. It is used in PropertyEditor, EditControl's Translator
         // and JavaMethodChangeHandler only.
//			endSignatureChange();
			super.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}



