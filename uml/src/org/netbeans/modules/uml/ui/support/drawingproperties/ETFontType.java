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
 * Created on Feb 3, 2004
 *
 */
package org.netbeans.modules.uml.ui.support.drawingproperties;

/**
 * @author jingmingm
 *
 */
public class ETFontType
{
	protected String m_Name = "";
	protected int m_Height = 0;
	protected int m_Weight = 0;
	protected boolean m_Italic = false;;
	protected int m_Color = 255;
	
	public String getName()
	{
		return m_Name;
	}
	
	public void setName(String newVal)
	{
		m_Name = newVal;
	}
	
	public int getHeight()
	{
		return m_Height;
	}
	
	public void setHeight(int newVal)
	{
		m_Height = newVal;
	}
	
	public int getWeight()
	{
		return m_Weight;
	}
	
	public void setWeight(int newVal)
	{
		m_Weight = newVal;
	}
	
	public boolean getItalic()
	{
		return m_Italic;
	}
	
	public void setItalic(boolean newVal)
	{
		m_Italic = newVal;
	}
	
	public int getColor()
	{
		return m_Color;
	}
	
	public void setColor(int newVal)
	{
		m_Color = newVal;
	}
}



