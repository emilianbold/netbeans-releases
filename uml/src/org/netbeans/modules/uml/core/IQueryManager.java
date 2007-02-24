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

package org.netbeans.modules.uml.core;

import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IQueryManager
{
	/**
     * Connects the manager to various event sinks. The manager must be
     * initialized.
     */
	public void initialize();

	/**
     * Disconnects the manager to various event sinks. The manager must be
     * de-initialized.
	*/
	public void deinitialize();

	/**
	 * The collection of IQueryUpdater objects.
	*/
	public ETList<IQueryUpdater> getUpdaters();

	/**
	 * Establishes a .QueryCache file for the passed in IProject.
	*/
	public void establishCache( IProject pProject );
	
	/**
	 * Saves and closed the .QueryCache associated with passed in IProject.
	*/
	public void closeCache(IProject pProject);
}
