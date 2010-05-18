/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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



