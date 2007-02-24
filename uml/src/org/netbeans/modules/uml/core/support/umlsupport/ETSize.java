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

package org.netbeans.modules.uml.core.support.umlsupport;

import java.awt.Dimension;

public class ETSize implements IETSize
{
	public static final String CLSID = "{FCB8A3B8-00F1-4E62-88EE-1C9EC25B1383}";

	private int m_width;
	private int m_height;
	
	public ETSize(IETSize pNewETSize)
	{
		this.m_width = pNewETSize.getWidth();
		this.m_height = pNewETSize.getHeight();
	}

	public ETSize(int pWidth, int pHeight)
	{
		this.m_width = pWidth;
		this.m_height = pHeight;
	}

	public void setSize(int pWidth, int pHeight) {
		this.m_width = pWidth;
		this.m_height = pHeight;
	}

	public int getHeight() {
		return this.m_height;
	}

	public int getWidth() {
		return this.m_width;
	}

	public void setHeight(int pValue) {
		this.m_height = pValue;
	}

	public void setWidth(int pValue) {
		this.m_width = pValue;
	}

   public String toString()
   {
      StringBuffer buffer = new StringBuffer("(");
      buffer.append(getWidth());
      buffer.append(", ");
      buffer.append(getHeight());
      buffer.append(")");
      return buffer.toString();
   }
   
   public Dimension asDimension()
   {
      return new Dimension( getWidth(), getHeight() );
   }
}
