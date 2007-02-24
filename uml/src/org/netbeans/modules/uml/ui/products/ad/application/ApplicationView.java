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


package org.netbeans.modules.uml.ui.products.ad.application;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Component;
import javax.swing.BoxLayout;

import javax.swing.JToolBar;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.netbeans.modules.uml.ui.swing.plaf.basic.BasicPullDownButtonBorder;
import org.netbeans.modules.uml.ui.swing.pulldownbutton.JPullDownButton;
import org.netbeans.modules.uml.ui.swing.pulldownbutton.PopupMenuInvoker;
import org.netbeans.modules.uml.ui.swing.testbed.addin.menu.TestBedMenuManager;

/**
 * 
 * @author Trey Spiva
 */
public abstract class ApplicationView extends JPanel
{
    private String m_Title = "";
    private Icon   m_Icon = null;
    private String m_ToolTip = null;
    private String m_Id = "";
    private JToolBar m_ToolBarManager = null;
    
    // IZ# 78924 - conover: needed to be a panel to left align the toolbar
    //                      wasn't working as a Box for some reason
    // private Box m_ToolComponent = Box.createHorizontalBox();
    private JPanel m_ToolComponent;
    
    public ApplicationView(String id)
    {
        setId(id);
    }
    
    public JPanel getView()
    {
        setLayout(new BorderLayout());
        
        JPanel viewSite = new JPanel();
        createViewControl(viewSite);
        add(viewSite, BorderLayout.CENTER);
        setPopupMenuListener(new PopupMenuManager(viewSite));
        initializeUIBars(this);
        
        return this;
    }
   
    public PopupMenuManager getPopupMenuManager(JPanel panel) {
        return new PopupMenuManager(panel);
    }

    protected void initializeUIBars(JPanel view)
    {
        // IZ# 78924 - conover: layout the toolbar panel to left align the icons

        m_ToolComponent = new JPanel();
        BoxLayout boxlayout = new BoxLayout(m_ToolComponent, BoxLayout.X_AXIS);
        m_ToolComponent.setLayout(boxlayout);

        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        m_ToolBarManager = bar;
        
        TestBedMenuManager menuManager = new TestBedMenuManager();
        JMenu menu = new JMenu();
        menuManager.setMenuItem(menu);

        contributeActionBars(m_ToolBarManager);

        m_ToolComponent.add(m_ToolBarManager);
        menuManager.createMenuBar();

        if (menu.getItemCount() > 0)
        {
            JPullDownButton btn = new JPullDownButton();
            btn.setBorder(new BasicPullDownButtonBorder(false));
            btn.setPulldownInvoker(new PopupMenuInvoker(menu.getPopupMenu()));
            m_ToolComponent.add(btn);
        }

        view.add(m_ToolComponent, BorderLayout.NORTH);
    }

   public abstract void createViewControl(JPanel parent);
   
   protected void contributeActionBars(JToolBar bars)
   {
   }

   public void setPopupMenuListener(MouseListener listener)
   {
   }
   

   //**************************************************
   // Context Menu Methods
   //**************************************************
//   private PopupMenuExtender  m_ContextMenu        = null;
   private TestBedMenuManager m_ContextMenuManager = null;
   private JMenu              m_ContextPopup       = new JMenu();
   
   public void registerContextMenu(boolean clearBeforeShow)
   {
//      registerContextMenu(getId(), clearBeforeShow, prov);
       registerContextMenu(getId(), clearBeforeShow);
   }
   
   public void registerContextMenu(String id, boolean clearBeforeShow)
   {
      m_ContextMenuManager = new TestBedMenuManager();
      m_ContextMenuManager.setRemoveAllWhenShown(clearBeforeShow);
      
      m_ContextMenuManager.setMenuItem(m_ContextPopup);
//      m_ContextMenu = new PopupMenuExtender(id, m_ContextMenuManager, prov, this);      
   }
   
   /**
    * This method should be over ridden by any subclass which wants to contribute items to the menu
    * 
    */
   public void menuAboutToShow(IMenuManager manager)
   {
   }
   
   /**
    * This method should be overridden by any subclass that wants to remove any items from the context menu.
    * @param manager
    */
   public void processRemovesIfAny(IMenuManager manager)
   {
   }
   
   //**************************************************
   // Data Access Methods
   //**************************************************
   
   /**
    * @return
    */
   public String getTitle()
   {
      return m_Title;
   }

   /**
    * @param string
    */
   public void setTitle(String string)
   {
      m_Title = string;
   }

   /**
    * @return
    */
   public Icon getIcon()
   {
      return m_Icon;
   }

   /**
    * @param icon
    */
   public void setIcon(Icon icon)
   {
      m_Icon = icon;
   }

   /**
    * @return
    */
   public String getToolTip()
   {
      return m_ToolTip;
   }

   /**
    * @param string
    */
   public void setToolTip(String string)
   {
      m_ToolTip = string;
   }
   /**
    * @return
    */
   public String getId()
   {
      return m_Id;
   }

   /**
    * @param string
    */
   public void setId(String string)
   {
      m_Id = string;
   }

   public class PopupMenuManager extends MouseAdapter
   {
      JPanel m_View = null;
      
      public PopupMenuManager(JPanel view)
      {
         m_View = view;
      }
      
      /* (non-Javadoc)
       * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
       */
      public void mouseReleased(MouseEvent e)
      {
         // Some Operating Systems will show the popupmenu on the mouse
         // down and some will show it on the mouse up.
         showPopupMenu(e);
      }
      
      public void showPopupMenu(Component c) {
          
          if(m_ContextMenuManager.getRemoveAllWhenShown() == true) {
              m_ContextMenuManager.removeAll();
              m_ContextPopup.removeAll();
          }
          
          menuAboutToShow(m_ContextMenuManager);
          processRemovesIfAny(m_ContextMenuManager);
          
//            m_ContextMenu.menuAboutToShow(m_ContextMenuManager);
          m_ContextMenuManager.createMenuBar();
          JPopupMenu menu = m_ContextPopup.getPopupMenu();
          menu.show(c, c.getLocation().x, c.getLocation().y);
      }

      private boolean showPopupMenu(MouseEvent e)
      {
         boolean retVal = e.isPopupTrigger();
         
         if(retVal == true)
         {
            if(m_ContextMenuManager.getRemoveAllWhenShown() == true)
            {
               m_ContextMenuManager.removeAll();
               m_ContextPopup.removeAll();
            }
            
            menuAboutToShow(m_ContextMenuManager);
            processRemovesIfAny(m_ContextMenuManager);
            
//            m_ContextMenu.menuAboutToShow(m_ContextMenuManager);
            m_ContextMenuManager.createMenuBar();
            JPopupMenu menu = m_ContextPopup.getPopupMenu();
            Component source = e.getComponent();
            menu.show(source, e.getX(), e.getY());
         }
         else
         {
            super.mousePressed(e);
         } 
         
         return retVal;
      }

      
      /* (non-Javadoc)
       * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
       */
      public void mousePressed(MouseEvent e)
      {
         // Some Operating Systems will show the popupmenu on the mouse
         // down and some will show it on the mouse up.
         showPopupMenu(e);
      }

   }

}
