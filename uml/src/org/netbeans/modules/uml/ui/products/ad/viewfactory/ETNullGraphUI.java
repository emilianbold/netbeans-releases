/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */



package org.netbeans.modules.uml.ui.products.ad.viewfactory;

import com.tomsawyer.editor.graphics.TSEGraphics;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;

/**
 * @author KevinM
 *
 * Used when the grpah window is shutting down by a thread..
 * It stops any acess to the graph.
 */
public class ETNullGraphUI extends ETGenericGraphUI
{

	/**
	 *
	 */
	public ETNullGraphUI()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.ui.TSEGraphUI#draw(com.tomsawyer.editor.graphics.TSEGraphics, boolean, boolean, com.tomsawyer.util.TSConstRect, boolean)
	 */
	public synchronized void draw(TSEGraphics arg0, boolean arg1, boolean arg2, TSConstRect arg3, boolean arg4)
	{
	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.ui.TSEGraphUI#draw(com.tomsawyer.editor.graphics.TSEGraphics, boolean, com.tomsawyer.util.TSConstRect)
	 */
	public synchronized void draw(TSEGraphics arg0, boolean arg1, TSConstRect arg2)
	{

	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.ui.TSEGraphUI#draw(com.tomsawyer.editor.graphics.TSEGraphics, boolean)
	 */
	public void draw(TSEGraphics arg0, boolean arg1)
	{
	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.ui.TSEGraphUI#drawAll(com.tomsawyer.editor.graphics.TSEGraphics, com.tomsawyer.util.TSConstRect, boolean)
	 */
	public void drawAll(TSEGraphics arg0, TSConstRect arg1, boolean arg2)
	{
	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.ui.TSEGraphUI#drawAll(com.tomsawyer.editor.graphics.TSEGraphics, com.tomsawyer.util.TSConstRect)
	 */
	public void drawAll(TSEGraphics arg0, TSConstRect arg1)
	{
	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEObjectUI#drawOutline(com.tomsawyer.editor.graphics.TSEGraphics)
	 */
	public void drawOutline(TSEGraphics arg0)
	{
	}

}
