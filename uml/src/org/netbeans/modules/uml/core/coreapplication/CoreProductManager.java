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


package org.netbeans.modules.uml.core.coreapplication;

import java.util.Enumeration;
import java.util.Hashtable;

import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.ADProduct;


public class CoreProductManager implements ICoreProductManager
{


	/// process ID, product
	//typedef std::map < long, CComPtr < ICoreProduct > > ProductMap;
	// Here's a map of the products and their process id's
	private Hashtable < String, ICoreProduct > m_CoreProducts = new Hashtable < String, ICoreProduct >();

	// Here's the revoke number
	private long m_RevokeNum = 0;

	// Here's the ICoreProductManager that's on the ROT.  Note that if this is NULL then
	// THIS is the item on the ROT.
	private static ICoreProductManager m_ROTManager = null;

	//	Used when the user wants to connect to an external process (ie 
	//	ER/Studio connecting to Describe.
 	public static String s_sProductAlias = "";
 	public static int s_nProductAliasPID = 0;

	public static ICoreProductManager instance()
	{
		if (m_ROTManager == null)
		{
			m_ROTManager = new CoreProductManager();
		}
		return m_ROTManager;
	}


	private CoreProductManager()
	{
		registerWithROT();
	}

	/**
	 * Registers the product manager on the ROT.  If there is already a product
	 * manager on the ROT then this instance becomes a proxy.
	 *
	 * @return HRESULT
	 */
	private void registerWithROT() 
	{
		if (m_ROTManager == null)
		{
			m_ROTManager = this;
		}
	}

	/**
	 * Gets the product that is in-process for the object that made this call.
	 * This routine will get the current pid and then call get_CoreProduct2 (a 
	 * hidden interface).  If this product manager is not the one on the ROT 
	 * then it will route to the product manager on the ROT.
	 *
	 * @param pVal[out,retval] The product associated with this application (through pid)
	 *
	 * @return HRESULT
	 *
	 * @see CCoreProductManager::get_CoreProduct2()
	 */
	public ICoreProduct getCoreProduct() 
	{		
		return m_ROTManager != null ? m_ROTManager.getCoreProduct(getPID()) : getCoreProduct(getPID());
	}

	/**
	 * Returns the pid of this process, or the alias
	 */
	private int getPID() 
	{		
		return s_nProductAliasPID != 0 ? s_nProductAliasPID : 0;
	}

	/**
	 * Puts the product that is in-process for the object that made this call.
	 * This routine will get the current pid and then call put_CoreProduct2 (a 
	 * hidden interface).  If this product manager is not the one on the ROT 
	 * then it will route to the product manager on the ROT.
	 *
	 * @param newVal[in] The product associated with this application (through pid)
	 *
	 * @return HRESULT
	 *
	 * @see CCoreProductManager::put_CoreProduct2()
	 */
	public void setCoreProduct(ICoreProduct value) 
	{
		int pid = getPID();
		if (m_ROTManager != null)
		{
			m_ROTManager.setCoreProduct(pid, value);
		}
		else
		{
			setCoreProduct(pid, value);
		}
	}

	/**
	 * Gets the product that is associated with this process id.  This is a hidden
	 * interface.  Most objects will use get_CoreProduct.
	 *
	 * @param nPID[in] The process id for the running application
	 * @param pVal[out,retval] The product associated with this pid
	 *
	 * @return HRESULT
	 *
	 * @see CCoreProductManager::get_CoreProduct()
	 */
	public ICoreProduct getCoreProduct(int nPID) 
	{
		ICoreProduct retVal = m_CoreProducts.get(Integer.toString(nPID));
		if (retVal == null)
		{
			if (!m_CoreProducts.isEmpty())
			{
				retVal = m_CoreProducts.elements().nextElement();
			}
			else
			{
				retVal = new ADProduct();
                                //Fix for bug # 6371314
                                setCoreProduct(retVal);  
				retVal.initialize();   
				
			}
		}		
		return retVal;
	}

	/**
	 * Sets the product that is associated with this process id.  This is a hidden
	 * interface.  Most objects will use put_CoreProduct.
	 *
	 * @param nPID[in] The process id for the running application
	 * @param newVal[in] The product associated with this pid
	 *
	 * @return HRESULT
	 *
	 * @see CCoreProductManager::put_CoreProduct()
	 */
	public void setCoreProduct(int nPID, ICoreProduct value) 
	{
		ICoreProduct prod = m_CoreProducts.get(Integer.toString(nPID));
		if (prod != null)
		{
			// If there is already a product registered for this
			// process, then we need to be sure to destroy that
			// product before continuing.
			prod.quit();
		}
		
		if (value == null)
		{
			m_CoreProducts.remove(Integer.toString(nPID));
		}
		else
		{
			m_CoreProducts.put(Integer.toString(nPID), value);
		}
	}

	/**
	 *
	 * Retrieves the descriptors of the products this manager manages.
	 *
	 * @param pVal[out] The descriptor collection
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<IProductDescriptor> getProducts() 
	{
		IProductDescriptor[] descs = null;
		ETList<IProductDescriptor> descList = new ETArrayList<IProductDescriptor>();
		if (m_CoreProducts != null && m_CoreProducts.size() > 0)
		{
			Enumeration enumVal = m_CoreProducts.keys();
			int count = m_CoreProducts.size();
			descs = new ProductDescriptor[count];
			int i=0;
			while (enumVal.hasMoreElements())
			{
				IProductDescriptor desc = new ProductDescriptor();
				String obj = (String)enumVal.nextElement();				
				if (obj != null)
				{
					ICoreProduct prod = m_CoreProducts.get(obj);
					desc.setProcessID(Integer.parseInt(obj));
					desc.setCoreProduct(prod);
					descList.add(i,desc);
					i++;
				}				

			}
		}
		return descList;
	}

	/**
	 * Returns the product manager that is on the ROT.
	 *
	 * @param pVal [out] The product manager present on the ROT
	 * 
	 * @return HRESULT
	 *
	 */
	public ICoreProductManager getProductManagerOnROT() 
	{
		return m_ROTManager != null ? m_ROTManager : this;
	}

	/**
	 * Removes the product manager from the ROT
	 *
	 * @return HRESULT
	 */
	public void removeFromROT() 
	{
		// If we have only one product then allow this to be removed, otherwise there's
		// several products using this manager.
		if (m_RevokeNum != 0 && m_CoreProducts.size() <= 1)
		{
			// Clear the product map
			Enumeration < ICoreProduct > enumVal = m_CoreProducts.elements();
			while (enumVal.hasMoreElements())
			{
				ICoreProduct prod = enumVal.nextElement();
				prod.preDestroy();
			}
			m_CoreProducts.clear();
		}		
	}

	/**
	 * Sets the product for which you want to attach (ie Describe).
	 *
	 * @param sAlias [in] The external process to retrieve the product from
	 * 
	 * @return HRESULT
	 *
	 */
	public void setProductAlias(String value) 
	{
		s_sProductAlias = value;
		s_nProductAliasPID = 0;

		if (m_RevokeNum != 0)
		{
		   // If we are on the rot then disconnect ourselves.  We're only a
		   // product manager looking for another external process.  This happens
		   // during ER/Studio export which the product manager in the erstudio
		   // space is not placed on the ROT because it's alias is Describe.
		   removeFromROT();
		   m_RevokeNum = 0;
		}
		//findAliasPID();
	}

	/**
	 * Returns the product for which you want to attach (ie Describe).
	 *
	 * @param sAlias [in] The external process to retrieve the product from
	 * 
	 * @return HRESULT
	 *
	 */
	public String getProductAlias() 
	{
		return s_sProductAlias != null && s_sProductAlias.length() > 0 ? s_sProductAlias : null;
	}

}


