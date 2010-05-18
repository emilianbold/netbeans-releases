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
