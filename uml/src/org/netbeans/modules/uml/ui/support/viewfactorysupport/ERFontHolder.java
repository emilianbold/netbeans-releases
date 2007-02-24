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

import com.tomsawyer.editor.TSEFont;

import java.awt.Font;

/**
 * @author jingmingm
 *
 */
public class ERFontHolder
{
	protected Font m_pFont = null;
	protected Font m_pZoomedFont = null;
	protected double m_nZoomLevel = 1.0;
	protected int m_nOrigSize = 6;
	protected int m_nPointSize = 6;
	protected boolean m_bBold = false;
	protected boolean m_bItalic = false;
	protected boolean m_bStrikeout = false;
	protected boolean m_bUnderline = false;
	protected String m_sFacename = "Arial";
	protected int m_nCharset = 0;

	public ERFontHolder()
	{
		super();
	}
	
	
	public ERFontHolder(String faceName, int nHeight, boolean italic, boolean bold)
	{
		m_sFacename = faceName;
		m_bItalic = italic;
		m_nPointSize = nHeight;
		m_nOrigSize = nHeight;
		m_bBold = bold;
		createDefaultFont();	// Derive the font.
	}
	
	public ERFontHolder(String faceName, int nHeight, boolean italic, boolean underline, boolean bold)
	{
		m_sFacename = faceName;
		m_bItalic = italic;
		m_bUnderline = underline;
		m_nPointSize = nHeight;
		m_nOrigSize = nHeight;
		m_bBold = bold;
		createDefaultFont();	// Derive the font.
	}

	protected int fontStyle()
	{
		int style = Font.PLAIN;

		if (m_bItalic)
		{
			style |= Font.ITALIC;
		}
		if (m_bBold)
		{
			style |= Font.BOLD;
		}

		return style;
	}

	protected void createDefaultFont()
	{
		if (m_pFont == null)
		{
			m_pFont = new Font(m_sFacename, fontStyle(), m_nPointSize);
		}

	}

	public boolean isSame(String sFacename, int nHeight, boolean bBold, boolean bItalic)
	{

		// Typical values are 400 (FW_NORMAL) or 700 (FW_BOLD).
		return nHeight == m_nOrigSize && bBold == m_bBold && bItalic == m_bItalic && sFacename.equals(m_sFacename);
	}

	public void setFont(Font pFont)
	{
		if (pFont != null)
		{
			m_nZoomLevel = 1.0f;
			m_pZoomedFont = pFont;
			m_pFont = pFont;

			//String sName = m_pFont.getName();  This looks like a bug??? (Kevin)
			m_sFacename = m_pFont.getName();
			m_bBold = m_pFont.isBold();
			m_bItalic = m_pFont.isItalic();
			m_nPointSize = m_pFont.getSize();
		}
	}

	public Font getFont(double nZoomLevel /*= 1.0*/
	)
	{
		// Make sure we have the fonts created
		createDefaultFont();

		if (nZoomLevel == 1.0)
		{
			return m_pFont;
		}
		else
		{
			// Make sure we have the fonts created		
			if (m_nZoomLevel == nZoomLevel && m_pZoomedFont != null)
			{
				return m_pZoomedFont;
			}
			else
			{
				// Something has changed recreate the zoomed font.
				TSEFont tseFont = new TSEFont(m_pFont);
				m_pZoomedFont = tseFont.getScaledFont(nZoomLevel);
				m_nZoomLevel = nZoomLevel;
			}

			return m_pZoomedFont;
		}
	}

	public int getSize()
	{
		return m_nPointSize;
	}

	void setSize(int nSize)
	{
		if (m_nOrigSize == 0)
		{
			m_nOrigSize = nSize;
		}

		m_nPointSize = nSize;
		if (m_pFont != null)
		{
			m_pFont = m_pFont.deriveFont((float) nSize);
		}
		m_pZoomedFont = null;
	}

	public boolean getBold()
	{
		return m_bBold;
	}

	public void setBold(boolean bBold)
	{
		m_bBold = bBold;
		if (m_pFont != null)
		{
			m_pFont = m_pFont.deriveFont(fontStyle());
		}
		m_pZoomedFont = null;
	}

	public boolean getItalic()
	{
		return m_bItalic;
	}

	public void setItalic(boolean bItalic)
	{
		m_bItalic = bItalic;
		if (m_pFont != null)
		{
			m_pFont = m_pFont.deriveFont(fontStyle());
		}
		m_pZoomedFont = null;
	}

	public boolean getStrikeout()
	{
		return m_bStrikeout;
	}

	public void SetStrikeout(boolean bStrikeout)
	{
		m_bStrikeout = bStrikeout;
		if (m_pFont != null)
		{
			m_pFont = m_pFont.deriveFont(fontStyle());
		}
		m_pZoomedFont = null;
	}

	public boolean getUnderline()
	{
		return m_bUnderline;
	}

	void setUnderline(boolean bUnderline)
	{
		m_bUnderline = bUnderline;
		if (m_pFont != null)
		{
			m_pFont = m_pFont.deriveFont(fontStyle());
		}
		m_pZoomedFont = null;
	}

	public String getFacename()
	{
		return m_sFacename;
	}

	public void setFacename(String sFacename)
	{
		m_sFacename = sFacename;
		if (m_pFont != null)
		{
			//m_pFont.put_Name(sFacename);
			m_pFont = null;
			createDefaultFont();
		}
		m_pZoomedFont = null;
	}

	public int getCharset()
	{
		return m_nCharset;
	}

	public void setCharset(int nCharset)
	{
		m_nCharset = nCharset;
		if (m_pFont != null)
		{
			m_pFont = m_pFont.deriveFont(fontStyle());
		}
		m_pZoomedFont = null;
	}
}
