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
import javax.swing.SwingUtilities;
import org.netbeans.modules.form.InPlaceEditLayer;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADVisualComponent;
import org.netbeans.modules.form.RADVisualContainer;

/**
 * Handles navigation of menu items using the keyboard
 * 
 * @author joshua.marinacci@sun.com
 */
public class KeyboardMenuNavigator implements KeyListener {
    MenuEditLayer menuEditLayer;
    RADVisualContainer menuBarRAD;
    RADVisualContainer currentMenuRAD;
    RADVisualComponent selectedRADComponent;
    private boolean isEditing = false;
    KeyboardFinishListener listener;
    
    public KeyboardMenuNavigator(MenuEditLayer menuEditLayer) {
        this.menuEditLayer = menuEditLayer;
        configure();
    }
    
    public void configure() {
        listener = new KeyboardFinishListener();
        menuEditLayer.formDesigner.getInPlaceEditLayer().addFinishListener(listener);
    }
    public void unconfigure() {
        menuEditLayer.formDesigner.getInPlaceEditLayer().removeFinishListener(listener);
    }
    
    public void keyPressed(KeyEvent e) {
        //p("pressed: ");
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
    }
    public void keyReleased(KeyEvent e) {
    }
    public void keyTyped(KeyEvent e) {
    }
    
    // select the next menu item offset from the current one.
    // pass in -1 and +1 to do prev and next menu items
    private void selectOffsetMenuItem(int offset) {
        if(currentMenuRAD.getSubComponents().length == 0) {
            menuEditLayer.setSelectedComponent(null);
            return;
        }
        if(selectedRADComponent == null) {
            selectedRADComponent = currentMenuRAD.getSubComponent(0);
        }
        int index = currentMenuRAD.getIndexOf(selectedRADComponent);
        if(index+offset >=0 && index+offset < currentMenuRAD.getSubComponents().length) {
            selectedRADComponent = currentMenuRAD.getSubComponent(index+offset);
        } else {
            if(index >= 0) {
                selectedRADComponent = currentMenuRAD.getSubComponent(index);
            }
        }
        
        JComponent item = (JComponent) menuEditLayer.formDesigner.getComponent(selectedRADComponent);
        menuEditLayer.setSelectedComponent(item);
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
                menuEditLayer.setSelectedComponent((JComponent) menuEditLayer.formDesigner.getComponent(currentMenuRAD));
                currentMenuRAD = currentMenuRAD.getParentContainer();
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
    private RADVisualContainer getTopLevelMenu(RADVisualContainer currentMenuRAD) {
        if(menuBarRAD.getIndexOf(currentMenuRAD) >= 0) {
            return currentMenuRAD;
        }
        return getTopLevelMenu(currentMenuRAD.getParentContainer());
    }
    
    private void startEditing() {
        isEditing = true;
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
