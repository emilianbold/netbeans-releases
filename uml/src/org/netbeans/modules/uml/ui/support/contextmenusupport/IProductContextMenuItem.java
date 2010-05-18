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
