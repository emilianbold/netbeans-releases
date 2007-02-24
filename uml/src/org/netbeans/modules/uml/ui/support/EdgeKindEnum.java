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

/*
 *
 * @author KevinM
 *
 */
public interface EdgeKindEnum
{
	public final static int EK_ALL = 0;
	public final static int EK_REALIZATION= 1;
	public final static int EK_ASSOCIATION= 2;
	public final static int EK_DEPENDENCY= 3;
	public final static int EK_USAGE= 4;
	public final static int EK_MESSAGE= 5;
	public final static int EK_GENERALIZATION= 6;
	public final static int EK_IMPLEMENTATION= 7;
	public final static int EK_INTERFACE= 8;
	public final static int EK_NESTED_LINK= 9;
}
