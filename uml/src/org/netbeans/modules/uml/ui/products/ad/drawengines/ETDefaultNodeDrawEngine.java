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


//	 $Date$

package org.netbeans.modules.uml.ui.products.ad.drawengines;

import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import com.tomsawyer.editor.TSESolidObject;
import com.tomsawyer.editor.graphics.TSEGraphics;

public class ETDefaultNodeDrawEngine extends ETNodeDrawEngine
{
	

	public void doDraw(IDrawInfo drawInfo)
	{
		TSEGraphics graphics = drawInfo.getTSEGraphics();

		ETGenericNodeUI parentUI = (ETGenericNodeUI)this.getParent();
		
		// draw yourself only if you have an owner
		if (parentUI.getOwnerNode() != null)
		{
			TSESolidObject owner = parentUI.getOwnerNode();
			// draw the background of the node if necessary
		
			if (!parentUI.isTransparent())
			{
				graphics.setColor(parentUI.getFillColor());
				graphics.fillRect(owner.getLocalBounds());
			}

			// draw the border of the node if necessary

			if (parentUI.isBorderDrawn())
			{
				graphics.setColor(parentUI.getBorderColor());
				graphics.drawRect(owner.getLocalBounds());
			}

			// draw the text of the node if necessary
			
			if (parentUI.getOwner().getText() != null)
			{
				parentUI.drawText(graphics);
			}
			
			// draw the layout constraint badge if necessary
			
			parentUI.drawConstraintBadge(graphics);
		}
		
	}
	
	public void initCompartments()
	{

	}

}
