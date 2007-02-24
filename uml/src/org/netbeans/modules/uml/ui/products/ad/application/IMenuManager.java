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

import java.awt.Point;
import org.netbeans.modules.uml.ui.products.ad.application.action.IContributionManager;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import org.netbeans.modules.uml.ui.swing.testbed.addin.menu.Separator;

public interface IMenuManager extends IContributionManager
{

   /**
   * Adds a menu listener to this menu.
   * Has no effect if an identical listener is already registered.
   *
   * @param listener a menu listener
   */
   //public void addMenuListener(IMenuListener listener);
    
       /**
    * Finds the contribution item with the given id.
    *
    * @param id the contribution item id
    * @return the contribution item, or <code>null</code> if
    *   no item with the given id can be found
    */
   public Object find(String id);
   /**
    * Returns all contribution items known to this manager.
    *
    * @return a list of contribution items
    */
   public Object[] getItems();
   /**
    * Finds the manager for the menu at the given path. A path
    * consists of contribution item ids separated by the separator 
    * character.  The path separator character is <code>'/'</code>.
    * <p>
    * Convenience for <code>findUsingPath(path)</code> which
    * extracts an <code>IMenuManager</code> if possible.
    * </p>
    *
    * @param path the path string
    * @return the menu contribution item, or <code>null</code>
    *   if there is no such contribution item or if the item does
    *   not have an associated menu manager
    */
   public IMenuManager findMenuUsingPath(String path);
   /**
    * Finds the contribution item at the given path. A path
    * consists of contribution item ids separated by the separator 
    * character. The path separator character is <code>'/'</code>.
    *
    * @param path the path string
    * @return the contribution item, or <code>null</code> if there is no
    *   such contribution item
    */
//   public IContributionItem findUsingPath(String path);
   public Object findUsingPath(String path);
   /**
    * Returns whether all items should be removed when the menu is first shown,
    * but before notifying menu listeners.  The default is <code>false</code>.
    *
    * @return <code>true</code> if all items should be removed when shown, <code>false</code> if not 
    */
   public boolean getRemoveAllWhenShown();
   /**
    * Returns whether this menu should be enabled or not.
    *
    * @return <code>true</code> if enabled, and
    *   <code>false</code> if disabled
    */
   public boolean isEnabled();
   /**
    * Removes the given menu listener from this menu.
    * Has no effect if an identical listener is not registered.
    *
    * @param listener the menu listener
    */
   //public void removeMenuListener(IMenuListener listener);
   /**
    * Sets whether all items should be removed when the menu is first shown,
    * but before notifying menu listeners.
    *
    * @param removeAll <code>true</code> if all items should be removed when shown, <code>false</code> if not 
    */
   public void setRemoveAllWhenShown(boolean removeAll);
   /**
    * Incrementally builds the menu from the contribution items, and
    * does so recursively for all submenus.
    *
    * @param force <code>true</code> means update even if not dirty,
    *   and <code>false</code> for normal incremental updating
    */
   public void updateAll(boolean force);
   
   /**
    * @param label
    * @param id
    * @return
    */
   public IMenuManager createSubMenu(String label, String id);
   public IMenuManager createOrGetSubMenu(String label, String id);

	public void setLocation(Point p);
	public Point getLocation();
	public void setContextObject(Object obj);
	public Object getContextObject();
        
        /**
    * Removes and returns the contribution item with the given id from this manager.  
    * Returns <code>null</code> if this manager has no contribution items
    * with the given id.
    *
    * @param id the contribution item id
    * @return the item that was found and removed, or <code>null</code> if none
    */
   public Object remove(String id);
   /**
    * Removes the given contribution item from the contribution items
    * known to this manager.
    *
    * @param item the contribution item
    * @return the <code>item</code> parameter if the item was removed,
    *   and <code>null</code> if it was not found
    */
   public Object remove(Object item);
   
//   public void add(JSeparator item);
   
//   public void add(java.lang.Object obj);
   
   public void add(Separator item, String key);
   
   public void add(IMenuManager item);
   
//   public boolean isDynamic();
   
    public boolean isVisible();
    
    public void setVisible(boolean visible);
    
    public void setParent(IMenuManager parent);
    
    public void setLabel(String label);
    
    public String getLabel();
    
    public boolean isSeparator();
    
    public String getId();
    
    public void fill(JMenu parent, int index);
    public void fill(JMenuBar menu, int index);
    public void fill(JToolBar parent, int index);
       
}
