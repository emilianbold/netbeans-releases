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

package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREOperation;
public interface ILanguageParser
{
	/**
	 * Process the given file. The fileName should be an absolute path to the file.
	*/
	public void parseFile( String filename );

	/**
	 * Parse the contents of the specified operation.  Message events will be sent the registered reverse engineering listeners.
	*/
	public void parseOperation( String filename, IREOperation operation );

	/**
	 * Get/Sets the parsers state listener.
	*/
	public IStateListener getStateListener();

	/**
	 * Get/Sets the parsers state listener.
	*/
	public void setStateListener( IStateListener stateListener );

	/**
	 * Get/Sets the parsers state filter.
	*/
	public IStateFilter getStateFilter();

	/**
	 * Get/Sets the parsers state filter.
	*/
	public void setStateFilter( IStateFilter filter );

	/**
	 * Get/Sets the interface that will process tokens found while parsing a file.
	*/
	public ITokenProcessor getTokenProcessor();

	/**
	 * Get/Sets the interface that will process tokens found while parsing a file.
	*/
	public void setTokenProcessor( ITokenProcessor tokenProcessor );

	/**
	 * Get/Sets the interface that will process tokens found while parsing a file.
	*/
	public ITokenFilter getTokenFilter();

	/**
	 * Get/Sets the interface that will process tokens found while parsing a file.
	*/
	public void setTokenFilter( ITokenFilter filter );

	/**
	 * Get/Sets the interface that will process errors found while parsing a file.
	*/
	public IErrorListener getErrorListener();

	/**
	 * Get/Sets the interface that will process errors found while parsing a file.
	*/
	public void setErrorListener( IErrorListener errorListener );

	/**
	 * Process the given file. The fileName should be an absolute path to the file.
	*/
	public void processStreamByType( String stream, /* ProcessTypeKind */ int kind );

}
