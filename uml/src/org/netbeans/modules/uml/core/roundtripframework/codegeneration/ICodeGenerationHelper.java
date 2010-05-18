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
