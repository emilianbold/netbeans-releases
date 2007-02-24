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

import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;

/*
 *
 * @author KevinM
 *
 */
public class ETBridgeNodeDrawEngine extends ETNodeDrawEngine
{
	public ETBridgeNodeDrawEngine()
	{
		super();
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getElementType()
	 */
	public String getElementType()
	{
		String type = super.getElementType();
		if (type == null)
		{
			type = new String("Bridge");
		}
		return type;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDrawEngineID()
	 */
	public String getDrawEngineID() 
	{
		return "BridgeNodeDrawEngine";
	}
	
	/**
	 * This is the string to be used when looking for other similar drawengines.  Bridges
	 * don't get selected when select all similar is activated.
	 *
	 * @param sID [out,retval] The unique engine identifier
	 */
	public String getDrawEngineMatchID() {		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#doDraw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
	 */
	public void doDraw(IDrawInfo pDrawInfo) {
		// Don't draw.
		//super.doDraw(pDrawInfo);
		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#isDrawEngineValidForModelElement()
	 */
	public boolean isDrawEngineValidForModelElement() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#sizeToContents()
	 */
	public void sizeToContents() {
		try
		{
		   if (getOwnerNode() != null)
		   {
			 resize(1, 1, true);
		   }
		}
		catch(Exception e)
		{
		   e.printStackTrace();
		}
	}

}
