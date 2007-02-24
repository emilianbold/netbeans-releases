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
 * Created on Jun 11, 2003
 *
 *
 */
package org.netbeans.modules.uml.ui.support.drawingproperties;

import java.awt.Font;

/**
 * @author sumitabhk
 *
 *
 */
public interface IFontProperty extends IDrawingProperty
{
	public void initialize( IDrawingPropertyProvider pDrawingPropertyProvider,
							String sDrawEngineName,
							String sResourceName,
							String sFaceName,
							short nCharSet,
							short nSize,
							boolean bItalic,
							boolean bStrikeout,
							boolean bUnderline,
							int nWeight,
							int nColor);
	public void initialize2(IDrawingPropertyProvider pDrawingPropertyProvider,
							String sDrawEngineName,
							String sResourceName,
							Font font,
							int nColor);
	public String getFaceName();
	public void setFaceName(String newVal);
	public void setCharSet(short nCharSet);
	public short getCharSet(short nCharSet);
	public void setSize(int nSize);
	public int getSize();
	public void setItalic(boolean bItalic);
	public boolean getItalic();
	public void setStrikeout(boolean bStrikeout);
	public boolean getStrikeout();
	public void setUnderline(boolean bUnderline);
	public boolean getUnderline();
	public void setWeight(int nWeight);
	public int getWeight();
	public void setColor(int nColor);
	public int getColor();
	public void setFont(Font pFont);
	public Font getFont();
}


