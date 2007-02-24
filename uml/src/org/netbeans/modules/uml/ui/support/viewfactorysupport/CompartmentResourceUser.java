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

/*
 * Created on Feb 11, 2004
 *
 */
package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/**
 * @author jingmingm
 *
 */
public class CompartmentResourceUser extends ResourceUser
{	
	public CompartmentResourceUser(IResourceUserHelper pResourceUserHelper)
	{
		super( pResourceUserHelper );
	}
	
	public IDrawingAreaControl getDrawingArea()
	{
		return getResourceUserHelper().getDrawingArea();
	}
	
	public int getColorID(int nColorStringID)
	{
		return getResourceUserHelper().getColorID(nColorStringID);
	}
	
	public int getFontID(int nFontStringID)
	{
		return getResourceUserHelper().getFontID(nFontStringID);
	}
	
	public boolean verifyDrawEngineStringID()
	{
		return false;
	}
}



