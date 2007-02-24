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

public interface TSLabelKind {
	public static int TSLK_UNKNOWN = -1;
	public static int TSLK_INTERFACE = TSLK_UNKNOWN + 1;
	public static int TSLK_ASSOCIATION_NAME = TSLK_INTERFACE + 1;
	public static int TSLK_ASSOCIATION_END0_ROLE_NAME = TSLK_ASSOCIATION_NAME + 1;
	public static int TSLK_ASSOCIATION_END0_MULTIPLICITY = TSLK_ASSOCIATION_END0_ROLE_NAME + 1;
	public static int TSLK_ASSOCIATION_END1_ROLE_NAME = TSLK_ASSOCIATION_END0_MULTIPLICITY + 1;
	public static int TSLK_ASSOCIATION_END1_MULTIPLICITY = TSLK_ASSOCIATION_END1_ROLE_NAME + 1;
	public static int TSLK_STEREOTYPE = TSLK_ASSOCIATION_END1_MULTIPLICITY + 1;
	public static int TSLK_MESSAGE_OPERATION_NAME = TSLK_STEREOTYPE + 1;
	public static int TSLK_INTERACTION_CONSTRAINT = TSLK_MESSAGE_OPERATION_NAME + 1;
	public static int TSLK_ACTIVITYEDGE_NAME  = TSLK_INTERACTION_CONSTRAINT + 1;
	public static int TSLK_GUARD_CONDITION = TSLK_ACTIVITYEDGE_NAME + 1;
	public static int TSLK_ICON_LABEL = TSLK_GUARD_CONDITION + 1;
	public static int TSLK_NAME = TSLK_ICON_LABEL + 1;
	public static int TSLK_DERIVATION_BINDING = TSLK_NAME + 1;
	public static int TSLK_MESSAGECONNECTOR_OPERATION_NAME = TSLK_DERIVATION_BINDING + 1;
	public static int TSLK_MESSAGE_NUMBER = TSLK_MESSAGECONNECTOR_OPERATION_NAME + 1;
	public static int TSLK_PRE_CONDITION = TSLK_MESSAGE_NUMBER + 1;
	public static int TSLK_POST_CONDITION = TSLK_PRE_CONDITION + 1;
}
