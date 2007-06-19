/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.form.menu;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import org.netbeans.modules.form.InPlaceEditLayer;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADContainer;
import org.netbeans.modules.form.RADVisualComponent;
import org.netbeans.modules.form.RADVisualContainer;

/**
 * Handles navigation of menu items using the keyboard
 *
 * @author joshua.marinacci@sun.com
 */
public class KeyboardMenuNavigator implements KeyListener {
    MenuEditLayer menuEditLayer;
    private RADVisualContainer menuBarRAD;
    private RADVisualContainer currentMenuRAD;
    RADComponent selectedRADComponent;
    KeyboardFinishListener listener;
    
    public KeyboardMenuNavigator(MenuEditLayer menuEditLayer) {
        this.menuEditLayer = menuEditLayer;
        configure();
    }
    
    public void setCurrentMenuRAD(RADVisualContainer currentMenuRAD) {
        this.currentMenuRAD = currentMenuRAD;
        this.menuBarRAD = getMenuBarRad(currentMenuRAD);
    }
    
    private RADVisualContainer getMenuBarRad(RADComponent comp) {
        if(JMenuBar.class.isAssignableFrom(comp.getBeanClass())) {
            return (RADVisualContainer) comp;
        }
        if(comp.getParentComponent() == null) return null;
        return getMenuBarRad(comp.getParentComponent());
    }
    
    public void configure() {
        listener = new KeyboardFinishListener();
        menuEditLayer.formDesigner.getInPlaceEditLayer().addFinishListener(listener);
    }
    public void unconfigure() {
        menuEditLayer.formDesigner.getInPlaceEditLayer().removeFinishListener(listener);
    }
    
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_DOWN) {
            selectOffsetMenuItem(+1);
        }
        if(e.getKeyCode() == KeyEvent.VK_UP) {
            selectOffsetMenuItem(-1);
        }
        if(e.getKeyCode() == KeyEvent.VK_LEFT) {
            selectOffsetMenu(-1);
        }
        if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
            selectOffsetMenu(+1);
        }
        
        if(e.getKeyCode() == KeyEvent.VK_SPACE) {
            startEditing();
        }
        //we aren't getting tabs for some reason
        if(e.getKeyCode() == KeyEvent.VK_A) {
            if(e.isShiftDown()) {
                selectNextMenuItem(-1);
            } else {
                selectNextMenuItem(+1);
            }
        }
    }
    public void keyReleased(KeyEvent e) {
    }
    public void keyTyped(KeyEvent e) {
    }
    
    private void selectNextMenuItem(int offset) {
        //josh: do nothing here until i figure out why tab events aren't being called
        if(currentMenuRAD == null) return;
        if(selectedRADComponent == null) selectedRADComponent = currentMenuRAD.getSubComponent(0);
        
        //if menu, descend into the menu
        if(isJMenu(selectedRADComponent) && offset == +1) {
            RADVisualContainer newMenu = (RADVisualContainer) selectedRADComponent;
            if(newMenu.getSubComponents().length > 0) {
                currentMenuRAD = newMenu;
                selectedRADComponent = null;
                selectOffsetMenuItem(offset);
                return;
            }
        }
        
        //if already at the end of this menu
        if(isLastItem(selectedRADComponent,currentMenuRAD) && offset == +1) {
            goUpOneLevelAndNext();
            return;
        }
        if(isFirstItem(selectedRADComponent, currentMenuRAD) && offset == -1) {
            goUpOneLevel();
            return;
        }
        selectOffsetMenuItem(offset);
    }
    
    
    // select the next menu item offset from the current one.
    // pass in -1 and +1 to do prev and next menu items
    private void selectOffsetMenuItem(int offset) {
        if(currentMenuRAD == null) return;
        if(currentMenuRAD.getSubComponents().length == 0) {
            menuEditLayer.setSelectedComponent((JComponent)null);
            return;
        }
        if(selectedRADComponent == null) {
            selectedRADComponent = currentMenuRAD.getSubComponent(0);
            menuEditLayer.setSelectedRADComponent(selectedRADComponent);
            return;
        }
        int index = currentMenuRAD.getIndexOf(selectedRADComponent);
        if(index+offset >=0 && index+offset < currentMenuRAD.getSubComponents().length) {
            selectedRADComponent = currentMenuRAD.getSubComponent(index+offset);
        } else {
            if(index >= 0 && index < currentMenuRAD.getSubComponents().length) {
                selectedRADComponent = currentMenuRAD.getSubComponent(index);
            }
        }
        
        JComponent item = (JComponent) menuEditLayer.formDesigner.getComponent(selectedRADComponent);
        menuEditLayer.setSelectedComponent(item);
    }
    
    private boolean isJMenu(RADComponent comp) {
        return menuEditLayer.formDesigner.getComponent(comp) instanceof JMenu;
    }
    
    
    // select the next menu offset from the current one
    // pass in -1 and + 1 to do prev and next menu items
    private void selectOffsetMenu(int offset) {
        
        //clear the selected component
        //menuEditLayer.setSelectedComponent(null);
        
        //if the current component is a JMenu
        if(menuEditLayer.formDesigner.getComponent(selectedRADComponent) instanceof JMenu) {
            RADVisualContainer menuRAD = (RADVisualContainer) selectedRADComponent;
            // make it's first element be highlighted
            if(menuRAD.getSubComponents() != null &&
                    menuRAD.getSubComponents().length > 0 &&
                    menuRAD.getSubComponent(0) != null) {
                RADVisualComponent firstItemRad = menuRAD.getSubComponent(0);
                // open the menu
                menuEditLayer.showMenuPopup((JMenu)menuEditLayer.formDesigner.getComponent(selectedRADComponent));
                menuEditLayer.setSelectedComponent((JComponent) menuEditLayer.formDesigner.getComponent(firstItemRad));
                currentMenuRAD = menuRAD;
                return;
            }
        }
        
        // if not a toplevel menu
        int index = menuBarRAD.getIndexOf(currentMenuRAD);
        if(index < 0) {
            // if left then head back up the heirarchy
            if(offset < 0) {
                goUpOneLevel();
                return;
            }
            // if right then switch to the next a full toplevel menu
            if(offset > 0) {
                currentMenuRAD = getTopLevelMenu(currentMenuRAD);
                index = menuBarRAD.getIndexOf(currentMenuRAD);
                // now continue on as normal
            }
        }
        
        // set the current to the new one
        index = index+offset;
        // wrap around if necessary
        if(index <0) {
            index = menuBarRAD.getSubComponents().length-1;
        }
        if(index >= menuBarRAD.getSubComponents().length) {
            index = 0;
        }
        currentMenuRAD = (RADVisualContainer) menuBarRAD.getSubComponent(index);
        
        // show the new current menu
        JMenu menu = (JMenu) menuEditLayer.formDesigner.getComponent(currentMenuRAD);
        menuEditLayer.openMenu(currentMenuRAD,menu);
        
        // set the first item as selected
        if(currentMenuRAD.getSubComponents().length > 0) {
            selectedRADComponent = currentMenuRAD.getSubComponents()[0];
            menuEditLayer.setSelectedComponent((JComponent) menuEditLayer.formDesigner.getComponent(selectedRADComponent));
        }
    }
    
    private void goUpOneLevel() {
        menuEditLayer.setSelectedComponent((JComponent) menuEditLayer.formDesigner.getComponent(currentMenuRAD));
        currentMenuRAD = currentMenuRAD.getParentContainer();
    }
    
    private void goUpOneLevelAndNext() {
        selectedRADComponent = currentMenuRAD;
        currentMenuRAD = currentMenuRAD.getParentContainer();
        if(isLastItem(selectedRADComponent, currentMenuRAD)) {
            goUpOneLevelAndNext();
            return;
        } else {
            selectOffsetMenuItem(+1);
            //menuEditLayer.setSelectedComponent(selectedRADComponent);
        }
    }
    
    private boolean isFirstItem(RADComponent comp, RADVisualContainer cont) {
        int index = cont.getIndexOf(comp);
        if(index == 0) return true;
        return false;
    }
    
    private boolean isLastItem(RADComponent comp, RADVisualContainer cont) {
        int index = cont.getIndexOf(comp);
        if(index == cont.getSubComponents().length-1) {
            return true;
        }
        return false;
    }
    
    private RADVisualContainer getTopLevelMenu(RADVisualContainer currentMenuRAD) {
        if(menuBarRAD.getIndexOf(currentMenuRAD) >= 0) {
            return currentMenuRAD;
        }
        return getTopLevelMenu(currentMenuRAD.getParentContainer());
    }
    
    private void startEditing() {
        menuEditLayer.formDesigner.startInPlaceEditing(selectedRADComponent);
    }
    
    private class KeyboardFinishListener implements InPlaceEditLayer.FinishListener {
        public void editingFinished(boolean changed) {
            //System.out.println("finished editing");
            if(menuEditLayer.isVisible()) {
                menuEditLayer.glassLayer.requestFocusInWindow();
            }
        }
    }
}
