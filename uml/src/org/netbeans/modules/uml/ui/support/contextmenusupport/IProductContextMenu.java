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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

import java.awt.Point;

public interface IProductContextMenu
{
	/**
	 * Should the context menu be canceled?
	*/
	public boolean getCancel();

	/**
	 * Should the context menu be canceled?
	*/
	public void setCancel( boolean value );

	/**
	 * The position of the mouse when this menu was created.
	*/
	public Point getLogicalMousePosition();

	/**
	 * The position of the mouse when this menu was created.
	*/
	public long setLogicalMousePosition( int lLogicalX, int lLogicalY );

	/**
	 * The Node, Edge or Label that was right clicked on
	*/
	public Object getItemClickedOn();

	/**
	 * The Node, Edge or Label that was right clicked on
	*/
	public void setItemClickedOn( Object value );

	/**
	 * The Node, Edge or Label that was right clicked on, returned as a presentation element.
	*/
	public IPresentationElement getPresentationElementClickedOn();

	/**
	 * The title of the context menu
	*/
	public String getMenuTitle();

	/**
	 * The title of the context menu
	*/
	public void setMenuTitle( String value );

	/**
	 * The menu items making up the context menu
	*/
	public ETList<IProductContextMenuItem> getSubMenus();

	/**
	 * The menu items making up the context menu
	*/
	public void setSubMenus( ETList<IProductContextMenuItem> value );

	/**
	 * The parent control that kicked off this event
	*/
	public Object getParentControl();

	/**
	 * The parent control that kicked off this event
	*/
	public void setParentControl( Object value );

	/**
	 * The display has been handled by an event listener
	*/
	public boolean getDisplayHandled();

	/**
	 * The display has been handled by an event listener
	*/
	public void setDisplayHandled( boolean value );

	/**
	 * The object that should sort the context menu
	*/
	public IProductContextMenuSorter getSorter();

	/**
	 * The object that should sort the context menu
	*/
	public void setSorter( IProductContextMenuSorter value );

	/**
	 * Sort the context menu
	*/
	public long sort();

	/**
	 * The closed list that will be notified when the context menu goes away
	*/
	public IProductContextMenuClosedList getClosedList();

	/**
	 * The closed list that will be notified when the context menu goes away
	*/
	public void setClosedList( IProductContextMenuClosedList value );

	/**
	 * Adds a closed list item to the list.
	*/
	public long addToClosedList( IProductContextMenuClosed newVal );

	/**
	 * Searches through the menus and submenus to find the first menuitem with thisID
	*/
	public IProductContextMenuItem getMenuItemByID( int nMenuID );

	/**
	 * Searches through the menus and submenus to find the first menuitem with buttonSource
	*/
	public IProductContextMenuItem getMenuItemByButtonSource( String sButtonSource );

	/**
	 * Searches through the menus and submenus to find the first menuitem with name
	*/
	public IProductContextMenuItem getMenuItemByName( String sName );

	/**
	 * Searches through the menus and submenus to remove the first menuitem with buttonSource
	*/
	public boolean removeMenuItemByButtonSource( String sButtonSource );

}
