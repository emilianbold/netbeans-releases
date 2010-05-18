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


package org.netbeans.modules.uml.drawingarea.ui.trackbar;

import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;

import org.netbeans.modules.uml.ui.controls.trackbar.TrackBarResource;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;

/**
 *
 * @author Trey Spiva
 */
public abstract class TrackItem extends JComponent implements Accessible
{
   public static final int INSET_V = 3;

   private TrackItem m_PreviousItem = null;
   private TrackItem m_NextItem = null;

   private JTrackBar m_Bar = null;

   public TrackItem(JTrackBar bar)
   {
      setTrackBar(bar);
      addMouseListener(new MenuManagerMouseListener());
//      addKeyListener(new MenuManagerKeyListener());
      addFocusListener(new TrackItemFocusListener());
      
      getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("shift F10"),
                                               "SHOW_CONTEXT_MENU");
      getActionMap().put("SHOW_CONTEXT_MENU", new AbstractAction()
      {
          public void actionPerformed(ActionEvent e)
          {
              showContextMenu(null);
          }
      });
   }

   public boolean isCoupling()
   {
      return false;
   }

   public Rectangle determineBoundingRect()
   {
      Rectangle retVal = new Rectangle();

      JTrackBar bar = getTrackBar();

      limitBoundingRect(retVal);
      if (bar != null)
      {
         retVal.height = bar.getHeight() - (INSET_V * 2);
         retVal.y = INSET_V;
      }

      // Adjust the X bounds for the border.
      Border border = getBorder();
      if (border != null)
      {
         Insets insets = border.getBorderInsets(this);
         retVal.x += insets.left + 1;
         retVal.width += insets.right + 1;
      }
      return retVal;
   }

   public JTrackBar getTrackBar()
   {
      return m_Bar;
   }

   public void setTrackBar(JTrackBar bar)
   {
      m_Bar = bar;
   }

   /**
    * Restores the state of this track item from the archive ( etlp file).  
    * Right now this routine does nothing.
    */
   public void load(IProductArchiveElement pProductArchive)
   {
   }
   
   /**
    * Saves the state of this track item to the archive ( etlp file).  Right now
    * this routine does nothing.
    */
   public void save(IProductArchiveElement pProductArchive)
   {
   }
   
   //**************************************************
   // Data Access Methods
   //**************************************************

   /**
    * @return
    */
   public TrackItem getNextItem()
   {
      return m_NextItem;
   }

   /**
    * @param item
    */
   public void setNextItem(TrackItem item)
   {
      m_NextItem = item;
   }

   /**
    * @return
    */
   public TrackItem getPreviousItem()
   {
      return m_PreviousItem;
   }

   /**
    * @param item
    */
   public void setPreviousItem(TrackItem item)
   {
      m_PreviousItem = item;
   }

   /**
    * @param xDelta
    */
   public void push(int xDelta)
   {
   	if (xDelta != 0)
   	{
			setLocation(getX() + xDelta, getY());
			notifyNeighborsOfMove(xDelta);
   	}
   }

   /**
    * @param xDelta
    */
   public void updateContraints(int xDelta)
   {
   }

   //**************************************************
   // Helper Methods
   //**************************************************

   /**
    * Determine the size of this item.  The height and top of the item will be 
    * set after the limitBoundingRect is called.
    * 
    * @param rect The rectangle to update with items width.
    */
   protected abstract void limitBoundingRect(Rectangle rect);

   protected void notifyNeighborsOfMove(int xDelta)
   {
      TrackItem previousItem = getPreviousItem();
      TrackItem nextItem = getNextItem();

      if (previousItem != null)
      {
         if (xDelta < 0)
         {
            previousItem.push(xDelta);
         }
         else
         {
            previousItem.updateContraints(xDelta);
         }
      }

      if (nextItem != null)
      {
         if (xDelta > 0)
         {
            nextItem.push(xDelta);
         }
         else
         {
            nextItem.updateContraints(xDelta);
         }
      }
   }

   public String toString()
   {
      String retVal = "Track Item [";

      if (isCoupling() == true)
      {
         retVal += "Coupling";
      }
      else
      {
         retVal += "Car";
      }

      retVal += "] " + getName();

      return retVal;
   }

   /**
    * Checks if both sides of this track item has the same neighbors are the 
    * same neighbors as the item that is passed in.
    * 
    * @param rhs The track item to compare with this track item.
    * @return <code>true</code> if the two track items have the same neighbors.
    */
   public boolean isBetweenSameNeighbors(TrackItem rhs)
   {
      boolean retVal = false;

      if (getPreviousItem() == rhs.getPreviousItem())
      {
         if (getNextItem() == rhs.getNextItem())
         {
            retVal = true;
         }
      }

      return retVal;
   }

   /**
    * Copies the track item attributes.  This implementaion does not copy any
    * attributes since TrackItems do not have attributes. 
    * 
    * @param item The track item to copy the attributes from.
    */
   public void copyAttributes(TrackItem item)
   {

   }

   /**
    * Displays the context menu to the user.  This must be overridden to 
    * actually display the context menu.
    * 
    * @param e The mouse information.
    */
   public void showContextMenu(MouseEvent e)
   {

      JPopupMenu menu = new JPopupMenu();
      buildContextMenu(menu);

      int x = 0;
      int y = 0;
      if(e != null)
      {
          x = e.getX();
          y = e.getY();
      }
      
      menu.show(this, x, y);
   }

   public void buildContextMenu(JPopupMenu menu)
   {
   }

   public class MenuManagerMouseListener extends MouseInputAdapter
   {
      /* (non-Javadoc)
      * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
      */
      public void mousePressed(MouseEvent e)
      {
         if (e.isPopupTrigger() == true)
         {
            showContextMenu(e);
         }
//         requestFocus();
      }

      /* (non-Javadoc)
      * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
      */
      public void mouseReleased(MouseEvent e)
      {
         if (e.isPopupTrigger() == true)
         {
            showContextMenu(e);
         }
      }

   }
   
   public class MenuManagerKeyListener extends KeyAdapter
   {
       public void keyPressed(KeyEvent e)
       {
           if(e.isShiftDown() == true)
           {
               if(e.getKeyCode() == KeyEvent.VK_F10)
               {
                   showContextMenu(null);
                   e.consume();
               }
           }
       }
   }
   
   public class TrackItemFocusListener implements FocusListener
   {
        public void focusGained(FocusEvent e)
        {
            repaint();
        }

        public void focusLost(FocusEvent e)
        {
            repaint();
        }
       
   }


    /////////////
    // Accessible
    /////////////

    AccessibleContext accessibleContext;

    public AccessibleContext getAccessibleContext() {
	if (accessibleContext == null) {
	    accessibleContext = new AccessibleTrackItem();
	} 
	return accessibleContext;
    }


    public class AccessibleTrackItem extends AccessibleJComponent {


	public String getAccessibleName() {
	    if (isCoupling()) {
		return TrackBarResource.getString("ACSN_TrackCoupling");
	    } else {
		String name = getName();
		if (name == null || name.equals("")) {
		    return TrackBarResource.getString("ACSN_TrackCarNoName");
		} else {
		    return MessageFormat.format(TrackBarResource.getString("ACSN_TrackCar"), name);
		}
	    }
	}


	public String getAccessibleDescription() {
	    if (! isCoupling()) {
		return MessageFormat.format(TrackBarResource.getString("ACSD_TrackCar"), getName());
	    }
	    return getAccessibleName();
	}
	

    }


}
