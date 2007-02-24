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
 * Created on Jul 15, 2003
 *
 */
package org.netbeans.modules.uml.ui.controls.newdialog;

/**
 * @author sumitabhk
 *
 */
public class NewDialogTabKind
{
	public static int NDTK_NONE	= 0;
	public static int NDTK_ALL	= NDTK_NONE + 1;
	public static int NWIK_NEW_WORKSPACE	= NDTK_ALL + 1;
	public static int NWIK_NEW_PROJECT	= NWIK_NEW_WORKSPACE + 1;
	public static int NWIK_NEW_DIAGRAM	= NWIK_NEW_PROJECT + 1;
	public static int NWIK_NEW_PACKAGE	= NWIK_NEW_DIAGRAM + 1;
	public static int NWIK_NEW_ELEMENT	= NWIK_NEW_PACKAGE + 1;

}



