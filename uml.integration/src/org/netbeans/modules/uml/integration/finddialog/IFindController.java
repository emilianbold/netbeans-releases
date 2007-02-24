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

package org.netbeans.modules.uml.integration.finddialog;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;

public interface IFindController
{
	/**
	 * Display the find dialog
	*/
	public void showFindDialog();

	/**
	 * Gets/Sets the scope of the find dialog - Project or Workspace
	*/
	public int getScope();

	/**
	 * Gets/Sets the scope of the find dialog - Project or Workspace
	*/
	public void setScope( /* FindDialogScope */ int value );

	/**
	 * Gets/Sets the string for the search, what was typed by user
	*/
	public String getSearchString();

	/**
	 * Gets/Sets the string for the search, what was typed by user
	*/
	public void setSearchString( String value );

	/**
	 * Whether or not the search is an advanced xpath search or not
	*/
	public int getKind();

	/**
	 * Whether or not the search is an advanced xpath search or not
	*/
	public void setKind( /* FindDialogKind */ int value );

	/**
	 * Whether or not the search should be case sensitive
	*/
	public boolean getCaseSensitive();

	/**
	 * Whether or not the search should be case sensitive
	*/
	public void setCaseSensitive( boolean value );

	/**
	 * The value to use for the replace
	*/
	public String getReplaceString();

	/**
	 * The value to use for the replace
	*/
	public void setReplaceString( String value );

	/**
	 * Whether or not the controller is in replace mode
	*/
	public boolean getIsReplace();

	/**
	 * Whether or not the controller is in replace mode
	*/
	public void setIsReplace( boolean value );

	/**
	 * Perform the search and populate the passed in results object with what is found
	*/
	public void search( IFindResults pResults );

	/**
	 * Display the replace dialog
	*/
	public void showReplaceDialog();

	/**
	 * Navigate to the presentation of this element
	*/
	public boolean navigateToElement( IElement pElement );

	/**
	 * What to search - elements, descriptions
	*/
	public int getResultType();

	/**
	 * What to search - elements, descriptions
	*/
	public void setResultType( /* FindDialogType */ int value );

	/**
	 * Add the project to the list of projects to search
	*/
	public void addToProjectList( String newVal );

	/**
	 * Clear out our list of projects to search
	*/
	public void clearProjectList();

	/**
	 * Perform the replace on the passed in results object
	*/
	public void replace( IFindResults pResults );

	/**
	 * Whether or not the alias field should also be searched
	*/
	public boolean getSearchAlias();

	/**
	 * Whether or not the alias field should also be searched
	*/
	public void setSearchAlias( boolean value );

	/**
	 * Another search path that provides the project to search
	*/
	public void search2( IProject pProject, IFindResults pResults );

	/**
	 * Open the proxy diagram
	*/
	public boolean navigateToDiagram( IProxyDiagram pDiagram );

	/**
	 * Whether or not to load all project elements in memory
	*/
	public boolean getExternalLoad();

	/**
	 * Whether or not to load all project elements in memory
	*/
	public void setExternalLoad( boolean value );

	/**
	 * Whether or not the user has cancelled out of the find
	*/
	public boolean getCancelled();

	/**
	 * Whether or not the user has cancelled out of the find
	*/
	public void setCancelled( boolean value );

	/**
	 * The find or the replace dialog window that is active
	*/
	public long getActiveWindow();

	/**
	 * The find or the replace dialog window that is active
	*/
	public void setActiveWindow( long value );

	/**
	 * Whether or not the whole word search is on
	*/
	public boolean getWholeWordSearch();

	/**
	 * Whether or not the whole word search is on
	*/
	public void setWholeWordSearch( boolean value );

	/**
	 * Whether or not to open diagrams when navigating
	*/
	public boolean getDiagramNavigate();

	/**
	 * Whether or not to open diagrams when navigating
	*/
	public void setDiagramNavigate( boolean value );

}
