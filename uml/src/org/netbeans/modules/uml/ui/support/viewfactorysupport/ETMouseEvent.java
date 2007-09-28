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




