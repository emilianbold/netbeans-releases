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

package org.netbeans.modules.uml.core.roundtripframework.codegeneration;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.structure.ISourceFileArtifact;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IFileInformation;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ICodeGenerationScript;
import org.netbeans.modules.uml.core.support.umlutils.IDataFormatter;

public interface ICodeGenerationHelper
{
	/**
	 * Parses all files associated with an element and returns parse information for those files.
	*/
	public IFileInformation getParseInformationForElement( IElement element );

	/**
	 * Parses the source file artifact and returns parse information for the artifact.
	*/
	public IFileInformation getParseInformationForArtifact( ISourceFileArtifact artifact );

	/**
	 * Generates source code for an IElement using the specified script.
	*/
	public String generateCodeForElement( IElement element, String scriptName );

	/**
	 * Generates source code for an IElement using the specified script.
	*/
	public String generateCodeForElement2( IElement element, ICodeGenerationScript pScript );

	/**
	 * Returns the Default Code Generator
	*/
	public ILanguageCodeGenerator getDefaultCodeGenerator();

	/**
	 * Gets / Sets DataFormatter
	*/
	public IDataFormatter getDataFormatter();

	/**
	 * Gets / Sets DataFormatter
	*/
	public void setDataFormatter( IDataFormatter value );

	/**
	 * Gets Source Code from an ISourceFileArtifact object
	*/
	public String getSourceCode( ISourceFileArtifact pArtifact, int rangeStart, int rangeEnd );

	/**
	 * Gets / Sets SourceCodeManipulationMap
	*/
	public ISourceCodeManipulationMap getSourceCodeManipulationMap();

	/**
	 * Gets / Sets SourceCodeManipulationMap
	*/
	public void setSourceCodeManipulationMap( ISourceCodeManipulationMap value );

	/**
	 * Generates formatted source code for an element
	*/
	public String generateCodeForElement3( IElement element, String language, String eventName, /* CodeGenerationStyle */ int style );

}
