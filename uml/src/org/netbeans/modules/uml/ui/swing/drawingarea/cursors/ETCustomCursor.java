/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
