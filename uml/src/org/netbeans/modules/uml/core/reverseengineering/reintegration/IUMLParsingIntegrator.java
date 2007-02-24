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

package org.netbeans.modules.uml.core.reverseengineering.reintegration;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IClassEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IDependencyEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IPackageEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
// import org.netbeans.modules.uml.ui.support.messaging.IProgressDialog;
import org.netbeans.modules.uml.util.ITaskWorker;

public interface IUMLParsingIntegrator extends ITaskWorker
{
	/**
	 * The files to reverse engineer.
	*/
	public IStrings getFiles();

	/**
	 * The files to reverse engineer.
	*/
	public void setFiles( IStrings value );

	/**
	 * The ProgressDialog used during the RE process.
	*/
        
// TODO: conover - re-enable with intent to retrieve NB Output window ???
//	public IProgressDialog getProgressDialog();

	/**
	 * The ProgressDialog used during the RE process.
	*/
// TODO: conover - re-enable with intent to initialize NB Output window ???
//	public void setProgressDialog( IProgressDialog value );

	/**
	 * Reverse engineer the files set in the Files property.
	*/
	public boolean reverseEngineer( INamespace pSpace, boolean useFileChooser, boolean useDiagramCreateWizard, boolean displayProgress, boolean extractClasses );

	/**
	 * Verifies that the input operation can be processed by reverse engineering
	*/
	public boolean canOperationBeREed( IOperation pOperation );

	/**
	 * Reverse engineer the input operations
	*/
	public void reverseEngineerOperations( INamespace pSpace, ETList<IElement> pElements );

	/**
	 * Reverse engineer the input operation
	*/
	public void reverseEngineerOperation( INamespace pSpace, IElement pElement );

	/**
	 * Add the input REClass to the associated project
	*/
	public void  addClassToProject( IREClass pClass );
}
