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


package org.netbeans.modules.uml.core.metamodel.diagrams;

/**
 * @author sumitabhk
 *
 */
public interface IDiagramKind
{
	public static int DK_UNKNOWN	= 0;
	public static int DK_DIAGRAM	= 1;
	public static int DK_ACTIVITY_DIAGRAM	= 2;
	public static int DK_CLASS_DIAGRAM	= 4;
	public static int DK_COLLABORATION_DIAGRAM	= 8;
	public static int DK_COMPONENT_DIAGRAM	= 16;
	public static int DK_DEPLOYMENT_DIAGRAM	= 32;
	public static int DK_SEQUENCE_DIAGRAM	= 64;
	public static int DK_STATE_DIAGRAM	= 128;
	public static int DK_USECASE_DIAGRAM	= 256;
	public static int DK_ENTITY_DIAGRAM	= 512;
	public static int DK_ALL	= 0xffff;
}


