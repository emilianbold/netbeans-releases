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



package org.netbeans.modules.uml.ui.swing.testbed.addin.menu;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContributionManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.IContributionManager;
import java.util.HashMap;
import javax.swing.JSeparator;
import org.netbeans.modules.uml.ui.products.ad.application.action.BaseAction;

/**
 *
 * @author Trey Spiva
 */
//public class TestBedMenuManager extends ContributionManager implements IMenuManager
public class TestBedMenuManager implements IMenuManager
{
    private JMenuBar m_MenuBar = null;
    private Hashtable<String, IMenuManager> m_SubMenus = new Hashtable<String, IMenuManager>();
    private Point m_Point = null;
    private Object m_ObjectClickedOn = null;
    
    private List m_Contributions = new ArrayList();
    
    private boolean isDirty = true;
    
//   private int dynamicItems = 0;
    
    /**
     * The mnemonic character of the the menu.
     */
    private char m_Mnemonic = 0;
    
    /**
     * The menu id.
     */
    private String m_Id = "";
    
    /**
     * The menu item widget; <code>null</code> before
     * creation and after disposal. This field is used
     * when this menu manager is a sub-menu.
     */
    JMenu m_MenuItem = null;
    
    /**
     * The text for a sub-menu.
     */
    private String m_MenuText = "";
    
    /**
     * Indicates whether <code>removeAll</code> should be
     * called just before the menu is displayed.
     */
    private boolean m_RemoveAllWhenShown = false;
    
    /**
     * The parent contribution manager.
     */
    private IMenuManager m_Parent;
    
    /**
     * Indicates this item is visible in its manager; <code>true</code>
     * by default.
     */
    private boolean m_Visible = true;
    
    /**
     * The overrides for items of this manager
     */
    
    public TestBedMenuManager()
    {
        this(true);
    }
    
    public TestBedMenuManager(IMenuManager mgr)
    {
        this();
        m_Parent = mgr;
        setVisible(mgr.isVisible());
    }
    
    public TestBedMenuManager(String text, String id)
    {
        setId(id);
        setMenuText(text);
    }
    
    public TestBedMenuManager(IMenuManager mgr, String text, String id)
    {
        setId(id);
        setMenuText(text);
        
        m_Parent = mgr;
    }
    
    protected TestBedMenuManager(boolean addAdditions)
    {
    }
    
    /**
     * Creates and returns an SWT menu bar control for this menu, for use in the
     * given <code>Shell</code>, and installs all registered contributions. Does not
     * create a new control if one already exists. This implementation simply calls
     * the <code>createMenuBar(Decorations)</code> method
     *
     * @return the menu control
     */
    public JMenuBar createMenuBar()
    {
        if(menuExist() == false)
        {
            setMenuBar(new JMenuBar());
        }
        
        update(false);
        
        return getMenuBar();
    }
    
    public JMenuBar getMenuBar()
    {
        return m_MenuBar;
    }
    
    public void setMenuBar(JMenuBar bar)
    {
        m_MenuBar = bar;
    }
    
    public void setMenuItem(JMenu menu)
    {
        m_MenuItem = menu;
    }
    
    public JMenu getMenuItem()
    {
        return m_MenuItem;
    }
    
    //**************************************************
    // IMenuManager Implementation
    //**************************************************
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.ui.application.IMenuManager#findMenuUsingPath(java.lang.String)
    */
    public IMenuManager findMenuUsingPath(String path)
    {
        IMenuManager retVal = null;
        
        Object item = findUsingPath(path);
        if (item instanceof IMenuManager)
        {
            retVal = (IMenuManager)item;
        }
        
        return retVal;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.ui.application.IMenuManager#findUsingPath(java.lang.String)
    */
    public Object findUsingPath(String path)
    {
        String id = path;
        String rest = null;
        int separator = path.indexOf('/');
        
        if (separator != -1)
        {
            id = path.substring(0, separator);
            rest = path.substring(separator + 1);
        }
        else
        {
            return find(path);
        }
        
        Object item = find(id);
        if (item instanceof IMenuManager)
        {
            IMenuManager manager = (IMenuManager)item;
            return manager.findUsingPath(rest);
        }
        return null;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.ui.application.IMenuManager#getRemoveAllWhenShown()
    */
    public boolean getRemoveAllWhenShown()
    {
        return m_RemoveAllWhenShown;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.ui.application.IMenuManager#isEnabled()
    */
    public boolean isEnabled()
    {
        return true;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.ui.application.IMenuManager#setRemoveAllWhenShown(boolean)
    */
    public void setRemoveAllWhenShown(boolean removeAll)
    {
        m_RemoveAllWhenShown = removeAll;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.ui.application.IMenuManager#updateAll(boolean)
    */
    public void updateAll(boolean force)
    {
        update(force, true);
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.ui.action.IContributionManager#update(boolean)
    */
    public void update(boolean force)
    {
        update(force, false);
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.ui.action.IContributionItem#update()
    */
    public void update()
    {
        updateMenuItem();
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.ui.action.IContributionItem#fill(java.awt.Container)
    */
    public void fill(JMenu parent, int index)
    {
        if(isSeparator() == true)
        {
            parent.add(new JSeparator());
            return;
        }
        
        if (index >= 0)
        {
            m_MenuItem = new JMenu(getMenuText());
            
            if(getMnemonic() > 0)
            {
                m_MenuItem.setMnemonic(getMnemonic());
            }
            parent.add(m_MenuItem);
        }
        else
        {
            m_MenuItem = new JMenu(getMenuText());
            parent.add(m_MenuItem);
        }
        
        initializeMenu();
        
        // populate the submenu, in order to enable accelerators
        // and to set enabled state on the menuItem properly
        update(true);
        
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.ui.action.IContributionItem#fill(org.netbeans.modules.uml.core.addinframework.ui.application.IMenu, int)
    */
    public void fill(JMenuBar parent, int index)
    {
        if (m_MenuItem == null)
        {
            if (index >= 0)
            {
                m_MenuItem = new JMenu(getMenuText());
                
                if(getMnemonic() > 0)
                {
                    m_MenuItem.setMnemonic(getMnemonic());
                }
                parent.add(m_MenuItem);
            }
            else
            {
                m_MenuItem = new JMenu(getMenuText());
                parent.add(m_MenuItem);
            }
            
            initializeMenu();
            
            // populate the submenu, in order to enable accelerators
            // and to set enabled state on the menuItem properly
            update(true);
        }
    }
    
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.ui.action.IContributionItem#getId()
    */
    public String getId()
    {
        return m_Id;
    }
    
    public void setId(String id)
    {
        m_Id = id;
    }
    
    public String getMenuText()
    {
        String retVal = m_MenuText;
        
        if((retVal == null) || (retVal.length() <= 0))
        {
            retVal = ((TestBedMenuManager)getParent()).getMenuText();
        }
        
        return retVal;
    }
    
    public void setMenuText(String text)
    {
        StringBuffer name = new StringBuffer();
        for (int index = 0; index < text.length(); index++)
        {
            char curChar = text.charAt(index);
            if ((curChar == '&') && ((index + 1) < text.length()))
            {
                index++;
                curChar = text.charAt(index);
                setMnemonic(curChar);
            }
            
            name.append(curChar);
        }
        
        m_MenuText = name.toString();
    }
    
    public void setMnemonic(char ch)
    {
        m_Mnemonic = ch;
    }
    
    public char getMnemonic()
    {
        char retVal = m_Mnemonic;
        
        if(retVal > 0)
        {
            retVal = ((TestBedMenuManager)getParent()).getMnemonic();
        }
        
        return retVal;
    }
    
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.ui.action.IContributionItem#isDynamic()
    */
//   public boolean isDynamic()
//   {
//      return false;
//   }
    
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.ui.action.IContributionItem#isSeparator()
    */
    public boolean isSeparator()
    {
        return false;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.ui.action.IContributionItem#isVisible()
    */
    public boolean isVisible()
    {
        return m_Visible;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.ui.action.IContributionItem#setParent(org.netbeans.modules.uml.core.addinframework.ui.action.IContributionManager)
    */
    public void setParent(IMenuManager parent)
    {
        m_Parent = parent;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.ui.action.IContributionItem#setVisible(boolean)
    */
    public void setVisible(boolean visible)
    {
        m_Visible = visible;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.ui.application.IMenuManager#createSubMenu(java.lang.String, java.lang.String)
    */
    public IMenuManager createSubMenu(String label, String id)
    {
        IMenuManager subManager = new TestBedMenuManager(label, id);
        subManager.setVisible(true);
        subManager.setParent(this);
        
        IMenuManager retVal = new SubMenuManager(subManager);
        retVal.setLabel(label);
        retVal.setVisible(true);
        m_SubMenus.put(label, retVal);
        
//        add(retVal);
        
        return retVal;
    }
    
    public IMenuManager createOrGetSubMenu(String label, String id)
    {
        IMenuManager retVal = null;
        IMenuManager subManager = m_SubMenus.get(label);
        if (subManager == null)
        {
            retVal = createSubMenu(label, id);
            add(retVal);
        }
        else
        {
            retVal = subManager;
        }
        return retVal;
    }
    
    
    //**************************************************
    // Helper Methods
    //**************************************************
    
    /**
     * Incrementally builds the menu from the contribution items.
     * This method leaves out double separators and separators in the first
     * or last position.
     *
     * @param force <code>true</code> means update even if not dirty,
     *   and <code>false</code> for normal incremental updating
     * @param recursive <code>true</code> means recursively update
     *   all submenus, and <code>false</code> means just this menu
     */
    protected void update(boolean force, boolean recursive)
    {
        if (isDirty() || force)
        {
            if (menuExist() == true)
            {
                // clean contains all active items without double separators
                Object[] items = getItems();
                List clean = new ArrayList(items.length);
                IMenuManager separator = null;
                for (int i = 0; i < items.length; ++i)
                {
                    Object ci = items[i];
                    if ( ci instanceof IMenuManager)
                    {
                        IMenuManager currmm = (IMenuManager)ci;
                        if (!currmm.isVisible())
                        {
                            continue;
                        }
                        
                        if (currmm.isSeparator())
                        {
                            // delay creation until necessary
                            // (handles both adjacent separators, and separator at end)
                            separator = currmm;
                        }
                        else
                        {
                            if (separator != null)
                            {
                                if (clean.size() > 0) // no separator if first item
                                    clean.add(separator);
                                separator = null;
                            }
                            clean.add(ci);
                        }
                    }
                    else if (ci instanceof BaseAction)
                    {
                        if (separator != null)
                        {
                            if (clean.size() > 0) // no separator if first item
                                clean.add(separator);
                            separator = null;
                        }                 
                        clean.add(ci);
                    }
                    
                }
                
                // remove obsolete (removed or nonactive)
                updateMenu(force, recursive, clean);
            }
        }
        else
        {
            // I am not dirty. Check if I must recursivly walk down the hierarchy.
            if (recursive)
            {
                Object[] items = getItems();
                for (int i = 0; i < items.length; ++i)
                {
                    Object ci = items[i];
                    if (ci instanceof IMenuManager)
                    {
                        IMenuManager mm = (IMenuManager)ci;
                        if (mm.isVisible())
                        {
                            mm.updateAll(force);
                        }
                    }
                    else if (ci instanceof BaseAction)
                    {                       
                        updateAll(force);
                    }
                }
            }
        }
        updateMenuItem();
    }
    
    
    protected void updateMenu(boolean force, boolean recursive, List clean)
    {
        
        int index = 0;
        
        for (Iterator e = clean.iterator(); e.hasNext();)
        {
            Object obj = e.next();
            if(obj instanceof IMenuManager)
            {
                IMenuManager src = (IMenuManager)obj;
                
                if(m_MenuBar != null)
                {
                    src.fill(m_MenuBar, index);
                }
                else if(m_MenuItem != null)
                {
                    src.fill(m_MenuItem, index);
                }
            }
            else if(obj instanceof BaseAction)
            {
                if(m_MenuBar != null)
                {
                }
                else if(m_MenuItem != null)
                {
                    m_MenuItem.add(((BaseAction)obj).getActionComponent());
//                    m_MenuItem.add((BaseAction)obj);
                }
            }
            index++;
        }
    }
    
    /**
     * Updates the menu item for this sub menu.
     * The menu item is disabled if this sub menu is empty.
     * Does nothing if this menu is not a submenu.
     */
    protected void updateMenuItem()
    {
   /*
    * Commented out until proper solution to enablement of
    * menu item for a sub-menu is found. See bug 30833 for
    * more details.
    *
      if (menuItem != null && !menuItem.isDisposed() && menuExist()) {
         IContributionItem items[] = getItems();
         boolean enabled = false;
         for (int i = 0; i < items.length; i++) {
            IContributionItem item = items[i];
            enabled = item.isEnabled();
            if(enabled) break;
         }
         // Workaround for 1GDDCN2: SWT:Linux - MenuItem.setEnabled() always causes a redraw
         if (menuItem.getEnabled() != enabled)
            menuItem.setEnabled(enabled);
      }
    */
    }
    
    /**
     * Returns whether the menu control is created
     * and not disposed.
     *
     * @return <code>true</code> if the control is created
     * and not disposed, <code>false</code> otherwise
     */
    private boolean menuExist()
    {
        boolean retVal = false;
        
        if(m_MenuBar != null)
        {
            retVal = true;
        }
        else if(m_MenuItem != null)
        {
            retVal = true;
        }
        
        return retVal;
    }
    
    /**
     * Initializes the menu control.
     */
    private void initializeMenu()
    {
        markDirty();
        // Don't do an update(true) here, in case menu is never opened.
        // Always do it lazily in handleAboutToShow().
    }
    
    public void setLocation(Point p)
    {
        m_Point = p;
    }
    public Point getLocation()
    {
        if( null == m_Point )
        {
            if( m_Parent instanceof IMenuManager )
            {
                IMenuManager parentMenu = (IMenuManager)m_Parent;
                m_Point = parentMenu.getLocation();
            }
        }
        
        
        return m_Point;
    }
    public void setContextObject(Object obj)
    {
        m_ObjectClickedOn = obj;
    }
    public Object getContextObject()
    {
        return m_ObjectClickedOn;
    }
    
    //override removeAll to clear the sub menus too.
    public void removeAll()
    {
        m_Contributions.clear();
//       dynamicItems = 0;
        markDirty();
        m_SubMenus.clear();
    }
    
/* (non-Javadoc)
 * @see org.netbeans.modules.uml.ui.products.ad.application.action.IContributionItem#getLabel()
 */
    public String getLabel()
    {
        return m_Label;
    }
    
/* (non-Javadoc)
 * @see org.netbeans.modules.uml.ui.products.ad.application.action.IContributionItem#setLabel(java.lang.String)
 */
    public void setLabel(String label)
    {
        m_Label = label;
        setMenuText(label);
    }
    
    public Object remove(String ID)
    {
        Object ci = find(ID);
        if (ci == null)
            throw new IllegalArgumentException("can't find ID"); //$NON-NLS-1$
        return remove(ci);
    }
   /* (non-Javadoc)
    * Method declared on IContributionManager.
    */
    public Object remove(Object item)
    {
        if (m_Contributions.remove(item))
        {
            itemRemoved(item);
            return item;
        }
        return null;
    }
    
    public Object find(String id)
    {
        //search in the hash map with the id and return it..
        Object item = itemMap.get(id);
        if(item == null)
        {
            Iterator e = m_Contributions.iterator();
            while (e.hasNext())
            {
                Object obj = e.next();
                if(obj instanceof IMenuManager)
                {
                    IMenuManager curItem = (IMenuManager)obj;
                    String itemId = curItem.getId();
                    if (itemId != null && itemId.equalsIgnoreCase(id))
                    {
                        item = curItem;
                        break;
                    }
                }
                else if(obj instanceof BaseAction)
                {
                    BaseAction action = (BaseAction)obj;
                    String itemId = action.getId();
                    if (itemId != null && itemId.equalsIgnoreCase(id))
                    {
                        item = action;
                        break;
                    }
                }
            }
        }
        
        return item;
    }
    
    public Object[] getItems()
    {
        Object[] items = new Object[m_Contributions.size()];
        m_Contributions.toArray(items);
        return items;
    }
    
//   protected boolean hasDynamicItems()
//   {
//      return (dynamicItems > 0);
//   }
    
    public boolean isDirty()
    {
        if (isDirty)
            return true;
//      if (hasDynamicItems())
//      {
//         for (Iterator iter = m_Contributions.iterator(); iter.hasNext();)
//         {
//            IMenuManager item = (IMenuManager)iter.next();
//            if (item.isDirty())
//               return true;
//         }
//      }
        return false;
    }
    
    public boolean isEmpty()
    {
        return m_Contributions.isEmpty();
    }
    
    public void markDirty()
    {
        setDirty(true);
    }
    
    protected void itemRemoved(Object item)
    {
        markDirty();
//      if (item.isDynamic())
//         dynamicItems--;
    }
    
    protected void setDirty(boolean d)
    {
        isDirty = d;
    }
    
    public void add(IMenuManager item)
    {
        m_Contributions.add(item);
        item.setVisible(visible);
        itemAdded(item);
        item.setParent(this);
    }
    
    protected void itemAdded(IMenuManager item)
    {
        markDirty();
//      if (item.isDynamic())
//         dynamicItems++;
    }
    
    public void fill(JToolBar parent, int index)
    {
    }
    
    public IMenuManager getParent()
    {
        return m_Parent;
    }
    
    public void add(BaseAction action)
    {
        m_Contributions.add(action);
        action.setMenuManager(this);
    }
    
    public void add(Separator sep, String key)
    {
        add(sep);
        itemMap.put(key, sep);        
    }
    

    
    
    private java.util.HashMap itemMap = new java.util.HashMap();
    private boolean visible = true;
    private String m_Label = null;
    private IMenuManager parent;
    private BaseAction m_Action;
    
}
