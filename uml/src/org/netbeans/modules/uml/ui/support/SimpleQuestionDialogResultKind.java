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
 *
 */
public interface SimpleQuestionDialogResultKind
{
	public static int SQDRK_RESULT_ABORT	= 0;
	public static int SQDRK_RESULT_RETRY	= SQDRK_RESULT_ABORT + 1;
	public static int SQDRK_RESULT_IGNORE	= SQDRK_RESULT_RETRY + 1;
	public static int SQDRK_RESULT_OK	= SQDRK_RESULT_IGNORE + 1;
	public static int SQDRK_RESULT_CANCEL	= SQDRK_RESULT_OK + 1;
	public static int SQDRK_RESULT_YES	= SQDRK_RESULT_CANCEL + 1;
	public static int SQDRK_RESULT_NO	= SQDRK_RESULT_YES + 1;
	public static int SQDRK_RESULT_UNKNOWN	= SQDRK_RESULT_NO + 1;
	public static int SQDRK_RESULT_ALWAYS	= SQDRK_RESULT_UNKNOWN + 1;
	public static int SQDRK_RESULT_NEVER	= SQDRK_RESULT_ALWAYS + 1;

}

