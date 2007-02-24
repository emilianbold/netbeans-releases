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
import org.netbeans.modules.uml.core.reverseengineering.reframework.IParserData;

public interface IParseInformationCache
{
	/**
	 * Returns Parse Information for the specified ISourceFileArtifact
	*/
	public IFileInformation getParseInformation( ISourceFileArtifact pArtifact );

	/**
	 * Marks an ISourceFileArtifact's parse information as out of date.
	*/
	public long invalidateParseInformation( ISourceFileArtifact pArtifact );

	/**
	 * Returns an IParserData object for the given IElement object
	*/
	public IParserData getParserData( IElement pArtifactOwner, IElement pElement, String language );

	/**
	 * Finds an IParserData object for the given IElement object
	*/
	public IParserData findParserData( IElement pArtifactOwner, IElement pElement );

	/**
	 * Removes all cache entries
	*/
	public long removeAllCacheEntries();

}
