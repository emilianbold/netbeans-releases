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

package org.netbeans.modules.soa.mapper.basicmapper.palette;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JToolBar;

import org.netbeans.modules.soa.mapper.common.ui.palette.IPaletteCategory;

/**
 *
 *
 * @Created on Jun 11, 2004
 * @author sleong
 * @version 1.0
 */
/**
 * @author sleong
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MenuGroup
    extends JToolBar {


    MenuCategory mActiveMenu;
        
    public MenuGroup() {
        setFloatable(false);
        setBorder(null);
        this.setMargin (new Insets (0,0,0,0));
    }
    
    public void addMenu (MenuCategory menu) {
        insertMenu (getComponentCount(), menu); 
    }
    
    public void insertMenu (int index, final MenuCategory menu) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = index;
        c.gridy = 0;
        c.weightx = 1.0d;
        c.weighty = 1.0d;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets (0, 2, 0, 2);
        menu.setGroup(this);
        add(menu, c, index);
    }
        
    private void initMenu (MenuCategory menu) {
        menu.setBorder(null);
         
    }
    
    public void removeMenu (int index) {
        remove (index);
    }
    
    public void removeMenu (MenuCategory menu) {
        remove (menu);
    }
    
    public MenuCategory getNextMenu (MenuCategory menu) {
        synchronized (getTreeLock()) {
            for (int i = 0; i < getComponentCount(); i++) {
                Component comp = getComponent (i);
                if (comp == menu) {
                    if (i == getComponentCount() - 1) {
                        return (MenuCategory) getComponent(0);
                    } else {
                        return (MenuCategory) getComponent(i + 1);
                    }
                }
            }
        }
        throw new IllegalArgumentException (
               "The menu specified is not in this group.");
    }
    
    public MenuCategory getPreviousMenu (MenuCategory menu) {
        synchronized (getTreeLock()) {
            for (int i = 0; i < getComponentCount(); i++) {
                Component comp = getComponent (i);
                if (comp == menu) {
                    if (i == 0) {
                        return (MenuCategory) 
                            getComponent(getComponentCount() - 1);
                    } else {
                        return (MenuCategory) getComponent(i - 1);
                    }
                }
            }
        }
        throw new IllegalArgumentException (
               "The menu specified is not in this group.");
    }
    
    public MenuCategory findMenu (IPaletteCategory paletteCate) {
        synchronized (getTreeLock()) {
            for (int i = 0; i < getComponentCount(); i++) {
                Component comp = getComponent (i);
                if (comp instanceof MenuCategory) {
                    if (((MenuCategory) comp).getPaletteCategory() == paletteCate) {
                        return (MenuCategory) comp;
                    }
                }                
            }
        }
        return null;
    }
    
    public void setEnabled(boolean enable) {
        if (this.isEnabled() == enable) {
            return;
        }
        super.setEnabled(enable);
        synchronized (getTreeLock()) {
            for (int i = 0; i < getComponentCount(); i++) {
                Component comp = getComponent (i);
                comp.setEnabled(enable);
            }
        }
    }

    public void setActiveMenu(MenuCategory menu) {
        synchronized (getTreeLock()) {
            if (mActiveMenu != menu) {
                if (mActiveMenu != null) {
                    // if menu is null the active menu already 
                    // closed
                    if (menu != null) {
                        mActiveMenu.hideButtonMenu();
                    }
                }
            }
            mActiveMenu = menu;
        }
    }

    public MenuCategory getActiveMenu() {
        synchronized (getTreeLock()) {
            return mActiveMenu;
        }
    }
}
