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

import java.awt.geom.Ellipse2D;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.Color;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
//import com.tomsawyer.editor.ui.TSEDefaultPNodeUI;
import com.tomsawyer.editor.ui.TSEEdgeUI;

import com.tomsawyer.editor.TSEPNode;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
import com.tomsawyer.editor.graphics.TSEGraphics;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;
import com.tomsawyer.editor.TSEColor;

//public class ETGenericPathNodeUI extends TSEDefaultPNodeUI
public class ETGenericPathNodeUI extends TSEEdgeUI
{
	public ETGenericPathNodeUI()
	{
		super();		
	}
	
	public boolean isTransparent() 
	{
		 return true;
	}

	public boolean isTransparentByDefault()
	{
		return true;
	}
	
	public void drawSelected(TSEGraphics graphics)
	{
		doDraw(graphics);
	}
	
	public void draw(TSEGraphics graphics) 
	{
		doDraw(graphics);
	}

	public void drawOutline(TSEGraphics graphics)
	{ 
		doDraw(graphics);
	}
	
	public void drawSelectedOutline(TSEGraphics graphics)
	{
		doDraw(graphics);
	}
		
	public Object clone()
	{
		ETGenericPathNodeUI object = new ETGenericPathNodeUI();
		object.copy(this);
		return object;
	}
	
	protected void doDraw(TSEGraphics graphics)
	{
		TSConstRect bounds = this.getBounds();
		Rectangle dvRect = graphics.getTSTransform().boundsToDevice(bounds);
		double avg = (dvRect.getWidth() + dvRect.getHeight()) /2;
		Shape circle = new Ellipse2D.Double(dvRect.getX(), dvRect.getY(), avg, avg);		
		Color saveColor = graphics.getColor();
		graphics.setColor(TSEColor.white);
		graphics.fill(circle);
		graphics.setColor(saveColor);
		graphics.draw(circle);
	}
	
	public IElement getModelElement()
	{
		// no op. We don't have model elements for path nodes.
		return null; 
	}
	
	public void setModelElement(IElement element)
	{
		// We don't have model elements for path nodes.
	}
	
	public String getInitStringValue()
	{
		return null;
	}
	
	public void setInitStringValue(String string) 
	{
	}	
}
