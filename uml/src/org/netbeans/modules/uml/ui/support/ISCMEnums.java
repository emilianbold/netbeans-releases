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



package org.netbeans.modules.uml.ui.support;

/**
 * @author sumitabhk
 *
 */
public interface ISCMEnums
{
	//SCMFeatureKind
	public static int FK_GET_LATEST_VERSION	= 2;
	public static int FK_GET_FROM_SCM_DIR	= FK_GET_LATEST_VERSION + 1;
	public static int FK_GET_SCOPED_DIAGRAMS	= FK_GET_FROM_SCM_DIR + 1;
	public static int FK_CHECK_IN	= FK_GET_SCOPED_DIAGRAMS + 1;
	public static int FK_CHECK_OUT	= FK_CHECK_IN + 1;
	public static int FK_UNDO_CHECK_OUT	= FK_CHECK_OUT + 1;
	public static int FK_SHOW_HISTORY	= FK_UNDO_CHECK_OUT + 1;
	public static int FK_SHOW_DIFF	= FK_SHOW_HISTORY + 1;
	public static int FK_SILENT_DIFF	= FK_SHOW_DIFF + 1;
	public static int FK_ADD_TO_SOURCE_CONTROL	= FK_SILENT_DIFF + 1;
	public static int FK_REMOVE_FROM_SOURCE_CONTROL	= FK_ADD_TO_SOURCE_CONTROL + 1;
	public static int FK_LAUNCH_PROVIDER	= FK_REMOVE_FROM_SOURCE_CONTROL + 1;
}


