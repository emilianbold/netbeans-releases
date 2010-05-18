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


package org.netbeans.modules.uml.core.metamodel.structure;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.roundtripframework.codegeneration.IFileSystemManipulation;
import org.netbeans.modules.uml.core.roundtripframework.codegeneration.ISourceCodeManipulation;


public interface ISourceFileArtifact extends IArtifact
{
	/**
	 * The source files language.
	*/
	public ILanguage getLanguage();

	/**
	 * The source file artifact's file name, minus any directory information
	*/
	public String getShortName();

	/**
	 * The disk drive that the source file artifact resides on
	*/
	public String getDrive();

	/**
	 * The source file artifact's base directory
	*/
	public String getBaseDirectory();

	/**
	 * The source file artifact's directory (with no trailing directory delimiter)
	*/
	public String getDirectory();

	/**
	 * Registers an Event Sink that will be notified whenever the ISourceFileArtifact's source code is changed.
	*/
	public void registerForSourceFileArtifactEvents( ISourceFileArtifactEventsSink pEventsSink );

	/**
	 * Revokes an Event Sink that has previously been registered
	*/
	public void revokeSourceFileArtifactSink( ISourceFileArtifactEventsSink pEventsSink );

	/**
	 * Fires the OnTextInserted event.
	*/
	public void fireTextInserted( int fileOffset, String insertedText );

	/**
	 * Fires the OnRangeDeleted event
	*/
	public void fireRangeDeleted( int rangeStart, int rangeEnd, String deletedText );

	/**
	 * Fires the OnRangeModified event
	*/
	public void fireRangeModified( int rangeStart, int rangeEnd, String originalText, String newText );

	/**
	 * Calculates the CRC of the ISourceFileArtifact's source code
	*/
	public int calculateCRC();

	/**
	 * Returns an ISourceCodeManipulation object useful for modifying this ISourceFileArtifact's source code
	*/
	public ISourceCodeManipulation modify( IFileSystemManipulation pFileSystemManipulation );

	/**
	 * Returns the Source Code for this Artifact
	*/
	public String getSourceCode();

	/**
	 * Gets / Sets SourceCodeHolder
	*/
	public ISourceCodeHolder getSourceCodeHolder();

	/**
	 * Gets / Sets SourceCodeHolder
	*/
	public void setSourceCodeHolder( ISourceCodeHolder value );

	/**
	 * Commits all changes made to the ISourceFileArtifact's source code
	*/
	public void commitChanges();

	/**
	 * Returns the Source Code for this Artifact
	*/
	public void setSourceCode( String value );

	/**
	 * The absolute path to the source file.
	*/
	public String getSourceFile();

	/**
	 * The absolute path to the source file.
	*/
	public void setSourceFile( String value );

	/**
	 * Fires the OnSourceFileNameChanged Event.
	*/
	public void fireSourceFileNameChanged( String oldFileName, String newFileName );

	/**
	 * Makes sure that the Artifact can be written to
	*/
	public boolean ensureWriteAccess();

}
