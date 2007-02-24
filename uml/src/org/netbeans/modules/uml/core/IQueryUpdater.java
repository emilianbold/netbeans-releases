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
import org.dom4j.Document;
public interface IQueryUpdater
{
	/**
	 * Initializes this Updater upon query load. The context is an IProject interface.
	*/
	public void initialize( Object context, Document queryCache );

	/**
	 * Called before the cache is closed.
	*/
	public void updateCache( Document queryCache );

	/**
	 * de-Initializes this Updater.
	*/
	public void deinitialize();

}
