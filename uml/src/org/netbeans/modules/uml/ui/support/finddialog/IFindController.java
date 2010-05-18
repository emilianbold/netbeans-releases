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


package org.netbeans.modules.uml.ui.support.finddialog;

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
