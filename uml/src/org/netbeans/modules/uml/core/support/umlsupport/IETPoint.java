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

public interface IETPoint
{
	/**
	 * The X location of the point
	*/
	public int getX();

	/**
	 * The X location of the point
	*/
	public void setX( int value );

	/**
	 * The Y location of the point
	*/
	public int getY();

	/**
	 * The Y location of the point
	*/
	public void setY( int value );

	/**
	 * The points
	*/
	public long getPoints( int pX, int pY );

	/**
	 * The points
	*/
	public long setPoints( int nX, int nY );
	
	/*
	 * Adds the values by dx, dy to the current point values.
	 */
	public IETPoint offset(int dx, int dy);

	/*
	 * Returns this object converted to an java.awt.Point
	 */	
	public Point asPoint();

}
