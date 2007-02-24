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


public interface DrawEngineArrowheadKindEnum
{
	public final static int DEAK_NO_ARROWHEAD = 0;
	public final static int DEAK_UNFILLEDARROW = 1;   			// <----
	public final static int DEAK_UNFILLEDHALFARROW = 2;   		// /----
	public final static int DEAK_FILLED_WHITE = 3;    			// <|------
	public final static int DEAK_FILLED = 4;    				// <*|------
	public final static int DEAK_UNFILLEDDIAMOND = 5; 			// < >-----
	public final static int DEAK_FILLEDDIAMOND = 6;   			// <*>-----
	public final static int DEAK_UNFILLEDDIAMOND_NAVIGABLE = 7; // < ><-----
	public final static int DEAK_FILLEDDIAMOND_NAVIGABLE = 8;   // <*><-----
	public final static int DEAK_CIRCLE_WITH_PLUS = 9;    		// (+)<-----
}
