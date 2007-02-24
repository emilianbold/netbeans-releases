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
 * Created on Feb 4, 2004
 *
 */
package org.netbeans.modules.uml.ui.support.drawingproperties;

import java.awt.Font;

/**
 * @author jingmingm
 *
 */
public class FontProperty extends DrawingProperty implements IFontProperty
{
	protected String m_FaceName = "";
	protected short m_nCharset = 0;
	protected int m_nSize = 0;
	protected boolean m_bItalic = false;
	protected boolean m_bStrikeout = false;
	protected boolean m_bUnderline = false;
	protected int m_nWeight = 0;
	protected int m_nColor = -1;
	
	public String getResourceType()
	{
		return "font";
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.drawingproperties.IFontProperty#initialize(org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingPropertyProvider, java.lang.String, java.lang.String, java.lang.String, short, short, boolean, boolean, boolean, int, int)
	 */
	public void initialize(IDrawingPropertyProvider pDrawingPropertyProvider, String sDrawEngineName, String sResourceName, String sFaceName, short nCharSet, short nSize, boolean bItalic, boolean bStrikeout, boolean bUnderline, int nWeight, int nColor)
	{
		if (pDrawingPropertyProvider != null)
		{
			setDrawingPropertyProvider(pDrawingPropertyProvider);
		}
		
		if (sFaceName != null && sFaceName.length() > 0)
		{
			setDrawEngineName(sDrawEngineName);
			setResourceName(sResourceName);
			setFaceName(sFaceName);
			setCharSet(nCharSet);
			setSize(nSize);
			setItalic(bItalic);
			setStrikeout(bStrikeout);
			setUnderline(bUnderline);
			setWeight(nWeight);
			setColor(nColor);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.drawingproperties.IFontProperty#initialize(org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingPropertyProvider, java.lang.String, java.lang.String, java.awt.Font, int)
	 */
	public void initialize2(IDrawingPropertyProvider pDrawingPropertyProvider, String sDrawEngineName, String sResourceName, Font font, int nColor)
	{
		if (pDrawingPropertyProvider != null && sDrawEngineName != null && sDrawEngineName.length() > 0 && sResourceName != null && sResourceName .length() >0 && font != null)
		{
			setDrawingPropertyProvider(pDrawingPropertyProvider);
			setDrawEngineName(sDrawEngineName);
			setResourceName(sResourceName);
			setFont(font);
			setColor(nColor);
		}
	}

	public String getFaceName()
	{
		return m_FaceName;
	}

	public void setFaceName(String newVal)
	{
		m_FaceName = newVal;	
	}

	public void setCharSet(short nCharSet)
	{
		m_nCharset = nCharSet;		
	}

	public short getCharSet(short nCharSet)
	{
		return m_nCharset;
	}

	public void setSize(int nSize)
	{
		m_nSize = nSize;
	}

	public int getSize()
	{
		return m_nSize;
	}

	public void setItalic(boolean bItalic)
	{
		m_bItalic = bItalic;	
	}

	public boolean getItalic()
	{
		return m_bItalic;
	}

	public void setStrikeout(boolean bStrikeout)
	{
		m_bStrikeout = bStrikeout;	
	}

	public boolean getStrikeout()
	{
		return m_bStrikeout;
	}

	public void setUnderline(boolean bUnderline)
	{
		m_bUnderline = bUnderline;	
	}

	public boolean getUnderline()
	{
		return m_bUnderline;
	}

	public void setWeight(int nWeight)
	{
		m_nWeight = nWeight;;	
	}

	public int getWeight()
	{
		return m_nWeight;
	}

	public void setColor(int nColor)
	{
		m_nColor = nColor;		
	}

	public int getColor()
	{
		return m_nColor;
	}

	public void setFont(Font pFont)
	{
		setFaceName(pFont.getName());
		setItalic(pFont.isItalic());
		if (pFont.isBold())
		{
			setWeight(700);
		}
		else
		{
			setWeight(400);
		}
		setSize(pFont.getSize());
	}

	public Font getFont()
	{
		int style = Font.PLAIN;
		if (m_bItalic)
		{
			style |= Font.ITALIC;
		}
		if (m_bUnderline)
		{
			style |= Font.BOLD;
		}
		
		Font pFont = new Font(m_FaceName, style, m_nSize);
		return pFont;
	}

}



