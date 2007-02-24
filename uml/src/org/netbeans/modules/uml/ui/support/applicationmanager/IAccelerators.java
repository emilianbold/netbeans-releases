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


/*
 * Created on Feb 24, 2004
 *
 */
package org.netbeans.modules.uml.ui.support.applicationmanager;

/**
 * @author jingmingm
 *
 */
public interface IAccelerators
{
	// Contains the number of Accelerator keycodes in the collection"
	public int getCount();

	// Adds an Accelerator keycode to the collection"
	public void add(String keyCode);

	// Retrieves a specific Accelerator keycode from the collection"
	public String item(int index);

	// Remove a specific Accelerator keycode from the collection
	public void remove(int index);
}



