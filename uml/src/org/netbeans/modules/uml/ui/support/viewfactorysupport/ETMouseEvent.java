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
 * Created on Aug 5, 2003
 *
 */
package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import java.awt.Component;
import java.awt.event.MouseEvent;

/**
 * @author nickl
 *
 */
public class ETMouseEvent extends MouseEvent implements IMouseEvent {

	/**
	 * @param source
	 * @param id
	 * @param when
	 * @param modifiers
	 * @param x
	 * @param y
	 * @param clickCount
	 * @param popupTrigger
	 */
	public ETMouseEvent(Component source, int id, long when, int modifiers, int x, int y, int clickCount, boolean popupTrigger) {
		super(source, id, when, modifiers, x, y, clickCount, popupTrigger);
	}

	/**
	 * @param source
	 * @param id
	 * @param when
	 * @param modifiers
	 * @param x
	 * @param y
	 * @param clickCount
	 * @param popupTrigger
	 * @param button
	 */
	public ETMouseEvent(Component source, int id, long when, int modifiers, int x, int y, int clickCount, boolean popupTrigger, int button) {
		super(source, id, when, modifiers, x, y, clickCount, popupTrigger, button);
	}

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IMouseEvent#getDeviceX()
    */
   public int getDeviceX()
   {
      return getX();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IMouseEvent#getDeviceY()
    */
   public int getDeviceY()
   {
      return getY();
   }

   /**
    * The logical x for this event, set via Initializ()
   */
   public int getLogicalX()
   {
      // TODO
      return 0;
   }

   /**
    * The logical y for this event, set via Initializ()
   */
   public int getLogicalY()
   {
      // TODO
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IMouseEvent#setClientX(int)
    */
   public void setClientX(int value)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IMouseEvent#setClientY(int)
    */
   public void setClientY(int value)
   {
      // TODO Auto-generated method stub
      
   }
}




