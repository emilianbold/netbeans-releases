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

//import com.tomsawyer.jnilayout.TSSide;
import org.netbeans.modules.uml.ui.support.TSSide;

public interface QuadrantKindEnum
{
   // These value must match the TSSide value so that the code in
   // PortDrawEngine.getComponentSide() works properly.

   public static final int QK_RIGHT = TSSide.TS_SIDE_RIGHT;
	public static final int QK_TOP = TSSide.TS_SIDE_TOP;
   public static final int QK_LEFT = TSSide.TS_SIDE_LEFT;
	public static final int	QK_BOTTOM = TSSide.TS_SIDE_BOTTOM;
	public static final int	QK_ERROR = TSSide.TS_SIDE_UNDEFINED;
}
