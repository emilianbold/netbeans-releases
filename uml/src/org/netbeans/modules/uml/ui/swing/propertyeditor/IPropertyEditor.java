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
