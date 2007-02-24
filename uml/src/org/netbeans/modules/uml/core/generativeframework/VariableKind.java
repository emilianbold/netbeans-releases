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


package org.netbeans.modules.uml.core.generativeframework;

/**
 * @author sumitabhk
 *
 */
final public class VariableKind {

	public static int VK_NONE	= 0;
	public static int VK_ATTRIBUTE	= VK_NONE + 1;
	public static int VK_NODES	= VK_ATTRIBUTE + 1;
	public static int VK_TEXT_VALUE	= VK_NODES + 1;
	public static int VK_NODE_NAME	= VK_TEXT_VALUE + 1;
	public static int VK_PREFERENCE	= VK_NODE_NAME + 1;
	public static int VK_BOOLEAN	= VK_PREFERENCE + 1;
}


