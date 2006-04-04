/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.openide.util;

import java.awt.Component;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.openide.util.Lookup;

/** Provider of action presentations. Based on type of the action
 * should be able to derive its menu, popup menu and toolbar 
 * presenter.
 * <P>
 * In order to provide greater flexibility is made as a pluggable component
 * to allow more enhanced parts of the system to provide more enhanced
 * visualitions.
 */
public abstract class AWTBridge extends Object {
    /** Finds out the global implementtion of the object
     * @return the presenter
     */
    public static AWTBridge getDefault () {
        AWTBridge ap = Lookup.getDefault().lookup(AWTBridge.class);
        return ap == null ? new Default () : ap;
    }
    
    /** Creates a default empty implementation of popup menu.
     * @return popup menu
     */
    public abstract JPopupMenu createEmptyPopup();
    
    /** Creates a menu item that can present this action in a {@link javax.swing.JMenu}.
     * @param action the action to represent
     * @return the representation for this action
     */
    public abstract JMenuItem createMenuPresenter (Action action);
    
    /** Get a menu item that can present this action in a {@link javax.swing.JPopupMenu}.
     * @param action the action to represent
    * @return the representation for this action
    */
    public abstract JMenuItem createPopupPresenter (Action action);
    
    /** Get a component that can present this action in a {@link javax.swing.JToolBar}.
     * @param action the action to represent
    * @return the representation for this action
    */
    public abstract Component createToolbarPresenter (Action action);
    
    
    public abstract Component[] convertComponents(Component comp);
    
    //
    // Default implementation of the the presenter
    // 
    
    private static final class Default extends AWTBridge {
        
        public JMenuItem createMenuPresenter(Action action) {
            return new JMenuItem(action);
        }
        
        public JMenuItem createPopupPresenter(Action action) {
            return new JMenuItem(action);
        }
        
        public Component createToolbarPresenter(Action action) {
            return new JButton(action);
        }
        
        public JPopupMenu createEmptyPopup() {
            return new JPopupMenu();
        }
        
        public Component[] convertComponents(Component comp) {
            return new Component[] {comp};
        }
    }
}
