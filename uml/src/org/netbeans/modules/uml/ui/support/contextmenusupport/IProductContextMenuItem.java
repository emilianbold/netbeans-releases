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

public interface IProductContextMenuItem
{
	/**
	 * Is this item sensitive?
	*/
	public boolean getSensitive();

	/**
	 * Is this item sensitive?
	*/
	public void setSensitive( boolean value );

	/**
	 * Applicable when a selection event occurs.  Has this items selection been handled?
	*/
	public boolean getHandled();

	/**
	 * Applicable when a selection event occurs.  Has this items selection been handled?
	*/
	public void setHandled( boolean value );

	/**
	 * Is this item a separator in the menu?
	*/
	public boolean getIsSeparator();

	/**
	 * Is this item a separator in the menu?
	*/
	public void setIsSeparator( boolean value );

	/**
	 * What is the menu string?
	*/
	public String getMenuString();

	/**
	 * What is the menu string?
	*/
	public void setMenuString( String value );

	/**
	 * The description of what the menu does.
	*/
	public String getDescription();

	/**
	 * The description of what the menu does.
	*/
	public void setDescription( String value );

	/**
	 * The menu ID of this menu.  By default all menu items get a unique menu ID.
	*/
	public int getMenuID();

	/**
	 * The menu ID of this menu.  By default all menu items get a unique menu ID.
	*/
	public void setMenuID( int value );

	/**
	 * The sub menus of this item.  If this item has sub menus then it becomes a popup menu.
	*/
	public ETList < IProductContextMenuItem > getSubMenus();

	/**
	 * The sub menus of this item.  If this item has sub menus then it becomes a popup menu.
	*/
	public void setSubMenus( IProductContextMenuItem[] value );

	/**
	 * Is this item checked?
	*/
	public boolean getChecked();

	/**
	 * Is this item checked?
	*/
	public void setChecked( boolean value );

	/**
	 * The id that was used when an engine created this item.
	*/
	public String getButtonSource();

	/**
	 * The id that was used when an engine created this item.
	*/
	public void setButtonSource( String value );

	/**
	 * If the popup is actually created by a listener then this is who the listener calls if a button is selected.
	*/
	public IProductContextMenuSelectionHandler getSelectionHandler();

	/**
	 * If the popup is actually created by a listener then this is who the listener calls if a button is selected.
	*/
	public void setSelectionHandler( IProductContextMenuSelectionHandler value );

	/**
	 * Ensure that this button is a pullright.  If it has no subitems then desensitize the button.
	*/
	public boolean getEnsurePullright();

	/**
	 * Ensure that this button is a pullright.  If it has no subitems then desensitize the button.
	*/
	public void setEnsurePullright( boolean value );

}
