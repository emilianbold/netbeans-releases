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

//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import com.tomsawyer.editor.TSEColor;
import java.awt.Shape;

public interface IETArrowHead
{
	public boolean draw(IDrawInfo pInfo, TSConstPoint fromPt, TSConstPoint toPt,		
		TSEColor color);
	
	public int getKind();
	
	// It will use the defaults if you don't set the witdh and height.
	public void setHeight(int height);
	public void setWidth(int width);
	public int getHeight();
	public int getWidth();
	
	public int getDefaultHeight();
	public int getDefaultWidth();

	public Shape getShape(IDrawInfo pInfo, TSConstPoint fromPt, TSConstPoint toPt);	
}
