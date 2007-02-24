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



package org.netbeans.modules.uml.ui.swing.drawingarea.cursors;

import java.awt.Cursor;
import java.awt.Point;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

/**
 * The base class for cursors used while manipulating the diagrams.
 *
 * @author KevinM
 */
public abstract class ETCustomCursor {

   /** The name of the bundle file to use to retrieve the image to use. */
	protected final static String m_defaultBundle = "org/netbeans/modules/uml/ui/swing/drawingarea/Bundle";
	
	/**
	 * Creates a ETCustomCursor.
	 */
	protected ETCustomCursor() {
		super();
	}

   /** 
    * Create a new Cursor.  The image to use is retrieved from the properties 
    * file.  The method getCursorName is used to specify the name to look up in 
    * in the properties file.  The method getHotSpot is used to specify the 
    * hot spot of the cursor and getCursorName is used to specify the 
    * name of the cursor.
    * 
    * @return The created cursor.
    */
	protected Cursor createCursor()
	{
		//ImageIcon icon = new ImageIcon(ResourceBundle.getBundle(getBundlePath()).getString(getCursorName()));
      String path = ResourceBundle.getBundle(getBundlePath()).getString(getCursorName());
      ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource(path));
		return java.awt.Toolkit.getDefaultToolkit().createCustomCursor(icon.getImage(), 
                                                                     getHotSpot(), 
                                                                     getCursorName());
	}

   /**
    * A localized description of the cursor, for Java Accessibility use.
    */
	protected abstract String getCursorName();
   
   /** Retruns the path to the properties bundle. */
	protected String getBundlePath()
	{
		return m_defaultBundle;
	}
   
   /**
    * Returns the X and Y of the large cursor's hot spot; the hotSpot values 
    * must be less than the Dimension returned by getBestCursorSize.  
    * 
    * @return The cursors hot spot.  This implementation will always return
    *         x = 0 and y = 0.
    */
	protected Point getHotSpot()
	{
		return new Point(0,0);
	}
}
