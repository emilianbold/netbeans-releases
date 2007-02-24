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

import java.awt.Point;

public class ETPoint implements IETPoint

{
  private int m_X;
  private int m_Y;

	public ETPoint()
	{
		this(0,0);
	}

	public ETPoint(int pX, int pY)
	{
		m_X = pX;
		m_Y = pY;		
	}

   public ETPoint(Point p)
   {
//      this(p.x, p.y);
       if (p!=null) {
           m_X = p.x;
           m_Y = p.y;
       }
   }

	/**
	 * The X location of the point
	*/
	public int getX()
	{
          return m_X;
	}

	/**
	 * The X location of the point
	*/
	public void setX( int value )
	{
          m_X = value;
	}

	/**
	 * The Y location of the point
	*/
	public int getY()
	{
          return m_Y;
	}

	/**
	 * The Y location of the point
	*/
	public void setY( int value )
	{
          m_Y = value;
	}

	/**
	 * The points
	*/
	public long getPoints( int pX, int pY )
	{
          pX = m_X;
          pY = m_Y;
          return 0;
	}

	/**
	 * The points
	*/
	public long setPoints( int nX, int nY )
	{
          m_X = nX;
          m_Y = nY;
          return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.support.umlsupport.IETPoint#asPoint()
	 */
	public Point asPoint() {
		// TODO Auto-generated method stub
		return new Point(getX(),getY());
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.support.umlsupport.IETPoint#offset(int, int)
	 */
	public IETPoint offset( int dx, int dy )
   {
		setX(getX() + dx);
		setY(getY() + dy);
      
      return this;
	}
}
