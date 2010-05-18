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


package org.netbeans.modules.uml.ui.swing.propertyeditor;

import java.util.Vector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinitionFactory;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElementManager;

public interface IPropertyEditor
{
	/**
	 * Initialize the property editor
	*/
	public long initialize();

	/**
	 * property PropertyDefinitions
	*/
	public Vector<IPropertyDefinition> getPropertyDefinitions();

	/**
	 * property PropertyDefinitions
	*/
	public void setPropertyDefinitions( Vector<IPropertyDefinition> value );

	/**
	 * property PropertyElements
	*/
	public Vector<IPropertyElement> getPropertyElements();

	/**
	 * property PropertyElements
	*/
	public void setPropertyElements( Vector<IPropertyElement> value );

	/**
	 * Clear the property editor
	*/
	public long clear();

	/**
	 * property PropertyElements
	*/
	public IPropertyElementManager getPropertyElementManager();

	/**
	 * Clear the property editor
	*/
	public long save();

	/**
	 * property 
	*/
	public IPropertyDefinitionFactory getPropertyDefinitionFactory();

	/**
	 * property Project
	*/
	public void setProject( IProject value );

	/**
	 * Set focus in the property editor
	*/
	public long setFocus();

	/**
	 * Populate the grid with the current elements
	*/
	public long populateGrid();

	/**
	 * ReloadElement
	*/
	public long reloadElement( Object pDisp );

	/**
	 * Registers or revokes event sinks
	*/
	public long connectSinks( boolean __MIDL_0014 );

	/**
	 * Retrieves the actual grid control.
	*/
	public Object getGrid();

	/**
	 * Put the passed in element into the property editor
	*/
	public long loadElement( IElement pElement );

	/**
	 * Put the passed in elements into the property editor
	*/
	public long loadElements( IElement[] pElements );

	/**
	 * Reset the property editor settings(color, font)
	*/
	public long resetGridSettings();

	/**
	 * Reset the property editor filter
	*/
	public long resetGridFilter();

	/**
	 * Returns the HWND to the property editor.
	*/
	public int getWindowHandle();

	/**
	 * Ask the user what to do about a name collision
	*/
	public long questionUserAboutNameCollision( INamedElement pElement, String sProposedName, INamedElement pFirstCollidingElement, IResultCell pCell );

	/**
	 * Begin edit context
	*/
	public long beginEditContext();

	/**
	 * Whether or not the property editor should reload when receiving an OnElementModified event
	*/
	public boolean getRespondToReload();

	/**
	 * Whether or not the property editor should reload when receiving an OnElementModified event
	*/
	public void setRespondToReload( boolean value );

	public IPropertyElement processSelectedItem(String kind, Vector<IPropertyDefinition> propDefs, Object pElement);
}
