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


package org.netbeans.modules.uml.ui.support.presentationnavigation;

import org.netbeans.modules.uml.core.coreapplication.INavigator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.structure.ISourceFileArtifact;

public interface ISourceNavigator extends INavigator
{
	/**
	 * method ShowSource
	*/
	public long showSource( ISourceFileArtifact pArtifact, IClassifier pClassifier );

	/**
	 * method ShowElemSource
	*/
	public long showElemSource( IElement Elem );

	/**
	 * method CloseSource
	*/
	public long closeSource( String FilePath );

	/**
	 * method CloseElemSource
	*/
	public long closeElemSource( IElement Elem );

	/**
	 * method NavigateToLine
	*/
	public long navigateToLine( int Num );

	/**
	 * method NavigateToCol
	*/
	public long navigateToCol( int Num );

	/**
	 * method NavigateToPos
	*/
	public long navigateToPos( int Col, int Line );

	/**
	 * method SelectCol
	*/
	public long selectCol( int Num );

	/**
	 * method SelectLine
	*/
	public long selectLine( int Num );

	/**
	 * method SelectColRange
	*/
	public long selectColRange( int Col, int Start, int End );

	/**
	 * method SelectLineRange
	*/
	public long selectLineRange( int Col, int Start, int End );

	/**
	 * method Refresh
	*/
	public long refresh( String FilePath );

	/**
	 * Closes all source file windows
	*/
	public long closeAllSource();

	/**
	 * method RefreshElem
	*/
	public long refreshElem( IElement Elem );

	/**
	 * method SaveElemSource
	*/
	public long saveElemSource( IElement Elem );

	/**
	 * method SaveSource
	*/
	public long saveSource( ISourceFileArtifact pArtifact );

	/**
	 * Closes the source code pane associated with this Source File Artifact
	*/
	public long closeArtifact( ISourceFileArtifact pArtifact );

}
