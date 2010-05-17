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


package org.netbeans.modules.uml.ui.support.applicationmanager;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import java.awt.Frame;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import java.awt.Image;
import java.net.URL;

public interface IProxyUserInterface
{
	/**
	 * Returns the HWND for the main interface.
	*/
	public Frame getWindowHandle();

	/**
	 * Tells the gui that the dirty state has changed for the input diagram
	*/
	public long dirtyStateChanged( IDiagram pDiagram, boolean bNewDirtyState );

	/**
	 * Quits Describe.
	*/
	public void quit();

	/**
	 * Put/Get the visible state of the application.
	*/
	public void setVisible( boolean value );

	/**
	 * Put/Get the visible state of the application.
	*/
	public boolean getVisible();

	/**
	 * Put/Get the visible state of the property editor.
	*/
	public void setPropertyEditorVisible( boolean value );

	/**
	 * Put/Get the visible state of the property editor.
	*/
	public boolean getPropertyEditorVisible();

	/**
	 * Tell the gui to open the open workspace dialog
	*/
	public void openWorkspaceDialog();

	/**
	 * Tell the gui to open the new workspace dialog
	*/
	public void newWorkspaceDialog();

	/**
	 * Tell the gui to open the new package dialog. pDefaultNamespace can be null or the default namespace for the package
	*/
	public IElement newPackageDialog( INamespace pDefaultNamespace );

	/**
	 * Tell the gui to open the new element dialog. pDefaultNamespace can be null or the default namespace for the package
	*/
	public IElement newElementDialog( INamespace pDefaultNamespace );

	/**
	 * Closes the current IWorkspace and asks the user to save if the workspace is dirty.
	*/
	public void closeWorkspace();

	/**
	 * Closes the indicated project and asks the user to save if the project is dirty.
	*/
	public void closeProject( IProject pProject );

	/**
	 * Disables or enables the context menu on the application.
	*/
	public void setDisableContextMenu( boolean value );

	/**
	 * Disables or enables the context menu on the application.
	*/
	public boolean getDisableContextMenu();

    /**
     * Displays a URL to the user.  The URL will be displayed by the platform
     * that is hosting the UML application.
     */
    public void displayInBrowser(URL url);
    
    /**
     * Retreives the Image resource that is used by other parts of the framework.
     */
    public Image getResource(String iconLocation);
}
