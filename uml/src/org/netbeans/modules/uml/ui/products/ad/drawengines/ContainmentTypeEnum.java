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



package org.netbeans.modules.uml.ui.products.ad.drawengines;

public interface ContainmentTypeEnum {

	// Purely graphical containment.  Metadata is not affected
	public final int CT_GRAPHICAL = 0;
	
	// Moving in and out of this container will affect namespace
	public final int CT_NAMESPACE = 2;
	
	// Contained items are moved into the states region
	public final int CT_STATE_REGION = 4;
	
	// Contained items are moved into the activity groups list of nodes, namespace is not changed
	public final int CT_ACTIVITYGROUP = 8;

}
