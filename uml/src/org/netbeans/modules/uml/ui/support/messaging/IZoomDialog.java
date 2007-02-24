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


package org.netbeans.modules.uml.ui.support.messaging;

import org.netbeans.modules.uml.common.generics.ETPairT;


public interface IZoomDialog
{

	public static final int CANCEL = 0;
	public static final int FINISH = 1;

	public void setCurrentZoom(double nCurrentZoom);
	public double getCurrentZoom();
	public boolean getFitToWindow();
	
	//Displays the zoom dialog allowing folks to choose a zoom level
	public ETPairT <Double, Boolean> display(double nCurrentZoom);

}
