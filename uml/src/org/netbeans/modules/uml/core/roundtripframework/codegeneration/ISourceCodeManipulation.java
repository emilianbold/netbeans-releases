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

import org.netbeans.modules.uml.core.metamodel.structure.ISourceFileArtifact;

public interface ISourceCodeManipulation
{
	/**
	 * Get/Set the Source Code
	*/
	public String getSourceCode();

	/**
	 * Get/Set the Source Code
	*/
	public void setSourceCode( String value );

	/**
	 * Deletes a range of characters from start to one before stop
	*/
	public long deleteRange( int start, int stop );

	/**
	 * Modifies a range of characters from start to one before stop
	*/
	public long modifyRange( int start, int stop, String newText );

	/**
	 * Inserts text
	*/
	public long insertText( int insertBefore, String insertedText );

	/**
	 * Commits all changes
	*/
	public long commitChanges();

	/**
	 * Determines if the specified range has already been modified.  The range must exactly matches the range of a previously made modification for this operation to return True.
	*/
	public boolean isModificationRange( int start, int stop );

	/**
	 * If the specified range exactly matches the range of a previously made modification, the text of that modification is returned
	*/
	public String getModificationText( int start, int stop );

	/**
	 * Set the cookie that represents a relationship between this object and its owning FileSystemManipulation object
	*/
	public void setCookie( int value );

	/**
	 * Set the FileSystemManipulation object that owns this SourceCodeManipulation object
	*/
	public void setFileSystemManipulation( IFileSystemManipulation value );

	/**
	 * Returns a substring of the source code where the range of the substring is specified using offsets into the original, unchanged source code.  However, the returned substring will have applied to it any modifications made to that substring
	*/
	public String getSourceCode( int rangeStart, int rangeEnd );

	/**
	 * Gets / Sets SourceFileArtifact
	*/
	public ISourceFileArtifact getSourceFileArtifact();

	/**
	 * Gets / Sets SourceFileArtifact
	*/
	public void setSourceFileArtifact( ISourceFileArtifact value );

}
