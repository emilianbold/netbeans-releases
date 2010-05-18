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

package org.netbeans.modules.uml.ui.swing.plaf.basic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicButtonListener;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import org.netbeans.modules.uml.ui.swing.plaf.PullDownButtonUI;
import org.netbeans.modules.uml.ui.swing.pulldownbutton.IPulldownButtonInvoker;
import org.netbeans.modules.uml.ui.swing.pulldownbutton.JPullDownButton;

/**
 * 
 * @author Trey Spiva
 */
public class BasicPullDownButtonUI extends PullDownButtonUI
{
   // Shared UI object
   private final static BasicPullDownButtonUI buttonUI = new BasicPullDownButtonUI();

   /* These rectangles/insets are allocated once for all 
    * ButtonUI.paint() calls.  Re-using rectangles rather than 
    * allocating them in each paint call substantially reduced the time
    * it took paint to run.  Obviously, this method can't be re-entered.
    */
   

   // Visual constants
   protected int defaultTextIconGap;

   // Offset controlled by set method 
   private int shiftOffset = 0;
   protected int defaultTextShiftOffset;

   // Has the shared instance defaults been initialized?
   private boolean defaults_initialized = false;

   private JPullDownButton m_Button = null;

   private int m_ArrowWidth = 6;
   private int m_ArrowInsetWidth = 3;
   private int m_LargeArrowWidth = 16;
   
   private final static String propertyPrefix = "Button" + ".";

   // ********************************
   //          Create PLAF
   // ********************************
   public static ComponentUI createUI(JComponent c)
   {
      return buttonUI;
   }

   protected String getPropertyPrefix()
   {
      return propertyPrefix;
   }

   // ********************************
   //          Install PLAF
   // ********************************
   public void installUI(JComponent c)
   {
      m_Button = (JPullDownButton)c;

      installDefaults(m_Button);
      installListeners(m_Button);
      installKeyboardActions(m_Button);
      BasicHTML.updateRenderer(c, m_Button.getText());
   }

   private Color defaultForeground = null;
   private Color defaultBackground = null;
   private Font defaultFont = null;
   private Border defaultBorder = null;

   protected void installDefaults(AbstractButton b)
   {
      // load shared instance defaults
      String pp = getPropertyPrefix();
      if (!defaults_initialized)
      {
         defaultTextIconGap = ((Integer)UIManager.get(pp + "textIconGap")).intValue();
         defaultTextShiftOffset = ((Integer)UIManager.get(pp + "textShiftOffset")).intValue();

         // next four lines part of optimized component defaults installation
         /* defaultForeground = UIManager.getColor(pp + "foreground");
          defaultBackground = UIManager.getColor(pp + "background");
          defaultFont = UIManager.getFont(pp + "font");
          defaultBorder = UIManager.getBorder(pp + "border");*/

         defaults_initialized = true;
      }

      // set the following defaults on the button
      if (b.isContentAreaFilled())
      {
         b.setOpaque(true);
      }
      else
      {
         b.setOpaque(false);
      }

      if (b.getMargin() == null || (b.getMargin() instanceof UIResource))
      {
         b.setMargin(UIManager.getInsets(pp + "margin"));
      }

      // *** begin optimized defaults install ***

      /* Color currentForeground = b.getForeground();
         Color currentBackground = b.getBackground();
         Font currentFont = b.getFont();
         Border currentBorder = b.getBorder();
      
         if (currentForeground == null || currentForeground instanceof UIResource) {
               b.setForeground(defaultForeground);
         }
      
         if (currentBackground == null || currentBackground instanceof UIResource) {
                    b.setBackground(defaultBackground);
         }
      
         if (currentFont == null || currentFont instanceof UIResource) {
               b.setFont(defaultFont);
         }
      
         if (currentBorder == null || currentBorder instanceof UIResource) {
               b.setBorder(defaultBorder);
         } */

      // *** end optimized defaults install ***

      // old code below works for component defaults installation, but it is slow
      LookAndFeel.installColorsAndFont(b, pp + "background", pp + "foreground", pp + "font");
      LookAndFeel.installBorder(b, pp + "border");

   }

   protected void installListeners(AbstractButton b)
   {
      BasicButtonListener listener = createButtonListener(b);
      if (listener != null)
      {
         // put the listener in the button's client properties so that
         // we can get at it later
         b.putClientProperty(this, listener);

         b.addMouseListener(listener);
         b.addMouseMotionListener(listener);
         b.addFocusListener(listener);
         b.addPropertyChangeListener(listener);
         b.addChangeListener(listener);
      }

//      FocusListener fListener = createFocusListener();
//      if (fListener != null)
//      {
//         b.addFocusListener(fListener);
//      }
      
      MouseListener popupMouseListener = new InvocationMouseHandler();
      if (popupMouseListener != null) 
      {
         b.addMouseListener( popupMouseListener );
      }
   }

   protected void installKeyboardActions(AbstractButton b)
   {
      BasicButtonListener listener = (BasicButtonListener)b.getClientProperty(this);
      if (listener != null)
      {
         listener.installKeyboardActions(b);
      }
   }

   // ********************************
   //         Uninstall PLAF
   // ********************************
   public void uninstallUI(JComponent c)
   {
      setPopupVisible( m_Button, false);
              
      uninstallKeyboardActions((AbstractButton)c);
      uninstallListeners((AbstractButton)c);
      uninstallDefaults((AbstractButton)c);
      BasicHTML.updateRenderer(c, "");
   }

   protected void uninstallKeyboardActions(AbstractButton b)
   {
      BasicButtonListener listener = (BasicButtonListener)b.getClientProperty(this);
      if (listener != null)
      {
         listener.uninstallKeyboardActions(b);
      }
   }

   protected void uninstallListeners(AbstractButton b)
   {
      BasicButtonListener listener = (BasicButtonListener)b.getClientProperty(this);
      b.putClientProperty(this, null);
      if (listener != null)
      {
         b.removeMouseListener(listener);
         b.removeMouseListener(listener);
         b.removeMouseMotionListener(listener);
         b.removeFocusListener(listener);
         b.removeChangeListener(listener);
         b.removePropertyChangeListener(listener);
      }
   }

   protected void uninstallDefaults(AbstractButton b)
   {
      LookAndFeel.uninstallBorder(b);
      defaults_initialized = false;
   }

   //**************************************************
   // Popup Control Methods
   //**************************************************
      
   /**
    * Tells if the popup is visible or not.
    */
   public boolean isPopupVisible() 
   {
      boolean retVal = false;
      if(m_Button != null)
      {
         IPulldownButtonInvoker invoker = m_Button.getPulldownInvoker();
         if(invoker != null)
         {
            retVal = invoker.isPulldownVisible();
         }
      }
      
      return retVal;
   }

   /**
    * Hides the popup.
    */
   public void setPopupVisible( JPullDownButton c, boolean v ) 
   {
      if ( v = true ) 
      {
         show();
      } 
      else 
      {
         hide();
      }
   }
   
   /**
    * Implementation of ComboPopup.show().
    */
   public void show() 
   {
      if(m_Button != null)
      {
         IPulldownButtonInvoker invoker = m_Button.getPulldownInvoker();
         if(invoker != null)
         {
            invoker.showPulldown(m_Button);
         }
      }
    }

   
   /**
    * Implementation of ComboPopup.hide().
    */
   public void hide() 
   {
      MenuSelectionManager manager = MenuSelectionManager.defaultManager();
      MenuElement [] selection = manager.getSelectedPath();
      for ( int i = 0 ; i < selection.length ; i++ ) 
      {
         if ( selection[i] == this ) 
         {
            manager.clearSelectedPath();
            break;
         }
      }
      if (selection.length > 0) 
      {
         m_Button.repaint();
      }
   }
   
   // ********************************
   //        Create Listeners 
   // ********************************
   protected BasicButtonListener createButtonListener(AbstractButton b)
   {
//      m_PullDown = new JPopupMenu();
      return new BasicButtonListener(b)
      {
         public void mousePressed(MouseEvent e)
         {                       
            if (SwingUtilities.isLeftMouseButton(e))
            {
               AbstractButton b = (AbstractButton)e.getSource();
               if(isButtonPressed(e.getX(), e.getY()) == true)
               {                  
                  // show();
                  if(b.contains(e.getX(), e.getY())) 
                  {
                     long multiClickThreshhold = b.getMultiClickThreshhold();
                  
                     ButtonModel model = b.getModel();
                     if (model.isEnabled() == true) 
                     {
                        if (!model.isArmed()) 
                        {
                           // button not armed, should be
                           model.setArmed(true);
                        }
                          
                        //model.setPressed(true);
                        m_Button.setArrowButtonPressed(true);
                        if(!b.hasFocus() && b.isRequestFocusEnabled()) 
                        {
                          b.requestFocus();
                        } 
                     }
                  }
               }
               else
               {
                  super.mousePressed(e);
                  m_Button.setArrowButtonPressed(false);
               }
            }            
         }
         
         public void mouseReleased(MouseEvent e)
         {         
            m_Button.setArrowButtonPressed(false);              
            if (SwingUtilities.isLeftMouseButton(e))
            {
               super.mouseReleased(e);
               AbstractButton b = (AbstractButton)e.getSource();
               if(isButtonPressed(e.getX(), e.getY()) == true)
               {         
                  show();
               }
            }            
         }
      };
   }

   protected boolean isButtonPressed(int x, int y)
   {
      boolean retVal = false;
      
      if(isArrowButton() == false)
      {
         Insets insets = m_Button.getInsets();
         
         int arrowWidth = m_ArrowWidth + (m_ArrowInsetWidth * 2) + insets.right;
         if ((x < m_Button.getWidth()) && (x > m_Button.getWidth() - arrowWidth))
         {
            retVal = true;
         }
      }
      else
      {
         retVal = true;
      }
      
      return retVal;
   }
   
   public int getDefaultTextIconGap(AbstractButton b)
   {
      return defaultTextIconGap;
   }

   protected Color getSelectColor() 
   {
      return UIManager.getColor(getPropertyPrefix() + "select");
   }

   protected Color getDisabledTextColor() 
   {
      return UIManager.getColor(getPropertyPrefix() + "disabledText");
   }

   protected Color getFocusColor() 
   {
      return UIManager.getColor(getPropertyPrefix() + "focus");
   }
   
   /**
    * A listener to be registered upon the combo box
    * (<em>not</em> its popup menu)
    * to handle mouse events
    * that affect the state of the popup menu.
    * The main purpose of this listener is to make the popup menu
    * appear and disappear.
    * This listener also helps
    * with click-and-drag scenarios by setting the selection if the mouse was
    * released over the list during a drag.
    *
    * <p>
    * <strong>Warning:</strong>
    * We recommend that you <em>not</em> 
    * create subclasses of this class.
    * If you absolutely must create a subclass,
    * be sure to invoke the superclass
    * version of each method.
    *
    * @see BasicComboPopup#createMouseListener
    */
   protected class InvocationMouseHandler extends MouseAdapter
   {
      /**
       * Responds to mouse-pressed events on the combo box.
       *
       * @param e the mouse-press event to be handled
       */
      public void mousePressed(MouseEvent e)
      {
      }

      /**
       * Responds to the user terminating
       * a click or drag that began on the combo box.
       *
       * @param e the mouse-release event to be handled
       */
      public void mouseReleased(MouseEvent e)
      {
         
      }
   }

   /**
    * 
    */
   protected void togglePopup()
   {
      //m_PullDown.setVisible(!m_PullDown.isVisible());
      if(isPopupVisible() == true)
      {
         hide();
      }
      else
      {
         show();
      }
   }
   
//   protected MouseEvent convertMouseEvent(MouseEvent e)
//   {
//      Point convertedPoint = SwingUtilities.convertPoint((Component)e.getSource(), e.getPoint(), list);
//      MouseEvent newEvent = new MouseEvent((Component)e.getSource(), e.getID(), e.getWhen(), e.getModifiers(), convertedPoint.x, convertedPoint.y, e.getClickCount(), e.isPopupTrigger());
//      
//      return newEvent;
//   }
   //**************************************************
   // BannerMsg
   //**************************************************

   /**
    * Calculate the placement and size of the popup portion of the combo box based
    * on the combo box location and the enclosing screen bounds. If
    * no transformations are required, then the returned rectangle will
    * have the same values as the parameters.
    * 
    * @param px starting x location
    * @param py starting y location
    * @param pw starting width
    * @param ph starting height
    * @return a rectangle which represents the placement and size of the popup
    */
   //protected Rectangle computePopupBounds(int px,int py,int pw, int ph) 
   protected Point computePopupLocation(int height)
   {
//      Toolkit toolkit = Toolkit.getDefaultToolkit();
//      Rectangle screenBounds;
//
//      // Calculate the desktop dimensions relative to the combo box.
//      GraphicsConfiguration gc = m_Button.getGraphicsConfiguration();
//      Point offset = new Point();
//      SwingUtilities.convertPointFromScreen(offset, m_Button);
//
//      Point location = new Point(Math.abs(offset.x), Math.abs(offset.y) + height);
//      return location;

      return new Point(0, m_Button.getHeight());
   }

   //**************************************************
   // Paint Routines
   //**************************************************

   public void paint(Graphics g, JComponent c)
   {
      AbstractButton b = (AbstractButton)c;
      ButtonModel model = b.getModel();

      FontMetrics fm = g.getFontMetrics();

      Insets i = c.getInsets();
      
      g.setColor(c.getForeground());
      
      int arrowWidth = m_LargeArrowWidth;
      
      Rectangle viewRect = new Rectangle();
      Rectangle textRect = new Rectangle();
      Rectangle iconRect = new Rectangle();
         
      // perform UI specific press action, e.g. Windows L&F shifts text
      if((model.isArmed() && model.isPressed()) ||
         (m_Button.isArrowButtonPressed() == true))
      {
         paintButtonPressed(g, b);
      }
      
      if(isArrowButton() == false)
      {
         arrowWidth = m_ArrowWidth;
         
         int btnWidth = b.getWidth() - (m_ArrowWidth + (m_ArrowInsetWidth * 2));
         viewRect.x = i.left;
         viewRect.y = i.top;
         viewRect.width = btnWidth - (i.right + viewRect.x);
         viewRect.height = b.getHeight() - (i.bottom + viewRect.y);
   
         textRect.x = textRect.y = textRect.width = textRect.height = 0;
         iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;
   
         Font f = c.getFont();
         g.setFont(f);
   
         // layout the text and icon
         String text = SwingUtilities.layoutCompoundLabel(c,
                                                          fm,
                                                          b.getText(),
                                                          b.getIcon(),
                                                          b.getVerticalAlignment(),
                                                          b.getHorizontalAlignment(),
                                                          b.getVerticalTextPosition(),
                                                          b.getHorizontalTextPosition(),
                                                          viewRect,
                                                          iconRect,
                                                          textRect,
                                                          b.getText() == null ? 0 : b.getIconTextGap());
   
         clearTextShiftOffset();      
   
         // Paint the Icon
         if (b.getIcon() != null)
         {
            paintIcon(g, c, iconRect);
         }
   
         if (text != null && !text.equals(""))
         {
            View v = (View)c.getClientProperty(BasicHTML.propertyKey);
            if (v != null)
            {
               v.paint(g, textRect);
            }
            else
            {
               paintText(g, b, textRect, text);
            }
         }      
      }     
      
      if (b.isFocusPainted() && b.hasFocus())
      {
         // paint UI specific focus
         paintFocus(g, b, viewRect, textRect, iconRect);
      }
      
      int x = 0;
      int y = 0;
      
      Insets insets = c.getInsets();
      if((iconRect != null) && (iconRect.width > 0))
      {
         x = iconRect.x + iconRect.width + m_ArrowInsetWidth;
         y = iconRect.height / 2 + insets.top;         
      }
      
      if((textRect != null) && (textRect.width > 0))
      {
         x = textRect.x + textRect.width + m_ArrowInsetWidth;
         y = textRect.height / 2 + insets.top;
      }
      
      if((x == 0) && (y == 0))
      {
         x = (c.getWidth() / 2) / 2;
         y = (c.getHeight() / 3);      
      }
      
      paintArrow(g, x, y, arrowWidth);
   }

   protected void paintArrow(Graphics g, int x, int y, int width)
   {  
      int[] triangleX = { x, x + width, x + (width / 2)};
      int[] triangleY = { y, y, y + (width / 2)};
      g.fillPolygon(triangleX, triangleY, 3);
      
   }
   
   protected void paintIcon(Graphics g, JComponent c, Rectangle iconRect)
   {
      AbstractButton b = (AbstractButton)c;
      ButtonModel model = b.getModel();
      Icon icon = b.getIcon();
      Icon tmpIcon = null;

      if (icon == null)
      {
         return;
      }

      if (!model.isEnabled())
      {
         if (model.isSelected())
         {
            tmpIcon = (Icon)b.getDisabledSelectedIcon();
         }
         else
         {
            tmpIcon = (Icon)b.getDisabledIcon();
         }
      }
      else if (model.isPressed() && model.isArmed())
      {
         tmpIcon = (Icon)b.getPressedIcon();
         if (tmpIcon != null)
         {
            // revert back to 0 offset
            clearTextShiftOffset();
         }
      }
      else if (b.isRolloverEnabled() && model.isRollover())
      {
         if (model.isSelected())
         {
            tmpIcon = (Icon)b.getRolloverSelectedIcon();
         }
         else
         {
            tmpIcon = (Icon)b.getRolloverIcon();
         }
      }
      else if (model.isSelected())
      {
         tmpIcon = (Icon)b.getSelectedIcon();
      }

      if (tmpIcon != null)
      {
         icon = tmpIcon;
      }

      if (model.isPressed() && model.isArmed())
      {
         icon.paintIcon(c, g, iconRect.x + getTextShiftOffset(), iconRect.y + getTextShiftOffset());
      }
      else
      {
         icon.paintIcon(c, g, iconRect.x, iconRect.y);
      }

   }

   /**
    * As of Java 2 platform v 1.4 this method should not be used or overriden.
    * Use the paintText method which takes the AbstractButton argument.
    */
   protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text)
   {
      AbstractButton b = (AbstractButton)c;
      ButtonModel model = b.getModel();
      FontMetrics fm = g.getFontMetrics();
      int mnemonicIndex = b.getDisplayedMnemonicIndex();

      /* Draw the Text */
      if (model.isEnabled())
      {
         /*** paint the text normally */
         g.setColor(b.getForeground());
         BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, mnemonicIndex, textRect.x + getTextShiftOffset(), textRect.y + fm.getAscent() + getTextShiftOffset());
      }
      else
      {
         /*** paint the text disabled ***/
         g.setColor(b.getBackground().brighter());
         BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, mnemonicIndex, textRect.x, textRect.y + fm.getAscent());
         g.setColor(b.getBackground().darker());
         BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, mnemonicIndex, textRect.x - 1, textRect.y + fm.getAscent() - 1);
      }
   }

   /**
    * Method which renders the text of the current button.
    * <p>
    * @param g Graphics context
    * @param b Current button to render
    * @param textRect Bounding rectangle to render the text.
    * @param text String to render
    * @since 1.4
    */
   protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text)
   {
      paintText(g, (JComponent)b, textRect, text);
   }

   // Method signature defined here overriden in subclasses. 
   // Perhaps this class should be abstract?
   protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect, Rectangle iconRect)
   {
   }

   // Method signature defined here overriden in subclasses. 
   // Perhaps this class should be abstract?
   protected void paintButtonPressed(Graphics g, AbstractButton b)
   {
      if ( b.isContentAreaFilled() ) 
      {
         Dimension size = b.getSize();
         g.setColor(getSelectColor());
         g.fillRect(0, 0, size.width, size.height);
      }
   }

   protected void clearTextShiftOffset()
   {
      shiftOffset = 0;
   }

   protected void setTextShiftOffset()
   {
      shiftOffset = defaultTextShiftOffset;
   }

   protected int getTextShiftOffset()
   {
      return shiftOffset;
   }

   // ********************************
   //          Layout Methods
   // ********************************
   public Dimension getMinimumSize(JComponent c)
   {
      Dimension d = getPreferredSize(c);
      adjustForArrow(c, d);
      
      return d;
   }

   public Dimension getPreferredSize(JComponent c)
   {
      AbstractButton b = (AbstractButton)c;
      
      Dimension retVal = null;     
      
      Insets insets = c.getInsets();
      
      retVal = BasicGraphicsUtils.getPreferredButtonSize(b, b.getIconTextGap());
      if( isArrowButton() == false )
      {         
         retVal.width += m_ArrowWidth + (m_ArrowInsetWidth * 2);
      }
      else
      {  
         //retVal.width += insets.left + insets.right +  m_LargeArrowWidth;
         //retVal.height += insets.top + insets.bottom + m_LargeArrowWidth;
         retVal.width += insets.left +  m_LargeArrowWidth;
         retVal.height += insets.top + m_LargeArrowWidth;
      }      

      return retVal;
   }

   public Dimension getMaximumSize(JComponent c)
   {
      Dimension d = getPreferredSize(c);
      
      View v = (View)c.getClientProperty(BasicHTML.propertyKey);
      if (v != null)
      {
         d.width += v.getMaximumSpan(View.X_AXIS) - v.getPreferredSpan(View.X_AXIS);
         d.width += m_ArrowWidth + (m_ArrowInsetWidth * 2);
      }
      return d;
   }
   
   protected void adjustForArrow(JComponent c, Dimension dim)
   {
//      View v = (View)c.getClientProperty(BasicHTML.propertyKey);
//      if (v != null)
//      {
//         dim.width -= v.getPreferredSpan(View.X_AXIS) - v.getMinimumSpan(View.X_AXIS);         
//      }
//      
//      if(isArrowButton() == false)
//      {
//         dim.width += m_ArrowWidth + (m_ArrowInsetWidth * 2);
//      }
   }
   
   protected boolean isArrowButton()
   {
      String text = m_Button.getText();
      Icon   image = m_Button.getIcon();

      boolean retVal = true;

      if((text != null) && (text.length() > 0))
      {
         retVal = false;
      }
      else if(image != null)
      {
         retVal = false;         
      }
      
      return retVal;
   }
}
