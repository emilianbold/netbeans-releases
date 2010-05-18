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

package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREOperation;
public interface ILanguageParser
{
	/**
	 * Process the given file. The fileName should be an absolute path to the file.
	*/
	public void parseFile( String filename );

	/**
	 * Process the given file. The fileName should be an absolute path to the file.
	*/
	public void parseFile( String filename, String charset);

	/**
	 * Parse the contents of the specified operation.  Message events will be sent the registered reverse engineering listeners.
	*/
	public void parseOperation( String filename, IREOperation operation );

	/**
	 * Parse the contents of the specified operation.  Message events will be sent the registered reverse engineering listeners.
	*/
	public void parseOperation( String filename, String charset, IREOperation operation );

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
