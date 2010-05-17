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



