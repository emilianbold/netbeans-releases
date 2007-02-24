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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;

/**
 * @author jingmingm
 *
 */
public interface IStateDrawEngine
{
	public static final int SPTT_UNKNOWN = 0;
	public static final int SPTT_INVALID_PROCEDURE = 1;
	public static final int SPTT_EXIT = 2;
	public static final int SPTT_ENTRY = 3;
	public static final int SPTT_DOACTIVITY = 4;
	public static final int SPTT_INVALID_TRANSITION = 5;
	public static final int SPTT_INCOMING_TRANSITION = 6;
	public static final int SPTT_OUTGOING_TRANSITION = 7;
	public static final int pType = SPTT_UNKNOWN;
	
	public int getProcedureOrTransitionType(IElement pElement);
}
