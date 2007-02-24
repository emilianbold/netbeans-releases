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


package org.netbeans.modules.uml.ui.products.ad.application.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

//import org.netbeans.modules.uml.core.addinframework.plugins.IExtension;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;

/**
 * 
 * @author Trey Spiva
 */
public class BaseAction extends AbstractAction
{
   private boolean m_IsChecked   = false;
   private int     m_Style       = 0;
   private Icon    m_HoverIcon   = null;
   private Icon    m_DisableIcon = null;
   private String  m_Id          = "";
   private String  m_Label		 = "";
   private IMenuManager m_MenuManager = null;

   /**
    * Action style constant (value <code>0</code>) indicating action style 
    * is not specified yet. By default, the action will assume a push button
    * style. If <code>setChecked</code> is called, then the style will change
    * to a check box, or if <code>setMenuCreator</code> is called, then the
    * style will change to a drop down menu.
    */
   public static final int AS_UNSPECIFIED = 0x00;

   /**
    * Action style constant (value <code>1</code>) indicating action is 
    * a simple push button.
    */
   public static final int AS_PUSH_BUTTON = 0x01;

   /**
    * Action style constant (value <code>2</code>) indicating action is 
    * a check box (or a toggle button).
    */
   public static final int AS_CHECK_BOX = 0x02;

   /**
    * Action style constant (value <code>4</code>) indicating action is 
    * a drop down menu.
    */
   public static final int AS_DROP_DOWN_MENU = 0x04;

   /**
    * Action style constant (value <code>8</code>) indicating action is 
    * a radio button.
    * 
    * @since 2.1
    */
   public static final int AS_RADIO_BUTTON = 0x08;

   public static final int AS_CUSTOM_COMPONENT = 0x16;

   /**
    * Property name of an action's text (value <code>"text"</code>).
    */
   public static final String TEXT = "text"; //$NON-NLS-1$

   /**
    * Property name of an action's enabled state
    * (value <code>"enabled"</code>).
    */
   public static final String ENABLED = "enabled"; //$NON-NLS-1$

   /**
    * Property name of an action's image (value <code>"image"</code>).
    */
   public static final String IMAGE = "image"; //$NON-NLS-1$

   /**
    * Property name of an action's tooltip text (value <code>"toolTipText"</code>).
    */
   public static final String TOOL_TIP_TEXT = "toolTipText"; //$NON-NLS-1$

   /**
    * Property name of an action's description (value <code>"description"</code>).
    * Typically the description is shown as a (longer) help text in the status line.
    */
   public static final String DESCRIPTION = "description"; //$NON-NLS-1$

   public static final String MB_ADDITIONS = "additions";

   /**
    * Property name of an action's checked status (value
    * <code>"checked"</code>). Applicable when the style is
    * <code>AS_CHECK_BOX</code> or <code>AS_RADIO_BUTTON</code>.
    */
   public static final String CHECKED = "checked"; //$NON-NLS-1$

   /* (non-Javadoc)
    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    */
   public void actionPerformed(ActionEvent e)
   {

   }

   /**
       * @return
       */
   public String getText()
   {
      return (String)getValue(Action.NAME);
   }

   /**
    * @param label
    */
   public void setText(String label)
   {
      StringBuffer name = new StringBuffer();
      for (int index = 0; index < label.length(); index++)
      {
         char curChar = label.charAt(index);
         if ((curChar == '&') && ((index + 1) < label.length()))
         {
            index++;
            curChar = label.charAt(index);
            putValue(Action.MNEMONIC_KEY, new Integer(curChar));
         }

         name.append(curChar);
      }

      putValue(Action.NAME, name.toString());
   }

   /**
    * @param id
    */
   public void setId(String id)
   {
      m_Id = id;
   }

   /**
    * 
    */
   public String getId()
   {
      return m_Id;
   }

   public void setLabel(String label)
  {
	 m_Label = label;
  }

  /**
   * 
   */
  public String getLabel()
  {
	 return m_Label;
  }
   /**
    * @return
    */
   public String getToolTipText()
   {
      return (String)getValue(Action.SHORT_DESCRIPTION);
   }

   /**
    * @param tooltip
    */
   public void setToolTipText(String tooltip)
   {
      putValue(Action.SHORT_DESCRIPTION, tooltip);
   }

   /**
    * @param description
    */
   public void setDescription(String description)
   {
      putValue(Action.LONG_DESCRIPTION, description);
   }

   /**
    * @param b
    */
   public void setChecked(boolean b)
   {
      m_IsChecked = b;

   }

   /**
    * @return
    */
   public boolean isChecked()
   {
      return m_IsChecked;
   }

   /**
    * @param i
    */
   public void setAccelerator(int i)
   {
      // TODO Auto-generated method stub

   }

   /**
    * 
    */
   public int getAccelerator()
   {
      return 0;
      // TODO Auto-generated method stub

   }

   /**
    * @param text
    * @return
    */
   public static String removeAcceleratorText(String text)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @param acceleratorText
    * @return
    */
   public static int convertAccelerator(String acceleratorText)
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /**
    * Sets the actions icon.  The extension is used to retrieve the specified
    * icon from the filesystem.
    * 
    * @param extension The extension that defined the action.
    * @param icon The path to the icon.
    */
//   public void setSmallIcon(IExtension extension, String icon)
//   {
//      Icon smallIcon = retrieveIcon(extension, icon);
//      if (smallIcon != null)
//      {
//         putValue(Action.SMALL_ICON, smallIcon);
//      }
//   }

   /**
    * Sets the actions icon.
    * 
    * @param extension The extension that defined the action.
    * @param icon The path to the icon.
    */
   public void setSmallIcon(Icon icon)
   {
      if (icon != null)
      {
         putValue(Action.SMALL_ICON, icon);
      }
   }      
      
   /**
    * Gets the actions small icon.
    * 
    * @param hoverIcon
    */
   public Icon getSmallImage()
   {
      Icon retVal = null;

      Object obj = getValue(Action.SMALL_ICON);
      if (obj instanceof Icon)
      {
         retVal = (Icon)obj;

      }
      return retVal;
   }

   /**
    * Sets the icon to use when the mouse has hovered over a toolbar button.  
    * The extension is used to retrieve the specified icon from the filesystem.
    * 
    * @param extension The extension that defined the action.
    * @param icon The path to the icon.
    */
//   public void setHoverImage(IExtension extension, String icon)
//   {
//      setHoverImage(retrieveIcon(extension, icon));
//   }

   /**
    * Sets the icon to use when the mouse has hovered over a toolbar button.
    * 
    * @param extension The extension that defined the action.
    * @param icon The path to the icon.
    */
   public void setHoverImage(Icon icon)
   {
      m_HoverIcon = icon;
   
   }

   /**
    * Gets the icon to use when the mouse has hovered over a toolbar button.
    * 
    * @return The hover icon or <code>null</code> if a hover icon has not been
    *         defined.
    */
   public Icon getHoverImage()
   {
      return m_HoverIcon;
   }

   /**
    * Sets the icon to display when the action has been disabled.
    * 
    * @param extension The extension that defined the action.
    * @param icon The path to the icon.
    */
//   public void setDisabledImage(IExtension extension, String disabledIcon)
//   {
//      setDisabledImage(retrieveIcon(extension, disabledIcon));
//   }

   /**
    * Sets the icon to display when the action has been disabled.
    * The extension is used to retrieve the specified icon from the filesystem.
    * 
    * @param extension The extension that defined the action.
    * @param icon The path to the icon.
    */
   public void setDisabledImage(Icon icon)
   {
      m_DisableIcon = icon;
   }
   
   /**
    * Gets the icon to use when the action has been disabled.
    * 
    * @return The disable icon or <code>null</code> if a hover icon has not been
    *         defined.
    */
   public Icon getDisableImage()
   {
      return m_HoverIcon;
   }

   /**
    * @return
    */
   public int getStyle()
   {
      return m_Style;
   }

   public void setStyle(int s)
   {
      m_Style = s;
      
      if( (s == BaseAction.AS_CHECK_BOX)  && (isChecked()))
      {
          setChecked(true);
      }
   }

   // **************************************************
   // Helper Methods
   //**************************************************

   /**
    * Uses the extension to retrieve the specified icon from the plugins.
    * 
    * @param extension The extension that represents the location of the icon.
    * @param icon The relative path to the icon.  The icon is relative to the 
    *             extension.
    * @return The icon if it is found, <code>null</code> if the icon is not 
    *         found.
    */
//   protected Icon retrieveIcon(IExtension extension, String icon)
//   {
//      Icon retVal = null;
//
//      URL installURL = extension.getDeclaringPluginDescriptor().getInstallURL();
//      URL iconURL;
//      try
//      {
//         iconURL = new URL(installURL.getProtocol(), installURL.getHost(), installURL.getFile() + "/" + icon);
//         retVal = new ImageIcon(iconURL);
//      }
//      catch (MalformedURLException e)
//      {
//         // TODO Figure out what to do about exceptions
//         e.printStackTrace();
//      }
//
//      return retVal;
//   }
   
   /**
    * This method is for BaseACtions which want to return custom components like combobox etc.
    */
   public JComponent getActionComponent()
   {
       JComponent retVal = null;
       
       int style = getStyle();
       
       switch (style) {
           case AS_CHECK_BOX  :
               JCheckBoxMenuItem item = new JCheckBoxMenuItem(this);
               item.setState(isChecked());
               retVal = item;
               break;
           case AS_DROP_DOWN_MENU :
               retVal = new JMenu();
               break;
           case AS_RADIO_BUTTON :
               retVal = new JCheckBoxMenuItem(this);
               break;
           case AS_CUSTOM_COMPONENT :
               retVal = getCustomComponent();
               break;
           default :
               retVal = new JMenuItem();
               ((JMenuItem)retVal).setAction(this);
               break;
       }
       
       if(retVal instanceof AbstractButton)
       {
//           System.out.println("Got a button: " + getText());
           
           AbstractButton btn = (AbstractButton)retVal;
           
           Integer keyCode = (Integer) getValue(Action.MNEMONIC_KEY);
           if(keyCode != null) {
               btn.setMnemonic(keyCode);
           }
           btn.setText(getText());
           btn.getAccessibleContext().setAccessibleName(getText());
       }
       
       return retVal;
   }
   
   public void setMenuManager(IMenuManager mgr)
   {
      m_MenuManager = mgr;
   }
   public IMenuManager getMenuManager()
   {
      return m_MenuManager;
   }

    public JComponent getCustomComponent() {
        return null;
    }
}
