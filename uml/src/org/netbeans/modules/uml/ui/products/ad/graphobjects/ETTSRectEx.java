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



package org.netbeans.modules.uml.ui.products.ad.graphobjects;

//import com.tomsawyer.jnilayout.TSSide;
import org.netbeans.modules.uml.ui.support.TSSide;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSRect;
import com.tomsawyer.drawing.geometry.TSRect;

/**
 * @author KevinM
 *
 *	This class is used to access more complex rectangle functions only found in com.tomsawyer.jnilayout.TSRect.
 */
public class ETTSRectEx extends TSRect {
	
	public ETTSRectEx(TSConstRect rect)
	{
		 super(rect);
	}
	
	public void moveTo(double x, double y)
	{
		moveTo((int)x, (int)y);
	}
	
	public void moveTo(int x, int y)
	{
            /* commented by jyothi
		com.tomsawyer.jnilayout.TSRect serverRect = getLayoutRect();
		if (serverRect != null)
		{
			serverRect.moveTo(x, y);
			updateThis(serverRect);
		}
             */
	}
	
	public void moveTo(TSConstPoint pt)
	{
		if (pt != null)
		{
			moveTo(pt.getX(), pt.getY());
		}
	}
	
	public void move(double dx, double dy)
	{
		move((int)dx, (int)dy);
	}
	
	public void move(int dx, int dy)
	{
            /* commented by jyothi
		com.tomsawyer.jnilayout.TSRect serverRect = getLayoutRect();
		if (serverRect != null)
		{
			serverRect.move(dx, dy); 
			updateThis(serverRect);
		}	
             */	
	}
	
	/*
	 * returns a TSSide enum, given a point.
	 */
	public int closestSide(TSConstPoint pt)
	{
		if (pt != null)
		{
                    /* commented by jyothi
			com.tomsawyer.jnilayout.TSPoint nativePoint = new com.tomsawyer.jnilayout.TSPoint();
			if (nativePoint != null){
				nativePoint.x((int)pt.getX());
				nativePoint.y((int)pt.getY());
				return getLayoutRect().closestSide(nativePoint);				
			}
                     */
		}
		return TSSide.TS_SIDE_UNDEFINED;
	}
	
	/*
	 * Copies the Layout rectangle back to the client side.
	 */
	//private void updateThis(com.tomsawyer.jnilayout.TSRect fromRect)
        private void updateThis(TSRect fromRect)
	{
		if (fromRect != null)
		{/* commented by jyothi
			setLeft((double)fromRect.left());
			setTop((double)fromRect.top());
			setBottom((double)fromRect.bottom());
			setRight((double)fromRect.right());			
                  */
		}
	}
	
	/*
	 * Creates a server side rectangle intialized with this points. 
	 */
	//private com.tomsawyer.jnilayout.TSRect getLayoutRect()
	private TSRect getLayoutRect()
	{
            /* commented by jyothi
		com.tomsawyer.jnilayout.TSRect serverRect = new com.tomsawyer.jnilayout.TSRect();
		serverRect.setCorners((int)getLeft(), (int)getTop(), (int)getRight(), (int)getBottom());
		return serverRect;
             */ return null; //written by jyothi for compilation purposes.. need to remove this
	}
}
