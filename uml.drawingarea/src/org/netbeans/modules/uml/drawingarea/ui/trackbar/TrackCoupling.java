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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;

import javax.accessibility.AccessibleContext;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;


import org.netbeans.modules.uml.ui.controls.trackbar.TrackBarResource;


/**
 * 
 * @author Trey Spiva
 */
public class TrackCoupling extends TrackItem implements ActionListener
{
   public final static int INSET_H = 3;
   public final static int CAP_HEIGHT = 10;
   
   public final static int NO_CONSTRAINT           = 0;
   public final static int MIN_SIZE_CONSTRAINT     = 1;
   public final static int MESSAGE_SIZE_CONSTRAINT = 2;
   
   public final static String NO_WIDTH_CONSTRAINT      = "NO_WIDTH_CONSTRAINT"; //$NON-NLS-1$
   public final static String MINSIZE_WIDTH_CONSTRAINT = "MINSIZE_WIDTH_CONSTRAINT"; //$NON-NLS-1$
   public final static String MESSAGE_WIDTH_CONSTRAINT = "MESSAGE_WIDTH_CONSTRAINT"; //$NON-NLS-1$
   public final static String SHOW_INDICATORS          = "SHOW_INDICATORS"; //$NON-NLS-1$
   
   public final static String ELEMENT_COUPLING       = "SequenceCoupling";
   public final static String ATTR_COUPLING_PREVIOUS = "previous";
   public final static String ATTR_COUPLING_NEXT     = "next";
   public final static String ATTR_COUPLING_TYPE     = "type";
   public final static String ATTR_COUPLING_SHOW     = "show";
   //public final static String QUERY_FORMAT           = "ELEMENT_COUPLING[@ATTR_COUPLING_PREVIOUS='%s' and @ATTR_COUPLING_NEXT='%s']"
   
   //private int m_CouplingType = NO_CONSTRAINT;
   private int m_CouplingType = NO_CONSTRAINT;
   private boolean m_ShowCouplingType = true; 
   
   /**
    * @param bar
    */
   public TrackCoupling(JTrackBar bar, TrackItem prevItem, TrackItem nextItem)
   {
      super(bar);  
      setPreviousItem(prevItem);
      setNextItem(nextItem);
      
   }
   
   public boolean isCoupling() 
   {
      return true;
   }
   
   public void paint(Graphics g)
   {
      Color curColor = g.getColor();
      
      if(hasFocus() == true)
      {
        g.setColor(SystemColor.textHighlight);
      }
      else
      {
        g.setColor(getBackground());
      }
      
      Rectangle bounds = getBounds();
      g.fillRect(0, 0, bounds.width, bounds.height);
      
      if(isShowCouplingType() == true)
      {
         if(hasFocus() == true)
         {
            g.setColor(SystemColor.textHighlightText);
         }
         else
         {
            g.setColor(SystemColor.textText);
         }
         
         switch(getCouplingType())
         {
            case MIN_SIZE_CONSTRAINT:
               paintMinSizeConstraint(g);
               break;
                
            case MESSAGE_SIZE_CONSTRAINT:
               paintMessageSizeConstraint(g);
               break;
            
            default:
               paintNoConstraint(g);
         }
      }
      
      g.setColor(curColor);
   }
   
   public boolean equals(Object rhs)
   {
      boolean retVal = false;
          
      if(rhs instanceof TrackCoupling)
      {  
         retVal = isBetweenSameNeighbors((TrackCoupling)rhs);
      }
      
      return retVal;
   }
   
   /**
    * Restores the track coupling from the product archive (etlp) file.
    *
    * @param aElement [in] The product archive element being read in.
    */
   /*public void load(IProductArchiveElement aElement)
   {
      if(aElement != null)
      {
         Element domElement = aElement.getDOMElement();
         if(domElement != null)
         {
            // Prepare a query string looking for a coupling with the proper 
            // previous and next XMIIDs
            TrackCar prevCar = (TrackCar)getPreviousItem();
            TrackCar nextCar = (TrackCar)getNextItem();
            
            // We should be always have a previous and an after car.  However,
            // it does not hurt to check.
            String query = ELEMENT_COUPLING + "[";
            if(prevCar != null)
            {
               query += "@" + ATTR_COUPLING_PREVIOUS + "='" + prevCar.getXMIID() + "'";
            }
            
            if(nextCar != null)
            {
               if(prevCar != null)
               {
                  query += " and ";
               }
               
               query += "@" + ATTR_COUPLING_NEXT + "='" + nextCar.getXMIID() + "'";
            }
            
            query += "]";
            
            Node foundNode = XMLManip.selectSingleNode(domElement, query);
            if(foundNode instanceof Element)
            {
               Element foundElement = (Element)foundNode;
               
               // Convert found XML node to an IProductArchiveElement
               IProductArchiveElement newElement = new ProductArchiveElementImpl((Element)foundElement);
               setCouplingType((int)newElement.getAttributeLong(ATTR_COUPLING_TYPE));
               setShowCouplingType(newElement.getAttributeBool(ATTR_COUPLING_SHOW));
            }
         }            
      }
   }*/
   
   /**
    * Saves the track coupling from the product archive (etlp) file.
    *
    * @param aElement [in] The product archive element being read in.
    */
   /*public void save(IProductArchiveElement aElement)
   {
      if(aElement != null)
      {
         IProductArchiveElement newElement = aElement.createElement(ELEMENT_COUPLING);
         if(newElement != null)
         {
            if (getPreviousItem() instanceof TrackCar)
            {
               TrackCar prevCar = (TrackCar)getPreviousItem();
               newElement.addAttributeString(ATTR_COUPLING_PREVIOUS, prevCar.getXMIID());
            }
            
            if (getNextItem() instanceof TrackCar)
            {
               TrackCar prevCar = (TrackCar)getNextItem();
               newElement.addAttributeString(ATTR_COUPLING_NEXT, prevCar.getXMIID());
            }
            
            newElement.addAttributeLong(ATTR_COUPLING_TYPE, getCouplingType());
            newElement.addAttributeBool(ATTR_COUPLING_SHOW, isShowCouplingType());
         }
      }
   }*/
   
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
      
      TrackCar rhsPrevCar = null;
      if (rhs.getPreviousItem() instanceof TrackCar)
      {
         rhsPrevCar = (TrackCar)rhs.getPreviousItem();
      }
      TrackCar rhsNextCar = null;
      if (rhs.getNextItem() instanceof TrackCar)
      {
         rhsNextCar = (TrackCar)rhs.getNextItem();
      }
      
      TrackCar myPrevCar = null;
      if (getPreviousItem() instanceof TrackCar)
      {
         myPrevCar = (TrackCar)getPreviousItem();
      }
      
      TrackCar myNextCar = null;
      if (getNextItem() instanceof TrackCar)
      {
         myNextCar = (TrackCar)getNextItem();
      }
      
      if((rhsPrevCar != null) && (myPrevCar != null))
      {
         String rhsXMIID = rhsPrevCar.getXMIID();
         if(rhsXMIID.equals(myPrevCar.getXMIID()) == true)
         {
            retVal = true;
         }
      }
      else if ((rhsPrevCar == null) && (myPrevCar == null))
      {
         retVal = true;
      }
      
      if(retVal == true)
      {
         if((rhsNextCar != null) && (myNextCar != null))
         {
            String rhsXMIID = rhsNextCar.getXMIID();
            if(rhsXMIID.equals(myNextCar.getXMIID()) == true)
            {
               retVal = true;
            }
         }
         else if ((rhsNextCar == null) && (myNextCar == null))
         {
            retVal = true;
         }
      }
      
      return retVal;
   }
   
   /**
    * Copies the track item attributes.  
    * 
    * @param item The track item to copy the attributes from.
    */
   public void copyAttributes(TrackItem item)
   {
      if (item instanceof TrackCoupling)
      {
         TrackCoupling coupling = (TrackCoupling)item;
         setCouplingType(coupling.getCouplingType());
         setShowCouplingType(coupling.isShowCouplingType());
      }
      
   }
   
   /**
    * @param g
    */
   protected void paintNoConstraint(Graphics g)
   {
      // TODO Auto-generated method stub
      
   }

   /**
    * @param g
    */
   protected void paintMessageSizeConstraint(Graphics g)
   {
//      LineMetrics metrics = g.getFontMetrics().getLineMetrics("(", g);
//      
//      int capWidth = g.getFontMetrics().charWidth('(');
//      int fontHeight = (int) metrics.getHeight();
      
      int middleY = getHeight() / 2;
      int arcContainmentWidth = getHeight() * 2 / 3;
      int arcWidth = Math.min( getWidth(), arcContainmentWidth );
      
      int halfArcWidth = (arcWidth / 2);
      int right = getWidth() - INSET_H - arcWidth; 
      int capTop = middleY - halfArcWidth;
      
      g.drawArc(INSET_H, capTop, arcWidth, arcWidth, 90, 180);
      g.drawArc(right, capTop, arcWidth, arcWidth, 270, 180);
      g.drawLine(INSET_H + halfArcWidth, middleY, right + halfArcWidth, middleY);
   }

   /**
    * @param g
    */
   protected void paintMinSizeConstraint(Graphics g)
   {
      int right = getWidth() - INSET_H;
      int middleY = getHeight() / 2;
      int capTop = middleY - (CAP_HEIGHT / 2);
      int capBottom = middleY + (CAP_HEIGHT / 2);
      
      g.drawLine(INSET_H, capTop, INSET_H, capBottom);
      g.drawLine(right, capTop, right, capBottom);
      g.drawLine(INSET_H, middleY, right, middleY);
      
   }

   /**
    * @param xDelta
    */
   public void push(int xDelta)
   {
      switch(getCouplingType())
      {
         case MIN_SIZE_CONSTRAINT:
            pushMinSizeConstraint(xDelta);
            break;
       
         case MESSAGE_SIZE_CONSTRAINT:
            pushMessageSizeConstraint(xDelta);
            notifyNeighborsOfMove(xDelta);
            break;
   
         default:
            pushNoConstraint(xDelta);
      }
      
   }


   /**
    * @param xDelta
    */
   protected void pushNoConstraint(int xDelta)
   {
      if((getWidth() - Math.abs(xDelta)) <= 0)
      {
         notifyNeighborsOfMove(xDelta);         
      }
      else
      {
         updateContraints(xDelta);
      }
      
   }

   /**
    * @param xDelta
    */
   protected void pushMessageSizeConstraint(int xDelta)
   {
      
   }

   /**
    * @param xDelta
    */
   protected void pushMinSizeConstraint(int xDelta)
   {
//       super.push(xDelta);
      notifyNeighborsOfMove(xDelta);
   }

   /**
    * @param xDelta
    */
   public void updateContraints(int xDelta)
   {        
      resizeToFitNeighbors();
   }
   
   public void resizeToFitNeighbors()
   {
      resizeToFitNeighbors(null);
   }
   
   public void resizeToFitNeighbors(TrackCar center)
   {
   	if (getTrackBar() != null && !getTrackBar().isVisible())
   		return;
        Rectangle bounds = determineBoundingRect();
      
//      TrackItem prevItem = getPreviousItem();
//      TrackItem nextItem = getNextItem();
//      if((prevItem != null) && (nextItem != null))
//      {
//         int previousEnd = prevItem.getX() + prevItem.getWidth();
//         int nextStart   = nextItem.getX();
         
//         int width = (nextStart - previousEnd);
         int width = bounds.width;
         if(width < 0)
         {
            TrackItem prevItem = getPreviousItem();
            TrackItem nextItem = getNextItem();
            if(nextItem == center)
            {
               if(prevItem != null)
               {
                  prevItem.push(width);
               }
            }
            else if(nextItem != null)
            {
                  nextItem.push(-width);
            }
            
            width = 0; 
            bounds = determineBoundingRect();           
         }
         
         //setBounds(previousEnd, getY(), width, getHeight());
        setBounds(bounds);
   }

   //**************************************************
   // Helper Methods
   //**************************************************
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.TrackItem#limitBoundingRect(java.awt.Rectangle)
    */
   protected void limitBoundingRect(Rectangle rect)
   {
      TrackItem prevItem = getPreviousItem();
      TrackItem nextItem = getNextItem();
      
      if((prevItem != null) && (nextItem != null) )
      { 
         rect.x = prevItem.getX() + prevItem.getWidth();
         rect.width = nextItem.getX() - rect.x;
      }
   }

   /* (non-Javadoc)
    * @see java.awt.Component#getBounds()
    */
   public Rectangle getBounds()
   {
      return determineBoundingRect();
   }

   /**
    * Retrieves the coupling sizing constraint. 
    * 
    * @return The constraint type.  Can be <code>NO_CONSTRAINT</code>, 
    *         <code>NO_MIN_SIZE</code>, or <code>NO_MESSAGE_SIZE</code>.
    */
   public int getCouplingType()
   {
      return m_CouplingType;
   }

   /**
    * Sets the coupling sizing constraint.
    *  
    * @param value The constraint type.  Can be <code>NO_CONSTRAINT</code>, 
    *              <code>NO_MIN_SIZE</code>, or <code>NO_MESSAGE_SIZE</code>.
    */
   public void setCouplingType(int value)
   {
       if (m_CouplingType != value) {
	   fireAccessiblePropertyChange(AccessibleContext.ACCESSIBLE_DESCRIPTION_PROPERTY, 
					new Integer(m_CouplingType), 
					new Integer(value));
       }
       m_CouplingType = value;
   }

   /**
    * Determines if the user wants to show the coupling type.
    * 
    * @return <code>true</code> if the coupling type is to be drawn.
    */
   public boolean isShowCouplingType()
   {
      return m_ShowCouplingType;
   }

   /**
    * Sets if the user wants to show the coupling type. 
    * 
    * @param b <code>true</code> if the coupling type is to be drawn.
    */
   public void setShowCouplingType(boolean b)
   {
      m_ShowCouplingType = b;
   }

   /**
    * Retrieves the context menu.  The method showContextMenu uses 
    * buildContextMenu to determine the menu to be displayed.  If null is returned
    * the a menu will not be displayed.
    * 
    * @return The menu to be displayed.
    */
   public void buildContextMenu(JPopupMenu menu)
   {
      menu.add(getNoRestrictionsMenuItem());
      menu.add(getMinWidthMenuItem());
      menu.add(getMessageWidthMenuItem());
      menu.addSeparator();
      menu.add(getShowIndicatorsMenuItem());
   }
   
   public JMenuItem getNoRestrictionsMenuItem()
   {
      JCheckBoxMenuItem retVal = new JCheckBoxMenuItem(TrackBarResource.getString("TrackCoupling.No_Width_Restrictions"), //$NON-NLS-1$
                                                       getCouplingType() == NO_CONSTRAINT);
      //retVal.setMnemonic('N');
      String mnemStr = TrackBarResource.getString("TrackCoupling.No_Width_Restrictions_MNEMONIC");
      retVal.setMnemonic(mnemStr.charAt(0));
      retVal.addActionListener(this);
      retVal.setActionCommand(NO_WIDTH_CONSTRAINT);
      return retVal;
   }
   
   public JMenuItem getMinWidthMenuItem()
   {
      JCheckBoxMenuItem retVal = new JCheckBoxMenuItem(TrackBarResource.getString("TrackCoupling.Retain_This_Minimum_Width"), //$NON-NLS-1$
                                                       getCouplingType() == MIN_SIZE_CONSTRAINT);
      //retVal.setMnemonic('R');
      String mnemStr = TrackBarResource.getString("TrackCoupling.Retain_This_Minimum_Width_MNEMONIC");
      retVal.setMnemonic(mnemStr.charAt(0));
      retVal.addActionListener(this);
      retVal.setActionCommand(MINSIZE_WIDTH_CONSTRAINT);
      return retVal;
   }
   
   public JMenuItem getMessageWidthMenuItem()
   {
      JCheckBoxMenuItem retVal = new JCheckBoxMenuItem(TrackBarResource.getString("TrackCoupling.Set_Width_to_Message_Width"), //$NON-NLS-1$
                                                       getCouplingType() == MESSAGE_SIZE_CONSTRAINT);
      //retVal.setMnemonic('S');
      String mnemStr = TrackBarResource.getString("TrackCoupling.Set_Width_to_Message_Width_MNEMONIC");
      retVal.setMnemonic(mnemStr.charAt(0));
      retVal.addActionListener(this);
      retVal.setActionCommand(MESSAGE_WIDTH_CONSTRAINT);
      return retVal;
   }
 
   public JMenuItem getShowIndicatorsMenuItem()
   {
      JCheckBoxMenuItem retVal = new JCheckBoxMenuItem(TrackBarResource.getString("TrackCoupling.Show_Indicators_in_Track_Bar"), //$NON-NLS-1$
                                                       isShowCouplingType());
      //retVal.setMnemonic('h');
      String mnemStr = TrackBarResource.getString("TrackCoupling.Show_Indicators_in_Track_Bar_MNEMONIC");
      retVal.setMnemonic(mnemStr.charAt(0));
      retVal.addActionListener(this);
      retVal.setActionCommand(SHOW_INDICATORS);
      return retVal;
   }

   /* (non-Javadoc)
    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    */
   public void actionPerformed(ActionEvent e)
   {
      if(e.getActionCommand().equals(NO_WIDTH_CONSTRAINT))
      {
         setCouplingType(NO_CONSTRAINT);
      }
      else if(e.getActionCommand().equals(MINSIZE_WIDTH_CONSTRAINT))
      {
         setCouplingType(MIN_SIZE_CONSTRAINT);
      }
      else if(e.getActionCommand().equals(MESSAGE_WIDTH_CONSTRAINT))
      {
         setCouplingType(MESSAGE_SIZE_CONSTRAINT);
      }
      else if(e.getActionCommand().equals(SHOW_INDICATORS))
      {
         Object c = e.getSource();
         if (c instanceof JCheckBoxMenuItem)
         {
            JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)c;
            setShowCouplingType(menuItem.getState() == true);
         }
      }
      
      repaint();
   }


    /////////////
    // Accessible
    /////////////


    AccessibleTrackCoupling accessibleContext;

    public AccessibleContext getAccessibleContext() {
	if (accessibleContext == null) {
	    accessibleContext = new AccessibleTrackCoupling();
	} 
	return accessibleContext;
    }

    
    public void fireAccessiblePropertyChange(String propName, Object oldValue, Object newValue) {
	if (accessibleContext != null) {
	    if (propName != null 
		&& propName.equals(AccessibleContext.ACCESSIBLE_DESCRIPTION_PROPERTY)) 
	    {		
		String oldDescr = null;
		String newDescr = null;
		if (oldValue != null && oldValue instanceof Integer) {
		    oldDescr = accessibleContext.getAccessibleDescription(((Integer)oldValue).intValue());
		}
		if (newValue != null && newValue instanceof Integer) {
		    newDescr = accessibleContext.getAccessibleDescription(((Integer)newValue).intValue());
		}
		accessibleContext.firePropertyChange(propName, oldDescr, newDescr);
	    }
	}
    }


    public class AccessibleTrackCoupling extends AccessibleTrackItem {

	public String getAccessibleDescription() {
	    return getAccessibleDescription(getCouplingType());
	}

	public String getAccessibleDescription(int couplingType) {
	    String couplingTypeName = "";
	    switch(couplingType)
	    {
		case MIN_SIZE_CONSTRAINT:
		    couplingTypeName = TrackBarResource.getString("ACSD_TrackCoupling_MinimumWidth");
		    break;
                case MESSAGE_SIZE_CONSTRAINT:
		    couplingTypeName = TrackBarResource.getString("ACSD_TrackCoupling_MessageWidth");
		    break;
	        case NO_CONSTRAINT:
		    couplingTypeName = TrackBarResource.getString("ACSD_TrackCoupling_NoWidthRestrictions");
		    break;
	    }
	    
	    return MessageFormat.format(TrackBarResource.getString("ACSD_TrackCoupling"), couplingTypeName);
	}

	
    }


}
