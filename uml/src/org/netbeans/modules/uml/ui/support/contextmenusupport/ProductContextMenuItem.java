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



package org.netbeans.modules.uml.ui.support.contextmenusupport;

import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author sumitabhk
 *
 *
 */
public class ProductContextMenuItem implements IProductContextMenuItem
{

	/**
	 *
	 */
	public ProductContextMenuItem()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem#getSensitive()
	 */
	public boolean getSensitive()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem#setSensitive(boolean)
	 */
	public void setSensitive(boolean value)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem#getHandled()
	 */
	public boolean getHandled()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem#setHandled(boolean)
	 */
	public void setHandled(boolean value)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem#getIsSeparator()
	 */
	public boolean getIsSeparator()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem#setIsSeparator(boolean)
	 */
	public void setIsSeparator(boolean value)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem#getMenuString()
	 */
	public String getMenuString()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem#setMenuString(java.lang.String)
	 */
	public void setMenuString(String value)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem#getDescription()
	 */
	public String getDescription()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem#setDescription(java.lang.String)
	 */
	public void setDescription(String value)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem#getMenuID()
	 */
	public int getMenuID()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem#setMenuID(int)
	 */
	public void setMenuID(int value)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem#getSubMenus()
	 */
	public ETList<IProductContextMenuItem> getSubMenus()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem#setSubMenus(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem[])
	 */
	public void setSubMenus(IProductContextMenuItem[] value)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem#getChecked()
	 */
	public boolean getChecked()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem#setChecked(boolean)
	 */
	public void setChecked(boolean value)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem#getButtonSource()
	 */
	public String getButtonSource()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem#setButtonSource(java.lang.String)
	 */
	public void setButtonSource(String value)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem#getSelectionHandler()
	 */
	public IProductContextMenuSelectionHandler getSelectionHandler()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem#setSelectionHandler(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuSelectionHandler)
	 */
	public void setSelectionHandler(IProductContextMenuSelectionHandler value)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem#getEnsurePullright()
	 */
	public boolean getEnsurePullright()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem#setEnsurePullright(boolean)
	 */
	public void setEnsurePullright(boolean value)
	{
		// TODO Auto-generated method stub
		
	}

}


