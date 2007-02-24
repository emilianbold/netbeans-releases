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



package org.netbeans.modules.uml.ui.products.ad.drawEngineManagers;
public interface MenuButtonKind {

	public final static int MBK_SEPARATOR = 0;
	// Resets the labels.  Creates all labels with text and positions them
	public final static int MBK_OPERATION_NEW = 1;
	public final static int MBK_NEW_CONSTRUCTOR = 2;
	public final static int MBK_OPERATION_MORE = 3;
	public final static int MBK_SHOW_OPERATION_NAME = 4;
	public final static int MBK_SHOW_MESSAGE_NAME = 5;
	public final static int MBK_SHOW_RETURN = 6;
	public final static int MBK_RESET_LABELS = 7;
	public final static int MBK_OPERATION_START = 8;
	// all values in this enum must be added before this one
	public final static int MBK_OPERATIONS_GROUP = MBK_OPERATION_START + 400;
	public final static int MBK_INVALID = -1;

}
