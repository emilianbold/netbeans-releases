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



package org.netbeans.modules.uml.ui.support.viewfactorysupport;

public interface TSLabelPlacementKind {

	public static int TSLPK_UNKNOWN = -1;
	public static int TSLPK_FROM_NODE_ABOVE = TSLPK_UNKNOWN + 1;
	public static int TSLPK_FROM_NODE_BELOW = TSLPK_FROM_NODE_ABOVE + 1;
	public static int TSLPK_CENTER_ABOVE = TSLPK_FROM_NODE_BELOW + 1;
	public static int TSLPK_CENTER_BELOW = TSLPK_CENTER_ABOVE + 1;
	public static int TSLPK_TO_NODE_ABOVE = TSLPK_CENTER_BELOW + 1;
	public static int TSLPK_TO_NODE_BELOW = TSLPK_TO_NODE_ABOVE + 1;
	public static int TSLPK_SPECIFIED_XY = TSLPK_TO_NODE_BELOW + 1;
}
