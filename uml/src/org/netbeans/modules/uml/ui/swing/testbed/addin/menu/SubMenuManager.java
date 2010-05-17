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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.BaseAction;
import org.netbeans.modules.uml.ui.products.ad.application.action.IContributionManager;

public class SubMenuManager extends TestBedMenuManager implements IMenuManager
{
    private String m_Label = "";
    private Point m_Point = null;
    private Object m_ObjectClickedOn = null;
    private Hashtable<String, IMenuManager> m_SubMenus = new Hashtable<String, IMenuManager>();
    
    /**
     * Maps each submenu in the manager to a wrapper.  The wrapper is used to
     * monitor additions and removals.  If the visibility of the manager is modified
     * the visibility of the submenus is also modified.
     */
    private Map mapMenuToWrapper;
    
    /**
     * Constructs a new manager.
     *
     * @param mgr the parent manager.  All contributions made to the
     *      <code>SubMenuManager</code> are forwarded and appear in the
     *      parent manager.
     */
    public SubMenuManager(IMenuManager mgr)
    {
        super(mgr);
        setVisible(mgr.isVisible());
    }
    
    public String getLabel()
    {
        return m_Label;
    }
    
    public void setLabel(String label)
    {
        m_Label = label;
    }
    /**
     * The default implementation of this <code>IContributionItem</code>
     * method does nothing. Subclasses may override.
     */
    public void dispose()
    {
    }
    
   /* (non-Javadoc)
    * Method declared on IMenuManager.
    */
    public void fill(JMenu parent, int index)
    {
        if (isVisible() && (getParentMenuManager() != null))
        {
            super.fill(parent, index);
        }
    }
    
   /* (non-Javadoc)
    * Method declared on IMenuManager.
    */
    public void fill(JMenuBar parent, int index)
    {
        if (isVisible() && (getParentMenuManager() != null))
        {
            getParentMenuManager().fill(parent, index);
        }
    }
    
   /* (non-Javadoc)
    * Method declared on IMenuManager.
    */
    public void fill(JToolBar parent, int index)
    {
        if (isVisible() && (getParentMenuManager() != null))
        {
            getParentMenuManager().fill(parent, index);
        }
    }
    
    /**
     * <p>
     * The menu returned is wrapped within a <code>SubMenuManager</code> to
     * monitor additions and removals.  If the visibility of this menu is modified
     * the visibility of the submenus is also modified.
     * </p>
     */
    public IMenuManager findMenuUsingPath(String path)
    {
        Object item = findUsingPath(path);
        if (item instanceof IMenuManager)
        {
            return (IMenuManager)item;
        }
        return null;
    }
    
   /* (non-Javadoc)
    * Method declared on IMenuManager.
    *
    * Returns the item passed to us, not the wrapper.
    *
    * We use use the same algorithm as MenuManager.findUsingPath, but unwrap
    * submenus along so that SubMenuManagers are visible.
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
        Object item = find(id); // unwraps item
        if (rest != null && item instanceof IMenuManager)
        {
            IMenuManager menu = (IMenuManager)item;
            item = menu.findUsingPath(rest);
        }
        return item;
    }
    
   /* (non-Javadoc)
    * Method declared on IContributionManager.
    *
    * Returns the item passed to us, not the wrapper.
    * In the case of menu's not added by this manager,
    * ensure that we return a wrapper for the menu.
    */
    public Object find(String id)
    {
        return super.find(id);
    }
    
    /**
     * Returns the parent menu manager that this sub-manager contributes to.
     */
    protected final IMenuManager getParentMenuManager()
    {
        // Cast is ok because that's the only
        // thing we accept in the construtor.
        return (IMenuManager)getParent();
    }
    
   /* (non-Javadoc)
    * Method declared on IMenuManager.
    */
    public String getId()
    {
        String id = null;
        IMenuManager mm = getParentMenuManager();
        if (mm != null)
        {
            id = mm.getId();
        }
        return id;
    }
    
   /* (non-Javadoc)
    * Method declared on IMenuManager.
    */
    public boolean getRemoveAllWhenShown()
    {
        return false;
    }
    
    /**
     * Returns the menu wrapper for a menu manager.
     * <p>
     * The sub menus within this menu are wrapped within a <code>SubMenuManager</code> to
     * monitor additions and removals.  If the visibility of this menu is modified
     * the visibility of the sub menus is also modified.
     * <p>
     *
     * @return the menu wrapper
     */
    protected IMenuManager getWrapper(IMenuManager mgr)
    {
        if (mapMenuToWrapper == null)
        {
            mapMenuToWrapper = new HashMap(4);
        }
        SubMenuManager wrapper = (SubMenuManager)mapMenuToWrapper.get(mgr);
        if (wrapper == null)
        {
            wrapper = wrapMenu(mgr);
            mapMenuToWrapper.put(mgr, wrapper);
        }
        return wrapper;
    }
    
   /* (non-Javadoc)
    * Method declared on IMenuManager.
    */
//   public boolean isDynamic()
//   {
//      return getParentMenuManager().isDynamic();
//   }
    
   /* (non-Javadoc)
    * Method declared on IMenuManager.
    */
    public boolean isEnabled()
    {
        return isVisible() && (getParentMenuManager() != null) && getParentMenuManager().isEnabled();
    }
    
   /* (non-Javadoc)
    * Method declared on IMenuManager.
    */
//   public boolean isGroupMarker()
//   {
//      return getParentMenuManager().isGroupMarker();
//   }
    
   /* (non-Javadoc)
    * Method declared on IMenuManager.
    */
    public boolean isSeparator()
    {
        if(getParentMenuManager() != null)
        {
            return getParentMenuManager().isSeparator();
        }
        else return false;
    }
    
    /**
     * Remove all contribution items.
     */
    public void removeAll()
    {
        super.removeAll();
        if (mapMenuToWrapper != null)
        {
            Iterator iter = mapMenuToWrapper.values().iterator();
            while (iter.hasNext())
            {
                SubMenuManager wrapper = (SubMenuManager)iter.next();
                wrapper.removeAll();
            }
            mapMenuToWrapper.clear();
            mapMenuToWrapper = null;
        }
    }
    
   /* (non-Javadoc)
    * Method declared on IContributionItem.
    */
    public void setParent(IMenuManager parent)
    {
        // do nothing, our "parent manager's" parent
        // is set when it is added to a manager
    }
    
   /* (non-Javadoc)
    * Method declared on IMenuManager.
    */
    public void setRemoveAllWhenShown(boolean removeAll)
    {
        //      Assert.isTrue(false, "Should not be called on submenu manager"); //$NON-NLS-1$
    }
    
   /* (non-Javadoc)
    * Method declared on SubContributionManager.
    */
    public void setVisible(boolean visible)
    {
        super.setVisible(visible);
        if (mapMenuToWrapper != null)
        {
            Iterator iter = mapMenuToWrapper.values().iterator();
            while (iter.hasNext())
            {
                SubMenuManager wrapper = (SubMenuManager)iter.next();
                wrapper.setVisible(visible);
            }
        }
    }
    
   /* (non-Javadoc)
    * Method declared on IMenuManager.
    */
    public void update()
    {
        // This method is not governed by visibility.  The client may
        // call <code>setVisible</code> and then force an update.  At that
        // point we need to update the parent.
//      getParentMenuManager().update();
    }
    
   /* (non-Javadoc)
    * Method declared on IMenuManager.
    */
    public void update(boolean force)
    {
        // This method is not governed by visibility.  The client may
        // call <code>setVisible</code> and then force an update.  At that
        // point we need to update the parent.
        if (getParentMenuManager() != null)
        {
            super.update(force);
        }
    }
    
   /* (non-Javadoc)
    * Method declared on IMenuManager.
    */
    public void updateAll(boolean force)
    {
        // This method is not governed by visibility.  The client may
        // call <code>setVisible</code> and then force an update.  At that
        // point we need to update the parent.
        if (getParentMenuManager() != null)
        {
            super.updateAll(force);
        }
    }
    
   /* (non-Javadoc)
    * Method declared on IContributionItem.
    */
    public void update(String id)
    {
    }
    
    /**
     * Wraps a menu manager in a sub menu manager, and returns the new wrapper.
     */
    protected SubMenuManager wrapMenu(IMenuManager menu)
    {
        SubMenuManager retVal = null;
        if(menu instanceof SubMenuManager)
        {
            retVal = (SubMenuManager)menu;
        }
        else
        {
            retVal = new SubMenuManager(menu);
            retVal.setVisible(isVisible());
        }
        
        return retVal;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.ui.application.IMenuManager#createSubMenu(java.lang.String, java.lang.String)
    */
    public IMenuManager createSubMenu(String label, String id)
    {
        IMenuManager retVal = null;
        
        IMenuManager parent = getParent();
        if (parent instanceof IMenuManager)
        {
            IMenuManager manager = (IMenuManager)parent;
            retVal = manager.createSubMenu(label, id);
        }
        else
        {
            IMenuManager subManager = new TestBedMenuManager(label, id);
            retVal = new SubMenuManager(subManager);
            retVal.setVisible(subManager.isVisible());
            add(retVal);
        }
        
        return retVal;
    }
    
    public void setLocation(Point p)
    {
        m_Point = p;
    }
    public Point getLocation()
    {
        if( null == m_Point )
        {
            IMenuManager parent = getParent();
            if( parent instanceof IMenuManager )
            {
                IMenuManager parentMenu = (IMenuManager)parent;
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
    public IMenuManager createOrGetSubMenu(String label, String id)
    {
        IMenuManager retVal = null;
        IMenuManager subManager = m_SubMenus.get(label);
        if (subManager == null)
        {
            retVal = createSubMenu(label, id);
        }
        else
        {
            retVal = subManager;
        }
        return retVal;
    }
    
    public void add(BaseAction action)
    {
        super.add(action);
        action.setMenuManager(this);
    }
}
